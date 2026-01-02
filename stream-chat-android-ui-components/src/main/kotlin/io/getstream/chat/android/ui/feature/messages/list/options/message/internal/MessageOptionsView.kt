/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.list.options.message.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.TextView
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.databinding.StreamUiMessageOptionsViewBinding
import io.getstream.chat.android.ui.feature.messages.list.MessageListView
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.options.message.MessageOptionItem
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.inflater
import io.getstream.chat.android.ui.utils.extensions.setStartDrawable
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

/**
 * Displays all available message actions a user can execute on a message.
 */
internal class MessageOptionsView : FrameLayout {

    private val binding = StreamUiMessageOptionsViewBinding.inflate(streamThemeInflater, this, true)

    private var listener: MessageActionClickListener? = null

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
    )

    /**
     * Sets a click listener for message option item clicks.
     *
     * @param listener The callback to be invoked when an option item is clicked.
     */
    fun setMessageActionClickListener(listener: MessageActionClickListener) {
        this.listener = listener
    }

    /**
     * Initializes the view with a set of message option items.
     *
     * @param messageOptions The list of message option items to display.
     * @param style Style for [MessageListView].
     */
    fun setMessageOptions(
        messageOptions: List<MessageOptionItem>,
        style: MessageListViewStyle,
    ) {
        binding.messageOptionsContainer.setCardBackgroundColor(style.messageOptionsBackgroundColor)

        binding.optionListContainer.removeAllViews()
        messageOptions.forEach { option ->
            val messageOptionTextView = inflater.inflate(
                R.layout.stream_ui_message_option_item,
                this,
                false,
            ) as TextView

            messageOptionTextView.text = option.optionText
            messageOptionTextView.setStartDrawable(option.optionIcon)
            messageOptionTextView.setOnClickListener {
                listener?.onMessageActionClick(option.messageAction)
            }

            val textStyle = if (option.isWarningItem) {
                style.warningMessageOptionsText
            } else {
                style.messageOptionsText
            }
            messageOptionTextView.setTextStyle(textStyle)

            binding.optionListContainer.addView(messageOptionTextView)
        }
    }

    /**
     * Click listener for message option items.
     */
    fun interface MessageActionClickListener {
        fun onMessageActionClick(messageAction: MessageAction)
    }
}
