package io.getstream.chat.android.client.models

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

    @InternalStreamChatApi
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
) : CustomObject {
    public companion object {
        public const val TYPE_REGULAR: String = "regular"
        public const val TYPE_EPHEMERAL: String = "ephemeral"
    }
}
