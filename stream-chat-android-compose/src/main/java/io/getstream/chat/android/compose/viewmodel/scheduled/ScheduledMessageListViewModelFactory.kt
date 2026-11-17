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

package io.getstream.chat.android.compose.viewmodel.scheduled

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.common.feature.scheduled.ScheduledMessageListController

/**
 * A ViewModel factory for creating a [ScheduledMessageListViewModel].
 *
 * @param cid The channel of the scheduled messages, in the format `type:id`, or null for the scheduled messages of
 * every channel.
 * @param chatClient The [ChatClient] holding the scheduled messages.
 *
 * @see ScheduledMessageListViewModel
 */
public class ScheduledMessageListViewModelFactory(
    private val cid: String? = null,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ScheduledMessageListViewModel::class.java) {
            "ScheduledMessageListViewModelFactory can only create instances of ScheduledMessageListViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return ScheduledMessageListViewModel(
            controller = ScheduledMessageListController(cid = cid, chatClient = chatClient),
            user = chatClient.clientState.user,
            connectionState = chatClient.clientState.connectionState,
            chatClient = chatClient,
        ) as T
    }
}
