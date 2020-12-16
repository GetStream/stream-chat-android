package io.getstream.chat.android.ui.messages.header

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessagesHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx

public class MessagesHeaderView : ConstraintLayout {
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

    public constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        init(attrs)
    }

    private val binding: StreamUiMessagesHeaderViewBinding =
        StreamUiMessagesHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        configColors()

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

    public fun setTitle(title: String) {
        binding.title.text = title
        binding.title.isVisible = true
    }

    public fun setOnlineStateSubtitle(subtitle: String) {
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
        binding.onlineContainer.isVisible = false
        binding.offlineContainer.isVisible = false
        binding.searchingForNetworkContainer.isVisible = false
        binding.typingContainer.isVisible = false
    }

    public fun showOnlineStateSubtitle() {
        hideSubtitle()
        binding.onlineContainer.isVisible = true
    }

    public fun showSearchingForNetworkLabel() {
        hideSubtitle()
        binding.searchingForNetworkContainer.isVisible = true
    }

    public fun showOfflineStateLabel() {
        hideSubtitle()
        binding.offlineContainer.isVisible = true
    }

    public fun showTypingStateLabel(typingUsers: List<User>) {
        hideSubtitle()
        binding.typingContainer.isVisible = true
        binding.typingView.setTypingUsers(typingUsers)
    }

    public fun hideTitle() {
        binding.title.isVisible = false
    }

    public fun hideAvatar() {
        binding.avatar.isVisible = false
    }

    private fun configColors() {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                binding.headerRoot.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.stream_ui_dark_background
                    )
                )

                binding.backButton.setColorFilter(ContextCompat.getColor(context, R.color.stream_ui_white))
            }
            else -> {
                binding.headerRoot.setBackgroundColor(ContextCompat.getColor(context, R.color.stream_ui_white))
                binding.backButton.setColorFilter(ContextCompat.getColor(context, R.color.stream_ui_black))
            }
        }
    }

    private fun configSearchingForNetworkLabel(attrs: TypedArray) {
        val textStyle = getSearchingForNetworkTextStyle(attrs)
        binding.searchingForNetworkText.apply {
            text = attrs.getString(R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelText)
                ?: context.getString(R.string.stream_ui_message_list_header_searching_for_network)
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

    private fun getProgressbarTintList(attrs: TypedArray): ColorStateList? {
        return (
            attrs.getColorStateList(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkProgressBarTint
            )
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_blue)
            )
    }

    private fun getSearchingForNetworkTextStyle(attrs: TypedArray): TextStyle {
        return TextStyle.Builder(attrs).size(
            R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelTextSize,
            context.getDimension(R.dimen.stream_ui_text_small)
        )
            .color(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderSearchingForNetworkLabelColor,
                ContextCompat.getColor(context, getTextColor(R.color.stream_ui_text_color_black_translucent))
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

    private fun getTextColor(lightColour: Int): Int {
        return when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_NO -> lightColour
            Configuration.UI_MODE_NIGHT_YES -> R.color.stream_ui_white
            else -> lightColour
        }
    }

    private fun configOfflineLabel(attrs: TypedArray) {
        val textStyle = getOfflineTextStyle(attrs)
        binding.offlineText.apply {
            text = attrs.getString(R.styleable.MessagesHeaderView_streamUiMessagesHeaderOfflineLabelText)
                ?: context.getString(R.string.stream_ui_message_list_header_offline)
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
                ContextCompat.getColor(context, getTextColor(R.color.stream_ui_text_color_black_translucent))
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
            text = attrs.getString(R.styleable.MessagesHeaderView_streamUiMessagesHeaderDefaultLabelText) ?: ""
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
                ContextCompat.getColor(context, getTextColor(R.color.stream_ui_text_color_black_translucent))
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
            if (attrs.getBoolean(R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowBackButton, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
        }
        binding.backButtonBadge.apply {
            isVisible =
                attrs.getBoolean(R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowBackButtonBadge, false)
            val defaultColor = ContextCompat.getColor(context, R.color.stream_ui_light_red)
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
        val textStyle = getTitleTextStyle(attrs)
        binding.title.apply {
            setTextSizePx(textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun getTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large)
        )
            .color(
                R.styleable.MessagesHeaderView_streamUiMessagesHeaderTitleTextColor,
                ContextCompat.getColor(context, getTextColor(R.color.stream_ui_black))
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
        binding.avatar.apply {
            if (attrs.getBoolean(R.styleable.MessagesHeaderView_streamUiMessagesHeaderShowUserAvatar, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
        }
    }

    public fun interface OnClickListener {
        public fun onClick(): Unit
    }
}
