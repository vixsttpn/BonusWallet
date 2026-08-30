
package com.bonuswallet.app.domain.detector

/**
 * Обнаружение банковских платежных карт.
 * Требование: использовать реальные признаки локально, не считать любой Luhn как банковскую карту.
 * Приоритет безопасности - высокая точность, минимум ложных срабатываний.
 */

enum class PaymentNetwork {
    VISA, MASTERCARD, AMEX, DISCOVER, JCB, UNIONPAY, UNKNOWN
}

enum class PaymentCardConfidence {
    HIGH, MEDIUM, LOW, NOT_PAYMENT
}

data class PaymentCardDetectionResult(
    val isPotentialPaymentCard: Boolean,
    val confidence: PaymentCardConfidence,
    val network: PaymentNetwork?,
    val luhnValid: Boolean,
    val reason: String
)

object PaymentCardDetector {

    fun normalize(input: String): String {
        return input.replace("[^0-9]".toRegex(), "")
    }

    fun isLuhnValid(number: String): Boolean {
        val clean = normalize(number)
        if (clean.length < 13) return false
        var sum = 0
        var alternate = false
        for (i in clean.length - 1 downTo 0) {
            var n = clean[i].digitToInt()
            if (alternate) {
                n *= 2
                if (n > 9) n -= 9
            }
            sum += n
            alternate = !alternate
        }
        return sum % 10 == 0
    }

    fun detectNetwork(cleanNumber: String): PaymentNetwork? {
        if (cleanNumber.isEmpty()) return null
        return when {
            // Visa: 4, length 13,16,19
            cleanNumber.startsWith("4") && cleanNumber.length in setOf(13,16,19) -> PaymentNetwork.VISA
            // Mastercard: 51-55, 2221-2720
            isMastercard(cleanNumber) -> PaymentNetwork.MASTERCARD
            // Amex: 34,37 length 15
            (cleanNumber.startsWith("34") || cleanNumber.startsWith("37")) && cleanNumber.length == 15 -> PaymentNetwork.AMEX
            // Discover: 6011, 65, 644-649
            isDiscover(cleanNumber) -> PaymentNetwork.DISCOVER
            // JCB: 35
            cleanNumber.startsWith("35") && cleanNumber.length in 16..19 -> PaymentNetwork.JCB
            // UnionPay: 62
            cleanNumber.startsWith("62") && cleanNumber.length in 16..19 -> PaymentNetwork.UNIONPAY
            else -> null
        }
    }

    private fun isMastercard(number: String): Boolean {
        if (number.length !in 16..19) return false
        val prefix2 = number.take(2).toIntOrNull() ?: return false
        if (prefix2 in 51..55) return true
        val prefix4 = number.take(4).toIntOrNull() ?: return false
        return prefix4 in 2221..2720
    }

    private fun isDiscover(number: String): Boolean {
        if (number.length !in 16..19) return false
        if (number.startsWith("6011")) return true
        if (number.startsWith("65")) return true
        val prefix3 = number.take(3).toIntOrNull() ?: return false
        return prefix3 in 644..649
    }

    /**
     * Основной метод - совокупность признаков.
     * Не считаем любую строку прошедшую Luhn банковской картой.
     */
    fun detect(input: String): PaymentCardDetectionResult {
        val raw = input.trim()
        if (raw.isEmpty()) {
            return PaymentCardDetectionResult(false, PaymentCardConfidence.NOT_PAYMENT, null, false, "Пустой ввод")
        }

        val clean = normalize(raw)
        if (clean.length < 13 || clean.length > 19) {
            return PaymentCardDetectionResult(false, PaymentCardConfidence.NOT_PAYMENT, null, false, "Длина вне диапазона PAN 13-19")
        }

        // CVV/CVC detection - 3-4 цифры, часто отдельно вводят
        if (clean.length == 3 || clean.length == 4) {
            // Это не PAN, но может быть CVV - обрабатывается отдельно в валидаторе
            return PaymentCardDetectionResult(false, PaymentCardConfidence.LOW, null, false, "Слишком коротко для PAN, возможно CVV")
        }

        val luhn = isLuhnValid(clean)
        val network = detectNetwork(clean)

        // Высокая вероятность: Luhn + известный network + длина соответствует network
        if (luhn && network != null) {
            return PaymentCardDetectionResult(
                true,
                PaymentCardConfidence.HIGH,
                network,
                true,
                "Luhn валиден + сеть ${network.name} + длина ${clean.length}"
            )
        }

        // Средняя: Luhn + длина 13-19 но сеть не определена - подозрительно, требуем ручную проверку
        if (luhn && clean.length in 16..19) {
            return PaymentCardDetectionResult(
                true,
                PaymentCardConfidence.MEDIUM,
                null,
                true,
                "Luhn валиден и длина PAN, но сеть не определена - требует осторожности"
            )
        }

        // Низкая: не Luhn, но длина как у PAN и начинается с известных префиксов - не блокируем автоматически
        if (!luhn && network != null) {
            return PaymentCardDetectionResult(
                false,
                PaymentCardConfidence.LOW,
                network,
                false,
                "Похож на ${network.name} по префиксу но Luhn невалиден - скорее всего не банковская"
            )
        }

        return PaymentCardDetectionResult(
            false,
            PaymentCardConfidence.NOT_PAYMENT,
            null,
            luhn,
            "Не является платежной картой: Luhn=$luhn, сеть=$network"
        )
    }

    fun isHighRiskBankCard(input: String): Boolean {
        val result = detect(input)
        return result.isPotentialPaymentCard && result.confidence == PaymentCardConfidence.HIGH
    }
}

