package com.bonuswallet.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.min

object AutoCropUtil {

    fun autoCrop(context: Context, uri: Uri): Bitmap? {
        return try {
            val input = context.contentResolver.openInputStream(uri) ?: return null
            val original = BitmapFactory.decodeStream(input)
            input.close()
            if (original == null) return null
            smartCenterCrop(original)
        } catch(e: Exception) {
            null
        }
    }

    fun autoCrop(bitmap: Bitmap): Bitmap {
        return smartCenterCrop(bitmap)
    }

    fun autoCrop(context: Context, bitmap: Bitmap): Bitmap {
        return smartCenterCrop(bitmap)
    }

    // Smart center crop that keeps aspect 85x54 (bank card) and 85% of image
    private fun smartCenterCrop(bitmap: Bitmap): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        // Target card ratio ~1.586
        val targetRatio = 1.586f
        var cropW = w
        var cropH = (w / targetRatio).toInt()
        if (cropH > h) {
            cropH = h
            cropW = (h * targetRatio).toInt()
        }
        // Take 90% to remove borders
        cropW = (cropW * 0.92f).toInt()
        cropH = (cropH * 0.92f).toInt()
        val x = (w - cropW) / 2
        val y = (h - cropH) / 2
        return try {
            Bitmap.createBitmap(bitmap, x.coerceAtLeast(0), y.coerceAtLeast(0), cropW.coerceAtMost(w - x), cropH.coerceAtMost(h - y))
        } catch(e: Exception) {
            bitmap
        }
    }
}

