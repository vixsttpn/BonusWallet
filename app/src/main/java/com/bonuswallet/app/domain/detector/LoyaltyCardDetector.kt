
package com.bonuswallet.app.domain.detector

enum class CardConfidence {
    HIGH, MEDIUM, LOW, UNKNOWN
}

data class LoyaltyDetection(
    val providerId: String,
    val confidence: CardConfidence,
    val reason: String
)

data class LoyaltyDetectorResult(
    val bestMatch: LoyaltyDetection?,
    val allMatches: List<LoyaltyDetection>,
    val isDeterminable: Boolean
)

object LoyaltyCardDetector {

    /**
     * Честное определение программы лояльности.
     * НЕ утверждаем что карта принадлежит Bravo/Wolt/KFC только потому что длина совпала.
     * Высокая достоверность только если есть подтвержденный официальный формат.
     * Поскольку официальных публичных форматов Bravo/Wolt/KFC нет, максимум - MEDIUM.
     */
    fun detect(cardNumber: String, barcodeType: BarcodeType): LoyaltyDetectorResult {
        val clean = cardNumber.trim()
        if (clean.isEmpty()) {
            return LoyaltyDetectorResult(null, emptyList(), false)
        }

        val matches = mutableListOf<LoyaltyDetection>()

        // Generic loyalty - самый честный вариант
        // Большинство бонусных карт: 6-20 символов, буквенно-цифровые, Code128, QR
        if (clean.length in 4..30) {
            matches.add(LoyaltyDetection("generic", CardConfidence.LOW, "Универсальная бонусная карта"))
        }

        // Попытка определить по контексту, но без ложной уверенности
        // ВАЖНО: Мы НЕ имеем официального списка BIN для Bravo/Wolt/KFC, поэтому никогда не возвращаем HIGH
        // Только MEDIUM с пометкой "Похоже на"

        val lower = clean.lowercase()

        // Если пользователь ввел название организации отдельно, это не детекция по номеру
        // Детекция по номеру только по формату, не по бренду

        // Пример: если номер состоит только из цифр 8-13 длины и barcode EAN/UPC - это типично для супермаркетов
        if (barcodeType in listOf(BarcodeType.EAN_13, BarcodeType.EAN_8, BarcodeType.UPC_A, BarcodeType.UPC_E) && clean.all { it.isDigit() }) {
            matches.add(LoyaltyDetection("bravo", CardConfidence.MEDIUM, "Формат EAN/UPC часто используется в ритейле, похоже на карту супермаркета"))
            matches.add(LoyaltyDetection("other_retail", CardConfidence.MEDIUM, "Розничная бонусная карта"))
        }

        // QR с длинным содержимым - может быть Wolt, KFC или другие
        if (barcodeType == BarcodeType.QR_CODE) {
            matches.add(LoyaltyDetection("wolt", CardConfidence.MEDIUM, "QR-код используется в доставке и ресторанах"))
            matches.add(LoyaltyDetection("kfc", CardConfidence.MEDIUM, "QR-код используется в ресторанах быстрого питания"))
            matches.add(LoyaltyDetection("generic_qr", CardConfidence.LOW, "QR бонусная карта"))
        }

        // Code128 - универсальный, используется везде
        if (barcodeType == BarcodeType.CODE_128) {
            matches.add(LoyaltyDetection("generic", CardConfidence.MEDIUM, "Code 128 широко используется для карт лояльности"))
        }

        // Определяем лучший матч - с максимальной достоверностью, но не выше MEDIUM без официального подтверждения
        val best = matches.maxByOrNull { it.confidence.ordinal }

        // Если есть только generic LOW - значит определить невозможно
        val isDeterminable = best != null && best.confidence != CardConfidence.LOW

        return LoyaltyDetectorResult(
            bestMatch = best,
            allMatches = matches.distinctBy { it.providerId },
            isDeterminable = isDeterminable
        )
    }

    /**
     * Определение по названию организации (когда пользователь выбирает вручную)
     * Это ручной выбор, а не автоматическое определение по номеру - здесь уверенность HIGH
     */
    fun detectByOrganizationName(orgName: String): LoyaltyDetection {
        val lower = orgName.lowercase()
        return when {
            "bravo" in lower -> LoyaltyDetection("bravo", CardConfidence.HIGH, "Выбрано вручную: Bravo")
            "wolt" in lower -> LoyaltyDetection("wolt", CardConfidence.HIGH, "Выбрано вручную: Wolt")
            "kfc" in lower -> LoyaltyDetection("kfc", CardConfidence.HIGH, "Выбрано вручную: KFC")
            else -> LoyaltyDetection("generic", CardConfidence.HIGH, "Выбрано вручную: $orgName")
        }
    }
}

