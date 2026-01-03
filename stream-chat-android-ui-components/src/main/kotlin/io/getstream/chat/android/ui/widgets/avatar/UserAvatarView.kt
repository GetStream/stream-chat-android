/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.widgets.avatar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.initials
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.isRtlLayout
import io.getstream.chat.android.ui.widgets.avatar.internal.AvatarPlaceholderDrawable

/**
 * Represents the [User] avatar that's shown on the Messages screen or in headers of DMs.
 *
 * Based on the state within the [User], we either show an image or their initials.
 */
public class UserAvatarView : AvatarImageView {

    /**
     * Style for the avatar.
     */
    private lateinit var avatarStyle: AvatarStyle

    /**
     * [Paint] that will be used to draw the indicator outline.
     */
    private val onlineIndicatorOutlinePaint = Paint().apply {
        style = Paint.Style.FILL
    }

    /**
     * [Paint] that will be used to draw the indicator.
     */
    private val onlineIndicatorPaint = Paint().apply {
        style = Paint.Style.FILL
    }

    /**
     * [Paint] that will be used to draw the border.
     */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /**
     * If the user is online and we should display the indicator.
     */
    private var online: Boolean = false

    private var avatarRenderer: UserAvatarRenderer? = null

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    public constructor(context: Context, avatarStyle: AvatarStyle) : super(
        context.createStreamThemeWrapper(),
    ) {
        setAvatarStyle(avatarStyle)
    }

    /**
     * Sets the [User] we want to show the avatar for.
     *
     * @param user The user to display the avatar for.
     * @param online If the user is online.
     */
    @JvmOverloads
    public fun setUser(user: User, online: Boolean = user.online) {
        avatarRenderer?.render(avatarStyle, user, this) ?: setAvatar(
            avatar = user.image.applyStreamCdnImageResizingIfEnabled(ChatUI.streamCdnImageResizing),
            placeholder = AvatarPlaceholderDrawable(
                context,
                user.initials,
                avatarStyle.avatarInitialsTextStyle,
            ),
        )
        setOnline(online)
    }

    /**
     * Sets the [online] status for the user.
     *
     * @param online If the user is online.
     */
    public fun setOnline(online: Boolean) {
        this.online = online
        invalidate()
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        setAvatarStyle(AvatarStyle(context, attrs))
        avatarRenderer = ChatUI.userAvatarRenderer
    }

    private fun setAvatarStyle(avatarStyle: AvatarStyle) {
        this.avatarStyle = avatarStyle

        val padding = (avatarStyle.avatarBorderWidth - AVATAR_SIZE_EXTRA).coerceAtLeast(0)
        setPadding(padding, padding, padding, padding)

        onlineIndicatorOutlinePaint.color = avatarStyle.onlineIndicatorBorderColor
        onlineIndicatorPaint.color = avatarStyle.onlineIndicatorColor
        borderPaint.color = avatarStyle.avatarBorderColor
        borderPaint.strokeWidth = avatarStyle.avatarBorderWidth.toFloat()

        shapeAppearanceModel = when (avatarStyle.avatarShape) {
            AvatarShape.CIRCLE -> {
                ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCornerSizes(RelativeCornerSize(0.5f))
                    .build()
            }
            AvatarShape.ROUND_RECT -> {
                ShapeAppearanceModel()
                    .toBuilder()
                    .setAllCornerSizes(AbsoluteCornerSize(avatarStyle.borderRadius))
                    .build()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize(0, widthMeasureSpec)
        val height = resolveSize(0, heightMeasureSpec)
        val avatarViewSize = if (width > height) height else width

        setMeasuredDimension(avatarViewSize, avatarViewSize)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawBorder(canvas)
        drawIndicator(canvas)
    }

    /**
     * Draws the avatar online indicator on the canvas.
     *
     * @param canvas The [Canvas] to draw on.
     */
    private fun drawIndicator(canvas: Canvas) {
        if (!online || !avatarStyle.onlineIndicatorEnabled) return

        val isRtl = context.isRtlLayout
        val width = width
        val height = height
        val position = avatarStyle.onlineIndicatorPosition

        val cx: Float = when (position) {
            OnlineIndicatorPosition.TOP_START,
            OnlineIndicatorPosition.BOTTOM_START,
            -> if (isRtl) {
                width - (width / 8f)
            } else {
                width / 8f
            }
            OnlineIndicatorPosition.TOP_END,
            OnlineIndicatorPosition.BOTTOM_END,
            -> if (isRtl) {
                width / 8f
            } else {
                width - (width / 8f)
            }
        }

        val cy: Float = when (position) {
            OnlineIndicatorPosition.TOP_START,
            OnlineIndicatorPosition.TOP_END,
            -> height / 8f
            OnlineIndicatorPosition.BOTTOM_START,
            OnlineIndicatorPosition.BOTTOM_END,
            -> height - height / 8f
        }
        canvas.drawCircle(cx, cy, width / 8f, onlineIndicatorOutlinePaint)
        canvas.drawCircle(cx, cy, width / 10f, onlineIndicatorPaint)
    }

    /**
     * Draws the avatar border on the canvas.
     *
     * @param canvas The [Canvas] to draw on.
     */
    private fun drawBorder(canvas: Canvas) {
        if (avatarStyle.avatarBorderWidth == 0) return

        val borderOffset = (avatarStyle.avatarBorderWidth / 2).toFloat()
        when (avatarStyle.avatarShape) {
            AvatarShape.ROUND_RECT -> {
                canvas.drawRoundRect(
                    borderOffset,
                    borderOffset,
                    width.toFloat() - borderOffset,
                    height.toFloat() - borderOffset,
                    avatarStyle.borderRadius,
                    avatarStyle.borderRadius,
                    borderPaint,
                )
            }
            else -> {
                canvas.drawCircle(
                    width / 2f,
                    height / 2f,
                    width / 2f - borderOffset,
                    borderPaint,
                )
            }
        }
    }

    private companion object {
        private const val AVATAR_SIZE_EXTRA = 1
    }
}

/**
 * Custom renderer for the user avatar.
 */
public fun interface UserAvatarRenderer {
    /**
     * Renders the avatar for the given [user] into the [target].
     */
    public fun render(
        style: AvatarStyle,
        user: User,
        target: UserAvatarView,
    )
}
