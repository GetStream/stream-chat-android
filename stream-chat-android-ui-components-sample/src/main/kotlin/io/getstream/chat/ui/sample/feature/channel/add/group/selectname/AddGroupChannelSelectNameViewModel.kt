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

package io.getstream.chat.ui.sample.feature.channel.add.group.selectname

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import io.getstream.chat.android.client.api.state.Event as EventWrapper

class AddGroupChannelSelectNameViewModel : ViewModel() {

    private val _state: MutableLiveData<State> = MutableLiveData()
    private val _errorEvents: MutableLiveData<EventWrapper<ErrorEvent>> = MutableLiveData()
    val state: LiveData<State> = _state
    val errorEvents: LiveData<EventWrapper<ErrorEvent>> = _errorEvents

    fun onEvent(event: Event) {
        when (event) {
            is Event.CreateChannel -> createChannel(event.name, event.members)
        }
    }

    private fun createChannel(name: String, members: List<User>) {
        _state.value = State.Loading
        viewModelScope.launch(Dispatchers.Main) {
            val currentUserId =
                ChatClient.instance().clientState.user.value?.id ?: error("User must be set before create new channel!")
            val params = CreateChannelParams(
                members = (members.map(User::id) + currentUserId).map(::MemberData),
                extraData = mapOf(EXTRA_DATA_CHANNEL_NAME to name),
            )
            val result = ChatClient.instance()
                .createChannel(
                    channelType = CHANNEL_TYPE_MESSAGING,
                    channelId = UUID.randomUUID().toString(),
                    params = params,
                ).await()
            when (result) {
                is Result.Success -> _state.value = State.NavigateToChannel(result.value.cid)
                is Result.Failure -> _errorEvents.postValue(EventWrapper(ErrorEvent.CreateChannelError))
            }
        }
    }

    companion object {
        private const val CHANNEL_TYPE_MESSAGING = "messaging"
        private const val EXTRA_DATA_CHANNEL_NAME = "name"
    }

    sealed class State {
        object Loading : State()
        data class NavigateToChannel(val cid: String) : State()
    }

    sealed class Event {
        data class CreateChannel(val name: String, val members: List<User>) : Event()
    }

    sealed class ErrorEvent {
        object CreateChannelError : ErrorEvent()
    }
}
