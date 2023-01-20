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

import io.getstream.chat.android.client.extensions.internal.lastMessage
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.domain.channel.lastMessageInfo
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toEntity
import io.getstream.chat.android.offline.repository.domain.channel.member.internal.toModel
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import java.util.Date

/**
 * Repository to read and write [Channel] data.
 */
@SuppressWarnings("TooManyFunctions")
internal class DatabaseChannelRepository(
    private val channelDao: ChannelDao,
    private val getUser: suspend (userId: String) -> User,
    private val getMessage: suspend (messageId: String) -> Message?,
) : ChannelRepository {

    private val logger by taggedLogger("Chat:ChannelRepository")

    /**
     * Inserts a [Channel]
     *
     * @param channel [Channel]
     */
    override suspend fun upsertChannel(channel: Channel) {
        val entity = channel.convertToEntity()
        logger.v { "[upsertChannel] entity: ${entity.lastMessageInfo()}" }
        channelDao.insert(entity)
    }

    /**
     * Inserts many [Channel]s.
     *
     * @param channels collection of [Channel]
     */
    override suspend fun upsertChannels(channels: Collection<Channel>) {
        if (channels.isEmpty()) return
        val entities = channels.map { channel -> channel.convertToEntity() }
        logger.v { "[upsertChannels] entities.size: ${entities.size}" }
        channelDao.insertMany(entities)
    }

    /**
     * Deletes a [Channel] by the cid.
     *
     * @param cid String
     */
    override suspend fun deleteChannel(cid: String) {
        logger.v { "[deleteChannel] cid: $cid" }
        channelDao.delete(cid)
    }

    /**
     * Select a channels, but without loading the messages.
     *
     * @param cid String
     */
    override suspend fun selectChannelWithoutMessages(cid: String): Channel? {
        val entity = channelDao.select(cid = cid)
        return entity?.toModel(getUser, getMessage)
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
     *
     * @return A list of channels found in repository.
     */
    override suspend fun selectChannels(channelCIDs: List<String>): List<Channel> {
        return if (channelCIDs.isEmpty()) emptyList() else fetchChannels(channelCIDs)
    }

    /**
     * Reads channel using specified [cid].
     */
    override suspend fun selectChannelByCid(cid: String): Channel? {
        return channelDao.select(cid = cid)?.toModel(getUser, getMessage)
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
        return channelDao.selectSyncNeeded(limit = limit).map { it.toModel(getUser, getMessage) }
    }

    /**
     * Sets the Channel.deleteAt for a channel.
     *
     * @param cid String.
     * @param deletedAt Date.
     */
    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) {
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
        channelDao.setHidden(cid, hidden, hideMessagesBefore)
    }

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     */
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) {
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

    private suspend fun fetchChannels(channelCIDs: List<String>): List<Channel> {
        return channelDao.select(channelCIDs).map { it.toModel(getUser, getMessage) }
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
                }.also { upsertChannel(it) }
            }
        }
    }

    private suspend fun Channel.convertToEntity(): ChannelEntity {
        val dbChannel = channelDao.select(this.cid)

        return if (dbChannel?.lastMessageAt?.after(this.lastMessage?.createdAt ?: Date(Long.MIN_VALUE)) == true) {
            StreamLog.d("LastMessageDebug") {
                "Keeping last message at. dbChannel?.lastMessageAt: ${dbChannel.lastMessageAt}. backend lastMessageAt: $lastMessageAt"
            }
            this.lastMessageAt = dbChannel.lastMessageAt
            this.toEntity(dbChannel.lastMessageId, dbChannel.lastMessageAt)
        } else {
            StreamLog.d("LastMessageDebug") {
                "Updating last message at. dbChannel?.lastMessageAt: ${dbChannel?.lastMessageAt}. backend lastMessageAt: $lastMessageAt"
            }
            val lastMessage = this.lastMessage
            this.toEntity(lastMessage?.id, lastMessage?.createdAt ?: lastMessage?.createdLocallyAt)
        }
    }

    override suspend fun clear() {
        channelDao.deleteAll()
    }
}
