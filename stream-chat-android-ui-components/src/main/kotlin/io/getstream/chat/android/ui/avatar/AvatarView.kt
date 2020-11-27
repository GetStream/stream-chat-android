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
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

public class AvatarView : AppCompatImageView {
    private val bitmapFactory = AvatarBitmapFactory(context)
    private val clipPath = Path()
    private val clipRect = RectF()
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
    private var onlineIndicatorVisible: Boolean = false

    private var asyncAvatarDrawableProvider: (suspend () -> AvatarDrawable)? = null
    private var avatarViewSize: Int = 0
    private var loadAvatarImageJob: Job? = null

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

    public fun setChannelData(channel: Channel, users: List<User>) {
        asyncAvatarDrawableProvider = {
            AvatarDrawable(bitmapFactory.createChannelBitmaps(channel, users, avatarStyle, avatarViewSize))
        }
        loadAvatarIfLaidOut()
    }

    public fun setUserData(user: User) {
        this.onlineIndicatorVisible = user.online
        asyncAvatarDrawableProvider = {
            AvatarDrawable(listOfNotNull(bitmapFactory.createUserBitmap(user, avatarStyle, avatarViewSize)))
        }
        loadAvatarIfLaidOut()
    }

    public fun showOnlineIndicator(visible: Boolean) {
        this.onlineIndicatorVisible = visible
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(0, widthMeasureSpec)
        val height = resolveSize(0, heightMeasureSpec)
        avatarViewSize = if (width > height) height else width

        setMeasuredDimension(avatarViewSize, avatarViewSize)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        loadAvatarIfLaidOut()
    }

    private fun loadAvatarIfLaidOut() {
        asyncAvatarDrawableProvider.takeIf { it != null && avatarViewSize > 0 }
            ?.let {
                loadAvatarImageJob?.cancel()
                loadAvatarImageJob = GlobalScope.launch(DispatcherProvider.Main) {
                    setImageDrawable(it())
                }
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

    private fun init(context: Context, attrs: AttributeSet?) {
        setStyle(AvatarStyle(context, attrs))
    }

    private fun setStyle(avatarStyle: AvatarStyle) {
        this.avatarStyle = avatarStyle
        borderPaint.color = avatarStyle.avatarBorderColor
        borderPaint.strokeWidth = avatarStyle.avatarBorderWidth.toFloat()
    }

    private fun drawOnlineStatus(canvas: Canvas) {
        if (onlineIndicatorVisible && avatarStyle.onlineIndicatorEnabled) {
            val cx = width - (width / 8f)
            val cy = height / 8f
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
