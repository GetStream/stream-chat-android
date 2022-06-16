package io.getstream.chat.docs.kotlin.ui.utility

import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.core.graphics.applyCanvas
import coil.size.Size
import coil.transform.Transformation

/**
 * A [Transformation] that converts an image to grayscale.
 */
class GrayscaleTransformation : Transformation {

    override val cacheKey: String = GrayscaleTransformation::class.java.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        paint.colorFilter = COLOR_FILTER

        val output = Bitmap.createBitmap(input.width, input.height, Bitmap.Config.ARGB_8888)
        output.applyCanvas {
            drawBitmap(input, 0f, 0f, paint)
        }

        return output
    }

    override fun equals(other: Any?) = other is GrayscaleTransformation

    override fun hashCode() = javaClass.hashCode()

    override fun toString() = "GrayscaleTransformation()"

    private companion object {
        val COLOR_FILTER = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
}