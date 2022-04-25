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
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.utils.Event
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
            val currentUser = chatClient.getCurrentUser()!!

            val result = chatClient.queryChannels(
                request = QueryChannelsRequest(
                    filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.distinct(listOf(memberId, currentUser.id)),
                    ),
                    querySort = QuerySort.desc(Channel::lastUpdated),
                    messageLimit = 0,
                    limit = 1,
                )
            ).await()

            _state.value = if (result.isSuccess && result.data().isNotEmpty()) {
                State(directChannelCid = result.data().first().cid, loading = false)
            } else {
                State(directChannelCid = null, loading = false)
            }
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
            val result = chatClient.channel(cid).removeMembers(listOf(memberId), message).await()
            if (result.isSuccess) {
                _events.value = Event(UiEvent.Dismiss)
            } else {
                _errorEvents.postValue(Event(ErrorEvent.RemoveMemberError))
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
