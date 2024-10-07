/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel holding the state and managing the business logic related to group channel info.
 *
 * @param cid The full channel identifier (Ex. "messaging:123").
 * @param chatClient The initialized [ChannelClient] instance.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupChannelInfoViewModel(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Represents the render-able state of the 'Group Channel Info' screen.
     *
     * @param members The list of [Member]s in the channel.
     */
    data class State(
        val members: List<Member>,
        val createdBy: User = User(),
    )

    private val _state = MutableStateFlow(State(members = emptyList()))
    val state: StateFlow<State>
        get() = _state

    init {
        viewModelScope.launch {
            chatClient
                .watchChannelAsState(cid, 0, viewModelScope)
                .filterNotNull()
                .flatMapLatest { it.members }
                .collectLatest { members ->
                    _state.update { it.copy(members = members) }
                }
        }
        viewModelScope.launch {
            chatClient
                .watchChannelAsState(cid, 0, viewModelScope)
                .filterNotNull()
                .flatMapLatest { it.channelData }
                .collectLatest { channelData ->
                    _state.update { it.copy(createdBy = channelData.createdBy) }
                }
        }
    }
}

/**
 * ViewModel Factory for instantiating [GroupChannelInfoViewModel].
 *
 * @param cid The full channel identifier (Ex. "messaging:123").
 */
class GroupChannelInfoViewModelFactory(private val cid: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == GroupChannelInfoViewModel::class.java) {
            "ChannelInfoViewModelFactory can only create instances of ChannelInfoViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return GroupChannelInfoViewModel(cid) as T
    }
}
