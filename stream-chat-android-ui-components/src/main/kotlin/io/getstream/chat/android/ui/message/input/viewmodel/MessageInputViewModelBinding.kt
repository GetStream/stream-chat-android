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

@file:JvmName("MessageInputViewModelBinding")

package io.getstream.chat.android.ui.message.input.viewmodel

import androidx.lifecycle.LifecycleOwner
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.MessageInputView.ChatMode.DIRECT_CHAT
import io.getstream.chat.android.ui.message.input.MessageInputView.ChatMode.GROUP_CHAT
import java.io.File

/**
 * Binds [MessageInputView] with [MessageInputViewModel], updating the view's state
 * based on data provided by the ViewModel, and forwarding View events to the ViewModel.
 *
 * This function sets listeners on the view and ViewModel. Call this method
 * before setting any additional listeners on these objects yourself.
 *
 * @param view The View to bind to.
 * @param lifecycleOwner The lifecycle owner to bind the data observing to.
 */
@JvmName("bind")
public fun MessageInputViewModel.bindView(
    view: MessageInputView,
    lifecycleOwner: LifecycleOwner,
) {
    val handler = MessageInputView.DefaultUserLookupHandler(emptyList()) { query ->
        queryMembersByUserNameContains(query)
    }
    view.setUserLookupHandler(handler)
    members.observe(lifecycleOwner) { members ->
        handler.users = members.map(Member::user)
    }
    view.setMessageInputMentionListener { selectedUser ->
        selectMention(selectedUser)
    }
    commands.observe(lifecycleOwner, view::setCommands)
    maxMessageLength.observe(lifecycleOwner, view::setMaxMessageLength)
    cooldownInterval.observe(lifecycleOwner, view::setCooldownInterval)
    getActiveThread().observe(lifecycleOwner) {
        view.inputMode = if (it != null) {
            MessageInputView.InputMode.Thread(it)
        } else {
            MessageInputView.InputMode.Normal
        }
    }
    messageToEdit.observe(lifecycleOwner) { message ->
        message?.let { view.inputMode = MessageInputView.InputMode.Edit(it) }
    }
    isDirectMessage.observe(lifecycleOwner) { isDirectMessage ->
        view.chatMode = if (isDirectMessage) DIRECT_CHAT else GROUP_CHAT
    }

    ownCapabilities.observe(lifecycleOwner) {
        view.setOwnCapabilities(it)
    }

    view.setSendMessageHandler(
        object : MessageInputView.MessageSendHandler {
            val viewModel = this@bindView
            override fun sendMessage(messageText: String, messageReplyTo: Message?) {
                viewModel.sendMessage(messageText) { replyMessageId = messageReplyTo?.id }
            }

            override fun sendMessageWithAttachments(
                message: String,
                attachmentsWithMimeTypes: List<Pair<File, String?>>,
                messageReplyTo: Message?,
            ) {
                viewModel.sendMessageWithAttachments(message, attachmentsWithMimeTypes) {
                    replyMessageId = messageReplyTo?.id
                }
            }

            override fun sendMessageWithCustomAttachments(
                message: String,
                attachments: List<Attachment>,
                messageReplyTo: Message?,
            ) {
                viewModel.sendMessageWithCustomAttachments(message, attachments)
            }

            override fun sendToThreadWithAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsWithMimeTypes: List<Pair<File, String?>>,
            ) {
                viewModel.sendMessageWithAttachments(message, attachmentsWithMimeTypes) {
                    this.parentId = parentMessage.id
                    this.showInChannel = alsoSendToChannel
                }
            }

            override fun sendToThreadWithCustomAttachments(
                parentMessage: Message,
                message: String,
                alsoSendToChannel: Boolean,
                attachmentsWithMimeTypes: List<Attachment>,
            ) {
                viewModel.sendMessageWithCustomAttachments(message, attachmentsWithMimeTypes) {
                    this.parentId = parentMessage.id
                    this.showInChannel = alsoSendToChannel
                }
            }

            override fun sendToThread(parentMessage: Message, messageText: String, alsoSendToChannel: Boolean) {
                viewModel.sendMessage(messageText) {
                    this.parentId = parentMessage.id
                    this.showInChannel = alsoSendToChannel
                }
            }

            override fun editMessage(oldMessage: Message, newMessageText: String) {
                viewModel.editMessage(oldMessage.copy(text = newMessageText))
            }

            override fun dismissReply() {
                viewModel.dismissReply()
            }
        }
    )

    view.setTypingUpdatesBuffer(typingUpdatesBuffer)

    repliedMessage.observe(lifecycleOwner) {
        if (it != null) {
            view.inputMode = MessageInputView.InputMode.Reply(it)
        } else {
            view.inputMode = MessageInputView.InputMode.Normal
        }
    }
}
