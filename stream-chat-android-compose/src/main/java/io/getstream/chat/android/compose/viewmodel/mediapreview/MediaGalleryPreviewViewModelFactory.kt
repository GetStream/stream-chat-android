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

package io.getstream.chat.android.compose.viewmodel.mediapreview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState

/**
 * Holds the dependencies required for the Media Preview Screen.
 * Currently builds the [MediaGalleryPreviewViewModel] using those dependencies.
 *
 * @param chatClient An instance of the low level client
 * used for basic chat API functionality.
 * @param clientState Holds information about the current SDK state.
 * @param messageId The ID of the message we are fetching in order
 * to display the attachments.
 * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
 * by deleting an attachment contained within it. Set to false by default.
 */
public class MediaGalleryPreviewViewModelFactory(
    private val chatClient: ChatClient,
    private val clientState: ClientState = chatClient.clientState,
    private val messageId: String,
    private val skipEnrichUrl: Boolean = false,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [MediaGalleryPreviewViewModel] class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MediaGalleryPreviewViewModel(
            chatClient = chatClient,
            clientState = clientState,
            messageId = messageId,
            skipEnrichUrl = skipEnrichUrl,
        ) as T
    }
}
