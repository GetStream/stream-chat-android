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

package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.ui.sample.feature.chat.info.group.users.GroupChatInfoAddUsersViewModel

class ChatViewModelFactory(private val cid: String) : ViewModelProvider.Factory {
    private val factories: Map<Class<*>, () -> ViewModel> = mapOf(
        ChatViewModel::class.java to { ChatViewModel(cid) },
        GroupChatInfoAddUsersViewModel::class.java to { GroupChatInfoAddUsersViewModel(cid) },
    )

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val viewModel: ViewModel = factories[modelClass]?.invoke()
            ?: throw IllegalArgumentException("ChatViewModelFactory can only create instances of the following classes: ${factories.keys.joinToString { it.simpleName }}")

        @Suppress("UNCHECKED_CAST")
        return viewModel as T
    }
}
