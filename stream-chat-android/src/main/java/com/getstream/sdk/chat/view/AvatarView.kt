package com.getstream.sdk.chat.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.media.ThumbnailUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.Circle
import com.getstream.sdk.chat.view.messages.AvatarStyle
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.math.max

public class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    public fun setLastActiveUsers(lastActiveUsers: List<User>, style: AvatarStyle) {
        configUIs(style) { AvatarDrawable(lastActiveUsers.createBitmaps(style)) }
    }

    public fun setChannelAndLastActiveUsers(
        channel: Channel?,
        lastActiveUsers: List<User>,
        style: AvatarStyle
    ) {
        configUIs(style) {
            AvatarDrawable(
                channel?.createBitmap()?.let { listOf(it) }
                    ?: lastActiveUsers.createBitmaps(style).takeUnless { it.isEmpty() }
                    ?: channel?.initials?.let { listOf(createImageRounded(it, style)) }
                    ?: emptyList()
            )
        }
    }

    public fun setUser(user: User, style: AvatarStyle) {
        configUIs(style) { AvatarDrawable(listOfNotNull(user.createBitmap(style))) }
    }

    private fun configUIs(
        style: AvatarStyle,
        generateAvatarDrawable: suspend () -> AvatarDrawable
    ) {
        GlobalScope.launch(DispatcherProvider.Main) {
            layoutParams?.apply {
                width = style.avatarWidth
                height = style.avatarHeight
            }?.let(::setLayoutParams)
            setImageDrawable(generateAvatarDrawable())
        }
    }

    private suspend fun User.createBitmap(style: AvatarStyle): Bitmap =
        StreamImageLoader.instance().loadAsBitmap(
            context = context,
            url = image,
            transformation = Circle,
        ) ?: createImageRounded(initials, style)

    private suspend fun List<User>.createBitmaps(style: AvatarStyle): List<Bitmap> =
        take(3).map { it.createBitmap(style) }

    private suspend fun Channel.createBitmap(): Bitmap? =
        StreamImageLoader.instance().loadAsBitmap(
            context = context,
            url = image,
            transformation = Circle
        )

    private fun createImageRounded(initials: String, avatarStyle: AvatarStyle): Bitmap {
        val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            typeface = avatarStyle.avatarInitialText.font
            textAlign = Paint.Align.CENTER
            color = avatarStyle.avatarInitialText.color
            textSize = avatarStyle.avatarInitialText.size.toFloat()
        }
        val paintCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = avatarStyle.avatarBackgroundColor
        }
        val textBounds = Rect()
        paintText.getTextBounds(initials, 0, initials.length, textBounds)
        val radius =
            max(textBounds.width(), textBounds.height()).takeUnless { it <= 0 } ?: MIN_RADIUS_SIZE
        val bitmapSize = (radius * 2)
        val output = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawCircle(radius.toFloat(), radius.toFloat(), radius.toFloat(), paintCircle)
        canvas.drawText(
            initials,
            radius.toFloat(),
            (radius + textBounds.height() / 2).toFloat(),
            paintText
        )
        return output
    }
}

private const val MIN_RADIUS_SIZE = 100
private const val FACTOR = 1.7

private class AvatarDrawable(bitmaps: List<Bitmap>) : Drawable() {
    private val avatarBitmaps = bitmaps.take(3)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var avatarItems: List<AvatarItem> = listOf() // = ArrayList<AvatarItem>()

    private fun reconfigureItems() {
        avatarItems = when (avatarBitmaps.size) {
            0 -> listOf()
            1 -> configureSingleAvatar(avatarBitmaps[0])
            2 -> configureDoubleAvatar(avatarBitmaps[0], avatarBitmaps[1])
            else -> configureTripleAvatar(avatarBitmaps[0], avatarBitmaps[1], avatarBitmaps[2])
        }
    }

    private fun configureSingleAvatar(avatarBitmap: Bitmap) =
        listOf(
            AvatarItem(
                avatarBitmap.scaleCenterCrop(bounds),
                Rect(0, 0, bounds.width(), bounds.height())
            )
        )

    private fun configureDoubleAvatar(
        topAvatarBitmap: Bitmap,
        bottomAvatarBitmap: Bitmap
    ): List<AvatarItem> {
        val avatarBound = bounds.reduce()
        return listOf(
            AvatarItem(topAvatarBitmap.scaleCenterCrop(avatarBound), avatarBound),
            AvatarItem(
                bottomAvatarBitmap.scaleCenterCrop(avatarBound),
                Rect(
                    bounds.right - avatarBound.right,
                    bounds.bottom - avatarBound.bottom,
                    bounds.right,
                    bounds.bottom
                )
            )
        )
    }

    private fun configureTripleAvatar(
        topLeftAvatarBitmap: Bitmap,
        topRightAvatarBitmap: Bitmap,
        bottomAvatarBitmap: Bitmap
    ): List<AvatarItem> {
        val avatarBound = bounds.reduce()
        return listOf(
            AvatarItem(topLeftAvatarBitmap.scaleCenterCrop(avatarBound), avatarBound),
            AvatarItem(
                topRightAvatarBitmap.scaleCenterCrop(avatarBound),
                Rect(
                    bounds.right - avatarBound.right,
                    0,
                    bounds.right,
                    avatarBound.bottom
                )
            ),
            AvatarItem(
                bottomAvatarBitmap.scaleCenterCrop(avatarBound),
                Rect(
                    (bounds.right / 2) - (avatarBound.right / 2),
                    bounds.bottom - avatarBound.bottom,
                    (bounds.right / 2) + (avatarBound.right / 2),
                    bounds.bottom
                )
            )
        )
    }

    override fun draw(canvas: Canvas) {
        avatarItems.forEach {
            canvas.drawBitmap(it.bitmap, bounds, it.position, paint)
        }
    }

    private data class AvatarItem(val bitmap: Bitmap, val position: Rect)

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

    private fun Rect.reduce() =
        Rect(0, 0, width().div(FACTOR).toInt(), height().div(FACTOR).toInt())

    private fun Bitmap.scaleCenterCrop(size: Rect): Bitmap =
        ThumbnailUtils.extractThumbnail(this, size.width(), size.height())
}
