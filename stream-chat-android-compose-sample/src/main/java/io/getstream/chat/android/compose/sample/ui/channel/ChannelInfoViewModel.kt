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
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel holding the state and managing the business logic related to 1-to-1 channel info.
 *
 * @param cid The full channel identifier (Ex. "messaging:123").
 * @param chatClient The initialized [ChatClient] instance.
 * @param channelClient The [ChannelClient] for the given channel.
 * @param clientState The current state of Chat client.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChannelInfoViewModel(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val channelClient: ChannelClient = chatClient.channel(cid),
    private val clientState: ClientState = chatClient.clientState,
) : ViewModel() {

    /**
     * Represents the render-able state of the 'Channel Info' screen.
     *
     * @param member The member (not self) of the channel.
     * @param canDeleteChannel Indicator if the currently logged user can delete the channel.
     */
    data class State(
        val member: Member? = null,
        val canDeleteChannel: Boolean = false,
    )

    /**
     * Defines the possible errors that can occur during operations with the channel.
     */
    sealed interface ErrorEvent {
        /**
         * Error which occurred during the loading of the channel details.
         */
        data object LoadingError : ErrorEvent

        /**
         * Error which occurred during the deletion of the channel.
         */
        data object DeleteError : ErrorEvent
    }

    /**
     * Exposes the current UI state.
     */
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State>
        get() = _state

    /**
     * Emits one-shot error events.
     */
    private val _error = MutableSharedFlow<ErrorEvent>()
    val error: SharedFlow<ErrorEvent>
        get() = _error

    /**
     * Exposes event signaling that the channel was deleted.
     */
    private val _channelDeleted = MutableSharedFlow<Unit>()
    val channelDeleted: SharedFlow<Unit>
        get() = _channelDeleted

    init {
        subscribeForMemberUpdates()
        subscribeForChannelDataUpdates()
        loadChannelMember()
    }

    /**
     * Called when the user confirm the deletion of the channel.
     */
    fun onDeleteChannel() {
        viewModelScope.launch {
            val result = channelClient.delete().await()
            when (result) {
                is Result.Success -> {
                    _channelDeleted.emit(Unit)
                }

                is Result.Failure -> {
                    _error.emit(ErrorEvent.DeleteError)
                }
            }
        }
    }

    private fun subscribeForMemberUpdates() {
        viewModelScope.launch {
            chatClient
                .watchChannelAsState(cid, 0, viewModelScope)
                .filterNotNull()
                .flatMapLatest { it.members }
                .collectLatest { members ->
                    // Updates only if the user state is already set
                    val member = members
                        .find { member -> member.user.id == _state.value.member?.user?.id }
                    if (member != null) {
                        _state.update { it.copy(member = member) }
                    }
                }
        }
    }

    private fun subscribeForChannelDataUpdates() {
        viewModelScope.launch {
            chatClient
                .watchChannelAsState(cid, 0, viewModelScope)
                .filterNotNull()
                .flatMapLatest { it.channelData }
                .collectLatest { channelData ->
                    _state.update {
                        it.copy(
                            canDeleteChannel = channelData.ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL),
                        )
                    }
                }
        }
    }

    private fun loadChannelMember() {
        viewModelScope.launch {
            // Currently, we don't receive any event when channel member is banned/shadow banned, so
            // we need to get member data from the server
            val result = channelClient.queryMembers(
                offset = 0,
                limit = 1,
                filter = clientState.user.value?.id?.let { Filters.ne("id", it) } ?: Filters.neutral(),
                sort = QuerySortByField(),
            ).await()
            when (result) {
                is Result.Success -> {
                    val member = result.value.firstOrNull()
                    _state.update {
                        it.copy(member = member)
                    }
                }

                is Result.Failure -> {
                    _error.emit(ErrorEvent.LoadingError)
                }
            }
        }
    }
}

/**
 * ViewModel Factory for instantiating [ChannelInfoViewModel].
 *
 * @param cid The full channel identifier (Ex. "messaging:123").
 */
class ChannelInfoViewModelFactory(private val cid: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChannelInfoViewModel::class.java) {
            "ChannelInfoViewModelFactory can only create instances of ChannelInfoViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChannelInfoViewModel(cid) as T
    }
}
