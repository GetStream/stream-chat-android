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

package io.getstream.chat.android.compose.viewmodel.imagepreview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for loading and showing the images of a selected message.
 *
 *
 * @param chatClient The low level chat client used for API calls.
 * @param messageId The ID of the message containing the attachments to be previewed.
 * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 */
public class ImagePreviewViewModel(
    private val chatClient: ChatClient,
    messageId: String,
    private val skipEnrichUrl: Boolean = false,

) : ViewModel() {

    /**
     * The currently logged in user.
     */
    public val user: StateFlow<User?> = chatClient.globalState.user

    /**
     * Represents the message that we observe to show the UI data.
     */
    public var message: Message by mutableStateOf(Message())
        private set

    /**
     * Shows or hides the image options menu and overlay in the UI.
     */
    public var isShowingOptions: Boolean by mutableStateOf(false)
        private set

    /**
     * Shows or hides the image gallery menu in the UI.
     */
    public var isShowingGallery: Boolean by mutableStateOf(false)
        private set

    /**
     * Loads the message data, which then updates the UI state to shows images.
     */
    init {
        chatClient.getMessage(messageId).enqueue { result ->
            if (result is Result.Success) {
                this.message = result.value
            }
        }
    }

    /**
     * Toggles if we're showing the image options menu.
     *
     * @param isShowingOptions If we need to show or hide the options.
     */
    public fun toggleImageOptions(isShowingOptions: Boolean) {
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
     * Deletes the current image from the message we're observing, if possible.
     *
     * This will in turn update the UI accordingly or finish this screen in case there are no more images to show.
     *
     * @param currentImage The image attachment to remove from the message we're updating.
     * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
     * by deleting an attachment contained within it. Set to false by default.
     */
    public fun deleteCurrentImage(
        currentImage: Attachment,
        skipEnrichUrl: Boolean = this.skipEnrichUrl,
    ) {
        val attachments = message.attachments
        val numberOfAttachments = attachments.size

        if (message.text.isNotEmpty() || numberOfAttachments > 1) {
            val imageUrl = currentImage.assetUrl ?: currentImage.imageUrl
            val message = message

            attachments.removeAll {
                it.assetUrl == imageUrl || it.imageUrl == imageUrl
            }

            chatClient.updateMessage(
                message = message
                    .apply {
                        this.skipEnrichUrl = skipEnrichUrl
                    }
            ).enqueue()
        } else if (message.text.isEmpty() && numberOfAttachments == 1) {
            chatClient.deleteMessage(message.id).enqueue { result ->
                if (result is Result.Success) {
                    message = result.value
                }
            }
        }
    }
}
