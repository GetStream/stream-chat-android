package io.getstream.chat.android.ui.channel.list.header

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiChannelListHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.setTextSizePx

public class ChannelListHeaderView : ConstraintLayout {

    public constructor(context: Context) : super(context) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context, attrs)
    }

    private val binding = StreamUiChannelListHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    private fun init(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChannelListHeaderView).use { typedArray ->
            configUserAvatar(typedArray)
            configOnlineTitle(typedArray)
            configOfflineTitleContainer(typedArray)
            configAddChannelButton(typedArray)
        }
    }

    private fun configUserAvatar(typedArray: TypedArray) {
        val showAvatar = typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowUserAvatar, true)
        binding.userAvatar.apply {
            isVisible = showAvatar
            isClickable = showAvatar
        }
    }

    private fun configOnlineTitle(typedArray: TypedArray) {
        val textStyle = getOnlineTitleTextStyle(typedArray)
        binding.onlineTextView.apply {
            text = typedArray.getString(R.styleable.ChannelListHeaderView_streamUiOnlineTitleText)
                ?: context.getString(R.string.stream_ui_channels_header_view_online_title)
            setTextSizePx(textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun configOfflineTitleContainer(typedArray: TypedArray) {
        val textStyle = getOfflineTitleTextStyle(typedArray)
        binding.offlineTextView.apply {
            text = typedArray.getString(R.styleable.ChannelListHeaderView_streamUiOfflineTitleText)
                ?: context.getString(R.string.stream_ui_channels_header_view_offline_title)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }

        binding.offlineProgressBar.apply {
            isVisible =
                typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowOfflineProgressBar, true)
            indeterminateTintList = getProgressBarTint(typedArray)
        }
    }

    private fun getProgressBarTint(typedArray: TypedArray) =
        typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiOfflineProgressBarTint)
            ?: ContextCompat.getColorStateList(context, R.color.stream_ui_blue)

    private fun configAddChannelButton(typedArray: TypedArray) {
        binding.addChannelButton.apply {
            val showAddChannelButton = typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowAddChannelButton, true)

            isVisible = showAddChannelButton
            isClickable = showAddChannelButton

            layoutParams = layoutParams.apply {
                height = typedArray.getDimensionPixelSize(
                    R.styleable.ChannelListHeaderView_streamUiAddChannelButtonHeight,
                    DEFAULT_ADD_CHANNEL_BUTTON_SIZE
                )
                width = typedArray.getDimensionPixelSize(
                    R.styleable.ChannelListHeaderView_streamUiAddChannelButtonWidth,
                    DEFAULT_ADD_CHANNEL_BUTTON_SIZE
                )
            }
            iconTint = typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiAddChannelButtonTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_blue)
            icon = typedArray.getDrawable(R.styleable.ChannelListHeaderView_streamUiAddChannelButtonIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_pen)
            backgroundTintList = getBackgroundTint(typedArray)
        }
    }

    private fun getBackgroundTint(typedArray: TypedArray) =
        typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiAddChannelBackgroundTint)
            ?: ContextCompat.getColorStateList(context, R.color.stream_ui_white)

    private fun getOnlineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large)
        )
            .color(
                R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_black)
            )
            .font(
                R.styleable.ChannelListHeaderView_streamUiOnlineTitleFontAssets,
                R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextFont
            )
            .style(
                R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun getOfflineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large)
        )
            .color(
                R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_black)
            )
            .font(
                R.styleable.ChannelListHeaderView_streamUiOfflineTitleFontAssets,
                R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextFont
            )
            .style(
                R.styleable.ChannelListHeaderView_streamUiOfflineTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    public fun setUser(user: User) {
        binding.userAvatar.setUserData(user)
    }

    public fun showOnlineTitle() {
        binding.offlineTitleContainer.isVisible = false
        binding.onlineTextView.isVisible = true
    }

    public fun showOfflineTitle() {
        binding.offlineTitleContainer.isVisible = true
        binding.onlineTextView.isVisible = false
    }

    public fun hideTitle() {
        binding.offlineTitleContainer.isVisible = false
        binding.onlineTextView.isVisible = false
    }

    public fun setOnUserAvatarClickListener(listener: UserAvatarClickListener) {
        binding.userAvatar.setOnClickListener { listener.onUserAvatarClick() }
    }

    public fun setOnAddChannelButtonClickListener(listener: AddChannelButtonClickListener) {
        binding.addChannelButton.setOnClickListener { listener.onAddChannelClick() }
    }

    public fun interface UserAvatarClickListener {
        public fun onUserAvatarClick()
    }

    public fun interface AddChannelButtonClickListener {
        public fun onAddChannelClick()
    }

    private companion object {
        private val DEFAULT_ADD_CHANNEL_BUTTON_SIZE = 40.dpToPx()
    }
}
