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
import io.getstream.chat.android.models.querysort.ComparableFieldProvider
import java.util.Date

/**
 * Model holding data about a message.
 */
@Immutable
public data class Message(
    /**
     * The unique string identifier of the message. This is either created by Stream
     * or set on the client side when the message is added.
     */
    val id: String = "",

    /**
     * Channel unique identifier in <type>:<id> format
     */
    val cid: String = "",

    /**
     * The text of this message
     */
    val text: String = "",

    /**
     * The message text formatted as HTML
     */
    val html: String = "",

    /**
     * The ID of the parent message, if the message is a thread reply
     */
    val parentId: String? = null,

    /**
     * Contains provided slash command
     */
    val command: String? = null,

    /**
     * The list of message attachments
     */
    val attachments: List<Attachment> = listOf(),

    /**
     * The list of user mentioned in the message
     */
    val mentionedUsersIds: List<String> = listOf(),

    /**
     * The list of user mentioned in the message
     */
    val mentionedUsers: List<User> = listOf(),

    /**
     * The number of replies to this message
     */
    val replyCount: Int = 0,

    /**
     * The number of deleted replies to this message
     */
    val deletedReplyCount: Int = 0,

    /**
     * A mapping between reaction type and the count, ie like:10, heart:4
     */
    val reactionCounts: Map<String, Int> = mapOf(),

    /**
     * A mapping between reaction type and the reaction score, ie like:10, heart:4
     */
    val reactionScores: Map<String, Int> = mapOf(),

    /**
     * A mapping between reaction type and the [ReactionGroup].
     */
    val reactionGroups: Map<String, ReactionGroup> = mapOf(),

    /**
     * If the message has been synced to the servers, default is synced
     */
    val syncStatus: SyncStatus = SyncStatus.COMPLETED,

    /**
     * Contains type of the message. Can be one of the following: regular, ephemeral,
     * error, reply, system, deleted.
     */
    val type: String = "",

    /**
     * List of the latest reactions to this message
     */
    val latestReactions: List<Reaction> = listOf(),

    /**
     * List of reactions of authenticated user to this message
     */
    val ownReactions: List<Reaction> = listOf(),

    /**
     * When the message was created
     */
    val createdAt: Date? = null,

    /**
     * When the message was updated
     */
    val updatedAt: Date? = null,

    /**
     * When the message was deleted
     */
    val deletedAt: Date? = null,

    /**
     * When the message was updated locally
     */
    val updatedLocallyAt: Date? = null,

    /**
     * When the message was created locally
     */
    var createdLocallyAt: Date? = null,

    /**
     * The user who sent the message
     */
    val user: User = User(),

    /**
     * All the custom data provided for this message
     */
    override val extraData: Map<String, Any> = mapOf(),

    /**
     * Whether message is silent or not
     */
    val silent: Boolean = false,

    /**
     * If the message was sent by shadow banned user
     */
    val shadowed: Boolean = false,

    /**
     * Mapping with translations. Key `language` contains the original language key.
     * Other keys contain translations.
     */
    val i18n: Map<String, String> = mapOf(),

    /**
     * Whether thread reply should be shown in the channel as well
     */
    val showInChannel: Boolean = false,

    /**
     * Contains information about the channel where the message was sent.
     */
    val channelInfo: ChannelInfo? = null,

    /**
     * Contains quoted message
     */
    val replyTo: Message? = null,

    /**
     * The ID of the quoted message, if the message is a quoted reply.
     */
    val replyMessageId: String? = null,

    /**
     * Whether message is pinned or not
     */
    val pinned: Boolean = false,

    /**
     * Date when the message got pinned
     */
    val pinnedAt: Date? = null,

    /**
     * Date when pinned message expires
     */
    val pinExpires: Date? = null,

    /**
     * Contains user who pinned the message
     */
    val pinnedBy: User? = null,

    /**
     * The list of users who participate in thread
     */
    val threadParticipants: List<User> = emptyList(),

    /**
     * If the message should skip triggering a push notification when sent. Used when sending a new message.
     * False by default.
     *
     * Note: This property is local only, it is not sent to the backend.
     */
    val skipPushNotification: Boolean = false,

    /**
     * If the message should skip enriching the URL. If URl is not enriched, it will not be
     * displayed as a link attachment. Used when sending or updating a message. False by default.
     *
     * Note: This property is local only, it is not sent to the backend.
     */
    val skipEnrichUrl: Boolean = false,

    /**
     * Contains moderation details of the message. (used by moderation v1)
     */
    val moderationDetails: MessageModerationDetails? = null,

    /**
     * Contains moderation details of the message. (used by moderation v2)
     */
    val moderation: Moderation? = null,

    /**
     * Date when the message text was updated
     */
    val messageTextUpdatedAt: Date? = null,

    /**
     * Contains poll configuration
     */
    val poll: Poll? = null,

    /**
     * List of user ids that are allowed to see the message
     */
    val restrictedVisibility: List<String> = emptyList(),

    /**
     * The reminder information for this message if it has one set up.
     */
    val reminder: MessageReminderInfo? = null,

    /**
     * Location shared by the user in the message.
     */
    val sharedLocation: Location? = null,

    /**
     * The role of the member(who sent the message) in the channel.
     */
    val channelRole: String? = null,
) : CustomObject, ComparableFieldProvider {
    public companion object {
        /**
         * Represents a 'regular' message.
         */
        public const val TYPE_REGULAR: String = "regular"

        /**
         * Represents an 'ephemeral' message.
         */
        public const val TYPE_EPHEMERAL: String = "ephemeral"

        /**
         * Represents an 'error' message.
         */
        public const val TYPE_ERROR: String = "error"
    }

    @Suppress("ComplexMethod")
    override fun getComparableField(fieldName: String): Comparable<*>? =
        when (fieldName) {
            "id" -> id
            "cid" -> cid
            "text" -> text
            "html" -> html
            "parent_id", "parentId" -> parentId
            "command" -> command
            "reply_count", "replyCount" -> replyCount
            "deleted_reply_count", "deletedReplyCount" -> deletedReplyCount
            "type" -> type
            "created_at", "createdAt" -> createdAt
            "updated_at", "updatedAt" -> updatedAt
            "deleted_at", "deletedAt" -> deletedAt
            "updated_locally_at", "updatedLocallyAt" -> updatedLocallyAt
            "created_locally_at", "createdLocallyAt" -> createdLocallyAt
            "silent" -> silent
            "shadowed" -> shadowed
            "pinned" -> pinned
            "pinned_at", "pinnedAt" -> pinnedAt
            "pin_expires", "pinExpires" -> pinExpires
            else -> extraData[fieldName] as? Comparable<*>
        }

    /**
     * Retrieves the translated text message for the given []language].
     *
     * @param language The language code for the translation.
     * @return The translated text message, or empty if the translation is not available.
     */
    public fun getTranslation(language: String): String = i18n.get("${language}_text", "")

    /**
     * Retrieves the original language of the message.
     */
    public val originalLanguage: String
        get() = i18n.get("language", "")

    private fun <A, B> Map<A, B>.get(key: A, default: B): B {
        return get(key) ?: default
    }

    /**
     * Identifier of message. The message can't be considered the same if the id of the message AND the id of a
     * quoted message are not the same.
     */
    @Suppress("MagicNumber")
    public fun identifierHash(): Long {
        var result = id.hashCode()

        replyTo?.id.hashCode().takeIf { it != 0 }?.let { replyHash ->
            result = 31 * result + replyHash
        }

        return result.toLong()
    }

    override fun toString(): String = StringBuilder().apply {
        append("Message(")
        append("type=\"").append(type).append("\"")
        append(", id=\"").append(id).append("\"")
        append(", text=\"").append(text).append("\"")
        append(", html=\"").append(html).append("\"")
        append(", cid=\"").append(cid).append("\"")
        if (parentId != null) append(", parentId=").append(parentId)
        if (command != null) append(", command=").append(command)
        if (attachments.isNotEmpty()) append(", attachments=").append(attachments)
        if (mentionedUsersIds.isNotEmpty()) append(", mentionedUsersIds=").append(mentionedUsersIds)
        if (mentionedUsers.isNotEmpty()) append(", mentionedUsers=").append(mentionedUsers)
        if (replyCount > 0) append(", replyCount=").append(replyCount)
        if (deletedReplyCount > 0) append(", deletedReplyCount=").append(deletedReplyCount)
        if (reactionCounts.isNotEmpty()) append(", reactionCounts=").append(reactionCounts)
        if (reactionScores.isNotEmpty()) append(", reactionScores=").append(reactionScores)
        append(", syncStatus=").append(syncStatus)
        if (latestReactions.isNotEmpty()) append(", latestReactions=").append(latestReactions)
        if (ownReactions.isNotEmpty()) append(", ownReactions=").append(ownReactions)
        if (createdAt != null) append(", createdAt=").append(createdAt)
        if (updatedAt != null) append(", updatedAt=").append(updatedAt)
        if (deletedAt != null) append(", deletedAt=").append(deletedAt)
        if (updatedLocallyAt != null) append(", updatedLocallyAt=").append(updatedLocallyAt)
        if (createdLocallyAt != null) append(", createdLocallyAt=").append(createdLocallyAt)
        append(", sentBy=").append("User(id=\"").append(user.id).append("\", name=\"").append(user.name).append("\")")
        append(", silent=").append(silent)
        append(", shadowed=").append(shadowed)
        if (i18n.isNotEmpty()) append(", i18n=").append(i18n)
        append(", showInChannel=").append(showInChannel)
        if (channelInfo != null) append(", channelInfo=").append(channelInfo)
        if (replyMessageId != null) append(", replyMessageId=").append(replyMessageId)
        if (replyTo != null) append(", replyTo=").append(replyTo)
        append(", pinned=").append(pinned)
        if (pinnedAt != null) append(", pinnedAt=").append(pinnedAt)
        if (pinExpires != null) append(", pinExpires=").append(pinExpires)
        if (pinnedBy != null) append(", pinnedBy=").append(pinnedBy)
        if (threadParticipants.isNotEmpty()) append(", threadParticipants=").append(threadParticipants)
        append(", skipPushNotification=").append(skipPushNotification)
        append(", skipEnrichUrl=").append(skipEnrichUrl)
        if (moderationDetails != null) append(", moderationDetails=").append(moderationDetails)
        if (moderation != null) append(", moderation=").append(moderation)
        if (poll != null) append(", poll=").append(poll)
        if (channelRole != null) append(", channelRole=").append(channelRole)
        if (extraData.isNotEmpty()) append(", extraData=").append(extraData)
        append(")")
    }.toString()

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    @Suppress("TooManyFunctions")
    public class Builder() {
        private var id: String = ""
        private var cid: String = ""
        private var text: String = ""
        private var html: String = ""
        private var parentId: String? = null
        private var command: String? = null
        private var attachments: List<Attachment> = listOf()
        private var mentionedUsersIds: List<String> = listOf()
        private var mentionedUsers: List<User> = listOf()
        private var replyCount: Int = 0
        private var deletedReplyCount: Int = 0
        private var reactionCounts: Map<String, Int> = mapOf()
        private var reactionScores: Map<String, Int> = mapOf()
        private var reactionGroups: Map<String, ReactionGroup> = mapOf()
        private var syncStatus: SyncStatus = SyncStatus.COMPLETED
        private var type: String = ""
        private var latestReactions: List<Reaction> = listOf()
        private var ownReactions: List<Reaction> = listOf()
        private var createdAt: Date? = null
        private var updatedAt: Date? = null
        private var deletedAt: Date? = null
        private var updatedLocallyAt: Date? = null
        private var createdLocallyAt: Date? = null
        private var user: User = User()
        private var extraData: Map<String, Any> = mapOf()
        private var silent: Boolean = false
        private var shadowed: Boolean = false
        private var i18n: Map<String, String> = mapOf()
        private var showInChannel: Boolean = false
        private var channelInfo: ChannelInfo? = null
        private var replyTo: Message? = null
        private var replyMessageId: String? = null
        private var pinned: Boolean = false
        private var pinnedAt: Date? = null
        private var pinExpires: Date? = null
        private var pinnedBy: User? = null
        private var threadParticipants: List<User> = emptyList()
        private var skipPushNotification: Boolean = false
        private var skipEnrichUrl: Boolean = false
        private var moderationDetails: MessageModerationDetails? = null
        private var moderation: Moderation? = null
        private var messageTextUpdatedAt: Date? = null
        private var poll: Poll? = null
        private var restrictedVisibility: List<String> = emptyList()
        private var reminder: MessageReminderInfo? = null
        private var sharedLocation: Location? = null
        private var channelRole: String? = null

        public constructor(message: Message) : this() {
            id = message.id
            cid = message.cid
            text = message.text
            html = message.html
            parentId = message.parentId
            command = message.command
            attachments = message.attachments
            mentionedUsersIds = message.mentionedUsersIds
            mentionedUsers = message.mentionedUsers
            replyCount = message.replyCount
            deletedReplyCount = message.deletedReplyCount
            reactionCounts = message.reactionCounts
            reactionScores = message.reactionScores
            reactionGroups = message.reactionGroups
            syncStatus = message.syncStatus
            type = message.type
            latestReactions = message.latestReactions
            ownReactions = message.ownReactions
            createdAt = message.createdAt
            updatedAt = message.updatedAt
            deletedAt = message.deletedAt
            updatedLocallyAt = message.updatedLocallyAt
            createdLocallyAt = message.createdLocallyAt
            user = message.user
            extraData = message.extraData
            silent = message.silent
            shadowed = message.shadowed
            i18n = message.i18n
            showInChannel = message.showInChannel
            channelInfo = message.channelInfo
            replyTo = message.replyTo
            replyMessageId = message.replyMessageId
            pinned = message.pinned
            pinnedAt = message.pinnedAt
            pinExpires = message.pinExpires
            pinnedBy = message.pinnedBy
            threadParticipants = message.threadParticipants
            skipPushNotification = message.skipPushNotification
            skipEnrichUrl = message.skipEnrichUrl
            moderationDetails = message.moderationDetails
            moderation = message.moderation
            messageTextUpdatedAt = message.messageTextUpdatedAt
            poll = message.poll
            restrictedVisibility = message.restrictedVisibility
            reminder = message.reminder
            sharedLocation = message.sharedLocation
            channelRole = message.channelRole
        }

        public fun withId(id: String): Builder = apply { this.id = id }
        public fun withCid(cid: String): Builder = apply { this.cid = cid }
        public fun withText(text: String): Builder = apply { this.text = text }
        public fun withHtml(html: String): Builder = apply { this.html = html }
        public fun withParentId(parentId: String?): Builder = apply { this.parentId = parentId }
        public fun withCommand(command: String?): Builder = apply { this.command = command }
        public fun withAttachments(attachments: List<Attachment>): Builder = apply { this.attachments = attachments }
        public fun withMentionedUsersIds(mentionedUsersIds: List<String>): Builder = apply {
            this.mentionedUsersIds = mentionedUsersIds
        }
        public fun withMentionedUsers(mentionedUsers: List<User>): Builder = apply {
            this.mentionedUsers = mentionedUsers
        }
        public fun withReplyCount(replyCount: Int): Builder = apply { this.replyCount = replyCount }
        public fun withDeletedReplyCount(deletedReplyCount: Int): Builder =
            apply { this.deletedReplyCount = deletedReplyCount }
        public fun withReactionCounts(reactionCounts: Map<String, Int>): Builder = apply {
            this.reactionCounts = reactionCounts
        }
        public fun withReactionScores(reactionScores: Map<String, Int>): Builder = apply {
            this.reactionScores = reactionScores
        }

        public fun withReactionGroups(reactionGroups: Map<String, ReactionGroup>): Builder = apply {
            this.reactionGroups = reactionGroups
        }

        public fun withSyncStatus(syncStatus: SyncStatus): Builder = apply { this.syncStatus = syncStatus }
        public fun withType(type: String): Builder = apply { this.type = type }
        public fun withLatestReactions(latestReactions: List<Reaction>): Builder = apply {
            this.latestReactions = latestReactions
        }
        public fun withOwnReactions(ownReactions: List<Reaction>): Builder = apply { this.ownReactions = ownReactions }
        public fun withCreatedAt(createdAt: Date?): Builder = apply { this.createdAt = createdAt }
        public fun withUpdatedAt(updatedAt: Date?): Builder = apply { this.updatedAt = updatedAt }
        public fun withDeletedAt(deletedAt: Date?): Builder = apply { this.deletedAt = deletedAt }
        public fun withUpdatedLocallyAt(updatedLocallyAt: Date?): Builder = apply {
            this.updatedLocallyAt = updatedLocallyAt
        }
        public fun withCreatedLocallyAt(createdLocallyAt: Date?): Builder = apply {
            this.createdLocallyAt = createdLocallyAt
        }
        public fun withUser(user: User): Builder = apply { this.user = user }
        public fun withExtraData(extraData: Map<String, Any>): Builder = apply { this.extraData = extraData }
        public fun withSilent(silent: Boolean): Builder = apply { this.silent = silent }
        public fun withShadowed(shadowed: Boolean): Builder = apply { this.shadowed = shadowed }
        public fun withI18n(i18n: Map<String, String>): Builder = apply { this.i18n = i18n }
        public fun withShowInChannel(showInChannel: Boolean): Builder = apply { this.showInChannel = showInChannel }
        public fun withChannelInfo(channelInfo: ChannelInfo?): Builder = apply { this.channelInfo = channelInfo }
        public fun withReplyTo(replyTo: Message?): Builder = apply { this.replyTo = replyTo }
        public fun withReplyMessageId(replyMessageId: String?): Builder = apply { this.replyMessageId = replyMessageId }
        public fun withPinned(pinned: Boolean): Builder = apply { this.pinned = pinned }
        public fun withPinnedAt(pinnedAt: Date?): Builder = apply { this.pinnedAt = pinnedAt }
        public fun withPinExpires(pinExpires: Date?): Builder = apply { this.pinExpires = pinExpires }
        public fun withPinnedBy(pinnedBy: User?): Builder = apply { this.pinnedBy = pinnedBy }
        public fun withThreadParticipants(threadParticipants: List<User>): Builder = apply {
            this.threadParticipants = threadParticipants
        }
        public fun withSkipPushNotification(skipPushNotification: Boolean): Builder = apply {
            this.skipPushNotification = skipPushNotification
        }
        public fun withSkipEnrichUrl(skipEnrichUrl: Boolean): Builder = apply { this.skipEnrichUrl = skipEnrichUrl }
        public fun withModerationDetails(moderationDetails: MessageModerationDetails): Builder = apply {
            this.moderationDetails = moderationDetails
        }
        public fun withModeration(moderation: Moderation): Builder = apply { this.moderation = moderation }
        public fun withMessageTextUpdatedAt(messageTextUpdatedAt: Date?): Builder = apply {
            this.messageTextUpdatedAt = messageTextUpdatedAt
        }

        public fun withPoll(poll: Poll?): Builder = apply { this.poll = poll }
        public fun withRestrictedVisibility(restrictedVisibility: List<String>): Builder = apply {
            this.restrictedVisibility = restrictedVisibility
        }
        public fun withReminder(reminder: MessageReminderInfo?): Builder = apply { this.reminder = reminder }
        public fun withSharedLocation(sharedLocation: Location?): Builder = apply {
            this.sharedLocation = sharedLocation
        }
        public fun withChannelRole(channelRole: String?): Builder = apply { this.channelRole = channelRole }

        public fun build(): Message {
            return Message(
                id = id,
                cid = cid,
                text = text,
                html = html,
                parentId = parentId,
                command = command,
                attachments = attachments,
                mentionedUsersIds = mentionedUsersIds,
                mentionedUsers = mentionedUsers,
                replyCount = replyCount,
                deletedReplyCount = deletedReplyCount,
                reactionCounts = reactionCounts,
                reactionScores = reactionScores,
                reactionGroups = reactionGroups,
                syncStatus = syncStatus,
                type = type,
                latestReactions = latestReactions,
                ownReactions = ownReactions,
                createdAt = createdAt,
                updatedAt = updatedAt,
                deletedAt = deletedAt,
                updatedLocallyAt = updatedLocallyAt,
                createdLocallyAt = createdLocallyAt,
                user = user,
                extraData = extraData,
                silent = silent,
                shadowed = shadowed,
                i18n = i18n,
                showInChannel = showInChannel,
                channelInfo = channelInfo,
                replyTo = replyTo,
                replyMessageId = replyMessageId,
                pinned = pinned,
                pinnedAt = pinnedAt,
                pinExpires = pinExpires,
                pinnedBy = pinnedBy,
                threadParticipants = threadParticipants,
                skipPushNotification = skipPushNotification,
                skipEnrichUrl = skipEnrichUrl,
                moderationDetails = moderationDetails,
                moderation = moderation,
                messageTextUpdatedAt = messageTextUpdatedAt,
                restrictedVisibility = restrictedVisibility,
                poll = poll,
                reminder = reminder,
                sharedLocation = sharedLocation,
                channelRole = channelRole,
            )
        }
    }
}
