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

package io.getstream.chat.android.ui.feature.messages.header

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageListHeaderViewBinding
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

public class MessageListHeaderView : FrameLayout {

    private val binding = StreamUiMessageListHeaderViewBinding.inflate(streamThemeInflater, this, true)

    private var headerState: HeaderState = createInitialHeaderState(context)

    private lateinit var style: MessageListHeaderViewStyle

    public constructor(context: Context) : this(context, null, 0)

    public constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    ) {
        init(attrs)
    }

    public fun setAvatar(channel: Channel) {
        binding.channelAvatarView.setChannel(channel)
    }

    public fun setAvatarClickListener(listener: OnClickListener) {
        binding.channelAvatarView.setOnClickListener { listener.onClick() }
    }

    public fun hideAvatar() {
        binding.channelAvatarView.isInvisible = true
    }

    public fun showBackButtonBadge(text: String) {
        binding.backButtonBadge.apply {
            this.isVisible = true
            this.text = text
        }
    }

    public fun hideBackButtonBadge() {
        binding.backButtonBadge.isVisible = false
    }

    public fun setBackButtonClickListener(listener: OnClickListener) {
        binding.backButtonContainer.setOnClickListener { listener.onClick() }
    }

    public fun setNormalMode() {
        reduceHeaderState(isThread = false)
    }

    public fun setThreadMode() {
        reduceHeaderState(isThread = true)
    }

    public fun setTitle(title: String) {
        reduceHeaderState(
            isTitleEnabled = true,
            normalModeTitle = title,
            threadModeSubtitle = context.getString(R.string.stream_ui_message_list_header_thread_subtitle, title),
        )
    }

    public fun hideTitle() {
        reduceHeaderState(isTitleEnabled = false)
    }

    public fun setTitleClickListener(listener: OnClickListener) {
        binding.titleTextView.setOnClickListener { listener.onClick() }
    }

    public fun showOnlineStateSubtitle() {
        reduceHeaderState(onlineState = OnlineState.ONLINE)
    }

    public fun showSearchingForNetworkLabel() {
        reduceHeaderState(onlineState = OnlineState.CONNECTING)
    }

    public fun showOfflineStateLabel() {
        reduceHeaderState(onlineState = OnlineState.OFFLINE)
    }

    public fun setRetryClickListener(listener: OnClickListener) {
        binding.offlineRetryButton.apply {
            isVisible = true
            setOnClickListener { listener.onClick() }
        }
    }

    public fun showTypingStateLabel(typingUsers: List<User>) {
        reduceHeaderState(typingUsers = typingUsers)
    }

    public fun setOnlineStateSubtitle(subtitle: String) {
        reduceHeaderState(normalModeSubtitle = subtitle)
    }

    public fun getOnlineStateSubtitle(): String = binding.onlineTextView.text.toString()

    public fun setThreadSubtitle(subtitle: String) {
        reduceHeaderState(threadModeSubtitle = subtitle)
    }

    public fun hideSubtitle() {
        reduceHeaderState(isSubtitleEnabled = false)
    }

    public fun setSubtitleClickListener(listener: OnClickListener) {
        binding.subtitleContainer.setOnClickListener { listener.onClick() }
    }

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        style = MessageListHeaderViewStyle(context, attrs)

        configBackground()
        configChannelAvatar()
        configTitle()
        configBackButton()
        configOfflineLabel()
        configSearchingForNetworkLabel()
        configOnlineLabel()
    }

    /**
     * Uses the [style] to change the separator View's background visibility and drawable.
     */
    private fun configBackground() {
        setBackgroundColor(style.background)
        binding.separator.visibility = if (style.separatorBackgroundDrawable != null) VISIBLE else GONE
        binding.separator.background = style.separatorBackgroundDrawable
    }

    private fun configChannelAvatar() {
        binding.channelAvatarView.apply {
            isInvisible = !style.showUserAvatar
            isClickable = style.showUserAvatar
        }
    }

    private fun configTitle() {
        binding.titleTextView.setTextStyle(style.titleTextStyle)
    }

    private fun configBackButton() {
        binding.backButtonContainer.apply {
            isInvisible = !style.showBackButton
            isClickable = style.showBackButton
        }

        binding.backButton.setImageDrawable(style.backButtonIcon)

        binding.backButtonBadge.apply {
            isVisible = style.showBackButtonBadge

            ContextCompat.getDrawable(context, R.drawable.stream_ui_badge_bg)?.let {
                it.setTint(style.backButtonBadgeBackgroundColor)
                background = it
            }
        }
    }

    private fun configOfflineLabel() {
        val textStyle = style.offlineTextStyle
        binding.offlineTextView.apply {
            setTextSizePx(textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
        binding.offlineRetryButton.apply {
            setTextSizePx(textStyle.size.toFloat())
            typeface = textStyle.font
        }
    }

    private fun configSearchingForNetworkLabel() {
        binding.connectingTextView.setTextStyle(style.searchingForNetworkTextStyle)

        binding.connectingProgressBar.apply {
            isVisible = style.showSearchingForNetworkProgressBar
            indeterminateTintList = style.searchingForNetworkProgressBarTint
        }
    }

    private fun configOnlineLabel() {
        binding.onlineTextView.setTextStyle(style.onlineTextStyle)
    }

    private fun reduceHeaderState(
        isThread: Boolean = headerState.isThread,
        isTitleEnabled: Boolean = headerState.isTitleEnabled,
        normalModeTitle: String = headerState.normalModeTitle,
        threadModeTitle: String = headerState.threadModeTitle,
        isSubtitleEnabled: Boolean = headerState.isSubtitleEnabled,
        normalModeSubtitle: String = headerState.normalModeSubtitle,
        threadModeSubtitle: String = headerState.threadModeSubtitle,
        typingUsers: List<User> = headerState.typingUsers,
        onlineState: OnlineState = headerState.onlineState,
    ) {
        headerState = headerState.copy(
            isThread = isThread,
            isTitleEnabled = isTitleEnabled,
            normalModeTitle = normalModeTitle,
            threadModeTitle = threadModeTitle,
            isSubtitleEnabled = isSubtitleEnabled,
            normalModeSubtitle = normalModeSubtitle,
            threadModeSubtitle = threadModeSubtitle,
            typingUsers = typingUsers,
            onlineState = onlineState,
        )
        renderHeaderState()
    }

    private fun renderHeaderState() {
        renderTitleState()
        renderSubtitleState()
    }

    private fun renderTitleState() {
        with(binding) {
            if (headerState.isTitleEnabled) {
                val titleText = if (headerState.isThread) {
                    headerState.threadModeTitle
                } else {
                    headerState.normalModeTitle
                }
                titleTextView.text = titleText
                titleTextView.isVisible = titleText.isNotEmpty()
            } else {
                titleTextView.isVisible = false
            }
        }
    }

    private fun renderSubtitleState() {
        with(binding) {
            if (headerState.onlineState == OnlineState.CONNECTING && connectingContainer.isVisible) {
                // no to restart progress bar animation
                return
            }

            if (!headerState.isSubtitleEnabled) {
                subtitleContainer.isVisible = false
                return
            }

            subtitleContainer.forEach { it.isVisible = false }
            if (headerState.typingUsers.isNotEmpty()) {
                typingIndicatorView.isVisible = true
                typingIndicatorView.setTypingUsers(headerState.typingUsers)
            } else {
                when (headerState.onlineState) {
                    OnlineState.ONLINE -> {
                        val subtitleText = if (headerState.isThread) {
                            headerState.threadModeSubtitle
                        } else {
                            headerState.normalModeSubtitle
                        }
                        onlineTextView.text = subtitleText
                        onlineTextView.isVisible = subtitleText.isNotEmpty()
                    }
                    OnlineState.CONNECTING -> connectingContainer.isVisible = true
                    OnlineState.OFFLINE -> offlineContainer.isVisible = true
                    OnlineState.NONE -> Unit
                }
            }
        }
    }

    private data class HeaderState(
        val isThread: Boolean,
        val isTitleEnabled: Boolean,
        val normalModeTitle: String,
        val threadModeTitle: String,
        val isSubtitleEnabled: Boolean,
        val normalModeSubtitle: String,
        val threadModeSubtitle: String,
        val typingUsers: List<User>,
        val onlineState: OnlineState,
    )

    private enum class OnlineState {
        NONE,
        ONLINE,
        CONNECTING,
        OFFLINE,
    }

    public fun interface OnClickListener {
        public fun onClick()
    }

    private companion object {
        private fun createInitialHeaderState(context: Context): HeaderState = HeaderState(
            isThread = false,
            isTitleEnabled = true,
            normalModeTitle = String.EMPTY,
            threadModeTitle = context.getString(R.string.stream_ui_message_list_header_thread_title),
            isSubtitleEnabled = true,
            normalModeSubtitle = String.EMPTY,
            threadModeSubtitle = String.EMPTY,
            typingUsers = emptyList(),
            onlineState = OnlineState.NONE,
        )
    }
}
