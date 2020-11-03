package io.getstream.chat.android.ui.avatar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

public class AvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val clipPath = Path()
    private val clipRect = RectF()
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private lateinit var avatarStyle: AvatarStyle
    private val bitmapFactory = AvatarBitmapFactory(context)

    private var onlineIndicatorVisible: Boolean = false

    init {
        setStyle(AvatarStyle(context, attrs))
    }

    public fun setChannelData(channel: Channel, user: List<User>) {
        configureImageDrawable {
            AvatarDrawable(bitmapFactory.createChannelBitmaps(channel, user, avatarStyle))
        }
    }

    public fun setUserData(user: User) {
        configureImageDrawable {
            AvatarDrawable(listOfNotNull(bitmapFactory.createUserBitmap(user, avatarStyle)))
        }
    }

    public fun toggleOnlineIndicatorVisibility(visible: Boolean) {
        this.onlineIndicatorVisible = visible
        invalidate()
    }

    public fun setStyle(avatarStyle: AvatarStyle) {
        this.avatarStyle = avatarStyle
        borderPaint.color = avatarStyle.avatarBorderColor
        borderPaint.strokeWidth = avatarStyle.avatarBorderWidth.toFloat()
    }

    private fun configureImageDrawable(generateAvatarDrawable: suspend () -> AvatarDrawable) {
        GlobalScope.launch(Dispatchers.Main) {
            layoutParams?.apply {
                width = avatarStyle.avatarWidth
                height = avatarStyle.avatarHeight
            }?.let(::setLayoutParams)
            setImageDrawable(generateAvatarDrawable())
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (drawable != null) {
            drawBorder(canvas)
            canvas.save()
            clipToCircle(canvas)
            super.onDraw(canvas)
            canvas.restore()
            drawOnlineStatus(canvas)
        }
    }

    private fun drawOnlineStatus(canvas: Canvas) {
        if (onlineIndicatorVisible && avatarStyle.onlineIndicatorEnabled) {
            val outlinePaint = Paint()
            outlinePaint.style = Paint.Style.FILL
            outlinePaint.color = Color.WHITE
            canvas.drawCircle(width - (width / 8f), (height / 8f), width / 8f, outlinePaint)

            val statusPaint = Paint()
            statusPaint.style = Paint.Style.FILL
            statusPaint.color = Color.GREEN
            canvas.drawCircle(width - (width / 8f), height / 8f, width / 10f, statusPaint)
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

    private fun clipToCircle(canvas: Canvas) {
        clipPath.reset()
        clipRect.set(
            avatarStyle.avatarBorderWidth.toFloat(),
            avatarStyle.avatarBorderWidth.toFloat(),
            (width - avatarStyle.avatarBorderWidth).toFloat(),
            (height - avatarStyle.avatarBorderWidth).toFloat()
        )
        clipPath.addOval(clipRect, Path.Direction.CW)
        canvas.clipPath(clipPath)
    }

    internal companion object {
        internal const val MAX_AVATAR_SECTIONS = 4
    }
}
