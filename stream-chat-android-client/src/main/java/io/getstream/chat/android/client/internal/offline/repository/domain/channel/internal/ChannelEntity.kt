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

package io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.member.internal.MemberEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.userread.internal.ChannelUserReadEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.LocationEntity
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

/**
 * ChannelEntity stores both the channel information as well as references to all of the channel's state.
 * Messages are stored on their own table for easier pagination and updates.
 *
 * @param type Type of the channel.
 * @param channelId Channel's unique ID.
 * @param name Channel's name.
 * @param image Channel's image.
 * @param cooldown Cooldown period after sending each message in seconds.
 * @param createdByUserId Id of the user who created the channel.
 * @param filterTags The list of filter tags applied to the channel.
 * @param frozen If the channel is frozen or not (new messages wont be allowed).
 * @param hidden If the channel is hidden (new messages changes this field to false).
 * @param hideMessagesBefore Messages before this date are hidden from the user.
 * @param members The list of channel's members.
 * @param memberCount Number of members in the channel.
 * @param watcherIds The list of watchers` ids.
 * @param watcherCount Number of watchers in the channel.
 * @param reads The list of read states.
 * @param lastMessageAt Date of the last message sent.
 * @param lastMessageId The id of the last message.
 * @param createdAt Date/time of creation.
 * @param updatedAt Date/time of the last update.
 * @param deletedAt Date/time of deletion.
 * @param extraData A map of custom fields for the channel.
 * @param syncStatus If the channel has been synced.
 * @param team Team the channel belongs to (multi-tenant only).
 * @param ownCapabilities Channel's capabilities available for the current user. Note that the field is not provided in
 * the events.
 * @param membership Represents relationship of the current user to this channel.
 * @param activeLiveLocations List of active live locations in the channel.
 * @param messageCount The total number of messages in the channel, if known.
 */
@Entity(tableName = CHANNEL_ENTITY_TABLE_NAME, indices = [Index(value = ["syncStatus"])])
internal data class ChannelEntity(
    val type: String,
    val channelId: String,
    val name: String,
    val image: String,
    val cooldown: Int,
    val createdByUserId: String,
    val filterTags: List<String>,
    val frozen: Boolean,
    val hidden: Boolean?,
    val hideMessagesBefore: Date?,
    val members: Map<String, MemberEntity>,
    val memberCount: Int,
    val watcherIds: List<String>,
    val watcherCount: Int,
    val reads: Map<String, ChannelUserReadEntity>,
    val lastMessageAt: Date?,
    val lastMessageId: String?,
    val createdAt: Date?,
    val updatedAt: Date?,
    val deletedAt: Date?,
    val extraData: Map<String, Any>,
    val syncStatus: SyncStatus,
    val team: String,
    val ownCapabilities: Set<String>,
    val membership: MemberEntity?,
    val activeLiveLocations: List<LocationEntity>,
    val messageCount: Int?,
) {
    /**
     * The channel id in the format messaging:123.
     */
    @PrimaryKey
    var cid: String = "%s:%s".format(type, channelId)
}

internal const val CHANNEL_ENTITY_TABLE_NAME = "stream_chat_channel_state"
