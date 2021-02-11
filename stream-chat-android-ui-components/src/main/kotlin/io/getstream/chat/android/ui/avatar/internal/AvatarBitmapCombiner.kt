package io.getstream.chat.android.ui.avatar.internal

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import android.media.ThumbnailUtils
import androidx.annotation.Px
import androidx.core.graphics.applyCanvas
import io.getstream.chat.android.ui.avatar.AvatarView

internal object AvatarBitmapCombiner {
    private val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }

    fun combine(bitmaps: List<Bitmap>, @Px size: Int): Bitmap {
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).applyCanvas {
            val sourceRect = Rect(0, 0, size, size)
            createAvatarItems(bitmaps, size).forEach {
                drawBitmap(it.bitmap, sourceRect, it.position, paint)
            }
        }
    }

    private fun createAvatarItems(bitmaps: List<Bitmap>, @Px size: Int): List<AvatarItem> {
        val avatarBitmaps = bitmaps.take(AvatarView.MAX_AVATAR_SECTIONS)
        return when (avatarBitmaps.size) {
            0 -> emptyList()
            1 -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.FULL_CIRCLE, size)
            )
            2 -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.LEFT, size),
                avatarBitmaps[1].toAvatarItem(SectionType.RIGHT, size)
            )
            3 -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.TOP_LEFT, size),
                avatarBitmaps[1].toAvatarItem(SectionType.TOP_RIGHT, size),
                avatarBitmaps[2].toAvatarItem(SectionType.BOTTOM, size)
            )
            else -> listOf(
                avatarBitmaps[0].toAvatarItem(SectionType.TOP_LEFT, size),
                avatarBitmaps[1].toAvatarItem(SectionType.TOP_RIGHT, size),
                avatarBitmaps[2].toAvatarItem(SectionType.BOTTOM_LEFT, size),
                avatarBitmaps[3].toAvatarItem(SectionType.BOTTOM_RIGHT, size)
            )
        }
    }

    private fun Bitmap.toAvatarItem(sectionType: SectionType, @Px size: Int): AvatarItem {
        return when (sectionType) {
            SectionType.FULL_CIRCLE -> {
                AvatarItem(scaleCenterCrop(size, size), Rect(0, 0, size, size))
            }
            SectionType.LEFT -> {
                AvatarItem(scaleCenterCrop(size / 2, size), Rect(0, 0, size, size))
            }
            SectionType.RIGHT -> {
                AvatarItem(
                    scaleCenterCrop(size / 2, size),
                    Rect(size / 2, 0, size + size / 2, size)
                )
            }
            SectionType.BOTTOM -> {
                AvatarItem(
                    scaleCenterCrop(size, size / 2),
                    Rect(0, size / 2, size, size + size / 2)
                )
            }
            SectionType.TOP_LEFT -> {
                AvatarItem(
                    scaleCenterCrop(size, size),
                    Rect(0, 0, size / 2, size / 2)
                )
            }
            SectionType.TOP_RIGHT -> {
                AvatarItem(
                    scaleCenterCrop(size, size),
                    Rect(size / 2, 0, size, size / 2)
                )
            }
            SectionType.BOTTOM_LEFT -> {
                AvatarItem(
                    scaleCenterCrop(size, size),
                    Rect(0, size / 2, size / 2, size)
                )
            }
            SectionType.BOTTOM_RIGHT -> {
                AvatarItem(
                    scaleCenterCrop(size, size),
                    Rect(size / 2, size / 2, size, size)
                )
            }
        }
    }

    private fun Bitmap.scaleCenterCrop(@Px newWidth: Int, @Px newHeight: Int): Bitmap {
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
