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

package io.getstream.chat.android.ai.compose.sample.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.ai.compose.sample.data.repository.ChatAiRepository
import io.getstream.chat.android.ai.compose.sample.presentation.chat.ChatViewModel
import io.getstream.chat.android.client.ChatClient

/**
 * Factory for creating ChatViewModel with dependencies.
 *
 * @param chatAiRepository Repository that powers AI requests for the chat experience.
 * @param appContext Application context used to resolve picker URIs into attachments.
 * @param chatClient The Stream Chat client instance. Defaults to [ChatClient.instance] if not provided.
 * @param conversationId The optional channel ID (CID) for an existing conversation.
 * If null, a new conversation will be created when the first message is sent.
 */
class ChatViewModelFactory(
    private val chatAiRepository: ChatAiRepository,
    private val appContext: Context,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val conversationId: String?,
) : ViewModelProvider.Factory {

    /**
     * Creates a ChatViewModel instance.
     *
     * @param modelClass The class of the ViewModel to create
     * @return An instance of ChatViewModel
     * @throws IllegalArgumentException if the ViewModel type is not ChatViewModel
     */
    @Suppress("UNCHECKED_CAST", "OutdatedDocumentation")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ChatViewModel(
            chatClient = chatClient,
            chatAiRepository = chatAiRepository,
            appContext = appContext,
            conversationId = conversationId,
        ) as T
    }
}
