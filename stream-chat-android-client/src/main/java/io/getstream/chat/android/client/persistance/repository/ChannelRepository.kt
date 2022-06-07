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

package io.getstream.chat.android.client.persistance.repository

import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import java.util.Date

/**
 * Repository to read and write [Channel] data.
 */
@Suppress("TooManyFunctions")
public interface ChannelRepository {

    /**
     * Inserts a [Channel]
     *
     * @param channel [Channel]
     */
    public suspend fun insertChannel(channel: Channel)

    /**
     * Inserts many [Channel]s.
     *
     * @param channels collection of [Channel]
     */
    public suspend fun insertChannels(channels: Collection<Channel>)

    /**
     * Deletes a [Channel] by the cid.
     *
     * @param cid String
     */
    public suspend fun deleteChannel(cid: String)

    /**
     * Select a channels, but without loading the messages.
     *
     * @param cid String
     */
    public suspend fun selectChannelWithoutMessages(cid: String): Channel?

    /**
     * Selects all channels' cids.
     *
     * @return A list of channels' cids stored in the repository.
     */
    public suspend fun selectAllCids(): List<String>

    /**
     * Select channels by full channel IDs [Channel.cid]
     *
     * @param channelCIDs A list of [Channel.cid] as query specification.
     * @param forceCache A boolean flag that forces cache in repository and fetches data directly in database if passed
     * value is true.
     *
     * @return A list of channels found in repository.
     */
    public suspend fun selectChannels(channelCIDs: List<String>, forceCache: Boolean = false): List<Channel>

    /**
     * Select channel by full channel ID [Channel.cid]
     *
     * @param cid A [Channel.cid] as query specification.
     *
     * @return A channel found in repository.
     */
    public suspend fun selectChannelByCid(cid: String): Channel?

    /**
     * Select channels by full channel IDs [Channel.cid]
     *
     * @param cids A list of [Channel.cid] as query specification.
     *
     * @return A list of channels found in repository.
     */
    public suspend fun selectChannelsByCids(cids: List<String>): List<Channel>

    /**
     * Read which channel cids need sync.
     */
    public suspend fun selectChannelCidsBySyncNeeded(limit: Int = NO_LIMIT): List<String>

    /**
     * Read which channels need sync.
     */
    public suspend fun selectChannelsSyncNeeded(limit: Int = NO_LIMIT): List<Channel>

    /**
     * Sets the Channel.deleteAt for a channel.
     *
     * @param cid String.
     * @param deletedAt Date.
     */
    public suspend fun setChannelDeletedAt(cid: String, deletedAt: Date)

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     * @param hideMessagesBefore Date.
     */
    public suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date)

    /**
     * Sets the Channel.hidden for a channel.
     *
     * @param cid String.
     * @param hidden Date.
     */
    public suspend fun setHiddenForChannel(cid: String, hidden: Boolean)

    /**
     * Reads the member list of a channel.
     *
     * @param cid String.
     */
    public suspend fun selectMembersForChannel(cid: String): List<Member>

    /**
     * Updates the members of a [Channel]
     *
     * @param cid String.
     * @param members list of [Member]
     */
    public suspend fun updateMembersForChannel(cid: String, members: List<Member>)

    public suspend fun evictChannel(cid: String)

    /**
     * Updates the last message for a [Channel]
     *
     * @param cid String.
     * @param lastMessage [Message].
     */
    public suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message)

    @VisibleForTesting
    public fun clearChannelCache()

    private companion object {
        private const val NO_LIMIT: Int = -1
    }
}
