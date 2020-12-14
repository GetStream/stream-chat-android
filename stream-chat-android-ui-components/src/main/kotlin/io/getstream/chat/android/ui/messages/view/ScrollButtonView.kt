package io.getstream.chat.android.ui.messages.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import io.getstream.chat.android.ui.databinding.StreamUiScrollButtonViewBinding

public class ScrollButtonView : FrameLayout {

    private val binding: StreamUiScrollButtonViewBinding =
        StreamUiScrollButtonViewBinding.inflate(LayoutInflater.from(context), this)

    private var unreadBadgeEnabled: Boolean = true

    private var unreadCount: Int = 0
    private var isVisible: Boolean = false

    public constructor(context: Context) : super(context)

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    public fun setUnreadBadgeEnabled(enabled: Boolean) {
        this.unreadBadgeEnabled = enabled
    }

    public fun setButtonRippleColor(@ColorInt color: Int) {
        binding.scrollActionButton.rippleColor = color
    }

    public fun setButtonIcon(icon: Drawable?) {
        binding.scrollActionButton.setImageDrawable(icon)
    }

    public fun setButtonColor(@ColorInt color: Int) {
        binding.scrollActionButton.backgroundTintList = ColorStateList.valueOf(color)
    }

    public fun setUnreadBadgeColor(@ColorInt color: Int) {
        binding.unreadCountTextView.backgroundTintList = ColorStateList.valueOf(color)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.scrollActionButton.setOnClickListener(listener)
    }

    public fun setUnreadCount(unreadCount: Int) {
        if (unreadBadgeEnabled) {
            setUnreadCountValue(unreadCount)
            setUnreadCountTextViewVisible(unreadCount > 0)
        } else {
            setUnreadCountTextViewVisible(false)
        }
    }

    private fun setUnreadCountValue(unreadCount: Int) {
        if (this.unreadCount != unreadCount) {
            this.unreadCount = unreadCount
            binding.unreadCountTextView.text = formatUnreadCount(unreadCount)
        }
    }

    private fun setUnreadCountTextViewVisible(isVisible: Boolean) {
        if (this.isVisible != isVisible) {
            this.isVisible = isVisible
            binding.unreadCountTextView.isVisible = isVisible
        }
    }

    private fun formatUnreadCount(unreadCount: Int): CharSequence {
        return if (unreadCount > MAX_UNREAD_VALUE) "$MAX_UNREAD_VALUE+" else unreadCount.toString()
    }

    private companion object {
        private const val MAX_UNREAD_VALUE = 999
    }
}
