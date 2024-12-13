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

import androidx.compose.runtime.Immutable
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.querysort.ComparableFieldProvider
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
 * @param channelLastMessageAt The date of the last message sent received from the backend.
 */
@Immutable
public data class Channel(
    val id: String = "",
    val type: String = "",
    val name: String = "",
    val image: String = "",
    val watcherCount: Int = 0,
    val frozen: Boolean = false,
    val createdAt: Date? = null,
    val deletedAt: Date? = null,
    val updatedAt: Date? = null,
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,
    val memberCount: Int = 0,
    val messages: List<Message> = listOf(),
    val members: List<Member> = listOf(),
    val watchers: List<User> = listOf(),
    val read: List<ChannelUserRead> = listOf(),
    val config: Config = Config(),
    val createdBy: User = User(),
    @Deprecated(
        message = "Use the extension property Channel.currentUserUnreadCount instead",
        replaceWith = ReplaceWith(
            expression = "currentUserUnreadCount",
            imports = ["io.getstream.chat.android.client.extensions.currentUserUnreadCount"],
        ),
        level = DeprecationLevel.WARNING,
    )
    val unreadCount: Int = 0,
    val team: String = "",
    val hidden: Boolean? = null,
    val hiddenMessagesBefore: Date? = null,
    val cooldown: Int = 0,
    val pinnedMessages: List<Message> = listOf(),
    val ownCapabilities: Set<String> = setOf(),
    val membership: Member? = null,
    val cachedLatestMessages: List<Message> = emptyList(),
    val isInsideSearch: Boolean = false,
    internal val channelLastMessageAt: Date? = null,
    override val extraData: Map<String, Any> = mapOf(),
) : CustomObject, ComparableFieldProvider {

    /**
     * The date of the last message sent.
     */
    val lastMessageAt: Date? = channelLastMessageAt
        ?: messages
            .filterNot { it.shadowed }
            .filterNot { it.parentId != null && !it.showInChannel }
            .filterNot { type == MessageType.SYSTEM && config.skipLastMsgUpdateForSystemMsgs }
            .maxByOrNull { it.createdAt ?: it.createdLocallyAt ?: Date(0) }
            ?.let { it.createdAt ?: it.createdLocallyAt }

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
        get() = unreadCount > 0

    @Suppress("ComplexMethod")
    override fun getComparableField(fieldName: String): Comparable<*>? {
        return when (fieldName) {
            "cid" -> cid
            "id" -> id
            "type" -> type
            "name" -> name
            "image" -> image
            "watcher_count", "watcherCount" -> watcherCount
            "frozen" -> frozen
            "last_message_at", "lastMessageAt" -> lastMessageAt
            "created_at", "createdAt" -> createdAt
            "updated_at", "updatedAt" -> updatedAt
            "deleted_at", "deletedAt" -> deletedAt
            "member_count", "memberCount" -> memberCount
            "team" -> team
            "hidden" -> hidden
            "cooldown" -> cooldown
            "last_updated", "lastUpdated" -> lastUpdated
            "unread_count", "unreadCount" -> unreadCount
            "has_unread", "hasUnread" -> hasUnread
            "pinned_at", "pinnedAt" -> membership?.pinnedAt
            "archived_at", "archivedAt" -> membership?.archivedAt
            else -> extraData[fieldName] as? Comparable<*>
        }
    }

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    @Suppress("TooManyFunctions")
    public class Builder() {
        private var id: String = ""
        private var type: String = ""
        private var name: String = ""
        private var image: String = ""
        private var watcherCount: Int = 0
        private var frozen: Boolean = false
        private var channelLastMessageAt: Date? = null
        private var createdAt: Date? = null
        private var deletedAt: Date? = null
        private var updatedAt: Date? = null
        private var syncStatus: SyncStatus = SyncStatus.COMPLETED
        private var memberCount: Int = 0
        private var messages: List<Message> = listOf()
        private var members: List<Member> = listOf()
        private var watchers: List<User> = listOf()
        private var read: List<ChannelUserRead> = listOf()
        private var config: Config = Config()
        private var createdBy: User = User()
        private var unreadCount: Int = 0
        private var team: String = ""
        private var hidden: Boolean? = null
        private var hiddenMessagesBefore: Date? = null
        private var cooldown: Int = 0
        private var pinnedMessages: List<Message> = listOf()
        private var ownCapabilities: Set<String> = setOf()
        private var membership: Member? = null
        private var cachedLatestMessages: List<Message> = emptyList()
        private var isInsideSearch: Boolean = false
        private var extraData: Map<String, Any> = mapOf()

        public constructor(channel: Channel) : this() {
            id = channel.id
            type = channel.type
            name = channel.name
            image = channel.image
            watcherCount = channel.watcherCount
            frozen = channel.frozen
            channelLastMessageAt = channel.channelLastMessageAt
            createdAt = channel.createdAt
            deletedAt = channel.deletedAt
            updatedAt = channel.updatedAt
            syncStatus = channel.syncStatus
            memberCount = channel.memberCount
            messages = channel.messages
            members = channel.members
            watchers = channel.watchers
            read = channel.read
            config = channel.config
            createdBy = channel.createdBy
            unreadCount = channel.unreadCount
            team = channel.team
            hidden = channel.hidden
            hiddenMessagesBefore = channel.hiddenMessagesBefore
            cooldown = channel.cooldown
            pinnedMessages = channel.pinnedMessages
            ownCapabilities = channel.ownCapabilities
            membership = channel.membership
            cachedLatestMessages = channel.cachedLatestMessages
            isInsideSearch = channel.isInsideSearch
            extraData = channel.extraData
        }

        public fun withId(id: String): Builder = apply { this.id = id }
        public fun withType(type: String): Builder = apply { this.type = type }
        public fun withName(name: String): Builder = apply { this.name = name }
        public fun withImage(image: String): Builder = apply { this.image = image }
        public fun withWatcherCount(watcherCount: Int): Builder = apply { this.watcherCount = watcherCount }
        public fun withFrozen(frozen: Boolean): Builder = apply { this.frozen = frozen }
        public fun withChannelLastMessageAt(channelLastMessageAt: Date?): Builder = apply {
            this.channelLastMessageAt = channelLastMessageAt
        }
        public fun withCreatedAt(createdAt: Date?): Builder = apply { this.createdAt = createdAt }
        public fun withDeletedAt(deletedAt: Date?): Builder = apply { this.deletedAt = deletedAt }
        public fun withUpdatedAt(updatedAt: Date?): Builder = apply { this.updatedAt = updatedAt }
        public fun withSyncStatus(syncStatus: SyncStatus): Builder = apply { this.syncStatus = syncStatus }
        public fun withMemberCount(memberCount: Int): Builder = apply { this.memberCount = memberCount }
        public fun withMessages(messages: List<Message>): Builder = apply { this.messages = messages }
        public fun withMembers(members: List<Member>): Builder = apply { this.members = members }
        public fun withWatchers(watchers: List<User>): Builder = apply { this.watchers = watchers }
        public fun withRead(read: List<ChannelUserRead>): Builder = apply { this.read = read }
        public fun withConfig(config: Config): Builder = apply { this.config = config }
        public fun withCreatedBy(createdBy: User): Builder = apply { this.createdBy = createdBy }
        public fun withUnreadCount(unreadCount: Int): Builder = apply { this.unreadCount = unreadCount }
        public fun withTeam(team: String): Builder = apply { this.team = team }
        public fun withHidden(hidden: Boolean?): Builder = apply { this.hidden = hidden }
        public fun withHiddenMessagesBefore(hiddenMessagesBefore: Date?): Builder = apply {
            this.hiddenMessagesBefore = hiddenMessagesBefore
        }
        public fun withCooldown(cooldown: Int): Builder = apply { this.cooldown = cooldown }
        public fun withPinnedMessages(pinnedMessages: List<Message>): Builder = apply {
            this.pinnedMessages = pinnedMessages
        }
        public fun withOwnCapabilities(ownCapabilities: Set<String>): Builder = apply {
            this.ownCapabilities = ownCapabilities
        }
        public fun withMembership(membership: Member?): Builder = apply { this.membership = membership }
        public fun withCachedLatestMessages(cachedLatestMessages: List<Message>): Builder = apply {
            this.cachedLatestMessages = cachedLatestMessages
        }
        public fun withIsInsideSearch(isInsideSearch: Boolean): Builder = apply { this.isInsideSearch = isInsideSearch }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }

        public fun build(): Channel = Channel(
            id = id,
            type = type,
            name = name,
            image = image,
            watcherCount = watcherCount,
            frozen = frozen,
            channelLastMessageAt = channelLastMessageAt,
            createdAt = createdAt,
            deletedAt = deletedAt,
            updatedAt = updatedAt,
            syncStatus = syncStatus,
            memberCount = memberCount,
            messages = messages,
            members = members,
            watchers = watchers,
            read = read,
            config = config,
            createdBy = createdBy,
            unreadCount = unreadCount,
            team = team,
            hidden = hidden,
            hiddenMessagesBefore = hiddenMessagesBefore,
            cooldown = cooldown,
            pinnedMessages = pinnedMessages,
            ownCapabilities = ownCapabilities,
            membership = membership,
            cachedLatestMessages = cachedLatestMessages,
            isInsideSearch = isInsideSearch,
            extraData = extraData,
        )
    }
}

@InternalStreamChatApi
public fun Channel.mergeChannelFromEvent(that: Channel): Channel {
    return copy(
        name = that.name,
        image = that.image,
        hidden = that.hidden,
        frozen = that.frozen,
        team = that.team,
        config = that.config,
        extraData = that.extraData,
        syncStatus = that.syncStatus,
        hiddenMessagesBefore = that.hiddenMessagesBefore,
        memberCount = that.memberCount,
        members = that.members,
        channelLastMessageAt = that.channelLastMessageAt,
        createdAt = that.createdAt,
        updatedAt = that.updatedAt,
        deletedAt = that.deletedAt,
        /* Do not merge (messages, watcherCount, watchers, read, ownCapabilities, membership, unreadCount) fields.
        messages = that.messages,
        watcherCount = that.watcherCount,
        watchers = that.watchers,
        read = that.read,
        ownCapabilities = that.ownCapabilities,
        membership = that.membership,
        unreadCount = that.unreadCount,
         */
    )
}

/**
 * Converts the channel to the channel data.
 */
public fun Channel.toChannelData(): ChannelData {
    return ChannelData(
        type = type,
        id = id,
        name = name,
        image = image,
        frozen = frozen,
        cooldown = cooldown,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        memberCount = memberCount,
        extraData = extraData,
        createdBy = createdBy,
        team = team,
        ownCapabilities = ownCapabilities,
        membership = membership,
    )
}
