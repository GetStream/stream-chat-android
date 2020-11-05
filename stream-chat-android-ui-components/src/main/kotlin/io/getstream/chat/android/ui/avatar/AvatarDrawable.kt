package io.getstream.chat.android.ui.avatar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils

internal class AvatarDrawable(private val bitmaps: List<Bitmap>) : Drawable() {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }
    private var avatarItems: List<AvatarItem> = listOf()

    override fun draw(canvas: Canvas) {
        avatarItems.forEach {
            canvas.drawBitmap(it.bitmap, bounds, it.position, paint)
        }
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        reconfigureItems()
    }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
    }

    private fun reconfigureItems() {
        val avatarBitmaps = bitmaps.take(AvatarView.MAX_AVATAR_SECTIONS)
        avatarItems = when (avatarBitmaps.size) {
            0 -> emptyList()
            1 -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.FULL_CIRCLE)
            )
            2 -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.LEFT),
                avatarBitmaps[1].toAvatarItem(SectionType.RIGHT)
            )
            3 -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.TOP_LEFT),
                avatarBitmaps[1].toAvatarItem(SectionType.TOP_RIGHT),
                avatarBitmaps[2].toAvatarItem(SectionType.BOTTOM)
            )
            else -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.TOP_LEFT),
                avatarBitmaps[1].toAvatarItem(SectionType.TOP_RIGHT),
                avatarBitmaps[2].toAvatarItem(SectionType.BOTTOM_LEFT),
                avatarBitmaps[3].toAvatarItem(SectionType.BOTTOM_RIGHT)
            )
        }
    }

    private fun Bitmap.toAvatarItem(sectionType: SectionType): AvatarItem {
        val width = bounds.width()
        val height = bounds.height()
        return when (sectionType) {
            SectionType.FULL_CIRCLE -> {
                AvatarItem(scaleCenterCrop(width, height), Rect(0, 0, width, height))
            }
            SectionType.LEFT -> {
                AvatarItem(scaleCenterCrop(width / 2, height), Rect(0, 0, width, height))
            }
            SectionType.RIGHT -> {
                AvatarItem(
                    scaleCenterCrop(width / 2, height),
                    Rect(width / 2, 0, width + width / 2, height)
                )
            }
            SectionType.BOTTOM -> {
                AvatarItem(
                    scaleCenterCrop(width, height / 2),
                    Rect(0, height / 2, width, height + height / 2)
                )
            }
            SectionType.TOP_LEFT -> {
                AvatarItem(
                    scaleCenterCrop(width, height),
                    Rect(0, 0, width / 2, height / 2)
                )
            }
            SectionType.TOP_RIGHT -> {
                AvatarItem(
                    scaleCenterCrop(width, height),
                    Rect(width / 2, 0, width, height / 2)
                )
            }
            SectionType.BOTTOM_LEFT -> {
                AvatarItem(
                    scaleCenterCrop(width, height),
                    Rect(0, height / 2, width / 2, height)
                )
            }
            SectionType.BOTTOM_RIGHT -> {
                AvatarItem(
                    scaleCenterCrop(width, height),
                    Rect(width / 2, height / 2, width, height)
                )
            }
        }
    }

    private fun Bitmap.scaleCenterCrop(newWidth: Int, newHeight: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(this, newWidth, newHeight)
    }

    private data class AvatarItem(val bitmap: Bitmap, val position: Rect)

    private enum class SectionType {
        FULL_CIRCLE,
        LEFT,
        RIGHT,
        BOTTOM,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
