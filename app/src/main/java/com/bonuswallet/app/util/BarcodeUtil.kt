package com.bonuswallet.app.util

object BarcodeUtil {

    fun detectFormat(value: String): String {
        val clean = value.trim()
        if (clean.isEmpty()) return "Code 128"

        // EAN-13: 13 digits
        if (clean.matches(Regex("""^\d{13}$""")) && isValidEan13(clean)) return "EAN-13"
        // EAN-8: 8 digits
        if (clean.matches(Regex("""^\d{8}$"""))) return "EAN-8"
        // UPC-A: 12 digits
        if (clean.matches(Regex("""^\d{12}$"""))) return "UPC-A"
        // UPC-E: 8 digits starting 0/1
        if (clean.matches(Regex("""^[01]\d{7}$"""))) return "UPC-E"
        // ITF: even number of digits
        if (clean.matches(Regex("""^\d+$""")) && clean.length % 2 == 0 && clean.length >= 6) return "ITF"
        // Code 39
        if (clean.matches(Regex("""^[A-Z0-9\-\. $/+%]+$""")) && clean.length <= 20) return "Code 39"
        // Codabar
        if (clean.matches(Regex("""^[A-D][0-9\-${'$'}:/.+]+[A-D]$"""))) return "Codabar"
        // QR for urls or long mixed
        if (clean.length > 25 || clean.contains("http") || clean.contains("://")) return "QR Code"
        return "Code 128"
    }

    private fun isValidEan13(code: String): Boolean {
        if (code.length!= 13) return false
        return try {
            val sum = code.substring(0, 12).mapIndexed { i, c ->
                val digit = c.toString().toInt()
                if (i % 2 == 0) digit else digit * 3
            }.sum()
            val check = (10 - (sum % 10)) % 10
            check == code.last().toString().toInt()
        } catch (e: Exception) { false }
    }

    fun validateValue(value: String, formatStr: String): String? {
        if (value.isBlank()) return "Введите номер карты"
        if (value.length > 200) return "Слишком длинное значение"
        val fmt = if (formatStr == "Автоматически") detectFormat(value) else formatStr
        when (fmt) {
            "EAN-13" -> if (!value.matches(Regex("""^\d{13}$"""))) return "EAN-13 должен содержать 13 цифр"
            "EAN-8" -> if (!value.matches(Regex("""^\d{8}$"""))) return "EAN-8 должен содержать 8 цифр"
            "UPC-A" -> if (!value.matches(Regex("""^\d{11,12}$"""))) return "UPC-A должен содержать 11 или 12 цифр"
            "UPC-E" -> if (!value.matches(Regex("""^\d{6,8}$"""))) return "UPC-E должен содержать 6-8 цифр"
            "ITF" -> if (!value.matches(Regex("""^\d+$""")) || value.length % 2!= 0) return "ITF должен содержать четное количество цифр"
        }
        return null
    }
}
