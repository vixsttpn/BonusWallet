
package com.bonuswallet.app.util

import android.graphics.Bitmap
import com.bonuswallet.app.domain.detector.BarcodeTypeDetector
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
        return BarcodeTypeDetector.detect(value).formatString
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
        val result = BarcodeTypeDetector.detect(value)
        if (!result.isValid && result.type == com.bonuswallet.app.domain.detector.BarcodeType.UNKNOWN) {
            return result.error
        }
        return null
    }

    fun validateValue(value: String, formatStr: String): String? = validate(value, formatStr)
}

