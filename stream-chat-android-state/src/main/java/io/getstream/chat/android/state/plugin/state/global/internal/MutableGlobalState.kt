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

package io.getstream.chat.android.state.plugin.state.global.internal

import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Mutable global state of [StatePlugin].
 */
internal class MutableGlobalState : GlobalState {

    private var _totalUnreadCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _channelUnreadCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _unreadThreadsCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _banned: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _mutedUsers: MutableStateFlow<List<Mute>>? = MutableStateFlow(emptyList())
    private var _channelMutes: MutableStateFlow<List<ChannelMute>>? = MutableStateFlow(emptyList())
    private var _blockedUsersIds: MutableStateFlow<List<String>>? = MutableStateFlow(emptyList())
    private var _typingChannels: MutableStateFlow<Map<String, TypingEvent>>? = MutableStateFlow(emptyMap())
    private var _channelDraftMessages: MutableStateFlow<Map<String, DraftMessage>>? = MutableStateFlow(emptyMap())
    private var _threadDraftMessages: MutableStateFlow<Map<String, DraftMessage>>? = MutableStateFlow(emptyMap())

    override val totalUnreadCount: StateFlow<Int> = _totalUnreadCount!!
    override val channelUnreadCount: StateFlow<Int> = _channelUnreadCount!!
    override val unreadThreadsCount: StateFlow<Int> = _unreadThreadsCount!!
    override val muted: StateFlow<List<Mute>> = _mutedUsers!!
    override val channelMutes: StateFlow<List<ChannelMute>> = _channelMutes!!
    override val blockedUserIds: StateFlow<List<String>> = _blockedUsersIds!!
    override val banned: StateFlow<Boolean> = _banned!!
    override val typingChannels: StateFlow<Map<String, TypingEvent>> = _typingChannels!!
    override val channelDraftMessages: StateFlow<Map<String, DraftMessage>> = _channelDraftMessages!!
    override val threadDraftMessages: StateFlow<Map<String, DraftMessage>> = _threadDraftMessages!!

    /**
     * Destroys the state.
     */
    fun destroy() {
        _totalUnreadCount = null
        _channelUnreadCount = null
        _unreadThreadsCount = null
        _mutedUsers = null
        _channelMutes = null
        _blockedUsersIds = null
        _banned = null
        _typingChannels = null
        _channelDraftMessages = null
        _threadDraftMessages = null
    }

    fun setTotalUnreadCount(totalUnreadCount: Int) {
        _totalUnreadCount?.value = totalUnreadCount
    }

    fun setChannelUnreadCount(channelUnreadCount: Int) {
        _channelUnreadCount?.value = channelUnreadCount
    }

    fun setUnreadThreadsCount(unreadThreadsCount: Int) {
        _unreadThreadsCount?.value = unreadThreadsCount
    }

    fun setBanned(banned: Boolean) {
        _banned?.value = banned
    }

    fun setChannelMutes(channelMutes: List<ChannelMute>) {
        _channelMutes?.value = channelMutes
    }

    /**
     * Updates the current list of blocked users.
     *
     * @param blockedUserIds The new list of block user ids.
     */
    fun setBlockedUserIds(blockedUserIds: List<String>) {
        _blockedUsersIds?.value = blockedUserIds
    }

    fun setMutedUsers(mutedUsers: List<Mute>) {
        _mutedUsers?.value = mutedUsers
    }

    fun updateDraftMessage(draftMessage: DraftMessage) {
        println("JcLog: updateDraftMessage: $draftMessage")
        draftMessage.parentId?.let { parentId ->
            _threadDraftMessages?.let { it.value += (parentId to draftMessage) }
        }
        _channelDraftMessages
            ?.takeUnless { draftMessage.parentId != null }
            ?.let { it.value += (draftMessage.cid to draftMessage) }
    }

    fun removeDraftMessage(draftMessage: DraftMessage) {
        draftMessage.parentId?.let { parentId ->
            _threadDraftMessages?.let { it.value -= parentId }
        }
        _channelDraftMessages
            ?.takeUnless { draftMessage.parentId != null }
            ?.let { it.value -= draftMessage.cid }
    }

    /**
     * Tries emit typing event for a particular channel.
     *
     * @param cid The full channel id, i.e. "messaging:123" to which the message with reaction belongs.
     * @param typingEvent [TypingEvent] with information about typing users. Current user is excluded.
     */
    fun tryEmitTypingEvent(cid: String, typingEvent: TypingEvent) {
        _typingChannels?.let {
            it.tryEmit(
                it.value.toMutableMap().apply {
                    if (typingEvent.users.isEmpty()) {
                        remove(cid)
                    } else {
                        this[cid] = typingEvent
                    }
                },
            )
        }
    }
}
