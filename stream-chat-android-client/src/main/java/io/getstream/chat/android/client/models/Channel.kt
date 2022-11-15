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

package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.api.models.querysort.ComparableFieldProvider
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.SyncStatus
import java.util.Date

/**
 * Channel is where conversations take place between two or more chat users.
 * It contains a list of messages and have a list of the member users that are participating in the conversation.
 *
 * @param id Channel's unique ID.
 * @param type Type of the channel.
 * @param name Channel's name.
 * @param image Channel's image.
 * @param watcherCount Number of channel watchers.
 * @param frozen Whether channel is frozen or not.
 * @param lastMessageAt Date of the last message sent.
 * @param createdAt Date/time of creation.
 * @param deletedAt Date/time of deletion.
 * @param updatedAt Date/time of the last update.
 * @param syncStatus Local field used to store channel's sync status and retry requests if needed.
 * @param memberCount Number of members in the channel.
 * @param messages The list of channel's messages.
 * @param members The list of channel's members.
 * @param watchers The list of channel's watchers.
 * @param read The list of read states.
 * @param config Channel's configuration.
 * @param createdBy Creator of the channel.
 * @param unreadCount The number of unread messages for the current user.
 * @param team Team the channel belongs to (multi-tenant only).
 * @param hidden Whether this channel is hidden by current user or not.
 * @param hiddenMessagesBefore Messages before this date are hidden from the user.
 * @param cooldown Cooldown period after sending each message in seconds.
 * @param pinnedMessages The list of pinned messages in the channel.
 * @param ownCapabilities Channel's capabilities available for the current user. Note that the field is not provided
 * in the events.
 * @param membership Represents relationship of the current user to this channel.
 * @param extraData A map of custom fields for the channel.
 * @param cachedLatestMessages The list of cached messages if the regular list does not contain the newest messages.
 * @param isInsideSearch When the channel is inside search, eg. searching from the channel list for a message or when
 * hopping to a quoted message a number pages away without retaining the newest messages in the list.
 */
public data class Channel(
    var id: String = "",
    var type: String = "",
    var name: String = "",
    var image: String = "",
    var watcherCount: Int = 0,
    var frozen: Boolean = false,
    var lastMessageAt: Date? = null,
    var createdAt: Date? = null,
    var deletedAt: Date? = null,
    var updatedAt: Date? = null,
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,
    var memberCount: Int = 0,
    var messages: List<Message> = mutableListOf(),
    var members: List<Member> = mutableListOf(),
    var watchers: List<User> = mutableListOf(),
    var read: List<ChannelUserRead> = mutableListOf(),
    var config: Config = Config(),
    var createdBy: User = User(),
    var unreadCount: Int? = null,
    val team: String = "",
    var hidden: Boolean? = null,
    var hiddenMessagesBefore: Date? = null,
    val cooldown: Int = 0,
    var pinnedMessages: List<Message> = mutableListOf(),
    var ownCapabilities: Set<String> = setOf(),
    var membership: Member? = null,
    var cachedLatestMessages: List<Message> = emptyList(),
    var isInsideSearch: Boolean = false,
    override var extraData: MutableMap<String, Any> = mutableMapOf(),
) : CustomObject, ComparableFieldProvider {

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
     * Determines the last updated date/time.
     * Returns either [lastMessageAt] or [createdAt].
     */
    val lastUpdated: Date?
        get() = lastMessageAt?.takeIf { createdAt == null || it.after(createdAt) } ?: createdAt

    /**
     * Whether a channel contains unread messages or not.
     */
    val hasUnread: Boolean
        get() = unreadCount?.let { it > 0 } ?: false

    @Suppress("ComplexMethod")
    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "cid" -> cid
            "id" -> id
            "type" -> type
            "name" -> name
            "image" -> image
            "watcherCount" -> watcherCount
            "frozen" -> frozen
            "lastMessageAt" -> lastMessageAt
            "createdAt" -> createdAt
            "updatedAt" -> updatedAt
            "deletedAt" -> deletedAt
            "memberCount" -> memberCount
            "unreadCount" -> unreadCount
            "team" -> team
            "hidden" -> hidden
            "cooldown" -> cooldown
            "lastUpdated" -> lastUpdated
            "hasUnread" -> hasUnread
            else -> extraData[fieldName] as? Comparable<*>
        }
    }
}
