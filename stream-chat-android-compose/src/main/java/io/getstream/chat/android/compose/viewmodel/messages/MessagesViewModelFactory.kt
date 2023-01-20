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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.AttachmentFilter
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.common.composer.MessageComposerController
import io.getstream.chat.android.common.state.DeletedMessageVisibility
import io.getstream.chat.android.common.state.MessageFooterVisibility
import io.getstream.chat.android.compose.handlers.ClipboardHandlerImpl
import io.getstream.chat.android.compose.ui.util.StorageHelperWrapper
import java.util.concurrent.TimeUnit

/**
 * Holds all the dependencies needed to build the ViewModels for the Messages Screen.
 * Currently builds the [MessageComposerViewModel], [MessageListViewModel] and [AttachmentsPickerViewModel].
 * @param context Used to build the [ClipboardManager].
 * @param channelId The current channel ID, to load the messages from.
 * @param chatClient The client to use for API calls.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param messageLimit The limit when loading messages.
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param maxAttachmentSize The maximum file size of each attachment in bytes. By default, 100 MB for Stream CDN.
 * @param showDateSeparators If we should show date separator items in the list.
 * @param showSystemMessages If we should show system message items in the list.
 * @param deletedMessageVisibility The behavior of deleted messages in the list and if they're visible or not.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param dateSeparatorThresholdMillis The millisecond amount that represents the threshold for adding date separators.
 * @param messageId The ID of the message which we wish to focus on, if such exists.
 * @param navigateToThreadViaNotification If true, when a thread message arrives in a push notification,
 * clicking it will automatically open the thread in which the message is located. If false, the SDK will always
 * navigate to the channel containing the thread but will not navigate to the thread itself.
 */
public class MessagesViewModelFactory(
    private val context: Context,
    private val channelId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val enforceUniqueReactions: Boolean = true,
    private val messageLimit: Int = MessageListViewModel.DefaultMessageLimit,
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
    private val showDateSeparators: Boolean = true,
    private val showSystemMessages: Boolean = true,
    private val deletedMessageVisibility: DeletedMessageVisibility = DeletedMessageVisibility.ALWAYS_VISIBLE,
    private val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.WithTimeDifference(),
    private val dateSeparatorThresholdMillis: Long = TimeUnit.HOURS.toMillis(MessageListViewModel.DateSeparatorDefaultHourThreshold),
    private val messageId: String? = null,
    private val navigateToThreadViaNotification: Boolean = false,
) : ViewModelProvider.Factory {

    /**
     * The list of factories that can build [ViewModel]s that our Messages feature components use.
     */
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        MessageComposerViewModel::class.java to {
            MessageComposerViewModel(
                MessageComposerController(
                    chatClient = chatClient,
                    channelId = channelId,
                    maxAttachmentCount = maxAttachmentCount,
                    maxAttachmentSize = maxAttachmentSize
                )
            )
        },
        MessageListViewModel::class.java to {
            MessageListViewModel(
                chatClient = chatClient,
                channelId = channelId,
                messageLimit = messageLimit,
                enforceUniqueReactions = enforceUniqueReactions,
                clipboardHandler =
                ClipboardHandlerImpl(context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager),
                showDateSeparators = showDateSeparators,
                showSystemMessages = showSystemMessages,
                deletedMessageVisibility = deletedMessageVisibility,
                messageFooterVisibility = messageFooterVisibility,
                dateSeparatorThresholdMillis = dateSeparatorThresholdMillis,
                messageId = messageId,
                navigateToThreadViaNotification = navigateToThreadViaNotification
            )
        },
        AttachmentsPickerViewModel::class.java to {
            AttachmentsPickerViewModel(
                StorageHelperWrapper(context, StorageHelper(), AttachmentFilter()),
            )
        }
    )

    /**
     * Creates the required [ViewModel] for our use case, based on the [factories] we provided.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException(
                "MessageListViewModelFactory can only create instances of " +
                    "the following classes: ${factories.keys.joinToString { it.simpleName }}"
            )

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
