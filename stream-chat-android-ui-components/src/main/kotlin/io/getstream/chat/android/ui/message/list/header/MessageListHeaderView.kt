package io.getstream.chat.android.ui.message.list.header

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.common.extensions.internal.setTextSizePx
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiMessageListHeaderViewBinding

public class MessageListHeaderView : FrameLayout {

    private val binding: StreamUiMessageListHeaderViewBinding =
        StreamUiMessageListHeaderViewBinding.inflate(context.inflater, this, true)

    private var headerState: HeaderState = createInitialHeaderState(context)

    public constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    public fun setAvatar(channel: Channel) {
        binding.avatarView.setChannelData(channel)
    }

    public fun setAvatarClickListener(listener: OnClickListener) {
        binding.avatarView.setOnClickListener { listener.onClick() }
    }

    public fun hideAvatar() {
        binding.avatarView.isVisible = false
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
            threadModeSubtitle = context.getString(R.string.stream_ui_subtitle_thread, title)
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
        binding.offlineRetryButton.setOnClickListener { listener.onClick() }
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
        context.obtainStyledAttributes(attrs, R.styleable.MessageListHeaderView).use {
            configUserAvatar(it)
            configTitle(it)
            configBackButton(it)
            configOfflineLabel(it)
            configSearchingForNetworkLabel(it)
            configOnlineLabel(it)
        }
    }

    private fun configUserAvatar(attrs: TypedArray) {
        val showAvatar =
            attrs.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowUserAvatar, true)
        binding.avatarView.apply {
            isInvisible = !showAvatar
            isClickable = showAvatar
        }
    }

    private fun configTitle(attrs: TypedArray) {
        getTitleTextStyle(attrs).apply(binding.titleTextView)
    }

    private fun getTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray)
            .size(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextSize,
                context.getDimension(R.dimen.stream_ui_text_large)
            )
            .color(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_primary)
            )
            .font(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleFontAssets,
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextFont
            )
            .style(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun configBackButton(attrs: TypedArray) {
        binding.backButtonContainer.apply {
            val showBackButton =
                attrs.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowBackButton, true)
            isInvisible = !showBackButton
            isClickable = showBackButton
        }

        val backIcon = attrs.getDrawable(R.styleable.MessageListHeaderView_streamUiMessageListHeaderBackButtonIcon)
            ?: context.getDrawableCompat(R.drawable.stream_ui_arrow_left)

        binding.backButton.setImageDrawable(backIcon)

        binding.backButtonBadge.apply {
            isVisible =
                attrs.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowBackButtonBadge, false)
            val color = attrs.getColor(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderBackButtonBadgeBackgroundColor,
                context.getColorCompat(R.color.stream_ui_accent_red)
            )
            ContextCompat.getDrawable(context, R.drawable.stream_ui_badge_bg)?.let {
                it.setTint(color)
                background = it
            }
        }
    }

    private fun configOfflineLabel(attrs: TypedArray) {
        val textStyle = getOfflineTextStyle(attrs)
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

    private fun getOfflineTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray)
            .size(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextSize,
                context.getDimension(R.dimen.stream_ui_text_small)
            )
            .color(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_secondary)
            )
            .font(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelFontAssets,
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextFont
            )
            .style(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderOfflineLabelTextStyle,
                Typeface.NORMAL
            )
            .build()
    }

    private fun configSearchingForNetworkLabel(attrs: TypedArray) {
        getSearchingForNetworkTextStyle(attrs).apply(binding.connectingTextView)

        binding.connectingProgressBar.apply {
            isVisible = attrs.getBoolean(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowSearchingForNetworkProgressBar,
                true
            )
            indeterminateTintList = getProgressbarTintList(attrs)
        }
    }

    private fun getSearchingForNetworkTextStyle(attrs: TypedArray): TextStyle {
        return TextStyle.Builder(attrs)
            .size(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelTextSize,
                context.getDimension(R.dimen.stream_ui_text_small)
            )
            .color(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelColor,
                context.getColorCompat(R.color.stream_ui_text_color_secondary)
            )
            .font(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelFontAssets,
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelTextFont
            )
            .style(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkLabelTextStyle,
                Typeface.NORMAL
            )
            .build()
    }

    private fun getProgressbarTintList(attrs: TypedArray): ColorStateList? {
        return attrs.getColorStateList(
            R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkProgressBarTint
        ) ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)
    }

    private fun configOnlineLabel(attrs: TypedArray) {
        getOnlineTextStyle(attrs).apply(binding.onlineTextView)
    }

    private fun getOnlineTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray)
            .size(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextSize,
                context.getDimension(R.dimen.stream_ui_text_small)
            )
            .color(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_secondary)
            )
            .font(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelFontAssets,
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextFont
            )
            .style(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderDefaultLabelTextStyle,
                Typeface.NORMAL
            )
            .build()
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
            onlineState = onlineState
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
        OFFLINE
    }

    public fun interface OnClickListener {
        public fun onClick()
    }

    private companion object {
        private fun createInitialHeaderState(context: Context): HeaderState {
            return HeaderState(
                isThread = false,
                isTitleEnabled = true,
                normalModeTitle = String.EMPTY,
                threadModeTitle = context.getString(R.string.stream_ui_title_thread_reply),
                isSubtitleEnabled = true,
                normalModeSubtitle = String.EMPTY,
                threadModeSubtitle = String.EMPTY,
                typingUsers = emptyList(),
                onlineState = OnlineState.NONE
            )
        }
    }
}
