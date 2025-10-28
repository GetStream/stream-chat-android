/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.messages.list.internal

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import io.getstream.chat.android.ui.databinding.StreamUiScrollButtonViewBinding
import io.getstream.chat.android.ui.feature.messages.list.ScrollButtonViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class ScrollButtonView : FrameLayout {

    private lateinit var scrollButtonViewStyle: ScrollButtonViewStyle
    private var unreadCount: Int = 0
    private val binding: StreamUiScrollButtonViewBinding =
        StreamUiScrollButtonViewBinding.inflate(streamThemeInflater, this)

    constructor(context: Context) : super(context.createStreamThemeWrapper())

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    )

    /**
     * Sets [scrollButtonViewStyle] and calls the functions that apply the style.
     *
     * @param scrollButtonViewStyle The style to set.
     */
    fun setScrollButtonViewStyle(scrollButtonViewStyle: ScrollButtonViewStyle) {
        this.scrollButtonViewStyle = scrollButtonViewStyle
        setActionButtonStyle()
        setBadgeStyle()
    }

    /**
     * Applies style to the action button using [scrollButtonViewStyle].
     */
    private fun setActionButtonStyle() {
        (binding.scrollActionButton.layoutParams as? LayoutParams)?.let { layoutParams ->
            layoutParams.setMargins(scrollButtonViewStyle.scrollButtonInternalMargin)
        }
        binding.scrollActionButton.rippleColor = scrollButtonViewStyle.scrollButtonRippleColor
        binding.scrollActionButton.setImageDrawable(scrollButtonViewStyle.scrollButtonIcon)
        binding.scrollActionButton.backgroundTintList = ColorStateList.valueOf(scrollButtonViewStyle.scrollButtonColor)
        binding.scrollActionButton.compatElevation = scrollButtonViewStyle.scrollButtonElevation
    }

    /**
     * Applies style to the unread count badge using [scrollButtonViewStyle].
     */
    private fun setBadgeStyle() {
        (binding.unreadCountTextView.layoutParams as? LayoutParams).let { layoutParams ->
            layoutParams?.gravity = scrollButtonViewStyle.scrollButtonBadgeGravity
        }
        binding.unreadCountTextView.elevation = scrollButtonViewStyle.scrollButtonBadgeElevation
        binding.unreadCountTextView.background = scrollButtonViewStyle.scrollButtonBadgeIcon
        scrollButtonViewStyle.scrollButtonBadgeColor?.let { color ->
            binding.unreadCountTextView.backgroundTintList =
                ColorStateList.valueOf(color)
        }
        binding.unreadCountTextView.setTextStyle(scrollButtonViewStyle.scrollButtonBadgeTextStyle)
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        binding.scrollActionButton.setOnClickListener(listener)
    }

    fun setUnreadCount(unreadCount: Int) {
        if (scrollButtonViewStyle.scrollButtonEnabled && scrollButtonViewStyle.scrollButtonUnreadEnabled) {
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
        binding.unreadCountTextView.isVisible = isVisible
    }

    private fun formatUnreadCount(unreadCount: Int): CharSequence = if (unreadCount > MAX_UNREAD_VALUE) "$MAX_UNREAD_VALUE+" else unreadCount.toString()

    private companion object {
        private const val MAX_UNREAD_VALUE = 999
    }
}
