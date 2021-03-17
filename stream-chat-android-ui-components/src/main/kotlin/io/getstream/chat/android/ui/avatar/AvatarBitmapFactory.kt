package io.getstream.chat.android.ui.avatar

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
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.avatar.AvatarView.Companion.MAX_AVATAR_SECTIONS
import io.getstream.chat.android.ui.avatar.internal.AvatarBitmapCombiner
import io.getstream.chat.android.ui.common.extensions.internal.getIntArray
import io.getstream.chat.android.ui.common.internal.adjustColorLBrightness
import kotlinx.coroutines.withContext
import kotlin.math.abs

public open class AvatarBitmapFactory(private val context: Context) {
    private val gradientBaseColors = context.getIntArray(R.array.stream_ui_avatar_gradient_colors)

    internal suspend fun createUserBitmapInternal(
        user: User,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap {
        val customBitmap = withContext(DispatcherProvider.IO) {
            createUserBitmapBlocking(user, style, avatarSize)
        }
        val bitmap = when (customBitmap) {
            NOT_IMPLEMENTED_MARKER -> createUserBitmap(user, style, avatarSize)
            else -> customBitmap
        }

        // Image loading successful, return the loaded bitmap
        if (bitmap != null) {
            return bitmap
        }

        // Use default, locally generated image instead
        val customDefaultBitmap = withContext(DispatcherProvider.IO) {
            createDefaultUserBitmapBlocking(user, style, avatarSize)
        }
        return when (customDefaultBitmap) {
            NOT_IMPLEMENTED_MARKER -> createDefaultUserBitmap(user, style, avatarSize)
            else -> customDefaultBitmap
        }
    }

    /**
     * Load a Bitmap with the specified [avatarSize] to represent the [user].
     *
     * This method takes precedence over [createUserBitmap] if both are implemented.
     *
     * Override this method only if you can't provide a suspending implementation, otherwise
     * override [createUserBitmap] instead.
     *
     * @return The loaded bitmap or null if the loading failed (e.g. network issues).
     */
    public open fun createUserBitmapBlocking(
        user: User,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap? {
        return NOT_IMPLEMENTED_MARKER
    }

    /**
     * Load a Bitmap with the specified [avatarSize] to represent the [user], in a suspending operation.
     *
     * The [createUserBitmapBlocking] method takes precedence over this one if both are implemented.
     * Prefer implementing this method if possible.
     *
     * @return The loaded bitmap or null if the loading failed (e.g. network issues).
     */
    public open suspend fun createUserBitmap(
        user: User,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap? {
        return StreamImageLoader.instance().loadAsBitmap(context, user.image)
    }

    /**
     * Load a default Bitmap with the specified [avatarSize] to represent the [user].
     * This should be a process that can never fail (e.g. not depend on network).
     *
     * This method takes precedence over [createDefaultUserBitmap] if both are implemented.
     *
     * Override this method only if you can't provide a suspending implementation, otherwise
     * override [createDefaultUserBitmap] instead.
     *
     * @return The loaded bitmap.
     */
    public open fun createDefaultUserBitmapBlocking(
        user: User,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap {
        return NOT_IMPLEMENTED_MARKER
    }

    /**
     * Load a Bitmap with the specified [avatarSize] to represent the [user], in a suspending operation.
     *
     * The [createDefaultUserBitmapBlocking] method takes precedence over this one if both are implemented.
     * Prefer implementing this method if possible.
     *
     * @return The loaded bitmap.
     */
    public open suspend fun createDefaultUserBitmap(
        user: User,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap {
        return createInitialsBitmap(style, avatarSize, user.initials)
    }

    internal suspend fun createChannelBitmapInternal(

        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap {
        val customBitmap = withContext(DispatcherProvider.IO) {
            createChannelBitmapBlocking(channel, lastActiveUsers, style, avatarSize)
        }
        val bitmap = when (customBitmap) {
            NOT_IMPLEMENTED_MARKER -> createChannelBitmap(channel, lastActiveUsers, style, avatarSize)
            else -> customBitmap
        }

        // Image loading successful, return the loaded bitmap
        if (bitmap != null) {
            return bitmap
        }

        // Use default, locally generated image instead
        val customDefaultBitmap = withContext(DispatcherProvider.IO) {
            createDefaultChannelBitmapBlocking(channel, lastActiveUsers, style, avatarSize)
        }
        return when (customDefaultBitmap) {
            NOT_IMPLEMENTED_MARKER -> createDefaultChannelBitmap(channel, lastActiveUsers, style, avatarSize)
            else -> customDefaultBitmap
        }
    }

    public open fun createChannelBitmapBlocking(
        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap? {
        return NOT_IMPLEMENTED_MARKER
    }

    public open suspend fun createChannelBitmap(
        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap? {
        return StreamImageLoader.instance().loadAsBitmap(context, channel.image)
            ?: createUsersBitmaps(lastActiveUsers, style, avatarSize).takeIf { it.isNotEmpty() }
                ?.let {
                    if (it.size == 1) it[0] else AvatarBitmapCombiner.combine(it, avatarSize)
                }
    }

    public open fun createDefaultChannelBitmapBlocking(
        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap {
        return NOT_IMPLEMENTED_MARKER
    }

    public open suspend fun createDefaultChannelBitmap(
        channel: Channel,
        lastActiveUsers: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): Bitmap {
        return createInitialsBitmap(style, avatarSize, channel.initials)
    }

    private suspend fun createUsersBitmaps(
        users: List<User>,
        style: AvatarStyle,
        @Px avatarSize: Int,
    ): List<Bitmap> {
        return users.take(MAX_AVATAR_SECTIONS).map { createUserBitmapInternal(it, style, avatarSize) }
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
        @Px avatarSize: Int,
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

        /**
         * Marker object to detect whether methods have been implemented by custom subclasses.
         */
        private val NOT_IMPLEMENTED_MARKER: Bitmap by lazy { Bitmap.createBitmap(1, 1, Bitmap.Config.ALPHA_8) }
    }
}
