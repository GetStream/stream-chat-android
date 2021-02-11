package io.getstream.chat.android.ui.channel.list.header

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import com.getstream.sdk.chat.style.TextStyle
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.databinding.StreamUiChannelListHeaderViewBinding

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
            configActionButton(typedArray)
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
        getOnlineTitleTextStyle(typedArray).apply(binding.onlineTextView)
    }

    private fun configOfflineTitleContainer(typedArray: TypedArray) {
        getOfflineTitleTextStyle(typedArray).apply(binding.offlineTextView)

        binding.offlineProgressBar.apply {
            isVisible =
                typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowOfflineProgressBar, true)
            indeterminateTintList = getProgressBarTint(typedArray)
        }
    }

    private fun getProgressBarTint(typedArray: TypedArray) =
        typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiOfflineProgressBarTint)
            ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)

    private fun configActionButton(typedArray: TypedArray) {
        binding.actionButton.apply {
            val showActionButton =
                typedArray.getBoolean(R.styleable.ChannelListHeaderView_streamUiShowActionButton, true)

            isVisible = showActionButton
            isClickable = showActionButton

            imageTintList = typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiActionButtonTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_accent_blue)
            val drawable = typedArray.getDrawable(R.styleable.ChannelListHeaderView_streamUiActionButtonIcon)
                ?: ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_pen)
            setImageDrawable(drawable)
            backgroundTintList =
                typedArray.getColorStateList(R.styleable.ChannelListHeaderView_streamUiActionBackgroundTint)
                ?: ContextCompat.getColorStateList(context, R.color.stream_ui_icon_button_background_selector)
        }
    }

    private fun getOnlineTitleTextStyle(typedArray: TypedArray): TextStyle {
        return TextStyle.Builder(typedArray).size(
            R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextSize,
            context.getDimension(R.dimen.stream_ui_text_large)
        )
            .color(
                R.styleable.ChannelListHeaderView_streamUiOnlineTitleTextColor,
                context.getColorCompat(R.color.stream_ui_text_color_primary)
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
                context.getColorCompat(R.color.stream_ui_text_color_primary)
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

    public fun setOnUserAvatarClickListener(listener: UserAvatarClickListener) {
        binding.userAvatar.setOnClickListener { listener.onUserAvatarClick() }
    }

    public fun setOnActionButtonClickListener(listener: ActionButtonClickListener) {
        binding.actionButton.setOnClickListener { listener.onClick() }
    }

    public fun interface UserAvatarClickListener {
        public fun onUserAvatarClick()
    }

    public fun interface ActionButtonClickListener {
        public fun onClick()
    }
}
