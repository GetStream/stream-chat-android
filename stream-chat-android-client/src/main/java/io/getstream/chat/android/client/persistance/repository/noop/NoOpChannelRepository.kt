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

package io.getstream.chat.android.client.persistance.repository.noop

import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import java.util.Date

/**
 * Repository to read and write [Channel] data.
 */
@Suppress("TooManyFunctions")
internal object NoOpChannelRepository : ChannelRepository {
    override suspend fun insertChannel(channel: Channel) { /* No-Op */ }
    override suspend fun insertChannels(channels: Collection<Channel>) { /* No-Op */ }
    override suspend fun deleteChannel(cid: String) { /* No-Op */ }
    override suspend fun deleteChannelMessage(message: Message) { /* No-Op */ }
    override suspend fun deleteAllChannelUserMessages(cid: String?, userId: String) { /* No-Op */ }
    override suspend fun updateChannelMessage(message: Message) { /* No-Op */ }
    override suspend fun selectAllCids(): List<String> = emptyList()
    override suspend fun selectChannels(channelCIDs: List<String>): List<Channel> = emptyList()
    override suspend fun selectChannel(cid: String): Channel? = null
    override suspend fun selectChannelCidsBySyncNeeded(limit: Int): List<String> = emptyList()
    override suspend fun selectChannelsSyncNeeded(limit: Int): List<Channel> = emptyList()
    override suspend fun setChannelDeletedAt(cid: String, deletedAt: Date) { /* No-Op */ }
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean, hideMessagesBefore: Date) { /* No-Op */ }
    override suspend fun setHiddenForChannel(cid: String, hidden: Boolean) { /* No-Op */ }
    override suspend fun selectMembersForChannel(cid: String): List<Member> = emptyList()
    override suspend fun updateMembersForChannel(cid: String, members: List<Member>) { /* No-Op */ }
    override suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) { /* No-Op */ }

    override suspend fun evictChannel(cid: String) { /* No-Op */ }

    override suspend fun clear() { /* No-Op */ }
}
