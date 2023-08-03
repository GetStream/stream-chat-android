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

package io.getstream.chat.android.models

import java.util.Date

/**
 * A class that only stores the channel data and not the channel state that changes a lot
 * (for example messages, watchers, etc.).
 *
 * @param id Channel's unique ID.
 * @param type Type of the channel.
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
 * @param ownCapabilities Channel's capabilities available for the current user. Note that the field is not provided
 * in the events.
 * @param membership Represents relationship of the current user to the channel.
 */
public data class ChannelData(
    val id: String,
    val type: String,
    val name: String = "",
    val image: String = "",
    val createdBy: User = User(),
    val cooldown: Int = 0,
    val frozen: Boolean = false,
    val createdAt: Date? = null,
    val updatedAt: Date? = null,
    val deletedAt: Date? = null,
    val memberCount: Int = 0,
    val team: String = "",
    val extraData: Map<String, Any> = mapOf(),
    val ownCapabilities: Set<String> = setOf(),
    val membership: Member? = null,
) {

    /**
     * The channel id in the format messaging:123.
     */
    val cid: String
        get() = if (id.isEmpty() || type.isEmpty()) {
            ""
        } else {
            "$type:$id"
        }

    /**
     * Creates a [ChannelData] entity from a [Channel] object.
     * Keeps existing [ChannelData.ownCapabilities] if the [Channel] object comes with an empty set of capabilities.
     *
     * @param channel The [Channel] object to convert.
     * @param currentOwnCapabilities Set of existing own capabilities stored for the Channel.
     */
    public constructor(channel: Channel, currentOwnCapabilities: Set<String>) : this(
        type = channel.type,
        id = channel.id,
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
        ownCapabilities = channel.ownCapabilities.takeIf { ownCapabilities -> ownCapabilities.isNotEmpty() }
            ?: currentOwnCapabilities,
        membership = channel.membership,
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
    @Suppress("LongParameterList")
    public fun toChannel(
        messages: List<Message>,
        cachedLatestMessages: List<Message>,
        members: List<Member>,
        reads: List<ChannelUserRead>,
        watchers: List<User>,
        watcherCount: Int,
        insideSearch: Boolean,
    ): Channel {
        val messagesList = if (insideSearch) cachedLatestMessages else messages
        return Channel(
            type = type,
            id = id,
            name = name,
            image = image,
            frozen = frozen,
            createdAt = createdAt,
            updatedAt = updatedAt,
            deletedAt = deletedAt,
            extraData = extraData,
            cooldown = cooldown,
            lastMessageAt = messagesList.lastOrNull()?.let { it.createdAt ?: it.createdLocallyAt },
            createdBy = createdBy,
            messages = messages,
            members = members,
            watchers = watchers,
            watcherCount = watcherCount,
            read = reads,
            team = team,
            memberCount = memberCount,
            ownCapabilities = ownCapabilities,
            membership = membership,
            cachedLatestMessages = cachedLatestMessages,
            isInsideSearch = insideSearch,
        )
    }

    /**
     * Checks if the user has specific capabilities.
     *
     * You can find a list of capabilities in [ChannelCapabilities].
     *
     * @param channelCapability The specific ability we are checking against.
     */
    public fun isUserAbleTo(channelCapability: String): Boolean {
        return ownCapabilities.contains(channelCapability)
    }
}
