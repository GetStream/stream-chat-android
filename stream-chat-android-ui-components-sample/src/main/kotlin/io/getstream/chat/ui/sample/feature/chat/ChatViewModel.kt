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

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.ui.sample.util.extensions.isAnonymousChannel
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ChatViewModel(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val logger by taggedLogger("Chat:ChannelVM")

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: StateFlow<ChannelState?> = observeChannelState()

    private val _navigationEvent: MutableLiveData<Event<NavigationEvent>> = MutableLiveData()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    val members: LiveData<List<Member>> = channelState.filterNotNull().flatMapLatest { it.members }.asLiveData()

    private fun observeChannelState(): StateFlow<ChannelState?> {
        val messageLimit = 0
        logger.d { "[observeChannelState] cid: $cid, messageLimit: $messageLimit" }
        return chatClient.watchChannelAsState(cid, messageLimit, viewModelScope)
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.HeaderClicked -> {
                val channelData = requireNotNull(channelState.value?.channelData?.value)
                _navigationEvent.value = Event(
                    if (action.members.size > 2 || !channelData.isAnonymousChannel()) {
                        NavigationEvent.NavigateToGroupChatInfo(cid)
                    } else {
                        NavigationEvent.NavigateToChatInfo(cid)
                    },
                )
            }
        }
    }

    sealed class Action {
        class HeaderClicked(val members: List<Member>) : Action()
    }

    sealed class NavigationEvent {
        abstract val cid: String

        data class NavigateToChatInfo(override val cid: String) : NavigationEvent()
        data class NavigateToGroupChatInfo(override val cid: String) : NavigationEvent()
    }
}
