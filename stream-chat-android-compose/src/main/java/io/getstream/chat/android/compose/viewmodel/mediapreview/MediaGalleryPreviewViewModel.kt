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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.globalState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * A ViewModel capable of loading images, playing videos.
 */
public class MediaGalleryPreviewViewModel(
    private val chatClient: ChatClient,
    private val clientState: ClientState,
    private val messageId: String,
) : ViewModel() {

    /**
     * The currently logged in user.
     */
    public val user: StateFlow<User?> = chatClient.globalState.user

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
     * Represents the message that we observe to show the UI data.
     */
    public var message: Message by mutableStateOf(Message())
        internal set

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
    public var connectionState: ConnectionState by mutableStateOf(ConnectionState.OFFLINE)
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
            this.message = result.value
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
                ConnectionState.CONNECTED -> {
                    onConnected()
                    this.connectionState = connectionState
                }
                ConnectionState.CONNECTING -> this.connectionState = connectionState
                ConnectionState.OFFLINE -> this.connectionState = connectionState
            }
        }
    }

    private suspend fun onConnected() {
        if (message.id.isEmpty() || !hasCompleteMessage) {
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
     * @param currentMediaAttachment The image attachment to remove from the message we're updating.
     */
    public fun deleteCurrentMediaAttachment(currentMediaAttachment: Attachment) {
        val attachments = message.attachments
        val numberOfAttachments = attachments.size

        if (message.text.isNotEmpty() || numberOfAttachments > 1) {
            val message = message

            attachments.removeAll {
                it.url == currentMediaAttachment.url
            }

            chatClient.updateMessage(message).enqueue()
        } else if (message.text.isEmpty() && numberOfAttachments == 1) {
            chatClient.deleteMessage(message.id).enqueue { result ->
                if (result is Result.Success) {
                    message = result.value
                }
            }
        }
    }
}
