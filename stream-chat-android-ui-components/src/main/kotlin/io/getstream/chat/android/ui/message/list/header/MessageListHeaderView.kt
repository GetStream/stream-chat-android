package io.getstream.chat.android.ui.message.list.header

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.forEach
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.EMPTY
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.setTextSizePx
import io.getstream.chat.android.ui.databinding.StreamUiMessageListHeaderViewBinding

public class MessageListHeaderView : ConstraintLayout {

    private val binding: StreamUiMessageListHeaderViewBinding =
        StreamUiMessageListHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    private var subtitleState: SubtitleState = SubtitleState(emptyList(), OnlineState.NONE)

    private var normalModeTitle: String? = null
    private var normalModeSubTitle: String? = null

    private var threadMode = false

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
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

    public fun setAvatar(channel: Channel) {
        binding.avatar.setChannelData(channel)
    }

    public fun setTitle(title: String?) {
        binding.title.text = title ?: String.EMPTY
        binding.title.isVisible = true

        if (!threadMode) {
            normalModeTitle = title
        }
    }

    public fun setOnlineStateSubtitle(subtitle: String) {
        binding.onlineLabel.text = subtitle

        if (!threadMode) {
            normalModeSubTitle = subtitle
        }
    }

    public fun setThreadSubtitle(subtitle: String) {
        binding.onlineLabel.text = subtitle
    }

    public fun setBackButtonClickListener(listener: OnClickListener) {
        binding.backButtonContainer.setOnClickListener { listener.onClick() }
    }

    public fun setAvatarClickListener(listener: OnClickListener) {
        binding.avatar.setOnClickListener { listener.onClick() }
    }

    public fun setRetryClickListener(listener: OnClickListener) {
        binding.offlineRetryButton.setOnClickListener { listener.onClick() }
    }

    public fun setTitleClickListener(listener: OnClickListener) {
        binding.title.setOnClickListener { listener.onClick() }
    }

    public fun setSubtitleClickListener(listener: OnClickListener) {
        binding.subtitleContainer.setOnClickListener { listener.onClick() }
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

    public fun hideSubtitle() {
        reduceSubtitleState(typingUsers = emptyList(), onlineState = OnlineState.NONE)
    }

    public fun showOnlineStateSubtitle() {
        reduceSubtitleState(onlineState = OnlineState.ONLINE)
    }

    public fun showSearchingForNetworkLabel() {
        reduceSubtitleState(onlineState = OnlineState.SEARCHING_FOR_NETWORK)
    }

    public fun showOfflineStateLabel() {
        reduceSubtitleState(onlineState = OnlineState.OFFLINE)
    }

    public fun showTypingStateLabel(typingUsers: List<User>) {
        reduceSubtitleState(typingUsers = typingUsers)
    }

    public fun hideTitle() {
        binding.title.isVisible = false
    }

    public fun hideAvatar() {
        binding.avatar.isVisible = false
    }

    public fun setNormalMode() {
        binding.title.text = normalModeTitle
        binding.onlineLabel.text = normalModeSubTitle
    }

    public fun setThreadMode() {
        val title = context.getString(R.string.stream_ui_title_thread_reply)
        val subTitleComplement = normalModeTitle

        binding.title.text = title
        binding.onlineLabel.text =
            String.format(context.getString(R.string.stream_ui_subtitle_thread), subTitleComplement)
    }

    private fun configSearchingForNetworkLabel(attrs: TypedArray) {
        val textStyle = getSearchingForNetworkTextStyle(attrs)
        binding.searchingForNetworkText.apply {
            setTextSizePx(textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }

        binding.searchingForNetworkProgressbar.apply {
            isVisible =
                attrs.getBoolean(
                    R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowSearchingForNetworkProgressBar,
                    true
                )
            indeterminateTintList = getProgressbarTintList(attrs)
        }
    }

    private fun reduceSubtitleState(
        typingUsers: List<User> = subtitleState.typingUsers,
        onlineState: OnlineState = subtitleState.onlineState,
    ) {
        subtitleState = subtitleState.copy(
            typingUsers = typingUsers,
            onlineState = onlineState
        )
        renderSubtitleState()
    }

    private fun renderSubtitleState() {
        with(binding) {
            subtitleContainer.forEach { it.isVisible = false }
            if (subtitleState.typingUsers.isNotEmpty()) {
                typingContainer.isVisible = true
                typingView.setTypingUsers(subtitleState.typingUsers)
            } else {
                when (subtitleState.onlineState) {
                    OnlineState.ONLINE -> onlineContainer.isVisible = true
                    OnlineState.SEARCHING_FOR_NETWORK -> searchingForNetworkContainer.isVisible = true
                    OnlineState.OFFLINE -> offlineContainer.isVisible = true
                }
            }
        }
    }

    private fun getProgressbarTintList(attrs: TypedArray): ColorStateList? {
        return (
            attrs.getColorStateList(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderSearchingForNetworkProgressBarTint
            )
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)
            )
    }

    private fun getSearchingForNetworkTextStyle(attrs: TypedArray): TextStyle {
        return TextStyle.Builder(attrs).size(
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
                Typeface.BOLD
            )
            .build()
    }

    private fun configOfflineLabel(attrs: TypedArray) {
        val textStyle = getOfflineTextStyle(attrs)
        binding.offlineText.apply {
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
        return TextStyle.Builder(typedArray).size(
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

    private fun configOnlineLabel(attrs: TypedArray) {
        val textStyle = getOnlineTextStyle(attrs)
        binding.onlineLabel.apply {
            text = ""
            setTextSizePx(textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun getOnlineTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
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

    private fun configBackButton(attrs: TypedArray) {
        binding.backButtonContainer.apply {
            val showBackButton =
                attrs.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowBackButton, true)
            isVisible = showBackButton
            isClickable = showBackButton
        }
        binding.backButtonBadge.apply {
            isVisible =
                attrs.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowBackButtonBadge, false)
            val defaultColor = ContextCompat.getColor(context, R.color.stream_ui_accent_red)
            val color = attrs.getColor(
                R.styleable.MessageListHeaderView_streamUiMessageListHeaderBackButtonBadgeBackgroundColor,
                defaultColor
            )
            ContextCompat.getDrawable(context, R.drawable.stream_ui_badge_bg)?.let {
                it.setTint(color)
                background = it
            }
        }
    }

    private fun configTitle(attrs: TypedArray) {
        getTitleTextStyle(attrs).apply(binding.title)
    }

    private fun getTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
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

    private fun configUserAvatar(attrs: TypedArray) {
        val showAvatar =
            attrs.getBoolean(R.styleable.MessageListHeaderView_streamUiMessageListHeaderShowUserAvatar, true)
        binding.avatar.apply {
            isInvisible = !showAvatar
            isClickable = showAvatar
        }
    }

    private data class SubtitleState(
        val typingUsers: List<User>,
        val onlineState: OnlineState,
    )

    private enum class OnlineState {
        NONE,
        ONLINE,
        SEARCHING_FOR_NETWORK,
        OFFLINE
    }

    public fun interface OnClickListener {
        public fun onClick(): Unit
    }
}
