package com.kisanalert.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

object ImageCompressionUtils {
    private const val MAX_IMAGE_DIMENSION: Int = 1024
    private const val JPEG_QUALITY: Int = 80

    fun compressImageFile(imageFile: File): ByteArray {
        val originalBitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            ?: return imageFile.readBytes()
        val scaledBitmap = scaleBitmap(originalBitmap)
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, outputStream)
        if (scaledBitmap !== originalBitmap) {
            originalBitmap.recycle()
            scaledBitmap.recycle()
        }
        return outputStream.toByteArray()
    }

    private fun scaleBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= MAX_IMAGE_DIMENSION && height <= MAX_IMAGE_DIMENSION) {
            return bitmap
        }
        val scale = minOf(
            MAX_IMAGE_DIMENSION.toFloat() / width.toFloat(),
            MAX_IMAGE_DIMENSION.toFloat() / height.toFloat()
        )
        val scaledWidth = (width * scale).roundToInt()
        val scaledHeight = (height * scale).roundToInt()
        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }
}
