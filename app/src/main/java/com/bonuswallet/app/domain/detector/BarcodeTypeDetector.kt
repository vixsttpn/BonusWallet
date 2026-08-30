
package com.bonuswallet.app.domain.detector

enum class BarcodeType {
    EAN_13, EAN_8, UPC_A, UPC_E, CODE_128, CODE_39, ITF, CODABAR, QR_CODE, UNKNOWN
}

data class BarcodeDetectionResult(
    val type: BarcodeType,
    val formatString: String,
    val isValid: Boolean,
    val error: String? = null
)

object BarcodeTypeDetector {

    fun detect(value: String): BarcodeDetectionResult {
        val clean = value.trim()
        if (clean.isEmpty()) {
            return BarcodeDetectionResult(BarcodeType.UNKNOWN, "Автоматически", false, "Пустое значение")
        }
        if (clean.length > 200) {
            return BarcodeDetectionResult(BarcodeType.UNKNOWN, "Автоматически", false, "Слишком длинное значение")
        }

        // QR
        if (clean.length > 25 || clean.contains("http") || clean.contains("://")) {
            return BarcodeDetectionResult(BarcodeType.QR_CODE, "QR Code", true)
        }

        // EAN-13
        if (clean.matches(Regex("^\\d{13}$")) && isValidEan13(clean)) {
            return BarcodeDetectionResult(BarcodeType.EAN_13, "EAN-13", true)
        }
        if (clean.matches(Regex("^\\d{13}$"))) {
            // EAN-13 с невалидной контрольной суммой - все равно показываем как EAN-13 но с предупреждением
            return BarcodeDetectionResult(BarcodeType.EAN_13, "EAN-13", false, "Неверная контрольная сумма EAN-13")
        }

        // EAN-8
        if (clean.matches(Regex("^\\d{8}$"))) {
            return BarcodeDetectionResult(BarcodeType.EAN_8, "EAN-8", true)
        }

        // UPC-A
        if (clean.matches(Regex("^\\d{12}$"))) {
            return BarcodeDetectionResult(BarcodeType.UPC_A, "UPC-A", true)
        }
        if (clean.matches(Regex("^\\d{11}$"))) {
            return BarcodeDetectionResult(BarcodeType.UPC_A, "UPC-A", true)
        }

        // UPC-E
        if (clean.matches(Regex("^[01]\\d{7}$"))) {
            return BarcodeDetectionResult(BarcodeType.UPC_E, "UPC-E", true)
        }
        if (clean.matches(Regex("^\\d{6,8}$"))) {
            return BarcodeDetectionResult(BarcodeType.UPC_E, "UPC-E", true)
        }

        // ITF
        if (clean.matches(Regex("^\\d+$")) && clean.length % 2 == 0 && clean.length >= 6) {
            return BarcodeDetectionResult(BarcodeType.ITF, "ITF", true)
        }

        // Code 39
        if (clean.matches(Regex("^[A-Z0-9\\-\\. \\$\\/\\+\\%]+$")) && clean.length <= 20) {
            return BarcodeDetectionResult(BarcodeType.CODE_39, "Code 39", true)
        }

        // Codabar
        if (clean.matches(Regex("^[A-D][0-9\\-\\$:/\\.+]+[A-D]$"))) {
            return BarcodeDetectionResult(BarcodeType.CODABAR, "Codabar", true)
        }

        // Default Code 128 - универсальный для бонусных карт
        if (clean.matches(Regex("^[A-Za-z0-9\\-\\. \\$\\/\\+\\%]+$"))) {
            return BarcodeDetectionResult(BarcodeType.CODE_128, "Code 128", true)
        }

        return BarcodeDetectionResult(BarcodeType.UNKNOWN, "Автоматически", false, "Формат не определен")
    }

    private fun isValidEan13(code: String): Boolean {
        if (code.length != 13) return false
        return try {
            val sum = code.substring(0, 12).mapIndexed { i, c ->
                val digit = c.toString().toInt()
                if (i % 2 == 0) digit else digit * 3
            }.sum()
            val check = (10 - (sum % 10)) % 10
            check == code.last().toString().toInt()
        } catch (e: Exception) { false }
    }
}

