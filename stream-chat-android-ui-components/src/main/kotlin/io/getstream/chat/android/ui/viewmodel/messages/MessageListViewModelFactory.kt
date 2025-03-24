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

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.CompatUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.DefaultUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.toUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.list.DateSeparatorHandler
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.feature.messages.list.MessagePositionHandler
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * A ViewModel factory for MessageListViewModel, MessageListHeaderViewModel and MessageComposerViewModel.
 *
 * @param cid The current channel ID, to load the messages from.
 * @param messageId The message ID to which we want to scroll to when opening the message list.
 * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread. If the
 * message we want to scroll to is not in a thread, you can pass in a null value.
 * @param chatClient The client to use for API calls.
 * @param clientState The current state of the SDK.
 * @param mediaRecorder The media recorder for async voice messages.
 * @param messageLimit The limit of the messages to load in a single page.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param showSystemMessages If we should show system message items in the list.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param dateSeparatorHandler Handler that determines when the date separators should be visible.
 * @param threadDateSeparatorHandler Handler that determines when the thread date separators should be visible.
 * @param messagePositionHandler Determines the position of the message inside a group.
 * @param showDateSeparatorInEmptyThread Configures if we show a date separator when threads are empty.
 * Adds the separator item when the value is `true`.
 * @param showThreadSeparatorInEmptyThread Configures if we show a thread separator when threads are empty.
 * Adds the separator item when the value is `true`.
 * @param threadLoadOlderToNewer Configures if the thread should be loaded from older to newer messages.
 * @param isComposerDraftMessagesEnabled Configures if the composer should support draft messages.
 *
 * @see MessageListHeaderViewModel
 * @see MessageListViewModel
 * @see MessageComposerViewModel
 */
public class MessageListViewModelFactory @JvmOverloads constructor(
    context: Context,
    private val cid: String,
    private val messageId: String? = null,
    private val parentMessageId: String? = null,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val mediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(context.applicationContext),
    private val userLookupHandler: UserLookupHandler = DefaultUserLookupHandler(chatClient, cid),
    private val fileToUri: (File) -> String = { file -> file.toUri().toString() },
    private val messageLimit: Int = MessageListController.DEFAULT_MESSAGES_LIMIT,
    private val enforceUniqueReactions: Boolean = true,
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val showSystemMessages: Boolean = true,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
    private val threadDateSeparatorHandler: DateSeparatorHandler =
        DateSeparatorHandler.getDefaultThreadDateSeparatorHandler(),
    private val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
    private val showDateSeparatorInEmptyThread: Boolean = false,
    private val showThreadSeparatorInEmptyThread: Boolean = false,
    private val threadLoadOlderToNewer: Boolean = false,
    private val isComposerDraftMessagesEnabled: Boolean = ChatUI.draftMessagesEnabled,
) : ViewModelProvider.Factory {

    private val channelStateFlow: StateFlow<ChannelState?> by lazy {
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = messageLimit,
            coroutineScope = chatClient.inheritScope { SupervisorJob(it) },
        )
    }

    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageListHeaderViewModel::class.java to { MessageListHeaderViewModel(cid, messageId = messageId) },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                messageListController = MessageListController(
                    cid = cid,
                    clipboardHandler = {},
                    threadLoadOrderOlderToNewer = threadLoadOlderToNewer,
                    messageLimit = messageLimit,
                    messageId = messageId,
                    parentMessageId = parentMessageId,
                    chatClient = chatClient,
                    clientState = clientState,
                    channelState = channelStateFlow,
                    enforceUniqueReactions = enforceUniqueReactions,
                    showSystemMessages = showSystemMessages,
                    deletedMessageVisibility = deletedMessageVisibility,
                    messageFooterVisibility = messageFooterVisibility,
                    dateSeparatorHandler = dateSeparatorHandler,
                    threadDateSeparatorHandler = threadDateSeparatorHandler,
                    messagePositionHandler = messagePositionHandler,
                    showDateSeparatorInEmptyThread = showDateSeparatorInEmptyThread,
                    showThreadSeparatorInEmptyThread = showThreadSeparatorInEmptyThread,
                ),
                chatClient = chatClient,
            )
        },
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    channelCid = cid,
                    chatClient = chatClient,
                    mediaRecorder = mediaRecorder,
                    userLookupHandler = userLookupHandler,
                    fileToUri = fileToUri,
                    channelState = channelStateFlow,
                    config = MessageComposerController.Config(
                        maxAttachmentCount = maxAttachmentCount,
                        isDraftMessageEnabled = isComposerDraftMessagesEnabled,
                    ),
                ),
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
    public class Builder
    @SinceKotlin("99999.9")
    constructor(
        private val context: Context,
    ) {
        private var cid: String? = null
        private var messageId: String? = null
        private var chatClient: ChatClient = ChatClient.instance()
        private var enforceUniqueReactions: Boolean = true
        private var maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT
        private var showSystemMessages: Boolean = true
        private var deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE
        private var messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference()
        private var dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler()
        private var threadDateSeparatorHandler: DateSeparatorHandler =
            DateSeparatorHandler.getDefaultThreadDateSeparatorHandler()
        private var messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler()
        private var mediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(context.applicationContext)
        private var userLookupHandler: UserLookupHandler? = null
        private var userLookupHandlerCompat: CompatUserLookupHandler? = null
        private var threadLoadOlderToNewer: Boolean = false

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

        public fun mediaRecorder(mediaRecorder: StreamMediaRecorder): Builder = apply {
            this.mediaRecorder = mediaRecorder
        }

        public fun userLookupHandlerCompat(userLookupHandler: CompatUserLookupHandler): Builder = apply {
            this.userLookupHandlerCompat = userLookupHandler
        }

        public fun userLookupHandler(userLookupHandler: UserLookupHandler): Builder = apply {
            this.userLookupHandler = userLookupHandler
        }

        public fun enforceUniqueReactions(enforceUniqueReactions: Boolean): Builder = apply {
            this.enforceUniqueReactions = enforceUniqueReactions
        }

        public fun maxAttachmentCount(maxAttachmentCount: Int): Builder = apply {
            this.maxAttachmentCount = maxAttachmentCount
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

        public fun threadLoadOlderToNewer(threadLoadOlderToNewer: Boolean): Builder = apply {
            this.threadLoadOlderToNewer = threadLoadOlderToNewer
        }

        /**
         * Builds [MessageListViewModelFactory] instance.
         */
        public fun build(): ViewModelProvider.Factory {
            val cid = cid ?: error("Channel cid should not be null")
            return MessageListViewModelFactory(
                context = context,
                cid = cid,
                messageId = messageId,
                chatClient = chatClient,
                mediaRecorder = mediaRecorder,
                userLookupHandler = userLookupHandler
                    ?: userLookupHandlerCompat?.toUserLookupHandler()
                    ?: DefaultUserLookupHandler(chatClient, cid),
                enforceUniqueReactions = enforceUniqueReactions,
                maxAttachmentCount = maxAttachmentCount,
                showSystemMessages = showSystemMessages,
                deletedMessageVisibility = deletedMessageVisibility,
                messageFooterVisibility = messageFooterVisibility,
                dateSeparatorHandler = dateSeparatorHandler,
                threadDateSeparatorHandler = threadDateSeparatorHandler,
                messagePositionHandler = messagePositionHandler,
                threadLoadOlderToNewer = threadLoadOlderToNewer,
            )
        }
    }
}
