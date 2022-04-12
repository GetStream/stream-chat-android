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

package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.ui.common.extensions.isCurrentUserOwnerOrAdmin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class ChatInfoViewModel(
    private val cid: String?,
    userData: UserData?,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val globalState: GlobalState = chatClient.globalState,
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: Flow<ChannelState> =
        chatClient.watchChannelAsState(cid ?: "", DEFAULT_MESSAGE_LIMIT, viewModelScope).filterNotNull()

    private lateinit var channelClient: ChannelClient
    private val _state = MediatorLiveData<State>()
    private val _channelDeletedState = MutableLiveData(false)
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    val state: LiveData<State> = _state
    val channelDeletedState: LiveData<Boolean> = _channelDeletedState
    val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    init {
        if (cid != null) {
            channelClient = chatClient.channel(cid)
            _state.value = State()
            viewModelScope.launch {
                // Update channel mute status
                globalState.user.value?.channelMutes?.let(::updateChannelMuteStatus)

                _state.addSource(channelState.flatMapLatest { it.members }.asLiveData()) { memberList ->
                    // Updates only if the user state is already set
                    _state.value = _state.value!!.copy(canDeleteChannel = memberList.isCurrentUserOwnerOrAdmin())
                    memberList.find { member -> member.user.id == _state.value?.member?.user?.id }?.let { member ->
                        _state.value = _state.value?.copy(member = member)
                    }
                }
                // Currently, we don't receive any event when channel member is banned/shadow banned, so
                // we need to get member data from the server
                val result =
                    channelClient.queryMembers(
                        offset = 0,
                        limit = 1,
                        filter = globalState.user.value?.id?.let { Filters.ne("id", it) } ?: Filters.neutral(),
                        sort = QuerySort()
                    ).await()

                if (result.isSuccess) {
                    val member = result.data().firstOrNull()
                    // Update member, member block status, and channel notifications
                    _state.value = _state.value!!.copy(
                        member = member,
                        isMemberBlocked = member?.shadowBanned ?: false,
                        loading = false,
                    )
                } else {
                    // TODO: Handle error
                    _state.value = _state.value!!.copy(loading = false)
                }
            }
        } else {
            _state.value =
                State(
                    member = Member(user = userData!!.toUser()),
                    canDeleteChannel = false,
                    channelExists = false,
                    loading = false,
                )
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.OptionMuteDistinctChannelClicked -> switchChannelMute(action.isEnabled)
            is Action.OptionBlockUserClicked -> switchUserBlock(action.isEnabled)
            is Action.ChannelMutesUpdated -> updateChannelMuteStatus(action.channelMutes)
            is Action.ChannelDeleted -> deleteChannel()
        }
    }

    private fun updateChannelMuteStatus(channelMutes: List<ChannelMute>) {
        _state.value = _state.value!!.copy(channelMuted = channelMutes.any { it.channel.cid == cid })
    }

    private fun switchChannelMute(isEnabled: Boolean) {
        viewModelScope.launch {
            val result = if (isEnabled) {
                channelClient.mute().await()
            } else {
                channelClient.unmute().await()
            }
            if (result.isError) {
                _errorEvents.postValue(Event(ErrorEvent.MuteChannelError))
            }
        }
    }

    private fun switchUserBlock(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentState = _state.value!!
            if (currentState.member == null) {
                return@launch
            }
            val result = if (isEnabled) {
                channelClient.shadowBanUser(
                    targetId = currentState.member.getUserId(),
                    reason = null,
                    timeout = null
                ).await()
            } else {
                channelClient.removeShadowBan(currentState.member.getUserId()).await()
            }
            if (result.isError) {
                _errorEvents.postValue(Event(ErrorEvent.BlockUserError))
            }
        }
    }

    /**
     * Deletes the current channel.
     */
    private fun deleteChannel() {
        val cid = requireNotNull(cid)
        viewModelScope.launch {
            val result = chatClient.channel(cid).delete().await()
            if (result.isSuccess) {
                _channelDeletedState.value = true
            } else {
                _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError))
            }
        }
    }

    data class State(
        val member: Member? = null,
        val channelMuted: Boolean = false,
        val isMemberBlocked: Boolean = false,
        val canDeleteChannel: Boolean = false,
        val channelExists: Boolean = true,
        val loading: Boolean = true,
    )

    sealed class Action {
        data class OptionMuteDistinctChannelClicked(val isEnabled: Boolean) : Action()
        data class OptionBlockUserClicked(val isEnabled: Boolean) : Action()
        data class ChannelMutesUpdated(val channelMutes: List<ChannelMute>) : Action()
        object ChannelDeleted : Action()
    }

    sealed class ErrorEvent {
        object MuteChannelError : ErrorEvent()
        object BlockUserError : ErrorEvent()
        object DeleteChannelError : ErrorEvent()
    }

    private companion object {

        /**
         * The default limit for messages count in requests.
         */
        private const val DEFAULT_MESSAGE_LIMIT: Int = 30
    }
}

class ChatInfoViewModelFactory(private val cid: String?, private val userData: UserData?) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(cid != null || userData != null) {
            "Either cid or userData should not be null"
        }
        require(modelClass == ChatInfoViewModel::class.java) {
            "ChatInfoViewModelFactory can only create instances of ChatInfoViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChatInfoViewModel(cid, userData) as T
    }
}
