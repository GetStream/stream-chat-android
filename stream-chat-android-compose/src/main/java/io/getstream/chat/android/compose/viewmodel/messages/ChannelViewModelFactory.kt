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

package io.getstream.chat.android.compose.viewmodel.messages

import android.content.ClipboardManager
import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.watchChannelAsState
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.feature.messages.composer.MessageComposerController
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.DefaultUserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.list.DateSeparatorHandler
import io.getstream.chat.android.ui.common.feature.messages.list.MessageListController
import io.getstream.chat.android.ui.common.feature.messages.list.MessagePositionHandler
import io.getstream.chat.android.ui.common.helper.ClipboardHandler
import io.getstream.chat.android.ui.common.helper.ClipboardHandlerImpl
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import java.io.File

/**
 * Configuration for the message list behavior.
 *
 * @param messageLimit The number of messages to load in a single page.
 * @param showSystemMessages If we should show system message items in the list.
 * @param messageFooterVisibility The behavior of message footers in the list and their visibility.
 * @param enforceUniqueReactions Flag to enforce unique reactions or enable multiple from the same user.
 * @param dateSeparatorHandler Handler that determines when the date separators should be visible.
 * @param threadDateSeparatorHandler Handler that determines when the thread date separators should be visible.
 * @param messagePositionHandler Determines the position of the message inside a group.
 * @param showDateSeparatorInEmptyThread Configures if we show a date separator when threads are empty.
 * @param showThreadSeparatorInEmptyThread Configures if we show a thread separator when threads are empty.
 * @param threadLoadOlderToNewer Configures if the thread should load older messages to newer messages.
 */
public data class MessageListOptions(
    val messageLimit: Int = MessageListController.DEFAULT_MESSAGES_LIMIT,
    val showSystemMessages: Boolean = true,
    val messageFooterVisibility: MessageFooterVisibility = MessageFooterVisibility.LastInGroup,
    val enforceUniqueReactions: Boolean = false,
    val dateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultDateSeparatorHandler(),
    val threadDateSeparatorHandler: DateSeparatorHandler = DateSeparatorHandler.getDefaultThreadDateSeparatorHandler(),
    val messagePositionHandler: MessagePositionHandler = MessagePositionHandler.defaultHandler(),
    val showDateSeparatorInEmptyThread: Boolean = false,
    val showThreadSeparatorInEmptyThread: Boolean = false,
    val threadLoadOlderToNewer: Boolean = false,
)

/**
 * Configuration for the message composer behavior.
 *
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param linkPreviewEnabled If the link preview is enabled in the composer.
 * @param draftMessagesEnabled If draft messages are enabled in the composer.
 */
public data class ComposerOptions(
    val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    val linkPreviewEnabled: Boolean = false,
    val draftMessagesEnabled: Boolean = true,
)

/**
 * Holds all the dependencies needed to build the ViewModels for the Messages Screen.
 * Currently, builds the [MessageComposerViewModel], [MessageListViewModel] and [AttachmentsPickerViewModel].
 *
 * @param context Android context used to access system services and device storage.
 * @param channelId The current channel ID, to load the messages from.
 * @param messageId The message id to which we want to scroll to when opening the message list.
 * @param parentMessageId The ID of the parent [Message] if the message we want to scroll to is in a thread. If the
 * message we want to scroll to is not in a thread, you can pass in a null value.
 * @param autoTranslationEnabled Whether auto-translation of messages is enabled.
 * @param chatClient The client to use for API calls.
 * @param clientState The current state of the SDK.
 * @param mediaRecorder The media recorder for async voice messages.
 * @param userLookupHandler Handler used to look up users for mention autocomplete.
 * @param fileToUriConverter Converts a local [File] to a URI string used as an attachment source.
 * @param clipboardHandler [ClipboardHandler] used to copy messages.
 * @param messageListOptions Configuration for the message list behavior.
 * @param composerOptions Configuration for the message composer behavior.
 */
public class ChannelViewModelFactory(
    private val context: Context,
    internal val channelId: String,
    internal val messageId: String? = null,
    internal val parentMessageId: String? = null,
    private val autoTranslationEnabled: Boolean = true,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val mediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(context.applicationContext),
    private val userLookupHandler: UserLookupHandler = DefaultUserLookupHandler(chatClient, channelId),
    private val fileToUriConverter: (File) -> String = { file -> file.toUri().toString() },
    private val clipboardHandler: ClipboardHandler = ClipboardHandlerImpl(
        clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
        autoTranslationEnabled = autoTranslationEnabled,
        getCurrentUser = chatClient::getCurrentUser,
    ),
    private val messageListOptions: MessageListOptions = MessageListOptions(),
    private val composerOptions: ComposerOptions = ComposerOptions(),
) : ViewModelProvider.Factory {

    private val channelStateFlow: StateFlow<ChannelState?> by lazy {
        val scope = chatClient.inheritScope { SupervisorJob(it) + DispatcherProvider.Immediate }
        when {
            messageId != null && parentMessageId == null ->
                chatClient.watchChannelAsState(channelId, messageListOptions.messageLimit, messageId, scope)

            else ->
                chatClient.watchChannelAsState(channelId, messageListOptions.messageLimit, scope)
        }
    }

    private val storageHelper by lazy { AttachmentStorageHelper(context) }

    /**
     * Creates the required [ViewModel] for our use case.
     *
     * Supports [MessageComposerViewModel], [MessageListViewModel], and [AttachmentsPickerViewModel].
     * [MessageComposerViewModel] and [AttachmentsPickerViewModel] will receive a [SavedStateHandle]
     * sourced from [extras], allowing them to survive Activity recreation.
     * Throws [IllegalArgumentException] for any other class.
     *
     * @param modelClass The class of the [ViewModel] to create.
     * @param extras [CreationExtras] provided by the [androidx.lifecycle.ViewModelStoreOwner].
     */
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T =
        createViewModel(modelClass, savedStateHandle = extras.createSavedStateHandle())

    /**
     * Creates the required [ViewModel] for our use case.
     *
     * Supports [MessageComposerViewModel], [MessageListViewModel], and [AttachmentsPickerViewModel].
     * Throws [IllegalArgumentException] for any other class.
     *
     * Prefer [create] with [CreationExtras] so that [MessageComposerViewModel] and
     * [AttachmentsPickerViewModel] can survive Activity recreation.
     *
     * @param modelClass The class of the [ViewModel] to create.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        createViewModel(modelClass, savedStateHandle = null)

    private fun <T : ViewModel> createViewModel(modelClass: Class<T>, savedStateHandle: SavedStateHandle?): T {
        val viewModel: ViewModel = when (modelClass) {
            MessageComposerViewModel::class.java -> MessageComposerViewModel(
                messageComposerController = MessageComposerController(
                    chatClient = chatClient,
                    channelState = channelStateFlow,
                    mediaRecorder = mediaRecorder,
                    userLookupHandler = userLookupHandler,
                    fileToUri = fileToUriConverter,
                    channelCid = channelId,
                    config = MessageComposerController.Config(
                        maxAttachmentCount = composerOptions.maxAttachmentCount,
                        linkPreviewEnabled = composerOptions.linkPreviewEnabled,
                        draftMessageEnabled = composerOptions.draftMessagesEnabled,
                        activeCommandEnabled = true,
                    ),
                    savedStateHandle = savedStateHandle ?: SavedStateHandle(),
                ),
                storageHelper = storageHelper,
            )

            MessageListViewModel::class.java -> MessageListViewModel(
                MessageListController(
                    cid = channelId,
                    clipboardHandler = clipboardHandler,
                    threadLoadOrderOlderToNewer = messageListOptions.threadLoadOlderToNewer,
                    messageId = messageId,
                    parentMessageId = parentMessageId,
                    messageLimit = messageListOptions.messageLimit,
                    chatClient = chatClient,
                    clientState = clientState,
                    channelState = channelStateFlow,
                    enforceUniqueReactions = messageListOptions.enforceUniqueReactions,
                    showSystemMessages = messageListOptions.showSystemMessages,
                    messageFooterVisibility = messageListOptions.messageFooterVisibility,
                    dateSeparatorHandler = messageListOptions.dateSeparatorHandler,
                    threadDateSeparatorHandler = messageListOptions.threadDateSeparatorHandler,
                    messagePositionHandler = messageListOptions.messagePositionHandler,
                    showDateSeparatorInEmptyThread = messageListOptions.showDateSeparatorInEmptyThread,
                    showThreadSeparatorInEmptyThread = messageListOptions.showThreadSeparatorInEmptyThread,
                ),
            )

            AttachmentsPickerViewModel::class.java -> AttachmentsPickerViewModel(
                storageHelper = storageHelper,
                channelState = channelStateFlow,
                savedStateHandle = savedStateHandle ?: SavedStateHandle(),
            )

            else -> throw IllegalArgumentException(
                "ChannelViewModelFactory can only create instances of " +
                    "${MessageComposerViewModel::class.java.simpleName}, " +
                    "${MessageListViewModel::class.java.simpleName}, or " +
                    "${AttachmentsPickerViewModel::class.java.simpleName}.",
            )
        }

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
