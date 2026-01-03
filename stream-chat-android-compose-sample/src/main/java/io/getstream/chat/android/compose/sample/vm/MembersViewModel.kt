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

package io.getstream.chat.android.compose.sample.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class MembersViewModel(
    private val cid: String,
    private val chatClient: ChatClient,
) : ViewModel() {

    private val logger by taggedLogger("Chat:MembersVM")

    private val _memberNames: MutableStateFlow<List<MemberName>> = MutableStateFlow(emptyList())
    val memberNames: StateFlow<List<MemberName>> = _memberNames

    init {
        watchChannel()
    }

    private fun watchChannel() {
        chatClient.watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = viewModelScope)
            .filterNotNull()
            .flatMapLatest { state ->
                combine(
                    state.channelData,
                    state.membersCount,
                    state.watcherCount,
                ) { _, _, _ ->
                    state.toChannel()
                }
            }
            .distinctUntilChanged()
            .onEach(this::updateMembers)
            .launchIn(viewModelScope)
    }

    private fun updateMembers(channel: Channel) {
        val currentUserId = chatClient.getCurrentUser()?.id
        val names = channel.members.map {
            val isCurrentUser = currentUserId == it.user.id
            MemberName(
                name = if (isCurrentUser) "You" else it.user.name,
                isCurrentUser = isCurrentUser,
            )
        }
        logger.d { "[updateMembers] names: ${names.map { it.name }}" }
        _memberNames.value = names
    }
}

internal data class MemberName(val name: String, val isCurrentUser: Boolean)

internal class MembersViewModelFactory(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MembersViewModel(cid, chatClient) as T
    }
}
