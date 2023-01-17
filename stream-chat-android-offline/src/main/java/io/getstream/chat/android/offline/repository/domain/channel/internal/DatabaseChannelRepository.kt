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

package io.getstream.chat.android.offline.repository.domain.channel.internal

import androidx.collection.LruCache
import io.getstream.chat.android.client.extensions.internal.lastMessage
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toModel
import io.getstream.log.taggedLogger
import java.util.Date

/**
 * Repository to read and write [Channel] data.
 */
@SuppressWarnings("TooManyFunctions")
internal class DatabaseChannelRepository(
    private val channelDao: ChannelDao,
    private val getUser: suspend (userId: String) -> User,
    private val getLastMessageForChannel: suspend (cid: String) -> Message?,
    cacheSize: Int = 100,
) : ChannelRepository {

    private val logger by taggedLogger("Chat:ChannelRepository")

    // the channel cache is simple, just keeps the last several users in memory
    private val channelCache = LruCache<String, Channel>(cacheSize)

    /**
     * Inserts a [Channel]
     *
     * @param channel [Channel]
     */
    override suspend fun insertChannel(channel: Channel) {
        val entity = channel.toEntity()
        logger.v { "[insertChannel] entity: ${entity.lastMessageInfo()}" }
        updateCache(listOf(channel))
        channelDao.insert(entity)
    }

    /**
     * Inserts many [Channel]s.
     *
     * @param channels collection of [Channel]
     */
    override suspend fun insertChannels(channels: Collection<Channel>) {
        if (channels.isEmpty()) return
        val entities = channels.map(Channel::toEntity)
        logger.v { "[insertChannels] entities.size: ${entities.size}" }
        updateCache(channels)
        channelDao.insertMany(entities)
    }

    /**
     * Deletes a [Channel] by the cid.
     *
     * @param cid String
     */
    override suspend fun deleteChannel(cid: String) {
        logger.v { "[deleteChannel] cid: $cid" }
        channelCache.remove(cid)
        channelDao.delete(cid)
    }

    /**
     * Select a channels, but without loading the messages.
     *
     * @param cid String
     */
    override suspend fun selectChannelWithoutMessages(cid: String): Channel? {
        val entity = channelDao.select(cid = cid)
        return entity?.toModel(getUser, getLastMessageForChannel)
    }

    /**
     * Selects all channels' cids.
     *
     * @return A list of channels' cids stored in the repository.
     */
    override suspend fun selectAllCids(): List<String> = channelDao.selectAllCids()

    /**
     * Select channels by full channel IDs [Channel.cid]
     *
     * @param channelCIDs A list of [Channel.cid] as query specification.
     * @param forceCache A boolean flag that forces cache in repository and fetches data directly in database if passed
     * value is true.
     *
     * @return A list of channels found in repository.
     */
    override suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean): List<Channel> {
        if (channelCIDs.isEmpty()) {
            return emptyList()
        }
        return if (forceCache) {
            fetchChannels(channelCIDs)
        } else {
            val cachedChannels: MutableList<Channel> = channelCIDs.mapNotNullTo(mutableListOf(), channelCache::get)
            val missingChannelIds = channelCIDs.filter { channelCache.get(it) == null }
            val dbChannels = fetchChannels(missingChannelIds).toMutableList()
            dbChannels.addAll(cachedChannels)
            dbChannels
        }
    }

    /**
     * Reads channel using specified [cid].
     */
    override suspend fun selectChannelByCid(cid: String): Channel? {
        return channelDao.select(cid = cid)?.toModel(getUser, getLastMessageForChannel)
    }

    /**
     * Reads list of channels using specified [cids].
     */
    override suspend fun selectChannelsByCids(cids: List<String>): List<Channel> {
        return channelDao.select(cids = cids).map { it.toModel(getUser, getLastMessageForChannel) }
    }

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
        return channelDao.selectSyncNeeded(limit = limit).map { it.toModel(getUser, getLastMessageForChannel) }
    }

    /**
     * Sets the Channel.deleteAt for a channel.
     *
     * @param cid String.
     * @param deletedAt Date.
     */
    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
        channelCache.remove(cid)
        channelDao.setDeletedAt(cid, deletedAt)
    }

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     * @param hideMessagesBefore Date.
     */
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) {
        channelCache.remove(cid)
        channelDao.setHidden(cid, hidden, hideMessagesBefore)
    }

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     */
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
        channelCache.remove(cid)
        channelDao.setHidden(cid, hidden)
    }

    /**
     * Reads the member list of a channel. Allows us to avoid enriching channel just to select members
     *
     * @param cid String.
     */
    override suspend fun selectMembersForChannel(cid: String): List<Member> {
        return channelDao.select(cid)?.members?.values?.map { it.toModel(getUser) } ?: emptyList()
    }

    /**
     * Updates the members of a [Channel]
     *
     * @param cid String.
     * @param members list of [Member]
     */
    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) {
        members
            .map { it.toEntity() }
            .associateBy { it.userId }
            .let { memberMap ->
                channelDao.select(cid)?.copy(members = memberMap)
            }
            ?.let { updatedChannel ->
                channelDao.insert(updatedChannel)
            }
    }

    override suspend fun evictChannel(cid: String) {
        logger.v { "[evictChannel] cid: $cid" }
        channelCache.remove(cid)
    }

    override fun clearChannelCache() {
        logger.v { "[clearChannelCache] no args" }
        channelCache.evictAll()
    }

    private suspend fun fetchChannels(channelCIDs: List<String>): List<Channel> {
        return channelDao.select(channelCIDs).map { it.toModel(getUser, getLastMessageForChannel) }.also(::updateCache)
    }

    private fun updateCache(channels: Collection<Channel>) {
        logger.v { "[updateCache] channels.size: ${channels.size}" }
        for (channel in channels) {
            channelCache.put(channel.cid, channel)
        }
    }

    /**
     * Updates the last message for a [Channel]
     *
     * @param cid String.
     * @param lastMessage [Message].
     */
    override suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) {
        selectChannelWithoutMessages(cid)?.also { channel ->
            val messageCreatedAt = checkNotNull(
                lastMessage.createdAt
                    ?: lastMessage.createdLocallyAt
            ) { "created at cant be null, be sure to set message.createdAt" }

            val oldLastMessage = channel.lastMessage
            val updateNeeded = if (oldLastMessage != null) {
                lastMessage.id == oldLastMessage.id ||
                    channel.lastMessageAt == null ||
                    messageCreatedAt.after(channel.lastMessageAt)
            } else {
                true
            }

            if (updateNeeded) {
                channel.apply {
                    lastMessageAt = messageCreatedAt
                    messages = listOf(lastMessage)
                }.also { insertChannel(it) }
            }
        }
    }

    override suspend fun clear() {
        channelDao.deleteAll()
    }
}
