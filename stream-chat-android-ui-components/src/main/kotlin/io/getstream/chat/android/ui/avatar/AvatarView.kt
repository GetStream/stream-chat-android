package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.Circle
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.client.extensions.isAnonymousChannel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.avatar.internal.Avatar
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper

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

    public fun setChannelData(channel: Channel) {
        val otherUsers = channel.getUsers()
        if (channel.isAnonymousChannel() && otherUsers.size == 1) {
            setUserData(otherUsers.first())
        } else {
            load(
                data = Avatar.ChannelAvatar(channel, avatarStyle),
                transformation = Circle,
            )
            isOnline = false
        }
    }

    public fun setUserData(user: User) {
        load(
            data = Avatar.UserAvatar(user, avatarStyle),
            transformation = Circle,
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
            drawBorder(canvas)
            drawOnlineStatus(canvas)
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

    private fun drawOnlineStatus(canvas: Canvas) {
        if (isOnline && avatarStyle.onlineIndicatorEnabled) {
            val cx: Float = when (avatarStyle.onlineIndicatorPosition) {
                OnlineIndicatorPosition.TOP_LEFT,
                OnlineIndicatorPosition.BOTTOM_LEFT,
                -> width / 8f
                OnlineIndicatorPosition.TOP,
                OnlineIndicatorPosition.BOTTOM,
                OnlineIndicatorPosition.TOP_RIGHT,
                OnlineIndicatorPosition.BOTTOM_RIGHT,
                -> width - (width / 8f)
            }

            val cy: Float = when (avatarStyle.onlineIndicatorPosition) {
                OnlineIndicatorPosition.TOP_LEFT,
                OnlineIndicatorPosition.TOP_RIGHT,
                OnlineIndicatorPosition.TOP,
                -> height / 8f
                OnlineIndicatorPosition.BOTTOM_LEFT,
                OnlineIndicatorPosition.BOTTOM_RIGHT,
                OnlineIndicatorPosition.BOTTOM,
                -> height - height / 8f
            }
            canvas.drawCircle(cx, cy, width / 8f, onlineIndicatorOutlinePaint)
            canvas.drawCircle(cx, cy, width / 10f, onlineIndicatorPaint)
        }
    }

    private fun drawBorder(canvas: Canvas) {
        if (avatarStyle.avatarBorderWidth != 0) {
            canvas.drawCircle(
                width / 2f,
                height / 2f,
                width / 2f - avatarStyle.avatarBorderWidth / 2,
                borderPaint
            )
        }
    }

    public enum class OnlineIndicatorPosition {
        @Deprecated(
            message = "Use OnlineIndicatorPosition.TOP_RIGHT instead",
            level = DeprecationLevel.WARNING,
        )
        TOP,

        @Deprecated(
            message = "Use OnlineIndicatorPosition.BOTTOM_RIGHT instead",
            level = DeprecationLevel.WARNING,
        )
        BOTTOM,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    internal companion object {
        /**
         * A small extra added to the avatar size to prevent anti-aliasing issues
         */
        internal const val AVATAR_SIZE_EXTRA = 1

        internal const val MAX_AVATAR_SECTIONS = 4
    }
}
