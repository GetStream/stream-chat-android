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

package io.getstream.chat.android.compose.viewmodel.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController
import io.getstream.chat.android.ui.common.feature.messages.list.DateSeparatorHandler
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.feature.messages.list.MessagePositionHandler
import io.getstream.chat.android.ui.common.helper.ClipboardHandler
import io.getstream.chat.android.ui.common.helper.ClipboardHandlerImpl
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.state.messages.list.DeletedMessageVisibility
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import java.io.File

/**
 * Holds all the dependencies needed to build the ViewModels for the Messages Screen.
 * Currently builds the [MessageComposerViewModel], [MessageListViewModel] and [AttachmentsPickerViewModel].
 * @param context Used to build the [ClipboardManager].
 * @param channelId The current channel ID, to load the messages from.
 * @param messageId The message id to which we want to scroll to when opening the message list.
 * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread. If the
 * message we want to scroll to is not in a thread, you can pass in a null value.
 * @param chatClient The client to use for API calls.
 * @param clientState The current state of the SDK.
 * @param mediaRecorder The media recorder for async voice messages.
 * @param messageLimit The number of messages to load in a single page.
 * @param clipboardHandler [ClipboardHandler] used to copy messages.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param maxAttachmentSize The maximum file size of each attachment in bytes. By default, 100 MB for Stream CDN.
 * @param showSystemMessages If we should show system message items in the list.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param dateSeparatorHandler Handler that determines when the date separators should be visible.
 * @param threadDateSeparatorHandler Handler that determines when the thread date separators should be visible.
 * @param messagePositionHandler Determines the position of the message inside a group.
 * @param showDateSeparatorInEmptyThread Configures if we show a thread separator when threads are empty.
 * Adds the separator item when the value is `true`.
 */
public class MessagesViewModelFactory(
    private val context: Context,
    private val channelId: String,
    private val messageId: String? = null,
    private val parentMessageId: String? = null,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val mediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(context.applicationContext),
    private val fileToUriConverter: (File) -> String = { file -> file.toUri().toString() },
    private val messageLimit: Int = MessageListController.DEFAULT_MESSAGES_LIMIT,
    private val clipboardHandler: ClipboardHandler =
        ClipboardHandlerImpl(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager),
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
    private val showDateSeparatorInEmptyThread: Boolean = false,
) : ViewModelProvider.Factory {

    /**
     * The list of factories that can build [ViewModel]s that our Messages feature components use.
     */
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    chatClient = chatClient,
                    mediaRecorder = mediaRecorder,
                    fileToUri = fileToUriConverter,
                    channelId = channelId,
                    maxAttachmentCount = maxAttachmentCount,
                    maxAttachmentSize = maxAttachmentSize,
                ),
            )
        },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                MessageListController(
                    cid = channelId,
                    clipboardHandler = clipboardHandler,
                    messageId = messageId,
                    parentMessageId = parentMessageId,
                    messageLimit = messageLimit,
                    chatClient = chatClient,
                    clientState = clientState,
                    enforceUniqueReactions = enforceUniqueReactions,
                    showSystemMessages = showSystemMessages,
                    deletedMessageVisibility = deletedMessageVisibility,
                    messageFooterVisibility = messageFooterVisibility,
                    dateSeparatorHandler = dateSeparatorHandler,
                    threadDateSeparatorHandler = threadDateSeparatorHandler,
                    messagePositionHandler = messagePositionHandler,
                    showDateSeparatorInEmptyThread = showDateSeparatorInEmptyThread,
                ),
            )
        },
        AttachmentsPickerViewModel::class.java to {
            AttachmentsPickerViewModel(
                StorageHelperWrapper(context, StorageHelper(), AttachmentFilter()),
            )
        },
    )

    /**
     * Creates the required [ViewModel] for our use case, based on the [factories] we provided.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException(
                "MessageListViewModelFactory can only create instances of " +
                    "the following classes: ${factories.keys.joinToString { it.simpleName }}",
            )

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
