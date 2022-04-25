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

package io.getstream.chat.android.offline.model.channel

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.Date

/**
 * A class that only stores the channel data and not the channel state that changes a lot (for example messages, watchers, etc.).
 *
 * @param channelId Channel's unique ID.
 * @param type Type of the channel.
 * @param cid The channel id in the format messaging:123.
 * @param name Channel's name.
 * @param image Channel's image.
 * @param createdBy Creator of the channel.
 * @param cooldown Cooldown period after sending each message in seconds.
 * @param frozen Whether channel is frozen or not.
 * @param createdAt Date/time of creation.
 * @param updatedAt Date/time of the last update.
 * @param deletedAt Date/time of deletion.
 * @param memberCount Number of members in the channel.
 * @param team Team the channel belongs to (multi-tenant only).
 * @param extraData A map of custom fields for the channel.
 * @param ownCapabilities Channel's capabilities available for the current user. Note that the field is not provided in the events.
 */
public data class ChannelData(
    var channelId: String,
    var type: String,
    var cid: String = "%s:%s".format(type, channelId),
    var name: String = "",
    var image: String = "",
    var createdBy: User = User(),
    var cooldown: Int = 0,
    var frozen: Boolean = false,
    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var deletedAt: Date? = null,
    var memberCount: Int = 0,
    var team: String = "",
    var extraData: MutableMap<String, Any> = mutableMapOf(),
    var ownCapabilities: Set<String> = setOf(),
) {

    /**
     * Creates a [ChannelData] entity from a [Channel] object.
     *
     * @param channel The [Channel] object to convert
     */
    internal constructor(channel: Channel) : this(
        type = channel.type,
        channelId = channel.id,
        name = channel.name,
        image = channel.image,
        frozen = channel.frozen,
        cooldown = channel.cooldown,
        createdAt = channel.createdAt,
        updatedAt = channel.updatedAt,
        deletedAt = channel.deletedAt,
        memberCount = channel.memberCount,
        extraData = channel.extraData,
        createdBy = channel.createdBy,
        team = channel.team,
        ownCapabilities = channel.ownCapabilities
    )

    /**
     * Converts a [ChannelData] entity to a [Channel] based on additional information.
     *
     * @param messages The list of channel's messages.
     * @param members The list of channel's members.
     * @param reads The list of read states.
     * @param watchers The list of channel's watchers.
     * @param watcherCount Number of channel watchers.
     *
     * @return A [Channel] object.
     */
    internal fun toChannel(
        messages: List<Message>,
        members: List<Member>,
        reads: List<ChannelUserRead>,
        watchers: List<User>,
        watcherCount: Int,
    ): Channel {
        return Channel(
            type = type,
            id = channelId,
            name = name,
            image = image,
            cid = cid,
            frozen = frozen,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            extraData = extraData,
            cooldown = cooldown,
            lastMessageAt = messages.lastOrNull()?.let { it.createdAt ?: it.createdLocallyAt },
            createdBy = createdBy,
            messages = messages,
            members = members,
            watchers = watchers,
            watcherCount = watcherCount,
            read = reads,
            team = team,
            memberCount = memberCount,
            ownCapabilities = ownCapabilities
        )
    }

    /**
     * Checks if the user has specific capabilities.
     *
     * You can find a list of capabilities in [io.getstream.chat.android.client.models.ChannelCapabilities].
     *
     * @param channelCapability The specific ability we are checking against.
     */
    public fun isUserAbleTo(channelCapability: String): Boolean {
        return ownCapabilities.contains(channelCapability)
    }
}
