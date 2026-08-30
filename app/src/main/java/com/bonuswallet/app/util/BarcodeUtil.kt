package com.bonuswallet.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object BarcodeUtil {

    val SUPPORTED_FORMATS = listOf(
        "Автоматически",
        "QR Code",
        "Code 128",
        "EAN-13",
        "EAN-8",
        "UPC-A",
        "UPC-E",
        "ITF",
        "Code 39",
        "Codabar"
    )

    fun detectFormat(value: String): String {
        val clean = value.trim()
        if (clean.isEmpty()) return "Code 128"
        if (clean.matches(Regex("""^\d{13}$""")) && isValidEan13(clean)) return "EAN-13"
        if (clean.matches(Regex("""^\d{8}$"""))) return "EAN-8"
        if (clean.matches(Regex("""^\d{12}$"""))) return "UPC-A"
        if (clean.matches(Regex("""^[01]\d{7}$"""))) return "UPC-E"
        if (clean.matches(Regex("""^\d+$""")) && clean.length % 2 == 0 && clean.length >= 6) return "ITF"
        if (clean.matches(Regex("""^[A-Z0-9\-\. $/+%]+$""")) && clean.length <= 20) return "Code 39"
        if (clean.matches(Regex("""^[A-D][0-9\-${'$'}:/.+]+[A-D]$"""))) return "Codabar"
        if (clean.length > 25 || clean.contains("http") || clean.contains("://")) return "QR Code"
        return "Code 128"
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

    fun validateValue(value: String, formatStr: String): String? {
        if (value.isBlank()) return "Введите номер карты"
        if (value.length > 200) return "Слишком длинное значение"
        val fmt = if (formatStr == "Автоматически") detectFormat(value) else formatStr
        when (fmt) {
            "EAN-13" -> if (!value.matches(Regex("""^\d{13}$"""))) return "EAN-13 должен содержать 13 цифр"
            "EAN-8" -> if (!value.matches(Regex("""^\d{8}$"""))) return "EAN-8 должен содержать 8 цифр"
            "UPC-A" -> if (!value.matches(Regex("""^\d{11,12}$"""))) return "UPC-A должен содержать 11 или 12 цифр"
            "UPC-E" -> if (!value.matches(Regex("""^\d{6,8}$"""))) return "UPC-E должен содержать 6-8 цифр"
            "ITF" -> if (!value.matches(Regex("""^\d+$""")) || value.length % 2 != 0) return "ITF должен содержать четное количество цифр"
        }
        return null
    }

    // алиас для старых вызовов
    fun validate(value: String, formatStr: String): String? = validateValue(value, formatStr)

    fun generateBitmap(content: String, formatStr: String, width: Int, height: Int): Bitmap? {
        return try {
            val fmt = if (formatStr == "Автоматически") detectFormat(content) else formatStr
            val zxingFormat = when (fmt) {
                "QR Code" -> BarcodeFormat.QR_CODE
                "EAN-13" -> BarcodeFormat.EAN_13
                "EAN-8" -> BarcodeFormat.EAN_8
                "UPC-A" -> BarcodeFormat.UPC_A
                "UPC-E" -> BarcodeFormat.UPC_E
                "ITF" -> BarcodeFormat.ITF
                "Code 39" -> BarcodeFormat.CODE_39
                "Codabar" -> BarcodeFormat.CODABAR
                else -> BarcodeFormat.CODE_128
            }
            val matrix: BitMatrix = MultiFormatWriter().encode(content, zxingFormat, width, height)
            val w = matrix.width
            val h = matrix.height
            val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)
            for (x in 0 until w) {
                for (y in 0 until h) {
                    bmp.setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
}
