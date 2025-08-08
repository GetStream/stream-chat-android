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

package io.getstream.chat.android.compose.viewmodel.mediapreview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * A ViewModel capable of loading images, playing videos.
 *
 * @param chatClient The low level chat client used for API calls.
 * @param clientState Used to collect client state information such as the connectivity status.
 * @param messageId The ID of the message containing the attachments to be previewed.
 * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 */
public class MediaGalleryPreviewViewModel(
    private val chatClient: ChatClient,
    private val clientState: ClientState,
    private val messageId: String,
    private val skipEnrichUrl: Boolean = false,
) : ViewModel() {

    /**
     * The currently logged in user.
     */
    public val user: StateFlow<User?> = chatClient.clientState.user

    /**
     * Indicates if we have fetched the complete message from the backend.
     *
     * This is necessary because our first state is set via a minimum viable
     * data set needed to display the full UI in offline state.
     *
     * @see [io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewActivityAttachmentState]
     * and [io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewActivity.getIntent]
     */
    internal var hasCompleteMessage: Boolean = false

    /**
     * Represents the initial, partial [Message] object as passed to the Gallery Screen.
     */
    private val initialMessage: MutableStateFlow<Message?> = MutableStateFlow(null)

    /**
     * Represents the fresh [Message] object loaded from the server.
     */
    private val freshMessage: MutableStateFlow<Message?> = MutableStateFlow(null)

    /**
     * Represents the message that we observe to show the UI data.
     */
    public val message: Message by combine(initialMessage, freshMessage) { initial, fresh ->
        if (initial != null && fresh != null) {
            // Use initial attachments if available (they could be modified locally before fetching the full message)
            fresh.copy(attachments = initial.attachments)
        } else {
            initial ?: fresh ?: Message()
        }
    }.asState(viewModelScope, Message())

    /**
     * Exposes events indicating that the screen should be closed (message deleted or no attachments left).
     */
    private val _closeScreen: MutableSharedFlow<Boolean> = MutableSharedFlow(extraBufferCapacity = 1)
    internal val closeScreen: Flow<Boolean>
        get() = _closeScreen

    /**
     * If we are preparing a file for sharing or not.
     */
    public var isSharingInProgress: Boolean by mutableStateOf(false)

    /**
     * If an attachment needs a prompt to be shared due to a large file size
     * this value will be non-null.
     *
     * You should clear this value once the prompt is removed.
     */
    public var promptedAttachment: Attachment? by mutableStateOf(null)

    /**
     * Represent the header title of the gallery screen.
     */
    public var connectionState: ConnectionState by mutableStateOf(ConnectionState.Offline)
        private set

    /**
     * Shows or hides the media options menu and overlay in the UI.
     */
    public var isShowingOptions: Boolean by mutableStateOf(false)
        private set

    /**
     * Shows or hides the media gallery menu in the UI.
     */
    public var isShowingGallery: Boolean by mutableStateOf(false)
        private set

    /**
     * Loads the message data, which then updates the UI state to show media.
     */
    init {
        viewModelScope.launch {
            fetchMessage()
            observeConnectionStateChanges()
        }
    }

    /**
     * Fetches the message according to the message ID.
     */
    private suspend fun fetchMessage() {
        val result = chatClient.getMessage(messageId).await()

        if (result is Result.Success) {
            this.freshMessage.value = result.value
            hasCompleteMessage = true
        }
    }

    /**
     * Attempts to fetch the message again if it was not
     * successfully fetched the previous time
     */
    private suspend fun observeConnectionStateChanges() {
        clientState.connectionState.collect { connectionState ->
            when (connectionState) {
                is ConnectionState.Connected -> {
                    onConnected()
                    this.connectionState = connectionState
                }

                is ConnectionState.Connecting -> this.connectionState = connectionState
                is ConnectionState.Offline -> this.connectionState = connectionState
            }
        }
    }

    private suspend fun onConnected() {
        if (!hasCompleteMessage) {
            fetchMessage()
        }
    }

    /**
     * Toggles if we're showing the media options menu.
     *
     * @param isShowingOptions If we need to show or hide the options.
     */
    public fun toggleMediaOptions(isShowingOptions: Boolean) {
        this.isShowingOptions = isShowingOptions
    }

    /**
     * Toggles if we're showing the gallery screen.
     *
     * @param isShowingGallery If we need to show or hide the gallery.
     */
    public fun toggleGallery(isShowingGallery: Boolean) {
        this.isShowingGallery = isShowingGallery
    }

    /**
     * Deletes the current media attachment from the message we're observing, if possible.
     *
     * This will in turn update the UI accordingly or finish this screen in case there are no more media attachments
     * to show.
     *
     * @param currentMediaAttachment The media attachment to remove from the message we're updating.
     * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
     * by deleting an attachment contained within it. Set to false by default.
     */
    public fun deleteCurrentMediaAttachment(
        currentMediaAttachment: Attachment,
        skipEnrichUrl: Boolean = this.skipEnrichUrl,
    ) {
        val attachments = message.attachments
        if (message.text.isNotEmpty() || attachments.size > 1) {
            deleteAttachmentFromMessage(currentMediaAttachment, skipEnrichUrl)
        } else if (message.text.isEmpty() && attachments.size == 1) {
            deleteMessage()
        }
    }

    /**
     * Sets the initial message that will be used to display the media gallery preview.
     */
    internal fun setInitialMessage(message: Message) {
        initialMessage.value = message
    }

    private fun deleteAttachmentFromMessage(attachment: Attachment, skipEnrichUrl: Boolean) {
        val initialMessage = initialMessage.value
        val freshMessage = freshMessage.value

        if (initialMessage != null && freshMessage != null) {
            // Update the initial message attachments
            val displayedAttachments = initialMessage.attachments
            val attachmentPosition = displayedAttachments.indexOfFirst { it.assetUrl == attachment.assetUrl }
            removeAttachmentAndUpdate(
                attachmentPosition = attachmentPosition,
                message = initialMessage,
                skipEnrichUrl = skipEnrichUrl,
                updateServer = false,
                update = { newMessage ->
                    this.initialMessage.value = newMessage
                },
            )
            // Update the fresh message attachments (use only the freshMessage to update the server, if we use fields
            // from the initial message, we might override the server message)
            removeAttachmentAndUpdate(
                attachmentPosition = attachmentPosition,
                message = freshMessage,
                skipEnrichUrl = skipEnrichUrl,
                updateServer = true,
                update = { newMessage ->
                    this.freshMessage.value = newMessage
                },
            )
        } else if (initialMessage != null) {
            // Update the initial message attachments
            val attachmentPosition = initialMessage.attachments.indexOfFirst { it.assetUrl == attachment.assetUrl }
            removeAttachmentAndUpdate(
                attachmentPosition = attachmentPosition,
                message = initialMessage,
                skipEnrichUrl = skipEnrichUrl,
                updateServer = false,
                update = { newMessage ->
                    this.initialMessage.value = newMessage
                },
            )
        } else if (freshMessage != null) {
            // Update the fresh message attachments
            val attachmentPosition = freshMessage.attachments.indexOfFirst { it.assetUrl == attachment.assetUrl }
            removeAttachmentAndUpdate(
                attachmentPosition = attachmentPosition,
                message = freshMessage,
                skipEnrichUrl = skipEnrichUrl,
                updateServer = true,
                update = { newMessage ->
                    this.freshMessage.value = newMessage
                },
            )
        }
        if (message.isDeleted() || message.attachments.isEmpty()) {
            // If the message is deleted or has no attachments, close the screen as there is nothing to show.
            _closeScreen.tryEmit(true)
        }
    }

    private fun removeAttachmentAndUpdate(
        attachmentPosition: Int,
        message: Message,
        skipEnrichUrl: Boolean,
        updateServer: Boolean,
        update: (newMessage: Message) -> Unit,
    ) {
        if (attachmentPosition in message.attachments.indices) {
            val updatedAttachments = message.attachments.toMutableList().apply {
                removeAt(attachmentPosition)
            }
            val newMessage = message.copy(
                attachments = updatedAttachments,
                skipEnrichUrl = skipEnrichUrl,
            )
            if (updateServer) {
                chatClient.updateMessage(newMessage).enqueue()
            }
            update(newMessage)
        }
    }

    private fun deleteMessage() {
        chatClient.deleteMessage(message.id).enqueue { result ->
            if (result is Result.Success) {
                freshMessage.value = result.value
                // If the message is deleted, close the screen
                _closeScreen.tryEmit(true)
            }
        }
    }
}
