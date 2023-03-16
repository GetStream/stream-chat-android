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

@file:Suppress("PropertyName")

package io.getstream.chat.android.state.plugin.state.global.internal

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@InternalStreamChatApi
public object MutableGlobalStateInstance : MutableGlobalState {

    private val _totalUnreadCount = MutableStateFlow(0)
    private val _channelUnreadCount = MutableStateFlow(0)
    private val _banned = MutableStateFlow(false)

    private val _mutedUsers = MutableStateFlow<List<Mute>>(emptyList())
    private val _channelMutes = MutableStateFlow<List<ChannelMute>>(emptyList())
    private val _typingChannels = MutableStateFlow(emptyMap<String, TypingEvent>())

    private val _user = MutableStateFlow<User?>(null)

    override val totalUnreadCount: StateFlow<Int> = _totalUnreadCount

    override val channelUnreadCount: StateFlow<Int> = _channelUnreadCount

    override val muted: StateFlow<List<Mute>> = _mutedUsers

    override val channelMutes: StateFlow<List<ChannelMute>> = _channelMutes

    override val banned: StateFlow<Boolean> = _banned

    override val typingChannels: StateFlow<Map<String, TypingEvent>> = _typingChannels

    override val user: StateFlow<User?> = _user

    override fun clearState() {
        _user.value = null
        _totalUnreadCount.value = 0
        _channelUnreadCount.value = 0
        _mutedUsers.value = emptyList()
        _channelMutes.value = emptyList()
        _banned.value = false
        _typingChannels.value = emptyMap()
    }

    override fun setUser(user: User) {
        _user.value = user
    }

    override fun setTotalUnreadCount(totalUnreadCount: Int) {
        _totalUnreadCount.value = totalUnreadCount
    }

    override fun setChannelUnreadCount(channelUnreadCount: Int) {
        _channelUnreadCount.value = channelUnreadCount
    }

    override fun setBanned(banned: Boolean) {
        _banned.value = banned
    }

    override fun setChannelMutes(channelMutes: List<ChannelMute>) {
        _channelMutes.value = channelMutes
    }

    override fun setMutedUsers(mutedUsers: List<Mute>) {
        _mutedUsers.value = mutedUsers
    }

    override fun tryEmitTypingEvent(cid: String, typingEvent: TypingEvent) {
        val typingChannelsCopy = _typingChannels.value.toMutableMap()

        if (typingEvent.users.isEmpty()) {
            typingChannelsCopy.remove(cid)
        } else {
            typingChannelsCopy[cid] = typingEvent
        }
        _typingChannels.tryEmit(typingChannelsCopy)
    }
}
