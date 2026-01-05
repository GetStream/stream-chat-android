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

package io.getstream.chat.ui.sample.feature.chat.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.ui.sample.common.CHANNEL_ARG_DRAFT
import io.getstream.result.Result
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class ChatPreviewViewModel(
    private val memberId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
) : ViewModel() {

    private var cid: String? = null
    private val _state: MutableLiveData<State> = MutableLiveData()
    private val _events: MutableLiveData<Event<UiEvent>> = MutableLiveData()
    val state: LiveData<State> = _state
    val events = _events

    init {
        _state.value = State(cid = null)
        viewModelScope.launch {
            clientState.user.filterNotNull().collect { user ->
                val result = chatClient.createChannel(
                    channelType = "messaging",
                    channelId = "",
                    memberIds = listOf(memberId, user.id),
                    extraData = mapOf(CHANNEL_ARG_DRAFT to true),
                ).await()

                if (result is Result.Success) {
                    cid = result.value.cid
                    _state.value = State(cid!!)
                }
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.MessageSent -> updateChannel()
        }
    }

    private fun updateChannel() {
        val cid = requireNotNull(cid)
        viewModelScope.launch {
            val result =
                chatClient.channel(cid).update(message = null, extraData = mapOf(CHANNEL_ARG_DRAFT to false)).await()
            if (result is Result.Success) {
                _events.value = Event(UiEvent.NavigateToChat(cid))
            }
        }
    }

    data class State(val cid: String?)

    sealed class Action {
        object MessageSent : Action()
    }

    sealed class UiEvent {
        data class NavigateToChat(val cid: String) : UiEvent()
    }
}

class ChatPreviewViewModelFactory(private val memberId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChatPreviewViewModel::class.java) {
            "ChatPreviewViewModelFactory can only create instances of ChatPreviewViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChatPreviewViewModel(memberId) as T
    }
}
