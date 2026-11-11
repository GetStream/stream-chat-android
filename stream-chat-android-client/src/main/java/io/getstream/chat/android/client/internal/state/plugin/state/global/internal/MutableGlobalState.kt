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

package io.getstream.chat.android.client.internal.state.plugin.state.global.internal

import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.internal.state.utils.internal.mapState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.UserId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import java.util.Collections
import java.util.Date
import java.util.concurrent.ConcurrentHashMap

/**
 * Mutable global state of [StatePlugin].
 */
@Suppress("TooManyFunctions")
internal class MutableGlobalState(
    private val userId: UserId,
    private val now: () -> Long = System::currentTimeMillis,
) : GlobalState {

    private var _totalUnreadCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _channelUnreadCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _groupedUnreadChannels: MutableStateFlow<Map<String, Int>>? = MutableStateFlow(emptyMap())
    private var _unreadThreadsCount: MutableStateFlow<Int>? = MutableStateFlow(0)
    private var _banned: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _mutedUsers: MutableStateFlow<List<Mute>>? = MutableStateFlow(emptyList())
    private var _channelMutes: MutableStateFlow<List<ChannelMute>>? = MutableStateFlow(emptyList())
    private var _blockedUsersIds: MutableStateFlow<List<String>>? = MutableStateFlow(emptyList())
    private var _typingChannels: MutableStateFlow<Map<String, TypingEvent>>? = MutableStateFlow(emptyMap())
    private var _channelDraftMessages: MutableStateFlow<Map<String, DraftMessage>>? = MutableStateFlow(emptyMap())
    private var _threadDraftMessages: MutableStateFlow<Map<String, DraftMessage>>? = MutableStateFlow(emptyMap())
    private var _activeLiveLocations: MutableStateFlow<List<Location>> = MutableStateFlow(emptyList())

    /** End time of each live location ended by [endLiveLocation], by message id. */
    private val locallyEndedLiveLocations = ConcurrentHashMap<String, Date>()

    /** Channels and threads whose draft has a local change that has not reached the server yet. */
    private val pendingDrafts: MutableSet<String> = Collections.newSetFromMap(ConcurrentHashMap())

    override val totalUnreadCount: StateFlow<Int> = _totalUnreadCount!!
    override val channelUnreadCount: StateFlow<Int> = _channelUnreadCount!!
    override val groupedUnreadChannels: StateFlow<Map<String, Int>> = _groupedUnreadChannels!!
    override val unreadThreadsCount: StateFlow<Int> = _unreadThreadsCount!!
    override val muted: StateFlow<List<Mute>> = _mutedUsers!!
    override val channelMutes: StateFlow<List<ChannelMute>> = _channelMutes!!
    override val blockedUserIds: StateFlow<List<String>> = _blockedUsersIds!!
    override val banned: StateFlow<Boolean> = _banned!!
    override val typingChannels: StateFlow<Map<String, TypingEvent>> = _typingChannels!!
    override val channelDraftMessages: StateFlow<Map<String, DraftMessage>> = _channelDraftMessages!!
    override val threadDraftMessages: StateFlow<Map<String, DraftMessage>> = _threadDraftMessages!!
    override val activeLiveLocations: StateFlow<List<Location>> = _activeLiveLocations
    override val currentUserActiveLiveLocations: StateFlow<List<Location>> = activeLiveLocations.mapState {
        it.filter { location -> location.userId == userId }
    }

    /**
     * Destroys the state.
     */
    fun destroy() {
        _totalUnreadCount = null
        _channelUnreadCount = null
        _groupedUnreadChannels = null
        _unreadThreadsCount = null
        _mutedUsers = null
        _channelMutes = null
        _blockedUsersIds = null
        _banned = null
        _typingChannels = null
        _channelDraftMessages = null
        _threadDraftMessages = null
        _activeLiveLocations.value = emptyList()
        locallyEndedLiveLocations.clear()
        pendingDrafts.clear()
    }

    fun setTotalUnreadCount(totalUnreadCount: Int) {
        _totalUnreadCount?.value = totalUnreadCount
    }

    fun setChannelUnreadCount(channelUnreadCount: Int) {
        _channelUnreadCount?.value = channelUnreadCount
    }

    /**
     * The per-group counts the server sends with grouped queries and events, before the counts of filtered groups
     * replace them. Code that updates the counts from the server reads and writes this map.
     */
    var rawGroupedUnreadChannels: Map<String, Int> = emptyMap()
        private set
    private var filteredGroupedUnreadChannels: Map<String, Int> = emptyMap()

    @Synchronized
    fun setGroupedUnreadChannels(groupedUnreadChannels: Map<String, Int>) {
        rawGroupedUnreadChannels = groupedUnreadChannels
        _groupedUnreadChannels?.value = rawGroupedUnreadChannels + filteredGroupedUnreadChannels
    }

    /** Sets the badge count of a filtered group, which replaces the server's count for it. `null` removes it. */
    @Synchronized
    fun setFilteredGroupedUnreadChannels(groupKey: String, count: Int?) {
        filteredGroupedUnreadChannels = when (count) {
            null -> filteredGroupedUnreadChannels - groupKey
            else -> filteredGroupedUnreadChannels + (groupKey to count)
        }
        _groupedUnreadChannels?.value = rawGroupedUnreadChannels + filteredGroupedUnreadChannels
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

    /**
     * Stores a draft from the server, unless its channel or thread has a local change that has not reached the server
     * yet.
     */
    fun updateDraftMessage(draftMessage: DraftMessage) {
        if (draftMessage.pendingKey() !in pendingDrafts) putDraftMessage(draftMessage)
    }

    /** Adds the drafts the server returned with [channels]. */
    fun updateChannelDrafts(channels: List<Channel>) {
        channels.forEach { channel -> channel.draftMessage?.let(::updateDraftMessage) }
    }

    /**
     * Removes a draft deleted on the server, unless its channel or thread has a local change that has not reached the
     * server yet.
     */
    fun removeDraftMessage(draftMessage: DraftMessage) {
        if (draftMessage.pendingKey() !in pendingDrafts) dropDraftMessage(draftMessage)
    }

    /** Stores a draft saved locally. Drafts from the server do not replace it until [completeDraftMessage]. */
    fun updatePendingDraftMessage(draftMessage: DraftMessage) {
        pendingDrafts += draftMessage.pendingKey()
        putDraftMessage(draftMessage)
    }

    /** Removes a draft deleted locally. Drafts from the server do not bring it back until [completeDraftMessage]. */
    fun removePendingDraftMessage(draftMessage: DraftMessage) {
        pendingDrafts += draftMessage.pendingKey()
        dropDraftMessage(draftMessage)
    }

    /** Ends the local change of the channel or thread of [draftMessage], so drafts from the server apply again. */
    fun completeDraftMessage(draftMessage: DraftMessage) {
        pendingDrafts -= draftMessage.pendingKey()
    }

    private fun putDraftMessage(draftMessage: DraftMessage) {
        draftMessage.parentId?.let { parentId ->
            _threadDraftMessages?.let { it.value += (parentId to draftMessage) }
        }
        _channelDraftMessages
            ?.takeUnless { draftMessage.parentId != null }
            ?.let { it.value += (draftMessage.cid to draftMessage) }
    }

    private fun dropDraftMessage(draftMessage: DraftMessage) {
        draftMessage.parentId?.let { parentId ->
            _threadDraftMessages?.let { it.value -= parentId }
        }
        _channelDraftMessages
            ?.takeUnless { draftMessage.parentId != null }
            ?.let { it.value -= draftMessage.cid }
    }

    private fun DraftMessage.pendingKey(): String = parentId?.let { "thread:$it" } ?: "channel:$cid"

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
                    if (typingEvent.typingUsers.isEmpty()) {
                        remove(cid)
                    } else {
                        this[cid] = typingEvent
                    }
                },
            )
        }
    }

    fun addLiveLocation(location: Location) {
        addLiveLocations(listOf(location))
    }

    fun addLiveLocations(locations: List<Location>) {
        _activeLiveLocations.update { currentLocations ->
            (
                currentLocations
                    .asSequence()
                    .filterNot { location -> locations.any { it.messageId == location.messageId } } +
                    locations
                )
                .filter(nonExpiredPredicate())
                .filterNot { location -> locallyEndedLiveLocations[location.messageId] == location.endAt }
                .sortedBy(Location::messageId) // Keep the same order to prevent unnecessary emissions
                .toList()
        }
    }

    /**
     * Removes [location] when its end time is reached on this device, without waiting for the server.
     *
     * The same location is not added back, even when the device clock says it is still active. A location
     * with a different end time is.
     *
     * @return `true` if this call removed it, `false` if it was no longer active.
     */
    fun endLiveLocation(location: Location): Boolean {
        // Recorded before the removal, so a concurrent add either sees it or is removed with it.
        location.endAt?.let { endAt -> locallyEndedLiveLocations[location.messageId] = endAt }
        val isSameLocation = { active: Location ->
            active.messageId == location.messageId && active.endAt == location.endAt
        }
        return _activeLiveLocations.getAndUpdate { locations -> locations.filterNot(isSameLocation) }
            .any(isSameLocation)
    }

    fun removeExpiredLiveLocations() {
        _activeLiveLocations.update { locations ->
            locations.removeExpired()
        }
    }

    private fun List<Location>.removeExpired(): List<Location> =
        filter(nonExpiredPredicate())

    private fun nonExpiredPredicate(): (location: Location) -> Boolean =
        { location -> location.endAt?.after(Date(now())) ?: false }
}
