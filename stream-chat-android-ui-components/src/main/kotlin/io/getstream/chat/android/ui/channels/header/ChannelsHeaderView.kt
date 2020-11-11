package io.getstream.chat.android.ui.channels.header

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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamViewChannelsHeaderBinding
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getDimension

public class ChannelsHeaderView : ConstraintLayout {

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

    private lateinit var binding: StreamViewChannelsHeaderBinding

    private fun init(context: Context, attrs: AttributeSet?) {
        binding = StreamViewChannelsHeaderBinding.inflate(LayoutInflater.from(context), this, true)

        context.obtainStyledAttributes(attrs, R.styleable.ChannelsHeaderView).use { typedArray ->
            configUserAvatar(typedArray)
            configOnlineTitle(typedArray)
            configOfflineTitleContainer(typedArray)
            configAddChannelButton(typedArray)
        }
    }

    private fun configUserAvatar(typedArray: TypedArray) {
        binding.userAvatar.apply {
            if (typedArray.getBoolean(R.styleable.ChannelsHeaderView_streamShowUserAvatar, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
        }
    }

    private fun configOnlineTitle(typedArray: TypedArray) {
        val textStyle = getOnlineTitleTextStyle(typedArray)
        binding.onlineTextView.apply {
            text = typedArray.getString(R.styleable.ChannelsHeaderView_streamOnlineTitleText)
                ?: context.getString(R.string.stream_channels_header_view_online_title)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun configOfflineTitleContainer(typedArray: TypedArray) {
        val textStyle = getOfflineTitleTextStyle(typedArray)
        binding.offlineTextView.apply {
            text = typedArray.getString(R.styleable.ChannelsHeaderView_streamOfflineTitleText)
                ?: context.getString(R.string.stream_channels_header_view_offline_title)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }

        binding.offlineProgressBar.apply {
            isVisible = typedArray.getBoolean(R.styleable.ChannelsHeaderView_streamShowOfflineProgressBar, true)
            indeterminateTintList =
                typedArray.getColorStateList(R.styleable.ChannelsHeaderView_streamOfflineProgressBarTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_blue)
        }
    }

    private fun configAddChannelButton(typedArray: TypedArray) {
        binding.addChannelButton.apply {
            if (typedArray.getBoolean(R.styleable.ChannelsHeaderView_streamShowAddChannelButton, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
            layoutParams = layoutParams.apply {
                height = typedArray.getDimensionPixelSize(
                    R.styleable.ChannelsHeaderView_streamAddChannelButtonHeight,
                    DEFAULT_ADD_CHANNEL_BUTTON_SIZE
                )
                width = typedArray.getDimensionPixelSize(
                    R.styleable.ChannelsHeaderView_streamAddChannelButtonWidth,
                    DEFAULT_ADD_CHANNEL_BUTTON_SIZE
                )
            }
            iconTint = typedArray.getColorStateList(R.styleable.ChannelsHeaderView_streamAddChannelButtonTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_blue)
            icon = typedArray.getDrawable(R.styleable.ChannelsHeaderView_streamAddChannelButtonIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_pen)
            backgroundTintList =
                typedArray.getColorStateList(R.styleable.ChannelsHeaderView_streamAddChannelBackgroundTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_white)
        }
    }

    private fun getOnlineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelsHeaderView_streamOnlineTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.ChannelsHeaderView_streamOnlineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.ChannelsHeaderView_streamOnlineTitleFontAssets,
                R.styleable.ChannelsHeaderView_streamOnlineTitleTextFont
            )
            .style(
                R.styleable.ChannelsHeaderView_streamOnlineTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun getOfflineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelsHeaderView_streamOfflineTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.ChannelsHeaderView_streamOfflineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.ChannelsHeaderView_streamOfflineTitleFontAssets,
                R.styleable.ChannelsHeaderView_streamOfflineTitleTextFont
            )
            .style(
                R.styleable.ChannelsHeaderView_streamOfflineTitleTextStyle,
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
