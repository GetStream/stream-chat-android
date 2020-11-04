package io.getstream.chat.android.ui.avatar

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils

internal class AvatarDrawable(bitmaps: List<Bitmap>) : Drawable() {
    private val avatarBitmaps = bitmaps.take(AvatarView.MAX_AVATAR_SECTIONS)
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
        avatarItems = when (avatarBitmaps.size) {
            0 -> emptyList()
            1 -> configureSingleAvatar(avatarBitmaps[0])
            2 -> configureDoubleAvatar(avatarBitmaps[0], avatarBitmaps[1])
            3 -> configureTripleAvatar(avatarBitmaps[0], avatarBitmaps[1], avatarBitmaps[2])
            else -> configureQuadrupleAvatar(
                avatarBitmaps[0],
                avatarBitmaps[1],
                avatarBitmaps[2],
                avatarBitmaps[3]
            )
        }
    }

    private fun configureSingleAvatar(avatarBitmap: Bitmap) =
        listOf(
            AvatarItem(
                avatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(0, 0, bounds.width(), bounds.height())
            )
        )

    private fun configureDoubleAvatar(
        topAvatarBitmap: Bitmap,
        bottomAvatarBitmap: Bitmap
    ): List<AvatarItem> {
        return listOf(
            AvatarItem(
                topAvatarBitmap.scaleCenterCrop(bounds.width() / 2, bounds.height()),
                Rect(0, 0, bounds.width(), bounds.height())
            ),
            AvatarItem(
                bottomAvatarBitmap.scaleCenterCrop(bounds.width() / 2, bounds.height()),
                Rect(bounds.width() / 2, 0, bounds.width() + bounds.width() / 2, bounds.height())
            )
        )
    }

    private fun configureTripleAvatar(
        topLeftAvatarBitmap: Bitmap,
        topRightAvatarBitmap: Bitmap,
        bottomAvatarBitmap: Bitmap
    ): List<AvatarItem> {
        return listOf(
            AvatarItem(
                topLeftAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height() / 2),
                Rect(0, 0, bounds.width(), bounds.height())
            ),
            AvatarItem(
                topRightAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(0, bounds.height() / 2, bounds.width() / 2, bounds.height())
            ),
            AvatarItem(
                bottomAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(bounds.width() / 2, bounds.height() / 2, bounds.width(), bounds.height())
            )
        )
    }

    private fun configureQuadrupleAvatar(
        topLeftAvatarBitmap: Bitmap,
        topRightAvatarBitmap: Bitmap,
        bottomLeftAvatarBitmap: Bitmap,
        bottomRightAvatarBitmap: Bitmap
    ): List<AvatarItem> {
        return listOf(
            AvatarItem(
                topLeftAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(0, 0, bounds.width() / 2, bounds.height() / 2)
            ),
            AvatarItem(
                topRightAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(0, bounds.height() / 2, bounds.width() / 2, bounds.height())
            ),
            AvatarItem(
                bottomLeftAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(bounds.width() / 2, 0, bounds.width(), bounds.height() / 2)
            ),
            AvatarItem(
                bottomRightAvatarBitmap.scaleCenterCrop(bounds.width(), bounds.height()),
                Rect(bounds.width() / 2, bounds.height() / 2, bounds.width(), bounds.height())
            )
        )
    }

    private fun Bitmap.scaleCenterCrop(newWidth: Int, newHeight: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(this, newWidth, newHeight)
    }

    private data class AvatarItem(val bitmap: Bitmap, val position: Rect)
}
