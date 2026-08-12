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

package io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal

import android.util.LruCache
import io.getstream.chat.android.client.extensions.getCreatedAtOrDefault
import io.getstream.chat.android.client.extensions.internal.NEVER
import io.getstream.chat.android.client.extensions.syncUnreadCountWithReads
import io.getstream.chat.android.client.internal.offline.extensions.launchWithMutex
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.userread.internal.toModel
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.core.utils.date.minOf
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.Date

/**
 * Repository to read and write [Channel] data.
 */
@SuppressWarnings("TooManyFunctions")
internal class DatabaseChannelRepository(
    private val scope: CoroutineScope,
    private val channelDao: ChannelDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
    private val getDraftMessage: suspend (cid: String) -> DraftMessage?,
    private val currentUserId: String,
    private val isLocalUnreadCountEnabled: Boolean = false,
    private val now: () -> Long = { System.currentTimeMillis() },
    cacheSize: Int = 1000,
) : ChannelRepository {

    private val logger by taggedLogger("Chat:ChannelRepository")
    private val channelCache = LruCache<String, Channel>(cacheSize)
    private val dbMutex = Mutex()
    private val cacheMutex = Mutex()

    override suspend fun insertChannel(channel: Channel) {
        insertChannels(listOf(channel))
    }

    /**
     * Inserts many [Channel]s.
     *
     * @param channels collection of [Channel]
     */
    override suspend fun insertChannels(channels: Collection<Channel>) {
        if (channels.isEmpty()) return
        val storedReadsByCid = channels
            .filter { it.isReadTrackedLocally() && channelCache[it.cid] == null }
            .associate { it.cid to channelDao.select(it.cid)?.reads?.values?.map { read -> read.toModel(getUser) } }
        val channelsToInsert = cacheMutex.withLock {
            val updatedChannels = channels
                .map { channelCache[it.cid]?.let { cachedChannel -> it.combine(cachedChannel) } ?: it }
                .map { if (it.isReadTrackedLocally()) it.preserveLocallyTrackedReads(storedReadsByCid[it.cid]) else it }
            updatedChannels
                .filter { channelCache[it.cid] != it }
                .also { cacheChannel(updatedChannels) }
        }
        scope.launchWithMutex(dbMutex) {
            logger.v { "[insertChannels] inserting ${channelsToInsert.size} entities on DB" }
            channelsToInsert
                .takeUnless { it.isEmpty() }
                // Re-read the cache at write time: DAO writes are not guaranteed to run in launch
                // order, so the last write must persist the latest merged state, not its snapshot
                ?.map { channel -> (channelCache[channel.cid] ?: channel).toEntity() }
                ?.let { channelDao.insertMany(it) }
        }
    }

    private fun Channel.isReadTrackedLocally(): Boolean =
        isLocalUnreadCountEnabled && !config.readEventsEnabled

    /**
     * Keeps the stored current user read of read-events-disabled channels: it is tracked on-device and
     * must never be overwritten by server data (a recency check is not enough - server reads carry
     * lastReceivedEventDate = last_message_at, tying it). Other users' reads are merged by recency.
     *
     * [preloadedStoredReads] is the persisted read state fetched before acquiring the lock; the cache
     * still takes priority when populated.
     */
    private fun Channel.preserveLocallyTrackedReads(preloadedStoredReads: List<ChannelUserRead>?): Channel {
        val storedReads = channelCache[cid]?.read ?: preloadedStoredReads ?: return this
        if (storedReads.isEmpty()) return this
        val mergedByUser = read.associateByTo(mutableMapOf(), ChannelUserRead::getUserId)
        storedReads.forEach { stored ->
            val incoming = mergedByUser[stored.getUserId()]
            mergedByUser[stored.getUserId()] = when {
                incoming == null -> stored
                stored.getUserId() == currentUserId -> stored.copy(
                    // Only the user info and the delivered fields are merged from the server,
                    // mirroring the in-memory merge in the channel state.
                    user = incoming.user,
                    lastDeliveredAt = incoming.lastDeliveredAt ?: stored.lastDeliveredAt,
                    lastDeliveredMessageId = incoming.lastDeliveredMessageId ?: stored.lastDeliveredMessageId,
                )
                stored.lastReceivedEventDate.after(incoming.lastReceivedEventDate) -> stored
                else -> incoming
            }
        }
        return copy(read = mergedByUser.values.toList()).syncUnreadCountWithReads()
    }

    /**
     * The cache merge runs under [cacheMutex] and the DAO writes re-read the cache, so the persisted
     * row converges to the latest merged state regardless of write order.
     */
    override suspend fun upsertChannelReads(cid: String, reads: List<ChannelUserRead>) {
        if (reads.isEmpty()) return
        // Resolve the stored channel before the lock so no DB read runs while cacheMutex is held.
        val stored = selectChannel(cid) ?: return
        val updatedChannel = cacheMutex.withLock {
            // The cache takes priority in case it was updated between the read above and the lock.
            val base = channelCache[cid] ?: stored
            base
                .copy(read = (reads + base.read).distinctBy(ChannelUserRead::getUserId))
                .syncUnreadCountWithReads()
                .also { cacheChannel(it) }
        }
        scope.launchWithMutex(dbMutex) {
            logger.v { "[upsertChannelReads] cid: $cid, reads.size: ${reads.size}" }
            channelDao.insert((channelCache[cid] ?: updatedChannel).toEntity())
        }
    }

    private fun cacheChannel(vararg channels: Channel) {
        channels.forEach { channelCache.put(it.cid, it) }
    }

    private fun cacheChannel(channels: Collection<Channel>) {
        channels.forEach { channelCache.put(it.cid, it) }
    }

    /**
     * Deletes a [Channel] by the cid.
     *
     * @param cid String
     */
    override suspend fun deleteChannel(cid: String) {
        logger.v { "[deleteChannel] cid: $cid" }
        removeFromCache(cid)
        scope.launchWithMutex(dbMutex) { channelDao.delete(cid) }
    }

    override suspend fun deleteChannelMessage(message: Message) {
        logger.v { "[deleteChannelMessage] message.id: ${message.id}, message.text: ${message.text}" }
        channelCache[message.cid]?.let { cachedChannel ->
            val updatedChannel = cachedChannel.copy(
                messages = cachedChannel.messages.filter { it.id != message.id },
                pinnedMessages = cachedChannel.pinnedMessages.filter { it.id != message.id },
            )
            cacheChannel(updatedChannel)
        }
    }

    override suspend fun deleteAllChannelUserMessages(cid: String?, userId: String) {
        if (cid != null) {
            deleteAllChannelUserMessages(cid, userId)
        } else {
            deleteAllUserMessages(userId)
        }
    }

    private fun deleteAllUserMessages(userId: String) {
        // Find all channels that have messages from the user
        val cids = channelCache.snapshot().values
            .filter { channel -> channel.messages.any { msg -> msg.user.id == userId } }
            .map { it.cid }
        // Remove messages from the given user from those channels
        cids.forEach { cid ->
            deleteAllChannelUserMessages(cid, userId)
        }
    }

    private fun deleteAllChannelUserMessages(cid: String, userId: String) {
        channelCache[cid]?.let { cachedChannel ->
            val updatedChannel = cachedChannel.copy(
                messages = cachedChannel.messages.filter { msg -> msg.user.id != userId },
                pinnedMessages = cachedChannel.pinnedMessages.filter { msg -> msg.user.id != userId },
            )
            cacheChannel(updatedChannel)
        }
    }

    override suspend fun updateChannelMessage(message: Message) {
        logger.v { "[updateChannelMessage] message.id: ${message.id}, message.text: ${message.text}" }
        channelCache[message.cid]?.let { cachedChannel ->
            val updatedChannel = cachedChannel.copy(
                messages = cachedChannel.messages.map { if (it.id == message.id) message else it },
                pinnedMessages = cachedChannel.pinnedMessages.mapNotNull { existing ->
                    when (existing.id == message.id) {
                        true -> message.takeIf { it.isPinned(now) }
                        else -> existing
                    }
                },
            )
            cacheChannel(updatedChannel)
        }
    }

    /**
     * Select a channel by cid.
     *
     * @param cid String
     */
    override suspend fun selectChannel(cid: String): Channel? =
        channelCache[cid] ?: channelDao.select(cid = cid)?.toModel(getUser, getMessage, getDraftMessage)
            ?.also { cacheChannel(it) }

    /**
     * Select a list of channels by cid.
     *
     * @param channelCIDs List<String>
     */
    override suspend fun selectChannels(channelCIDs: List<String>): List<Channel> {
        val cachedChannels = channelCIDs.mapNotNull { channelCache[it] }
        val missingChannelIds = channelCIDs.minus(cachedChannels.map(Channel::cid).toSet())
        return cachedChannels +
            channelDao.select(missingChannelIds)
                .map { it.toModel(getUser, getMessage, getDraftMessage) }
                .also { cacheChannel(it) }
    }

    /**
     * Selects all channels' cids.
     *
     * @return A list of channels' cids stored in the repository.
     */
    override suspend fun selectAllCids(): List<String> = channelDao.selectAllCids()

    /**
     * Read which channel cids need sync.
     */
    override suspend fun selectChannelCidsBySyncNeeded(limit: Int): List<String> {
        return channelDao.selectCidsBySyncNeeded(limit = limit)
    }

    /**
     * Read which channels need sync.
     */
    override suspend fun selectChannelsSyncNeeded(limit: Int): List<Channel> {
        return channelDao.selectSyncNeeded(limit = limit).map { it.toModel(getUser, getMessage, getDraftMessage) }
    }

    /**
     * Sets the Channel.deleteAt for a channel.
     *
     * @param cid String.
     * @param deletedAt Date.
     */
    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
        removeFromCache(cid)
        scope.launchWithMutex(dbMutex) { channelDao.setDeletedAt(cid, deletedAt) }
    }

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     * @param hideMessagesBefore Date.
     */
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        removeFromCache(cid)
        scope.launchWithMutex(dbMutex) { channelDao.setHidden(cid, hidden, hideMessagesBefore) }
    }

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     */
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        removeFromCache(cid)
        scope.launchWithMutex(dbMutex) { channelDao.setHidden(cid, hidden) }
    }

    /**
     * Reads the member list of a channel.
     *
     * @param cid String.
     */
    override suspend fun selectMembersForChannel(cid: String): List<Member> =
        selectChannel(cid)?.members ?: emptyList()

    /**
     * Updates the members of a [Channel]
     *
     * @param cid String.
     * @param members list of [Member]
     */
    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        selectChannel(cid)?.let {
            insertChannel(it.copy(members = (members + it.members).distinctBy(Member::getUserId)))
        }
    }

    /**
     * Updates the last message for a [Channel]
     *
     * @param cid String.
     * @param lastMessage [Message].
     */
    override suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) {
        selectChannel(cid)?.let {
            insertChannel(it.copy(messages = listOf(lastMessage)))
        }
    }

    private fun Channel.combine(cachedChannel: Channel): Channel {
        val hideMessagesBefore = minOf(this.hiddenMessagesBefore, cachedChannel.hiddenMessagesBefore)
        val messages = (
            messages.filter { it.after(hideMessagesBefore) } +
                cachedChannel.messages.filter { it.after(hideMessagesBefore) }
            )
            .distinctBy { it.id }
            .sortedBy { it.getCreatedAtOrDefault(NEVER) }
        val read = (read + cachedChannel.read).distinctBy { it.getUserId() }
        return copy(
            messages = messages,
            hiddenMessagesBefore = hideMessagesBefore,
            members = members,
            read = read,
        ).syncUnreadCountWithReads()
    }

    private fun Message.after(date: Date?): Boolean =
        date?.let { (getCreatedAtOrDefault(NEVER)).after(it) } ?: true

    override suspend fun evictChannel(cid: String) {
        logger.v { "[evictChannel] cid: $cid" }
        removeFromCache(cid)
    }

    private fun removeFromCache(cid: String) {
        logger.v { "[removeFromCache] cid: $cid" }
        channelCache.remove(cid)
    }

    override suspend fun clear() {
        dbMutex.withLock { channelDao.deleteAll() }
    }
}
