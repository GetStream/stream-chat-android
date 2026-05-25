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

package io.getstream.chat.android.ui.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.api.state.state
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

/**
 * Observes the live [Poll] hosted by the message identified by [messageId] inside the channel
 * identified by [cid].
 */
internal class PollCommentsViewModel(
    cid: String,
    messageId: String,
    state: StateRegistry = ChatClient.instance().state,
) : ViewModel() {

    val poll: LiveData<Poll> = run {
        val (channelType, channelId) = cid.cidToTypeAndId()
        state.channel(channelType, channelId).messages
            .map { messages -> messages.find { it.id == messageId }?.poll }
            .filterNotNull()
            .distinctUntilChanged()
            .asLiveData()
    }

    class Factory(
        private val cid: String,
        private val messageId: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == PollCommentsViewModel::class.java) {
                "Factory can only create instances of PollCommentsViewModel"
            }
            return PollCommentsViewModel(cid = cid, messageId = messageId) as T
        }
    }
}
