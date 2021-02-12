package io.getstream.chat.android.ui.avatar.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import androidx.annotation.Px
import com.getstream.sdk.chat.images.StreamImageLoader
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.avatar.AvatarView.Companion.MAX_AVATAR_SECTIONS
import io.getstream.chat.android.ui.common.extensions.internal.getIntArray
import io.getstream.chat.android.ui.common.internal.adjustColorLBrightness
import kotlin.math.abs

internal class AvatarBitmapFactory(private val context: Context) {
    private val gradientBaseColors = context.getIntArray(R.array.stream_ui_avatar_gradient_colors)

    internal suspend fun createUserBitmap(
        user: User,
        style: AvatarStyle,
        @Px avatarSize: Int
    ): Bitmap {
        return StreamImageLoader.instance().loadAsBitmap(context, user.image)
            ?: createInitialsBitmap(style, avatarSize, user.initials)
    }

    internal suspend fun createChannelBitmaps(
        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int
    ): List<Bitmap> {
        return StreamImageLoader.instance().loadAsBitmap(context, channel.image)
            ?.let { listOf(it) }
            ?: createUsersBitmaps(lastActiveUsers, style, avatarSize).takeUnless { it.isEmpty() }
            ?: listOf(createInitialsBitmap(style, avatarSize, channel.initials))
    }

    private suspend fun createUsersBitmaps(
        users: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int
    ): List<Bitmap> {
        return users.take(MAX_AVATAR_SECTIONS).map { createUserBitmap(it, style, avatarSize) }
    }

    private fun createInitialsBitmap(
        avatarStyle: AvatarStyle,
        @Px avatarSize: Int,
        initials: String,
    ): Bitmap {
        val avatarSizeWithoutBorder = avatarSize - avatarStyle.avatarBorderWidth * 2
        return Bitmap.createBitmap(
            avatarSizeWithoutBorder,
            avatarSizeWithoutBorder,
            Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = Canvas(this)
            canvas.drawGradient(initials, avatarSizeWithoutBorder)
            canvas.drawInitials(avatarStyle, initials, avatarSizeWithoutBorder)
        }
    }

    private fun Canvas.drawGradient(initials: String, avatarSize: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = createLinearGradientShader(initials, avatarSize)
        }
        drawRect(
            0f,
            0f,
            avatarSize.toFloat(),
            avatarSize.toFloat(),
            paint
        )
    }

    private fun Canvas.drawInitials(
        avatarStyle: AvatarStyle,
        initials: String,
        @Px avatarSize: Int
    ) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            typeface = avatarStyle.avatarInitialText.font ?: Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            color = avatarStyle.avatarInitialText.color
            textSize = avatarStyle.avatarInitialText.size.toFloat()
        }
        drawText(
            initials,
            avatarSize / 2f,
            avatarSize / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint
        )
    }

    private fun createLinearGradientShader(initials: String, @Px avatarSize: Int): Shader {
        val baseColorIndex = abs(initials.hashCode()) % gradientBaseColors.size
        val baseColor = gradientBaseColors[baseColorIndex]
        return LinearGradient(
            0f,
            0f,
            0f,
            avatarSize.toFloat(),
            adjustColorLBrightness(baseColor, GRADIENT_DARKER_COLOR_FACTOR),
            adjustColorLBrightness(baseColor, GRADIENT_LIGHTER_COLOR_FACTOR),
            Shader.TileMode.CLAMP
        )
    }

    private companion object {
        private const val GRADIENT_DARKER_COLOR_FACTOR = 1.3f
        private const val GRADIENT_LIGHTER_COLOR_FACTOR = 0.7f
    }
}
