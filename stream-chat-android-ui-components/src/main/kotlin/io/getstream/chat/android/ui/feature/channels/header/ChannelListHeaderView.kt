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

package io.getstream.chat.android.ui.feature.channels.header

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiChannelListHeaderViewBinding
import io.getstream.chat.android.ui.font.TextStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * A component that shows the title of the channels list, the current connection status,
 * the avatar of the current user, and provides an action button which can be used to create a new conversation.
 * It is designed to be displayed at the top of the channels screen of your app.
 */
public class ChannelListHeaderView : ConstraintLayout {

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
        defStyleRes,
    ) {
        init(attrs)
    }

    private val binding = StreamUiChannelListHeaderViewBinding.inflate(streamThemeInflater, this, true)

    private fun init(attrs: AttributeSet?) {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ChannelListHeaderView,
            R.attr.streamUiChannelListHeaderStyle,
            R.style.StreamUi_ChannelListHeader,
        ).use { typedArray ->
            configUserAvatar(typedArray)
            configOnlineTitle(typedArray)
            configOfflineTitleContainer(typedArray)
            configActionButton(typedArray)
            configureSeparator(typedArray)
        }
    }

    private fun configUserAvatar(typedArray: TypedArray) {
        val showAvatar = typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowUserAvatar, true)
        binding.userAvatarView.apply {
            isInvisible = !showAvatar
            isClickable = showAvatar
        }
    }

    private fun configOnlineTitle(typedArray: TypedArray) {
        binding.onlineTextView.setTextStyle(getOnlineTitleTextStyle(typedArray))
    }

    private fun configOfflineTitleContainer(typedArray: TypedArray) {
        binding.offlineTextView.setTextStyle(getOfflineTitleTextStyle(typedArray))

        binding.offlineProgressBar.apply {
            isVisible =
                typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowOfflineProgressBar, true)
            indeterminateTintList = getProgressBarTint(typedArray)
        }
    }

    private fun getProgressBarTint(typedArray: TypedArray) = typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiOfflineProgressBarTint)
        ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)

    private fun configActionButton(typedArray: TypedArray) {
        binding.actionButton.apply {
            val showActionButton =
                typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowActionButton, true)

            isInvisible = !showActionButton
            isClickable = showActionButton

            val drawable = typedArray.getDrawable(R.styleable.ChannelListHeaderView_streamUiActionButtonIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_pen)
            setImageDrawable(drawable)
            backgroundTintList =
                typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiActionBackgroundTint)
                    ?: ContextCompat.getColorStateList(context, R.color.stream_ui_icon_button_background_selector)
        }
    }

    /**
     * Uses the [typedArray] to customize the separator View's background drawable.
     *
     * @param typedArray The attribute array the user passed to the XML component.
     */
    private fun configureSeparator(typedArray: TypedArray) {
        binding.separator.apply {
            val drawable =
                typedArray.getDrawable(R.styleable.ChannelListHeaderView_streamUiChannelListSeparatorBackgroundDrawable)
            visibility = if (drawable != null) VISIBLE else GONE
            background = drawable
        }
    }

    private fun getOnlineTitleTextStyle(typedArray: TypedArray): TextStyle = TextStyle.Builder(typedArray)
        .size(
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large),
        )
        .color(
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextColor,
            context.getColorCompat(R.color.stream_ui_text_color_primary),
        )
        .font(
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleFontAssets,
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextFont,
        )
        .style(
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextStyle,
            Typeface.BOLD,
        ).build()

    private fun getOfflineTitleTextStyle(typedArray: TypedArray): TextStyle = TextStyle.Builder(typedArray)
        .size(
            R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large),
        )
        .color(
            R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextColor,
            context.getColorCompat(R.color.stream_ui_text_color_primary),
        )
        .font(
            R.styleable.ChannelListHeaderView_streamUiOfflineTitleFontAssets,
            R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextFont,
        )
        .style(
            R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextStyle,
            Typeface.BOLD,
        ).build()

    /**
     * Sets [User] to bind user information with the avatar in the header.
     *
     * @param user A user that will represent the avatar in the header.
     */
    public fun setUser(user: User) {
        binding.userAvatarView.setUser(user)
    }

    /**
     * Sets the title that is shown on the header when the client's network state is online.
     *
     * @param title A title that indicates the online network state.
     */
    public fun setOnlineTitle(title: String) {
        binding.onlineTextView.text = title
    }

    /**
     * Shows the title that indicates the network state is online.
     */
    public fun showOnlineTitle() {
        binding.offlineTitleContainer.isVisible = false
        binding.onlineTextView.isVisible = true
    }

    /**
     * Sets a click listener for the title in the header.
     */
    public fun setOnTitleClickListener(listener: () -> Unit) {
        binding.offlineTextView.setOnClickListener { listener() }
        binding.onlineTextView.setOnClickListener { listener() }
    }

    /**
     * Sets a long click listener for the title in the header.
     */
    public fun setOnTitleLongClickListener(listener: () -> Unit) {
        binding.offlineTextView.setOnLongClickListener {
            listener()
            true
        }
        binding.onlineTextView.setOnLongClickListener {
            listener()
            true
        }
    }

    /**
     * Shows the title that indicates the network state is offline.
     */
    public fun showOfflineTitle() {
        binding.offlineTitleContainer.isVisible = true
        binding.offlineProgressBar.isVisible = false
        binding.onlineTextView.isVisible = false

        binding.offlineTextView.text = resources.getString(R.string.stream_ui_channel_list_header_offline)
    }

    /**
     * Shows the title that indicates the network state is connecting.
     */
    public fun showConnectingTitle() {
        binding.offlineTitleContainer.isVisible = true
        binding.offlineProgressBar.isVisible = true
        binding.onlineTextView.isVisible = false

        binding.offlineTextView.text = resources.getString(R.string.stream_ui_channel_list_header_disconnected)
    }

    /**
     * Sets a click listener for the left button in the header represented by the avatar of
     * the current user.
     */
    public fun setOnUserAvatarClickListener(listener: UserAvatarClickListener) {
        binding.userAvatarView.setOnClickListener { listener.onUserAvatarClick() }
    }

    /**
     * Sets a click listener for the right button in the header.
     */
    public fun setOnActionButtonClickListener(listener: ActionButtonClickListener) {
        binding.actionButton.setOnClickListener { listener.onClick() }
    }

    @InternalStreamChatApi
    public fun setOnUserAvatarLongClickListener(listener: () -> Unit) {
        binding.userAvatarView.setOnLongClickListener {
            listener()
            true
        }
    }

    /**
     * Click listener for the left button in the header represented by the avatar of
     * the current user.
     */
    public fun interface UserAvatarClickListener {
        public fun onUserAvatarClick()
    }

    /**
     * Click listener for the right button in the header.
     */
    public fun interface ActionButtonClickListener {
        public fun onClick()
    }
}
