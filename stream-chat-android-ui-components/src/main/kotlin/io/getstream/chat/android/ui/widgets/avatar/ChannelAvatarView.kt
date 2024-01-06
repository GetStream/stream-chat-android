/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.AbsoluteCornerSize
import com.google.android.material.shape.RelativeCornerSize
import com.google.android.material.shape.ShapeAppearanceModel
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.utils.extensions.initials
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.load
import io.getstream.chat.android.ui.widgets.avatar.internal.AvatarPlaceholderDrawable

/**
 * Represents the [Channel] avatar that's shown when browsing channels or when you open the Messages screen.
 *
 * Based on the state of the [Channel] and the number of members, it shows different types of images.
 */
public class ChannelAvatarView : ViewGroup {

    /**
     * Style for the avatar.
     */
    private lateinit var avatarStyle: AvatarStyle

    /**
     * [Paint] that will be used to draw the border.
     */
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    /**
     * Custom renderer for the channel avatar.
     */
    public var customAvatarRenderer: ChannelAvatarRenderer? = null

    public constructor(context: Context) : this(context, null)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        this.avatarStyle = AvatarStyle(context, attrs)

        val padding = (avatarStyle.avatarBorderWidth - AVATAR_SIZE_EXTRA).coerceAtLeast(0)
        setPadding(padding, padding, padding, padding)

        borderPaint.color = avatarStyle.avatarBorderColor
        borderPaint.strokeWidth = avatarStyle.avatarBorderWidth.toFloat()

        setWillNotDraw(false)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!isUserAvatar()) {
            drawBorder(canvas)
        }
    }

    /**
     * If the current channel avatar boils down to [UserAvatarView].
     */
    private fun isUserAvatar(): Boolean {
        return childCount == 1 && getChildAt(0) is UserAvatarView
    }

    /**
     * Sets the [Channel] for which we want to show the avatar.
     *
     * @param channel The channel to show the avatar for.
     * @param currentUser The currently logged in user.
     */
    @JvmOverloads
    public fun setChannel(
        channel: Channel,
        currentUser: User? = ChatUI.currentUserProvider.getCurrentUser(),
    ) {
        val members = channel.members
        val memberCount = members.size

        when {
            /**
             * If the channel has a custom avatar renderer we use that.
             */
            customAvatarRenderer != null -> {
                val target = createImageViews(1).first()
                customAvatarRenderer?.render(channel, currentUser, target)
            }
            /**
             * If the channel has an image we load that as a priority.
             */
            channel.image.isNotEmpty() -> {
                showChannelAvatar(channel)
            }
            /**
             * If the channel has one member we show the member's image or initials.
             */
            memberCount == 1 -> {
                val user = channel.members.first().user

                showUserAvatar(user)
            }
            /**
             * If the channel has two members and one of them is the current user - we show the other
             * member's image or initials.
             */
            memberCount == 2 && members.any { it.user.id == currentUser?.id } -> {
                val user = members.first { it.user.id != currentUser?.id }.user

                showUserAvatar(user)
            }
            /**
             * If the channel has more than two members - we load a matrix of their images or initials.
             */
            else -> {
                val users = members.filter { it.user.id != currentUser?.id }.map { it.user }

                showGroupAvatar(users)
            }
        }
    }

    /**
     * Shows either a channel image or a text placeholder.
     *
     * @param channel The channel to show the avatar for.
     */
    private fun showChannelAvatar(channel: Channel) {
        createImageViews(1).first().load(
            channel.image,
            placeholderDrawable = AvatarPlaceholderDrawable(
                context = context,
                initials = channel.initials,
                initialsTextStyle = avatarStyle.avatarInitialsTextStyle,
            ),
        )
    }

    /**
     * Show user avatar.
     */
    private fun showUserAvatar(user: User) {
        removeAllViews()

        UserAvatarView(context, avatarStyle).apply {
            addView(this)
            setUser(user)
        }
    }

    /**
     * Shows group avatar.
     */
    private fun showGroupAvatar(user: List<User>) {
        val size = 4.coerceAtMost(user.size)
        val imageViews: List<ImageView> = createImageViews(size)

        for (i in 0 until 4.coerceAtMost(user.size)) {
            imageViews[i].load(
                user[i].image,
                placeholderDrawable = AvatarPlaceholderDrawable(
                    context,
                    user[i].initials,
                    avatarStyle.groupAvatarInitialsTextStyle,
                ),
            )
        }
    }

    /**
     * Creates necessary amount of [ImageView]s to render the avatar.
     */
    private fun createImageViews(count: Int): List<ShapeableImageView> {
        removeAllViews()

        val imageViews: MutableList<ShapeableImageView> = ArrayList(count)
        for (i in 0 until count) {
            ShapeableImageView(context).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP
                addView(this)
                imageViews.add(this)
            }
        }

        when (avatarStyle.avatarShape) {
            AvatarShape.CIRCLE -> {
                when (count) {
                    1 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(RelativeCornerSize(0.5f))
                            .build()
                    }
                    2 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopLeftCornerSize(RelativeCornerSize(0.5f))
                            .setBottomLeftCornerSize(RelativeCornerSize(0.5f))
                            .build()

                        imageViews[1].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopRightCornerSize(RelativeCornerSize(0.5f))
                            .setBottomRightCornerSize(RelativeCornerSize(0.5f))
                            .build()
                    }
                    3 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopLeftCornerSize(RelativeCornerSize(1f))
                            .build()
                        imageViews[1].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopRightCornerSize(RelativeCornerSize(0.5f))
                            .setBottomRightCornerSize(RelativeCornerSize(0.5f))
                            .build()
                        imageViews[2].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setBottomLeftCornerSize(RelativeCornerSize(1f))
                            .build()
                    }
                    4 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setTopLeftCornerSize(RelativeCornerSize(1f))
                            .build()
                        imageViews[1].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setTopRightCornerSize(RelativeCornerSize(1f))
                            .build()
                        imageViews[2].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setBottomLeftCornerSize(RelativeCornerSize(1f))
                            .build()
                        imageViews[3].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setBottomRightCornerSize(RelativeCornerSize(1f))
                            .build()
                    }
                }
            }
            AvatarShape.ROUND_RECT -> {
                when (count) {
                    1 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setAllCornerSizes(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                    }
                    2 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopLeftCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .setBottomLeftCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                        imageViews[1].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopRightCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .setBottomRightCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                    }
                    3 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel()
                            .toBuilder()
                            .setTopLeftCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                        imageViews[1].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setTopRightCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .setBottomRightCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                        imageViews[2].shapeAppearanceModel = ShapeAppearanceModel().toBuilder()
                            .setBottomLeftCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                    }
                    4 -> {
                        imageViews[0].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setTopLeftCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                        imageViews[1].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setTopRightCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                        imageViews[2].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setBottomLeftCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                        imageViews[3].shapeAppearanceModel = ShapeAppearanceModel.builder()
                            .setBottomRightCornerSize(AbsoluteCornerSize(avatarStyle.borderRadius))
                            .build()
                    }
                }
            }
        }
        return imageViews
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        val halfWidth = width / 2
        val halfHeight = height / 2
        when (childCount) {
            1 -> getChildAt(0).measureExactly(width, height)
            2 -> {
                getChildAt(0).measureExactly(halfWidth, height)
                getChildAt(1).measureExactly(halfWidth, height)
            }
            3 -> {
                getChildAt(0).measureExactly(halfWidth, halfHeight)
                getChildAt(1).measureExactly(halfWidth, height)
                getChildAt(2).measureExactly(halfWidth, halfHeight)
            }
            4 -> {
                getChildAt(0).measureExactly(halfWidth, halfHeight)
                getChildAt(1).measureExactly(halfWidth, halfHeight)
                getChildAt(2).measureExactly(halfWidth, halfHeight)
                getChildAt(3).measureExactly(halfWidth, halfHeight)
            }
        }

        setMeasuredDimension(width, height)
    }

    private fun View.measureExactly(width: Int, height: Int) {
        measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY),
        )
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val halfWidth = measuredWidth / 2
        val halfHeight = measuredHeight / 2
        when (childCount) {
            1 -> getChildAt(0).layout(0, 0, measuredWidth, measuredHeight)
            2 -> {
                getChildAt(0).layout(0, 0, halfWidth, measuredHeight)
                getChildAt(1).layout(measuredWidth - halfWidth, 0, measuredWidth, measuredHeight)
            }
            3 -> {
                getChildAt(0).layout(0, 0, halfWidth, halfHeight)
                getChildAt(1).layout(halfWidth, 0, measuredWidth, measuredHeight)
                getChildAt(2).layout(0, halfHeight, halfWidth, measuredHeight)
            }
            4 -> {
                getChildAt(0).layout(0, 0, halfWidth, halfHeight)
                getChildAt(1).layout(measuredWidth - halfWidth, 0, measuredWidth, halfHeight)
                getChildAt(2).layout(0, halfHeight, halfWidth, measuredHeight)
                getChildAt(3).layout(halfWidth, halfHeight, measuredWidth, measuredHeight)
            }
        }
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

    internal companion object {
        /**
         * A small extra added to the avatar size to prevent anti-aliasing issues.
         */
        internal const val AVATAR_SIZE_EXTRA = 1
    }
}

/**
 * Custom renderer for the channel avatar.
 */
public interface ChannelAvatarRenderer {

    /**
     * Renders the avatar for the given [channel] and [currentUser] into the [target].
     */
    public fun render(
        channel: Channel,
        currentUser: User?,
        target: ShapeableImageView,
    )
}
