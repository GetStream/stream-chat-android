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
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

public data class Message(
    /**
     * The unique string identifier of the message. This is either created by Stream
     * or set on the client side when the message is added.
     */
    var id: String = "",

    /**
     * Channel unique identifier in <type>:<id> format
     */
    var cid: String = "",

    /**
     * The text of this message
     */
    var text: String = "",

    /**
     * The message text formatted as HTML
     */
    val html: String = "",

    /**
     * The ID of the parent message, if the message is a thread reply
     */
    var parentId: String? = null,

    /**
     * Contains provided slash command
     */
    var command: String? = null,

    /**
     * The list of message attachments
     */
    var attachments: MutableList<Attachment> = mutableListOf(),

    /**
     * The list of user mentioned in the message
     */
    var mentionedUsersIds: MutableList<String> = mutableListOf(),

    /**
     * The list of user mentioned in the message
     */
    var mentionedUsers: MutableList<User> = mutableListOf(),

    /**
     * The number of replies to this message
     */
    var replyCount: Int = 0,

    /**
     * A mapping between reaction type and the count, ie like:10, heart:4
     */
    var reactionCounts: MutableMap<String, Int> = mutableMapOf(),

    /**
     * A mapping between reaction type and the reaction score, ie like:10, heart:4
     */
    var reactionScores: MutableMap<String, Int> = mutableMapOf(),

    /**
     * If the message has been synced to the servers, default is synced
     */
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    /**
     * Contains details related to [syncStatus].
     */
    var syncDescription: MessageSyncDescription? = null,

    /**
     * Contains type of the message. Can be one of the following: regular, ephemeral,
     * error, reply, system, deleted.
     */
    var type: String = "",

    /**
     * List of the latest reactions to this message
     */
    var latestReactions: MutableList<Reaction> = mutableListOf(),

    /**
     * List of reactions of authenticated user to this message
     */
    var ownReactions: MutableList<Reaction> = mutableListOf(),

    /**
     * When the message was created
     */
    var createdAt: Date? = null,

    /**
     * When the message was updated
     */
    var updatedAt: Date? = null,

    /**
     * When the message was deleted
     */
    var deletedAt: Date? = null,

    /**
     * When the message was updated locally
     */
    var updatedLocallyAt: Date? = null,

    /**
     * When the message was created locally
     */
    var createdLocallyAt: Date? = null,

    /**
     * The user who sent the message
     */
    var user: User = User(),

    /**
     * All the custom data provided for this message
     */
    override var extraData: MutableMap<String, Any> = mutableMapOf(),

    /**
     * Whether message is silent or not
     */
    var silent: Boolean = false,

    /**
     * If the message was sent by shadow banned user
     */
    var shadowed: Boolean = false,

    /**
     * Mapping with translations. Key `language` contains the original language key.
     * Other keys contain translations.
     */
    val i18n: Map<String, String> = mapOf(),

    /**
     * Whether thread reply should be shown in the channel as well
     */
    var showInChannel: Boolean = false,

    @property:InternalStreamChatApi
    var channelInfo: ChannelInfo? = null,

    /**
     * Contains quoted message
     */
    var replyTo: Message? = null,

    /**
     * The ID of the quoted message, if the message is a quoted reply.
     */
    var replyMessageId: String? = null,

    /**
     * Whether message is pinned or not
     */
    var pinned: Boolean = false,

    /**
     * Date when the message got pinned
     */
    var pinnedAt: Date? = null,

    /**
     * Date when pinned message expires
     */
    var pinExpires: Date? = null,

    /**
     * Contains user who pinned the message
     */
    var pinnedBy: User? = null,

    /**
     * The list of users who participate in thread
     */
    var threadParticipants: List<User> = emptyList(),

    /**
     * If the message should skip triggering a push notification when sent. Used when sending a new message.
     * False by default.
     *
     * Note: This property is local only, it is not sent to the backend.
     */
    var skipPushNotification: Boolean = false,

    /**
     * If the message should skip enriching the URL. If URl is not enriched, it will not be
     * displayed as a link attachment. Used when sending or updating a message. False by default.
     *
     * Note: This property is local only, it is not sent to the backend.
     */
    var skipEnrichUrl: Boolean = false,

) : CustomObject, ComparableFieldProvider {
    public companion object {
        public const val TYPE_REGULAR: String = "regular"
        public const val TYPE_EPHEMERAL: String = "ephemeral"
    }

    @Suppress("ComplexMethod")
    override fun getComparableField(fieldName: String): Comparable<*>? =
        when (fieldName) {
            "id" -> id
            "cid" -> cid
            "text" -> text
            "html" -> html
            "parentId" -> parentId
            "command" -> command
            "replyCount" -> replyCount
            "type" -> type
            "createdAt" -> createdAt
            "updatedAt" -> updatedAt
            "deletedAt" -> deletedAt
            "updatedLocallyAt" -> updatedLocallyAt
            "createdLocallyAt" -> createdLocallyAt
            "silent" -> silent
            "shadowed" -> shadowed
            "pinned" -> pinned
            "pinnedAt" -> pinnedAt
            "pinExpires" -> pinExpires
            else -> extraData[fieldName] as? Comparable<*>
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
        if (reactionCounts.isNotEmpty()) append(", reactionCounts=").append(reactionCounts)
        if (reactionScores.isNotEmpty()) append(", reactionScores=").append(reactionScores)
        append(", syncStatus=").append(syncStatus)
        if (syncDescription != null) append(", syncDescription=").append(syncDescription)
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
        if (extraData.isNotEmpty()) append(", extraData=").append(extraData)
        append(")")
    }.toString()
}
