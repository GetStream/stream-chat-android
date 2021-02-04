package io.getstream.chat.android.ui.messages.header

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
import io.getstream.chat.android.ui.databinding.StreamUiMessagesHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.EMPTY
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx

public class MessagesHeaderView : ConstraintLayout {

    private val binding: StreamUiMessagesHeaderViewBinding =
        StreamUiMessagesHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

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
        context.obtainStyledAttributes(attrs, R.styleable.MessagesHeaderView).use {
            configUserAvatar(it)
            configTitle(it)
            configBackButton(it)
            configOfflineLabel(it)
            configSearchingForNetworkLabel(it)
            configOnlineLabel(it)
        }
    }

    @JvmOverloads
    public fun setAvatar(channel: Channel) {
        binding.avatar.setChannelData(channel)
    }

    public fun setAvatar(user: User) {
        binding.avatar.setUserData(user)
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
                    R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowSearchingForNetworkProgressBar,
                    true
                )
            indeterminateTintList = getProgressbarTintList(attrs)
        }
    }

    private fun reduceSubtitleState(
        typingUsers: List<User> = subtitleState.typingUsers,
        onlineState: OnlineState = subtitleState.onlineState
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
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkProgressBarTint
            )
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)
            )
    }

    private fun getSearchingForNetworkTextStyle(attrs: TypedArray): TextStyle {
        return TextStyle.Builder(attrs).size(
            R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelTextSize,
            context.getDimension(R.dimen.stream_ui_text_small)
        )
            .color(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelColor,
                ContextCompat.getColor(context, R.color.stream_ui_grey)
            )
            .font(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelFontAssets,
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelTextFont
            )
            .style(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelTextStyle,
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
            R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelTextSize,
            context.getDimension(R.dimen.stream_ui_text_small)
        )
            .color(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_grey)
            )
            .font(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelFontAssets,
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelTextFont
            )
            .style(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelTextStyle,
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
            R.styleable.MessagesHeaderView_streamUiMessagesHeaderDefaultLabelTextSize,
            context.getDimension(R.dimen.stream_ui_text_small)
        )
            .color(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderDefaultLabelTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_grey)
            )
            .font(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderDefaultLabelFontAssets,
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderDefaultLabelTextFont
            )
            .style(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderDefaultLabelTextStyle,
                Typeface.NORMAL
            )
            .build()
    }

    private fun configBackButton(attrs: TypedArray) {
        binding.backButtonContainer.apply {
            val showBackButton =
                attrs.getBoolean(R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowBackButton, true)
            isVisible = showBackButton
            isClickable = showBackButton
        }
        binding.backButtonBadge.apply {
            isVisible =
                attrs.getBoolean(R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowBackButtonBadge, false)
            val defaultColor = ContextCompat.getColor(context, R.color.stream_ui_accent_red)
            val color = attrs.getColor(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderBackButtonBadgeBackgroundColor,
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
            R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large)
        )
            .color(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_black)
            )
            .font(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleFontAssets,
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleTextFont
            )
            .style(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun configUserAvatar(attrs: TypedArray) {
        val showAvatar =
            attrs.getBoolean(R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowUserAvatar, true)
        binding.avatar.apply {
            isInvisible = !showAvatar
            isClickable = showAvatar
        }
    }

    private data class SubtitleState(
        val typingUsers: List<User>,
        val onlineState: OnlineState
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
