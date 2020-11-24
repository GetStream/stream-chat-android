package io.getstream.chat.android.ui.channel.list.header

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
import io.getstream.chat.android.ui.databinding.StreamChannelListHeaderViewBinding
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getDimension

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

    private val binding = StreamChannelListHeaderViewBinding.inflate(LayoutInflater.from(context), this, true)

    private fun init(context: Context, attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.ChannelListHeaderView).use { typedArray ->
            configUserAvatar(typedArray)
            configOnlineTitle(typedArray)
            configOfflineTitleContainer(typedArray)
            configAddChannelButton(typedArray)
        }
    }

    private fun configUserAvatar(typedArray: TypedArray) {
        binding.userAvatar.apply {
            if (typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamShowUserAvatar, true)) {
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
            text = typedArray.getString(R.styleable.ChannelListHeaderView_streamOnlineTitleText)
                ?: context.getString(R.string.stream_channels_header_view_online_title)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }
    }

    private fun configOfflineTitleContainer(typedArray: TypedArray) {
        val textStyle = getOfflineTitleTextStyle(typedArray)
        binding.offlineTextView.apply {
            text = typedArray.getString(R.styleable.ChannelListHeaderView_streamOfflineTitleText)
                ?: context.getString(R.string.stream_channels_header_view_offline_title)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textStyle.size.toFloat())
            setTextColor(textStyle.color)
            typeface = textStyle.font
        }

        binding.offlineProgressBar.apply {
            isVisible = typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamShowOfflineProgressBar, true)
            indeterminateTintList =
                typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamOfflineProgressBarTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_blue)
        }
    }

    private fun configAddChannelButton(typedArray: TypedArray) {
        binding.addChannelButton.apply {
            if (typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamShowAddChannelButton, true)) {
                visibility = View.VISIBLE
                isClickable = true
            } else {
                visibility = View.INVISIBLE
                isClickable = false
            }
            layoutParams = layoutParams.apply {
                height = typedArray.getDimensionPixelSize(
                    R.styleable.ChannelListHeaderView_streamAddChannelButtonHeight,
                    DEFAULT_ADD_CHANNEL_BUTTON_SIZE
                )
                width = typedArray.getDimensionPixelSize(
                    R.styleable.ChannelListHeaderView_streamAddChannelButtonWidth,
                    DEFAULT_ADD_CHANNEL_BUTTON_SIZE
                )
            }
            iconTint = typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamAddChannelButtonTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_blue)
            icon = typedArray.getDrawable(R.styleable.ChannelListHeaderView_streamAddChannelButtonIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ic_pen)
            backgroundTintList =
                typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamAddChannelBackgroundTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_white)
        }
    }

    private fun getOnlineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelListHeaderView_streamOnlineTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.ChannelListHeaderView_streamOnlineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.ChannelListHeaderView_streamOnlineTitleFontAssets,
                R.styleable.ChannelListHeaderView_streamOnlineTitleTextFont
            )
            .style(
                R.styleable.ChannelListHeaderView_streamOnlineTitleTextStyle,
                Typeface.BOLD
            ).build()
    }

    private fun getOfflineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelListHeaderView_streamOfflineTitleTextSize,
            context.getDimension(R.dimen.stream_text_large)
        )
            .color(
                R.styleable.ChannelListHeaderView_streamOfflineTitleTextColor,
                ContextCompat.getColor(context, R.color.stream_black)
            )
            .font(
                R.styleable.ChannelListHeaderView_streamOfflineTitleFontAssets,
                R.styleable.ChannelListHeaderView_streamOfflineTitleTextFont
            )
            .style(
                R.styleable.ChannelListHeaderView_streamOfflineTitleTextStyle,
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
