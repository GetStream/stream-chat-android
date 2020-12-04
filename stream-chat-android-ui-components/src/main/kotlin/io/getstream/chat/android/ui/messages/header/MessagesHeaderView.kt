package io.getstream.chat.android.ui.messages.header

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
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
import io.getstream.chat.android.ui.databinding.StreamMessagesHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.getDimension

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

    private val binding: StreamMessagesHeaderViewBinding =
        StreamMessagesHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.StreamMessagesHeaderView).use {
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

    private fun configSearchingForNetworkLabel(attrs: TypedArray) {
        val textStyle = getSearchingForNetworkTextStyle(attrs)
        binding.searchingForNetworkText.apply {
            text = attrs.getString(R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelText)
                ?: context.getString(R.string.stream_message_list_header_searching_for_network)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }

        binding.searchingForNetworkProgressbar.apply {
            isVisible =
                attrs.getBoolean(
                    R.styleable.StreamMessagesHeaderView_streamMessagesHeaderShowSearchingForNetworkProgressBar,
                    true
                )
            indeterminateTintList = getProgressbarTintList(attrs)
        }
    }

    private fun getProgressbarTintList(attrs: TypedArray): ColorStateList? {
        return (
            attrs.getColorStateList(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderSearchingForNetworkProgressBarTint
            )
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_blue)
            )
    }

    private fun getSearchingForNetworkTextStyle(attrs: TypedArray): TextStyle {
        return TextStyle.Builder(attrs).size(
            R.styleable.StreamMessagesHeaderView_streamMessagesHeaderSearchingForNetworkLabelTextSize,
            context.getDimension(R.dimen.stream_text_small)
        )
            .color(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderSearchingForNetworkLabelColor,
                ContextCompat.getColor(context, R.color.stream_ui_text_color_black_translucent)
            )
            .font(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderSearchingForNetworkLabelFontAssets,
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderSearchingForNetworkLabelTextFont
            )
            .style(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderSearchingForNetworkLabelTextStyle,
                Typeface.BOLD
            )
            .build()
    }

    private fun configOfflineLabel(attrs: TypedArray) {
        val textStyle = getOfflineTextStyle(attrs)
        binding.offlineText.apply {
            text = attrs.getString(R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelText)
                ?: context.getString(R.string.stream_message_list_header_offline)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
        binding.offlineRetryButton.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            typeface = textStyle.font
        }
    }

    private fun getOfflineTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelTextSize,
            context.getDimension(R.dimen.stream_text_small)
        )
            .color(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_text_color_black_translucent)
            )
            .font(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelFontAssets,
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelTextFont
            )
            .style(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderOfflineLabelTextStyle,
                Typeface.NORMAL
            )
            .build()
    }

    private fun configOnlineLabel(attrs: TypedArray) {
        val textStyle = getOnlineTextStyle(attrs)
        binding.onlineLabel.apply {
            text = attrs.getString(R.styleable.StreamMessagesHeaderView_streamMessagesHeaderDefaultLabelText) ?: ""
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun getOnlineTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.StreamMessagesHeaderView_streamMessagesHeaderDefaultLabelTextSize,
            context.getDimension(R.dimen.stream_text_small)
        )
            .color(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderDefaultLabelTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_text_color_black_translucent)
            )
            .font(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderDefaultLabelFontAssets,
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderDefaultLabelTextFont
            )
            .style(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderDefaultLabelTextStyle,
                Typeface.NORMAL
            )
            .build()
    }

    private fun configBackButton(attrs: TypedArray) {
        binding.backButtonContainer.apply {
            if (attrs.getBoolean(R.styleable.StreamMessagesHeaderView_streamMessagesHeaderShowBackButton, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
        }
        binding.backButtonBadge.apply {
            isVisible =
                attrs.getBoolean(R.styleable.StreamMessagesHeaderView_streamMessagesHeaderShowBackButtonBadge, false)
            val defaultColor = ContextCompat.getColor(context, R.color.stream_ui_light_red)
            val color = attrs.getColor(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderBackButtonBadgeBackgroundColor,
                defaultColor
            )
            ContextCompat.getDrawable(context, R.drawable.stream_badge_bg)?.let {
                it.setTint(color)
                background = it
            }
        }
    }

    private fun configTitle(attrs: TypedArray) {
        val textStyle = getTitleTextStyle(attrs)
        binding.title.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun getTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.StreamMessagesHeaderView_streamMessagesHeaderTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_black)
            )
            .font(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderTitleFontAssets,
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderTitleTextFont
            )
            .style(
                R.styleable.StreamMessagesHeaderView_streamMessagesHeaderTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun configUserAvatar(attrs: TypedArray) {
        binding.avatar.apply {
            if (attrs.getBoolean(R.styleable.StreamMessagesHeaderView_streamMessagesHeaderShowUserAvatar, true)) {
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
