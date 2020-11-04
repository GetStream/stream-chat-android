package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.graphics.Typeface
import com.getstream.sdk.chat.ImageLoader
import com.getstream.sdk.chat.utils.LlcMigrationUtils
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.avatar.AvatarView.Companion.MAX_AVATAR_SECTIONS
import io.getstream.chat.android.ui.utils.adjustColorLBrightness
import io.getstream.chat.android.ui.utils.extensions.getIntArray
import kotlin.math.abs

internal class AvatarBitmapFactory(private val context: Context) {
    private val gradientBaseColors = context.getIntArray(R.array.avatar_gradient_colors)

    internal suspend fun createUserBitmap(user: User, style: AvatarStyle): Bitmap {
        return ImageLoader.getBitmap(context, user.image)
            ?: createInitialsBitmap(style, LlcMigrationUtils.getInitials(user))
    }

    internal suspend fun createChannelBitmaps(
        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle
    ): List<Bitmap> {
        return ImageLoader.getBitmap(context, channel.image)
            ?.let { listOf(it) }
            ?: createUsersBitmaps(lastActiveUsers, style).takeUnless { it.isEmpty() }
            ?: listOf(createInitialsBitmap(style, LlcMigrationUtils.getInitials(channel)))
    }

    private suspend fun createUsersBitmaps(users: List<User>, style: AvatarStyle): List<Bitmap> {
        return users.take(MAX_AVATAR_SECTIONS).map { createUserBitmap(it, style) }
    }

    private fun createInitialsBitmap(
        avatarStyle: AvatarStyle,
        initials: String,
    ): Bitmap {
        val avatarWidth = avatarStyle.avatarWidth - avatarStyle.avatarBorderWidth * 2
        val avatarHeight = avatarStyle.avatarHeight - avatarStyle.avatarBorderWidth * 2
        return Bitmap.createBitmap(
            avatarWidth,
            avatarHeight,
            Bitmap.Config.ARGB_8888
        ).apply {
            val canvas = Canvas(this)
            canvas.drawGradient(initials, avatarWidth, avatarHeight)
            canvas.drawInitials(avatarStyle, initials, avatarWidth, avatarHeight)
        }
    }

    private fun Canvas.drawGradient(initials: String, avatarWidth: Int, avatarHeight: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            shader = createLinearGradientShader(initials, avatarHeight)
        }
        drawRect(
            0f,
            0f,
            avatarWidth.toFloat(),
            avatarHeight.toFloat(),
            paint
        )
    }

    private fun Canvas.drawInitials(
        avatarStyle: AvatarStyle,
        initials: String,
        avatarWidth: Int,
        avatarHeight: Int
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
            avatarWidth / 2f,
            avatarHeight / 2f - (textPaint.ascent() + textPaint.descent()) / 2f,
            textPaint
        )
    }

    private fun createLinearGradientShader(initials: String, height: Int): Shader {
        val baseColorIndex = abs(initials.hashCode()) % gradientBaseColors.size
        val baseColor = gradientBaseColors[baseColorIndex]
        return LinearGradient(
            0f,
            0f,
            0f,
            height.toFloat(),
            adjustColorLBrightness(baseColor, GRADIENT_DARKER_COLOR_FACTOR),
            adjustColorLBrightness(baseColor, GRADIENT_LIGHTER_COLOR_FACTOR),
            Shader.TileMode.CLAMP
        )
    }

    companion object {
        private const val GRADIENT_DARKER_COLOR_FACTOR = 1.3f
        private const val GRADIENT_LIGHTER_COLOR_FACTOR = 0.7f
    }
}
