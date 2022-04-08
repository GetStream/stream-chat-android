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

package io.getstream.chat.android.ui.message.preview.internal

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.FrameLayout
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.bold
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getAttachmentsText
import io.getstream.chat.android.ui.common.extensions.internal.singletonList
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiMessagePreviewItemBinding
import io.getstream.chat.android.ui.message.preview.MessagePreviewStyle

internal class MessagePreviewView : FrameLayout {

    private val binding = StreamUiMessagePreviewItemBinding.inflate(streamThemeInflater, this, true)

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet?) {
        parseAttrs(attrs)
    }

    private fun parseAttrs(attrs: AttributeSet?) {
        attrs ?: return
    }

    fun styleView(messagePreviewStyle: MessagePreviewStyle) {
        messagePreviewStyle.run {
            messageSenderTextStyle.apply(binding.senderNameLabel)
            messageTextStyle.apply(binding.messageLabel)
            messageTimeTextStyle.apply(binding.messageTimeLabel)
        }
    }

    fun setMessage(message: Message, currentUserMention: String? = null) {
        binding.avatarView.setUserData(message.user)
        binding.senderNameLabel.text = formatChannelName(message)
        binding.messageLabel.text = formatMessagePreview(message, currentUserMention)
        binding.messageTimeLabel.text = ChatUI.dateFormatter.formatDate(message.createdAt ?: message.createdLocallyAt)
    }

    private fun formatChannelName(message: Message): CharSequence {
        val channel = message.channelInfo
        return if (channel?.name != null && channel.memberCount > 2) {
            Html.fromHtml(
                context.getString(
                    R.string.stream_ui_message_preview_sender,
                    message.user.name,
                    channel.name,
                )
            )
        } else {
            message.user.name.bold()
        }
    }

    private fun formatMessagePreview(message: Message, currentUserMention: String?): CharSequence {
        val attachmentsText = message.getAttachmentsText()

        val previewText = message.text.trim().let {
            if (currentUserMention != null) {
                // bold mentions of the current user
                it.bold(currentUserMention.singletonList(), ignoreCase = true)
            } else {
                it
            }
        }

        return listOf(previewText, attachmentsText)
            .filterNot { it.isNullOrEmpty() }
            .joinTo(SpannableStringBuilder(), " ")
    }
}
