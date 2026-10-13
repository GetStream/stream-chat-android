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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.ai.compose.sample.presentation.conversations.ConversationListViewModel
import io.getstream.chat.android.client.ChatClient

/**
 * Factory for creating ConversationListViewModel with dependencies.
 * This factory injects required dependencies (ChatClient) into ConversationListViewModel.
 *
 * @param chatClient The Stream Chat client instance
 */
public class ConversationListViewModelFactory(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {
    /**
     * Creates a ConversationListViewModel instance.
     *
     * @param modelClass The class of the ViewModel to create
     * @return An instance of ConversationListViewModel
     * @throws IllegalArgumentException if the ViewModel type is not ConversationListViewModel
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ConversationListViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ConversationListViewModel(
            chatClient = chatClient,
        ) as T
    }
}
