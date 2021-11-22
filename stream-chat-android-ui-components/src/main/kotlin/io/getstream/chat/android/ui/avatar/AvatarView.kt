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

public class AvatarView : AppCompatImageView {
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE }
    private val onlineIndicatorOutlinePaint = Paint().apply { style = Paint.Style.FILL }
    private val onlineIndicatorPaint = Paint().apply { style = Paint.Style.FILL }
    private val backgroundPaint = Paint().apply { style = Paint.Style.FILL }
    private val textPaint by lazy {
        Paint().apply {
            textSize = avatarStyle.avatarInitialText.size.toFloat()
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            color = avatarStyle.avatarInitialText.color
        }
    }

    private lateinit var avatarStyle: AvatarStyle
    private var isOnline: Boolean = false
    private var avatarViewSize: Int = 0
    private var avatarInitials: String = "XX"

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
        val otherUsers = channel.getUsersExcludingCurrent()
        if (channel.isAnonymousChannel() && otherUsers.size == 1) {
            setUserData(otherUsers.first())
        } else {
            when (avatarStyle.avatarType) {
                AvatarType.PICTURE -> {
                    load(
                        data = Avatar.ChannelAvatar(channel, avatarStyle),
                        transformation = avatarShape(avatarStyle),
                    )
                }
                AvatarType.COLOR -> {
                    avatarInitials = parseInitials(channel.name)
                }
            }

            isOnline = false
        }
    }

    public fun setUserData(user: User) {
        when (avatarStyle.avatarType) {
            AvatarType.PICTURE -> {
                load(
                    data = Avatar.UserAvatar(user, avatarStyle),
                    transformation = avatarShape(avatarStyle),
                )
            }
            AvatarType.COLOR -> {
                avatarInitials = parseInitials(user.name)
            }
        }

        isOnline = user.online
    }

    private fun parseInitials(text: String): String {
        val textList = text.split(" ")

        return when {
            textList.size > 1 -> "${textList[0][0]}${textList[1][0]}"

            textList[0].length > 1 -> "${textList[0][0]}${textList[0][1]}"

            textList[0].isNotEmpty() -> "${textList[0][0]}${textList[0][0]}"

            else -> "XX"

        }.uppercase()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(0, widthMeasureSpec)
        val height = resolveSize(0, heightMeasureSpec)
        avatarViewSize = if (width > height) height else width

        setMeasuredDimension(avatarViewSize, avatarViewSize)
    }

    override fun onDraw(canvas: Canvas) {
        when (avatarStyle.avatarType) {
            AvatarType.PICTURE -> {
                if (drawable != null) {
                    super.onDraw(canvas)
                    drawBorder(canvas)
                    drawOnlineStatus(canvas)
                }
            }
            AvatarType.COLOR -> {
                drawColor(canvas, avatarStyle)
                drawInitials(canvas, avatarInitials)
            }
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
        backgroundPaint.color = avatarStyle.avatarColor
    }

    private fun drawOnlineStatus(canvas: Canvas) {
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

    private fun drawInitials(canvas: Canvas, initials: String) {
        canvas.drawText(
            initials,
            width.toFloat() / 2,
            (canvas.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2),
            textPaint
        )
    }

    private fun drawColor(canvas: Canvas, avatarStyle: AvatarStyle) {
        if (avatarStyle.avatarShape == AvatarShape.SQUARE) {
            canvas.drawRoundRect(
                0F,
                0F,
                width.toFloat(),
                height.toFloat(),
                avatarStyle.borderRadius,
                avatarStyle.borderRadius,
                backgroundPaint
            )
        } else {
            canvas.drawCircle(
                width / 2f,
                height / 2f,
                width / 2f,
                backgroundPaint
            )
        }
    }

    private fun drawBorder(canvas: Canvas) {
        if (avatarStyle.avatarBorderWidth != 0) {
            if (avatarStyle.avatarShape == AvatarShape.SQUARE) {
                canvas.drawRoundRect(
                    BORDER_OFFSET,
                    BORDER_OFFSET,
                    width.toFloat() - BORDER_OFFSET,
                    height.toFloat() - BORDER_OFFSET,
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

    public enum class OnlineIndicatorPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public enum class AvatarShape(public val value: Int) {
        CIRCLE(0), SQUARE(1)
    }

    public enum class AvatarType(public val value: Int) {
        PICTURE(0), COLOR(1)
    }

    internal companion object {
        /**
         * A small extra added to the avatar size to prevent anti-aliasing issues.
         */
        internal const val AVATAR_SIZE_EXTRA = 1

        internal const val MAX_AVATAR_SECTIONS = 4

        private const val BORDER_OFFSET = 4F
    }
}
