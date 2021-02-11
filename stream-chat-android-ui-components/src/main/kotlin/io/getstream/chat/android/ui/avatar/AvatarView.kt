package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.getstream.sdk.chat.images.StreamImageLoader.ImageTransformation.Circle
import com.getstream.sdk.chat.images.load
import com.getstream.sdk.chat.utils.extensions.getUsers
import com.getstream.sdk.chat.utils.extensions.isDistinctChannel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.avatar.internal.Avatar
import io.getstream.chat.android.ui.avatar.internal.AvatarStyle

public class AvatarView : AppCompatImageView {
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val onlineIndicatorOutlinePaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }
    private val onlineIndicatorPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.GREEN
    }

    private lateinit var avatarStyle: AvatarStyle
    private var isOnline: Boolean = false
    private var avatarViewSize: Int = 0

    public constructor(context: Context) : super(context) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    public fun setChannelData(channel: Channel) {
        val otherUsers = channel.getUsers()
        if (channel.isDistinctChannel() && otherUsers.size == 1) {
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
        val padding = this.avatarStyle.avatarBorderWidth - 1
        setPadding(padding, padding, padding, padding)
    }

    private fun drawOnlineStatus(canvas: Canvas) {
        if (isOnline && avatarStyle.onlineIndicatorEnabled) {
            val cx = width - (width / 8f)
            val cy: Float = when (avatarStyle.onlineIndicatorPosition) {
                OnlineIndicatorPosition.TOP -> height / 8f
                OnlineIndicatorPosition.BOTTOM -> height - height / 8f
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

    internal enum class OnlineIndicatorPosition {
        TOP,
        BOTTOM
    }

    internal companion object {
        const val MAX_AVATAR_SECTIONS = 4
    }
}
