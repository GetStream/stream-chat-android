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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient

/**
 * Holds the dependencies required for the Image Preview Screen.
 * Currently builds the [ImagePreviewViewModel] using those dependencies.
 *
 * @param chatClient The low level chat client used for API calls.
 * @param messageId The ID of the message containing the attachments to be previewed.
 * @param skipEnrichUrl If the message should skip enriching the URL. If URL is not enriched, it will not be
 * displayed as a link attachment. False by default.
 */
public class ImagePreviewViewModelFactory(
    private val chatClient: ChatClient,
    private val messageId: String,
    private val skipEnrichUrl: Boolean = false,
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of [ImagePreviewViewModel] class.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ImagePreviewViewModel(
            chatClient = chatClient,
            messageId = messageId,
            skipEnrichUrl = skipEnrichUrl
        ) as T
    }
}
