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

package io.getstream.chat.ui.sample.feature.chat.info.group.member

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.utils.Event
import io.getstream.result.Result
import kotlinx.coroutines.launch

class GroupChatInfoMemberOptionsViewModel(
    private val cid: String,
    private val memberId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val _events = MutableLiveData<Event<UiEvent>>()
    private val _state: MediatorLiveData<State> = MediatorLiveData()
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    val events: LiveData<Event<UiEvent>> = _events
    val state: LiveData<State> = _state
    val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    init {
        viewModelScope.launch {
            val currentUser = chatClient.clientState.user.value!!

            val result = chatClient.queryChannels(
                request = QueryChannelsRequest(
                    filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.distinct(listOf(memberId, currentUser.id)),
                    ),
                    querySort = QuerySortByField.descByName("last_updated"),
                    messageLimit = 0,
                    limit = 1,
                )
            ).await()

            val directChannelCid = when (result) {
                is Result.Success -> if (result.value.isNotEmpty()) result.value.first().cid else null
                is Result.Failure -> null
            }

            _state.value = State(directChannelCid = directChannelCid, loading = false)
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.MessageClicked -> handleMessageClicked()
            is Action.RemoveFromChannel -> removeFromChannel(action.username)
        }
    }

    private fun handleMessageClicked() {
        val state = state.value!!
        _events.value = Event(
            if (state.directChannelCid != null) {
                UiEvent.RedirectToChat(state.directChannelCid)
            } else {
                UiEvent.RedirectToChatPreview
            },
        )
    }

    private fun removeFromChannel(username: String) {
        viewModelScope.launch {
            val message = Message(text = "$username has been removed from this channel")
            when (chatClient.channel(cid).removeMembers(listOf(memberId), message).await()) {
                is Result.Success -> _events.value = Event(UiEvent.Dismiss)
                is Result.Failure -> _errorEvents.postValue(Event(ErrorEvent.RemoveMemberError))
            }
        }
    }

    data class State(val directChannelCid: String?, val loading: Boolean)

    sealed class Action {
        object MessageClicked : Action()
        data class RemoveFromChannel(val username: String) : Action()
    }

    sealed class UiEvent {
        object Dismiss : UiEvent()
        data class RedirectToChat(val cid: String) : UiEvent()
        object RedirectToChatPreview : UiEvent()
    }

    sealed class ErrorEvent {
        object RemoveMemberError : ErrorEvent()
    }
}

class GroupChatInfoMemberOptionsViewModelFactory(private val cid: String, private val memberId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == GroupChatInfoMemberOptionsViewModel::class.java) {
            "GroupChatInfoMemberOptionsViewModelFactory can only create instances of GroupChatInfoMemberOptionsViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return GroupChatInfoMemberOptionsViewModel(cid, memberId) as T
    }
}
