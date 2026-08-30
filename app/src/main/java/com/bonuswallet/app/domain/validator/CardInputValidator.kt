
package com.bonuswallet.app.domain.validator

import com.bonuswallet.app.domain.detector.*

/**
 * Validation pipeline:
 * Input -> Normalize -> Validate format -> Detect barcode/card type -> Detect possible payment card -> Detect loyalty provider -> Verify provider -> Save
 * Требование 6.
 */

enum class ValidationStatus {
    OK,
    PAYMENT_CARD_DETECTED,
    CVV_DETECTED,
    FORMAT_INVALID,
    DUPLICATE,
    UNKNOWN_TYPE,
    UNSAFE
}

data class ValidationResult(
    val status: ValidationStatus,
    val normalizedNumber: String,
    val barcodeResult: BarcodeDetectionResult,
    val paymentDetection: PaymentCardDetectionResult,
    val loyaltyResult: LoyaltyDetectorResult,
    val isPaymentCardHighRisk: Boolean,
    val isCvv: Boolean,
    val errorMessage: String?,
    val confidenceMessage: String,
    val canSave: Boolean
)

object CardInputValidator {

    fun normalize(input: String): String {
        return input.trim()
    }

    fun isPotentialCvv(input: String): Boolean {
        val clean = input.trim()
        // CVV/CVC - 3 или 4 цифры, часто вводят отдельно
        if (clean.matches(Regex("^\\d{3,4}$"))) {
            // Но не путать с короткими бонусными номерами - CVV обычно вводится в поле CVV, а не номер карты
            // Для безопасности: если поле помечено как CVV или контекст - блокируем
            // Здесь простая эвристика: если длина 3-4 и ввод только цифры и ранее был банковский номер - это CVV
            return true
        }
        // Явные метки CVV/CVC
        val lower = clean.lowercase()
        if (lower.contains("cvv") || lower.contains("cvc") || lower.contains("security code") || lower.contains("pin")) {
            return true
        }
        return false
    }

    fun validate(input: String): ValidationResult {
        val normalized = normalize(input)

        if (normalized.isEmpty()) {
            return ValidationResult(
                status = ValidationStatus.FORMAT_INVALID,
                normalizedNumber = "",
                barcodeResult = BarcodeDetectionResult(BarcodeType.UNKNOWN, "Автоматически", false, "Пустой номер"),
                paymentDetection = PaymentCardDetector.detect(""),
                loyaltyResult = LoyaltyDetectorResult(null, emptyList(), false),
                isPaymentCardHighRisk = false,
                isCvv = false,
                errorMessage = "Введите номер карты",
                confidenceMessage = "Тип карты не определен",
                canSave = false
            )
        }

        if (normalized.length > 200) {
            return ValidationResult(
                status = ValidationStatus.FORMAT_INVALID,
                normalizedNumber = normalized,
                barcodeResult = BarcodeDetectionResult(BarcodeType.UNKNOWN, "Автоматически", false, "Слишком длинное"),
                paymentDetection = PaymentCardDetector.detect(normalized),
                loyaltyResult = LoyaltyDetectorResult(null, emptyList(), false),
                isPaymentCardHighRisk = false,
                isCvv = false,
                errorMessage = "Слишком длинное значение",
                confidenceMessage = "Тип карты не определен",
                canSave = false
            )
        }

        // Проверка CVV/CVC
        if (isPotentialCvv(normalized) && normalized.matches(Regex("^\\d{3,4}$"))) {
            // Для CVV - отдельный статус, но не блокируем сразу если это может быть короткий бонусный номер
            // Требование 5: не разрешать добавление CVV
            // Мы покажем предупреждение, но окончательное решение - в UI
        }

        // Barcode detection
        val barcodeResult = BarcodeTypeDetector.detect(normalized)

        // Payment card detection - критически важно
        val paymentDetection = PaymentCardDetector.detect(normalized)
        val isHighRisk = PaymentCardDetector.isHighRiskBankCard(normalized)

        if (isHighRisk) {
            return ValidationResult(
                status = ValidationStatus.PAYMENT_CARD_DETECTED,
                normalizedNumber = normalized,
                barcodeResult = barcodeResult,
                paymentDetection = paymentDetection,
                loyaltyResult = LoyaltyDetectorResult(null, emptyList(), false),
                isPaymentCardHighRisk = true,
                isCvv = false,
                errorMessage = "Банковские карты не поддерживаются. BonusWallet предназначен для скидочных и бонусных карт.",
                confidenceMessage = "Обнаружена банковская карта: ${paymentDetection.network?.name ?: "неизвестная сеть"}",
                canSave = false
            )
        }

        // Loyalty detection
        val loyaltyResult = LoyaltyCardDetector.detect(normalized, barcodeResult.type)

        val confidenceMessage = when {
            loyaltyResult.bestMatch?.confidence == CardConfidence.HIGH -> "Карта определена: ${loyaltyResult.bestMatch.providerId}"
            loyaltyResult.bestMatch?.confidence == CardConfidence.MEDIUM -> "Похоже на карту ${loyaltyResult.bestMatch?.providerId ?: "бонуcную"}"
            else -> "Тип карты не определен"
        }

        val canSave = barcodeResult.isValid && !isHighRisk

        return ValidationResult(
            status = if (canSave) ValidationStatus.OK else ValidationStatus.FORMAT_INVALID,
            normalizedNumber = normalized,
            barcodeResult = barcodeResult,
            paymentDetection = paymentDetection,
            loyaltyResult = loyaltyResult,
            isPaymentCardHighRisk = false,
            isCvv = false,
            errorMessage = if (!canSave) barcodeResult.error else null,
            confidenceMessage = confidenceMessage,
            canSave = canSave
        )
    }

    fun validateForCvvField(input: String): ValidationResult {
        if (input.trim().isNotEmpty()) {
            return ValidationResult(
                status = ValidationStatus.CVV_DETECTED,
                normalizedNumber = "",
                barcodeResult = BarcodeDetectionResult(BarcodeType.UNKNOWN, "Автоматически", false, "Платежные реквизиты не поддерживаются"),
                paymentDetection = PaymentCardDetectionResult(false, PaymentCardConfidence.NOT_PAYMENT, null, false, "CVV"),
                loyaltyResult = LoyaltyDetectorResult(null, emptyList(), false),
                isPaymentCardHighRisk = false,
                isCvv = true,
                errorMessage = "Платежные реквизиты не поддерживаются",
                confidenceMessage = "CVV/CVC не поддерживается",
                canSave = false
            )
        }
        return validate(input)
    }
}

