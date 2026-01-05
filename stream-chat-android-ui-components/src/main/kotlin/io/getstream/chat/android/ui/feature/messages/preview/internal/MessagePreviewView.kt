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

package io.getstream.chat.android.ui.feature.messages.preview.internal

import android.content.Context
import android.text.Html
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import android.widget.FrameLayout
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.singletonList
import io.getstream.chat.android.ui.common.model.MessageResult
import io.getstream.chat.android.ui.common.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.databinding.StreamUiMessagePreviewItemBinding
import io.getstream.chat.android.ui.feature.messages.preview.MessagePreviewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.asMention
import io.getstream.chat.android.ui.utils.extensions.bold
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getAttachmentsText
import io.getstream.chat.android.ui.utils.extensions.getCreatedAtOrNull
import io.getstream.chat.android.ui.utils.extensions.getTranslatedText
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater

internal class MessagePreviewView : FrameLayout {

    internal val binding = StreamUiMessagePreviewItemBinding.inflate(streamThemeInflater, this, true)

    constructor(context: Context) : super(context.createStreamThemeWrapper()) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context.createStreamThemeWrapper(), attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context.createStreamThemeWrapper(),
        attrs,
        defStyleAttr,
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
            binding.senderNameLabel.setTextStyle(messageSenderTextStyle)
            binding.messageLabel.setTextStyle(messageTextStyle)
            binding.messageTimeLabel.setTextStyle(messageTimeTextStyle)
        }
    }

    fun renderMessageResult(messageResult: MessageResult) {
        renderMessage(messageResult.message)
        renderChannel(messageResult)
    }

    private fun renderDate(message: Message) {
        binding.messageTimeLabel.text = ChatUI.dateFormatter.formatDate(message.getCreatedAtOrNull())
    }

    private fun renderMessage(message: Message) {
        renderDate(message)
        binding.messageLabel.text = formatMessagePreview(
            message,
            ChatUI.currentUserProvider.getCurrentUser()?.asMention(context),
        )
    }

    private fun renderChannel(messageResult: MessageResult) {
        val isDirectMessaging = messageResult.channel?.isDirectMessaging() == true
        val currentUser = ChatUI.currentUserProvider.getCurrentUser()
        binding.userAvatarView.setUser(
            messageResult
                .channel
                ?.takeIf { isDirectMessaging }
                ?.let { it.members.firstOrNull { it.getUserId() != currentUser?.id }?.user }
                ?: messageResult.message.user,
        )

        binding.senderNameLabel.text = if (isDirectMessaging) {
            messageResult.channel?.members?.first { it.getUserId() != currentUser?.id }?.user?.name?.bold()
        } else {
            (
                messageResult.channel
                    ?.let { ChatUI.channelNameFormatter.formatChannelName(it, currentUser) }
                    ?: messageResult.message.channelInfo?.name
                )
                ?.let {
                    Html.fromHtml(
                        context.getString(
                            R.string.stream_ui_message_preview_sender,
                            messageResult.message.user.name,
                            it,
                        ),
                    )
                }
                ?: messageResult.message.user.name.bold()
        }
    }

    private fun formatMessagePreview(message: Message, currentUserMention: String?): CharSequence {
        val attachmentsText = message.getAttachmentsText()
        val displayedText = message.getTranslatedText()
        val previewText = displayedText.trim().let {
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
