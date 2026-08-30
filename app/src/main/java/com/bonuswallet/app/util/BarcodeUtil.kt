
package com.bonuswallet.app.util

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object BarcodeUtil {

    val SUPPORTED_FORMATS = listOf(
        "Автоматически",
        "EAN-13",
        "EAN-8",
        "UPC-A",
        "UPC-E",
        "Code 128",
        "Code 39",
        "ITF",
        "Codabar",
        "QR Code"
    )

    fun mapToZxingFormat(format: String): BarcodeFormat? {
        return when (format) {
            "EAN-13" -> BarcodeFormat.EAN_13
            "EAN-8" -> BarcodeFormat.EAN_8
            "UPC-A" -> BarcodeFormat.UPC_A
            "UPC-E" -> BarcodeFormat.UPC_E
            "Code 128" -> BarcodeFormat.CODE_128
            "Code 39" -> BarcodeFormat.CODE_39
            "ITF" -> BarcodeFormat.ITF
            "Codabar" -> BarcodeFormat.CODABAR
            "QR Code" -> BarcodeFormat.QR_CODE
            else -> null
        }
    }

    fun detectFormat(value: String): String {
        val clean = value.trim()
        if (clean.isEmpty()) return "Code 128"

        // EAN-13: 13 digits
        if (clean.matches(Regex("^\d{13}$")) && isValidEan13(clean)) return "EAN-13"
        // EAN-8: 8 digits
        if (clean.matches(Regex("^\d{8}$"))) return "EAN-8"
        // UPC-A: 12 digits
        if (clean.matches(Regex("^\d{12}$"))) return "UPC-A"
        // UPC-E: 8 digits starting 0/1
        if (clean.matches(Regex("^[01]\d{7}$"))) return "UPC-E"
        // ITF: even number of digits
        if (clean.matches(Regex("^\d+$")) && clean.length % 2 == 0 && clean.length >= 6) return "ITF"
        // Code 39: uppercase alphanum + - . space $ / + %
        if (clean.matches(Regex("^[A-Z0-9\-\. \$/\+\%]+$")) && clean.length <= 20) return "Code 39"
        // Codabar: starts/ends with A-D
        if (clean.matches(Regex("^[A-D][0-9\-\$:\./\+]+[A-D]$"))) return "Codabar"
        // QR for urls or long mixed
        if (clean.length > 25 || clean.contains("http") || clean.contains("://")) return "QR Code"
        // Default
        return "Code 128"
    }

    private fun isValidEan13(code: String): Boolean {
        try {
            var sum = 0
            for (i in 0 until 12) {
                val digit = code[i].digitToInt()
                sum += if (i % 2 == 0) digit else digit * 3
            }
            val check = (10 - (sum % 10)) % 10
            return check == code[12].digitToInt()
        } catch (e: Exception) { return true }
    }

    fun generateBitmap(value: String, formatStr: String, width: Int = 1000, height: Int = 400): Bitmap? {
        if (value.isBlank()) return null
        try {
            val actualFormatStr = if (formatStr == "Автоматически") detectFormat(value) else formatStr
            val zxingFormat = mapToZxingFormat(actualFormatStr) ?: BarcodeFormat.CODE_128

            val hints = mutableMapOf<EncodeHintType, Any>()
            hints[EncodeHintType.MARGIN] = if (zxingFormat == BarcodeFormat.QR_CODE) 1 else 10
            if (zxingFormat == BarcodeFormat.QR_CODE) {
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.M
            }

            val writer = MultiFormatWriter()
            val bitMatrix: BitMatrix = writer.encode(value, zxingFormat, width, height, hints)
            return bitMatrixToBitmap(bitMatrix)
        } catch (e: Exception) {
            // fallback to CODE_128
            return try {
                val writer = MultiFormatWriter()
                val bm = writer.encode(value, BarcodeFormat.CODE_128, width, height, mapOf(EncodeHintType.MARGIN to 10))
                bitMatrixToBitmap(bm)
            } catch (e2: Exception) { null }
        }
    }

    private fun bitMatrixToBitmap(matrix: BitMatrix): Bitmap {
        val w = matrix.width
        val h = matrix.height
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (x in 0 until w) {
            for (y in 0 until h) {
                bmp.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            }
        }
        return bmp
    }

    fun validate(value: String, formatStr: String): String? {
        if (value.isBlank()) return "Введите номер карты"
        if (value.length > 200) return "Слишком длинное значение"
        val fmt = if (formatStr == "Автоматически") detectFormat(value) else formatStr
        when (fmt) {
            "EAN-13" -> if (!value.matches(Regex("^\d{13}$"))) return "EAN-13 должен содержать 13 цифр"
            "EAN-8" -> if (!value.matches(Regex("^\d{8}$"))) return "EAN-8 должен содержать 8 цифр"
            "UPC-A" -> if (!value.matches(Regex("^\d{11,12}$"))) return "UPC-A должен содержать 11 или 12 цифр"
            "UPC-E" -> if (!value.matches(Regex("^\d{6,8}$"))) return "UPC-E должен содержать 6-8 цифр"
            "ITF" -> if (!value.matches(Regex("^\d+$")) || value.length % 2 != 0) return "ITF должен содержать четное количество цифр"
        }
        return null
    }
}
