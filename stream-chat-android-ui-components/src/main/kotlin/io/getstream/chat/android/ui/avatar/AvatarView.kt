package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.Circle
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.RoundedCorners
import com.getstream.sdk.chat.images.load
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.avatar.internal.Avatar
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx

/**
 * A component that shows the profile image of the [User] and [Channel] with the online indicator and border.
 * If the profile image does not exist, the initials will be shown up instead.
 */
public class AvatarView : AppCompatImageView {
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val onlineIndicatorOutlinePaint = Paint().apply { style = Paint.Style.FILL }
    private val onlineIndicatorPaint = Paint().apply { style = Paint.Style.FILL }

    private lateinit var avatarStyle: AvatarStyle
    private var isOnline: Boolean = false
    private var avatarViewSize: Int = 0

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    /**
     * Sets a [Channel] to show up the profile images of channel members.
     * The online indicator will be hidden if you set [Channel].
     *
     * @param channel A channel that includes user list information.
     */
    public fun setChannelData(channel: Channel) {
        val otherUsers = channel.getUsersExcludingCurrent()
        if (channel.isAnonymousChannel() && otherUsers.size == 1) {
            setUserData(otherUsers.first())
        } else {
            load(
                data = Avatar.ChannelAvatar(channel, avatarStyle),
                transformation = avatarShape(avatarStyle),
            )

            isOnline = false
        }
    }

    /**
     * Sets a [User] to show up the profile image of a user.
     * The online indicator will be shown or not by the user's connection state.
     *
     * @param user A user that includes user information.
     */
    public fun setUserData(user: User) {
        load(
            data = Avatar.UserAvatar(user, avatarStyle),
            transformation = avatarShape(avatarStyle),
        )

        isOnline = user.online
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(0, widthMeasureSpec)
        val height = resolveSize(0, heightMeasureSpec)
        avatarViewSize = if (width > height) height else width

        setMeasuredDimension(avatarViewSize, avatarViewSize)
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable != null) {
            super.onDraw(canvas)
            drawBorder(canvas, avatarStyle, borderPaint)
            drawOnlineStatus(canvas, isOnline, avatarStyle)
        }
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        setStyle(AvatarStyle(context, attrs))
    }

    private fun setStyle(avatarStyle: AvatarStyle) {
        this.avatarStyle = avatarStyle
        borderPaint.color = avatarStyle.avatarBorderColor
        borderPaint.strokeWidth = avatarStyle.avatarBorderWidth.toFloat()
        val padding = (avatarStyle.avatarBorderWidth - AVATAR_SIZE_EXTRA).coerceAtLeast(0)
        setPadding(padding, padding, padding, padding)
        onlineIndicatorOutlinePaint.color = avatarStyle.onlineIndicatorBorderColor
        onlineIndicatorPaint.color = avatarStyle.onlineIndicatorColor
    }

    private fun drawOnlineStatus(canvas: Canvas, isOnline: Boolean, avatarStyle: AvatarStyle) {
        if (isOnline && avatarStyle.onlineIndicatorEnabled) {
            val cx: Float = when (avatarStyle.onlineIndicatorPosition) {
                OnlineIndicatorPosition.TOP_LEFT,
                OnlineIndicatorPosition.BOTTOM_LEFT,
                -> width / 8f
                OnlineIndicatorPosition.TOP_RIGHT,
                OnlineIndicatorPosition.BOTTOM_RIGHT,
                -> width - (width / 8f)
            }

            val cy: Float = when (avatarStyle.onlineIndicatorPosition) {
                OnlineIndicatorPosition.TOP_LEFT,
                OnlineIndicatorPosition.TOP_RIGHT,
                -> height / 8f
                OnlineIndicatorPosition.BOTTOM_LEFT,
                OnlineIndicatorPosition.BOTTOM_RIGHT,
                -> height - height / 8f
            }
            canvas.drawCircle(cx, cy, width / 8f, onlineIndicatorOutlinePaint)
            canvas.drawCircle(cx, cy, width / 10f, onlineIndicatorPaint)
        }
    }

    private fun avatarShape(style: AvatarStyle): StreamImageLoader.ImageTransformation {
        return when (style.avatarShape) {
            AvatarShape.CIRCLE -> Circle
            AvatarShape.SQUARE -> RoundedCorners(style.borderRadius)
        }
    }

    private fun drawBorder(canvas: Canvas, avatarStyle: AvatarStyle, borderPaint: Paint) {
        if (avatarStyle.avatarBorderWidth != 0) {
            if (avatarStyle.avatarShape == AvatarShape.SQUARE) {
                val dpOffset = SQUARE_BORDER_OFFSET.dpToPx().toFloat()

                canvas.drawRoundRect(
                    dpOffset,
                    dpOffset,
                    width.toFloat() - dpOffset,
                    height.toFloat() - dpOffset,
                    avatarStyle.borderRadius,
                    avatarStyle.borderRadius,
                    borderPaint
                )
            } else {
                canvas.drawCircle(
                    width / 2f,
                    height / 2f,
                    width / 2f - avatarStyle.avatarBorderWidth / 2,
                    borderPaint
                )
            }
        }
    }

    /**
     * Used to set the position of the indicator on the avatar.
     */
    public enum class OnlineIndicatorPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    /**
     * Used to set the shape of the avatar.
     */
    public enum class AvatarShape(public val value: Int) {
        CIRCLE(0), SQUARE(1)
    }

    internal companion object {
        /**
         * A small extra added to the avatar size to prevent anti-aliasing issues.
         */
        internal const val AVATAR_SIZE_EXTRA = 1

        internal const val MAX_AVATAR_SECTIONS = 4

        private const val SQUARE_BORDER_OFFSET = 1
    }
}
