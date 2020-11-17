package io.getstream.chat.android.ui

import android.annotation.SuppressLint
import android.content.Context
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
import io.getstream.chat.android.ui.databinding.StreamMessageListHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.getDimension

public class MessageListHeaderView : ConstraintLayout {
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

    private val binding: StreamMessageListHeaderViewBinding =
        StreamMessageListHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    @SuppressLint("CustomViewStyleable")
    private fun init(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.StreamMessageListHeaderView).use {
            configUserAvatar(it)
            configOnlineTitle(it)
            configBackButton(it)
            configTypingInfoView(it)
            configOfflineInfoView(it)
            configSearchingForNetworkInfoView(it)
        }
    }

    @JvmOverloads
    public fun setupAvatar(channel: Channel, users: List<User> = channel.members.map { it.user }) {
        binding.avatar.setChannelData(channel, users)
    }

    public fun setTitle(title: String) {
        binding.title.text = title
    }

    public fun setOnlineStateSubtitle(subtitle: String) {
        binding.onlineSubtitle.text = subtitle
    }

    public fun setBackButtonClickListener(onClick: () -> Unit) {
        binding.backButton.setOnClickListener { onClick() }
    }

    public fun setAvatarClickListener(onClick: () -> Unit) {
        binding.avatar.setOnClickListener { onClick() }
    }

    public fun setRetryClickListener(onClick: () -> Unit) {
        binding.offlineRetryButton.setOnClickListener { onClick() }
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

    public fun hideTitle() {
        binding.title.isVisible = false
    }

    public fun hideAvatar() {
        binding.avatar.isVisible = false
    }

    private fun configSearchingForNetworkInfoView(attrs: TypedArray) {
        val textStyle = getSearchingForNetworkSubtitleTextStyle(attrs)
        binding.searchingForNetworkText.apply {
            text = attrs.getString(R.styleable.StreamMessageListHeaderView_streamOfflineTitleText)
                ?: context.getString(R.string.stream_message_list_header_searching_for_network)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }

        binding.searchingForNetworkProgressbar.apply {
            isVisible =
                attrs.getBoolean(R.styleable.StreamMessageListHeaderView_streamShowSearchingForNetworkProgressBar, true)
            indeterminateTintList =
                attrs.getColorStateList(R.styleable.StreamMessageListHeaderView_streamSearchingForNetworkProgressBarTint)
                    ?: ContextCompat.getColorStateList(context, R.color.stream_blue)
        }
    }

    private fun getSearchingForNetworkSubtitleTextStyle(attrs: TypedArray): TextStyle {
        return TextStyle.Builder(attrs).size(
            R.styleable.StreamMessageListHeaderView_streamSearchingForNetworkTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.StreamMessageListHeaderView_streamSearchingForNetworkTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.StreamMessageListHeaderView_streamSearchingForNetworkFontAssets,
                R.styleable.StreamMessageListHeaderView_streamSearchingForNetworkTextFont
            )
            .style(
                R.styleable.StreamMessageListHeaderView_streamSearchingForNetworkTextStyle,
                Typeface.BOLD
            )
            .build()
    }

    private fun configOfflineInfoView(attrs: TypedArray) {
        val textStyle = getOfflinesubtitleTextStyle(attrs)
        binding.offlineText.apply {
            text = attrs.getString(R.styleable.ChannelsHeaderView_streamOfflineTitleText)
                ?: context.getString(R.string.stream_channels_header_view_offline_title)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun getOfflinesubtitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.StreamMessageListHeaderView_streamOfflineTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.StreamMessageListHeaderView_streamOfflineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.StreamMessageListHeaderView_streamOfflineTitleFontAssets,
                R.styleable.StreamMessageListHeaderView_streamOfflineTitleTextFont
            )
            .style(
                R.styleable.StreamMessageListHeaderView_streamOfflineTitleTextStyle,
                Typeface.BOLD
            )
            .build()
    }

    private fun configTypingInfoView(attrs: TypedArray) {
        TODO("Not yet implemented")
    }

    private fun configBackButton(attrs: TypedArray) {
        binding.backButton.apply {
            if (attrs.getBoolean(R.styleable.StreamMessageListHeaderView_streamShowBackButton, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
        }
        binding.backButtonBadge.apply {
            isVisible = attrs.getBoolean(R.styleable.StreamMessageListHeaderView_streamShowBackButtonBadge, true)
            val defaultColor = ContextCompat.getColor(context, R.color.stream_light_red)
            setBackgroundColor(
                attrs.getColor(
                    R.styleable.StreamMessageListHeaderView_streamBackButtonBadgeBackgroundColor,
                    defaultColor
                )
            )
        }
    }

    private fun configOnlineTitle(attrs: TypedArray) {
        val textStyle = getOnlineTitleTextStyle(attrs)
        binding.title.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun getOnlineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.StreamMessageListHeaderView_streamOnlineTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.StreamMessageListHeaderView_streamOnlineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.StreamMessageListHeaderView_streamOnlineTitleFontAssets,
                R.styleable.StreamMessageListHeaderView_streamOnlineTitleTextFont
            )
            .style(
                R.styleable.StreamMessageListHeaderView_streamOnlineTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun configUserAvatar(attrs: TypedArray) {
        binding.avatar.apply {
            if (attrs.getBoolean(R.styleable.StreamMessageListHeaderView_streamShowUserAvatar, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
        }
    }
}
