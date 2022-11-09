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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController
import io.getstream.chat.android.ui.common.feature.messages.list.DateSeparatorHandler
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.feature.messages.list.MessagePositionHandler
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import io.getstream.chat.android.ui.common.utils.AttachmentConstants

/**
 * A ViewModel factory for MessageListViewModel, MessageListHeaderViewModel and MessageComposerViewModel.
 *
 * @param cid The current channel ID, to load the messages from.
 * @param messageId The message ID to which we want to scroll to when opening the message list.
 * @param chatClient The client to use for API calls.
 * @param clientState The current state of the SDK.
 * @param messageLimit The limit of the messages to load in a single page.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param maxAttachmentSize The maximum file size of each attachment in bytes. By default, 100mb for Stream CDN.
 * @param showSystemMessages If we should show system message items in the list.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param dateSeparatorHandler Handler that determines when the date separators should be visible.
 * @param threadDateSeparatorHandler Handler that determines when the thread date separators should be visible.
 * @param messagePositionHandler Determines the position of the message inside a group.
 *
 * @see MessageListHeaderViewModel
 * @see MessageListViewModel
 * @see MessageComposerViewModel
 */
public class MessageListViewModelFactory @JvmOverloads constructor(
    private val cid: String,
    private val messageId: String? = null,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val messageLimit: Int = MessageListController.DEFAULT_MESSAGES_LIMIT,
    private val enforceUniqueReactions: Boolean = true,
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
    private val showSystemMessages: Boolean = true,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
    private val threadDateSeparatorHandler: DateSeparatorHandler =
        DateSeparatorHandler.getDefaultThreadDateSeparatorHandler(),
    private val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
) : ViewModelProvider.Factory {

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageListHeaderViewModel::class.java to { MessageListHeaderViewModel(cid, messageId = messageId) },
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    cid,
                    messageId = messageId
                )
            )
        },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                messageListController = MessageListController(
                    cid = cid,
                    clipboardHandler = {},
                    messageLimit = messageLimit,
                    messageId = messageId,
                    chatClient = chatClient,
                    clientState = clientState,
                    enforceUniqueReactions = enforceUniqueReactions,
                    showSystemMessages = showSystemMessages,
                    deletedMessageVisibility = deletedMessageVisibility,
                    messageFooterVisibility = messageFooterVisibility,
                    dateSeparatorHandler = dateSeparatorHandler,
                    threadDateSeparatorHandler = threadDateSeparatorHandler,
                    messagePositionHandler = messagePositionHandler,
                ),
                chatClient = chatClient
            )
        },
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    channelId = cid,
                    chatClient = chatClient,
                    maxAttachmentCount = maxAttachmentCount,
                    maxAttachmentSize = maxAttachmentSize
                )
            )
        },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("MessageListViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }

    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public class Builder @SinceKotlin("99999.9") constructor() {
        private var cid: String? = null
        private var messageId: String? = null
        private var chatClient: ChatClient = ChatClient.instance()
        private var enforceUniqueReactions: Boolean = true
        private var maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT
        private var maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE
        private var showSystemMessages: Boolean = true
        private var deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE
        private var messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference()
        private var dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler()
        private var threadDateSeparatorHandler: DateSeparatorHandler =
            DateSeparatorHandler.getDefaultThreadDateSeparatorHandler()
        private var messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler()

        /**
         * Sets the channel id in the format messaging:123.
         */
        public fun cid(cid: String): Builder = apply {
            this.cid = cid
        }

        /**
         * Sets the id of the target message to displayed.
         */
        public fun messageId(messageId: String): Builder = apply {
            this.messageId = messageId
        }

        public fun chatClient(chatClient: ChatClient): Builder = apply {
            this.chatClient = chatClient
        }

        public fun enforceUniqueReactions(enforceUniqueReactions: Boolean): Builder = apply {
            this.enforceUniqueReactions = enforceUniqueReactions
        }

        public fun maxAttachmentCount(maxAttachmentCount: Int): Builder = apply {
            this.maxAttachmentCount = maxAttachmentCount
        }

        public fun maxAttachmentSize(maxAttachmentSize: Long): Builder = apply {
            this.maxAttachmentSize = maxAttachmentSize
        }

        public fun showSystemMessages(showSystemMessages: Boolean): Builder = apply {
            this.showSystemMessages = showSystemMessages
        }

        public fun deletedMessageVisibility(deletedMessageVisibility: DeletedMessageVisibility): Builder = apply {
            this.deletedMessageVisibility = deletedMessageVisibility
        }

        public fun messageFooterVisibility(messageFooterVisibility: MessageFooterVisibility): Builder = apply {
            this.messageFooterVisibility = messageFooterVisibility
        }

        public fun dateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler): Builder = apply {
            this.dateSeparatorHandler = dateSeparatorHandler
        }

        public fun threadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler): Builder = apply {
            this.threadDateSeparatorHandler = threadDateSeparatorHandler
        }

        /**
         * Builds [MessageListViewModelFactory] instance.
         */
        public fun build(): ViewModelProvider.Factory {
            return MessageListViewModelFactory(
                cid = cid ?: error("Channel cid should not be null"),
                messageId = messageId,
                chatClient = chatClient,
                enforceUniqueReactions = enforceUniqueReactions,
                maxAttachmentCount = maxAttachmentCount,
                maxAttachmentSize = maxAttachmentSize,
                showSystemMessages = showSystemMessages,
                deletedMessageVisibility = deletedMessageVisibility,
                messageFooterVisibility = messageFooterVisibility,
                dateSeparatorHandler = dateSeparatorHandler,
                threadDateSeparatorHandler = threadDateSeparatorHandler,
                messagePositionHandler = messagePositionHandler
            )
        }
    }
}
