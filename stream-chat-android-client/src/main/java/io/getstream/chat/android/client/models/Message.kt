package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

public data class Message(
    var id: String = "",
    var cid: String = "",
    var text: String = "",
    val html: String = "",
    @SerializedName("parent_id")
    var parentId: String? = null,
    var command: String? = null,
    var attachments: MutableList<Attachment> = mutableListOf(),

    @IgnoreDeserialisation
    @SerializedName("mentioned_users")
    var mentionedUsersIds: MutableList<String> = mutableListOf(),

    @IgnoreSerialisation
    @SerializedName("mentioned_users")
    var mentionedUsers: MutableList<User> = mutableListOf(),

    @IgnoreSerialisation
    @SerializedName("reply_count")
    var replyCount: Int = 0,

    @SerializedName("reaction_counts")
    @IgnoreSerialisation
    var reactionCounts: MutableMap<String, Int> = mutableMapOf(),

    @SerializedName("reaction_scores")
    @IgnoreSerialisation
    var reactionScores: MutableMap<String, Int> = mutableMapOf(),

    /** if the message has been synced to the servers */
    @IgnoreSerialisation
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    @IgnoreSerialisation
    var type: String = "",

    @IgnoreSerialisation
    @SerializedName("latest_reactions")
    var latestReactions: MutableList<Reaction> = mutableListOf(),

    @IgnoreSerialisation
    @SerializedName("own_reactions")
    var ownReactions: MutableList<Reaction> = mutableListOf(),

    @IgnoreSerialisation
    @SerializedName("created_at")
    var createdAt: Date? = null,
    @IgnoreSerialisation
    @SerializedName("updated_at")
    var updatedAt: Date? = null,
    @IgnoreSerialisation
    @SerializedName("deleted_at")
    var deletedAt: Date? = null,
    @IgnoreSerialisation
    @IgnoreDeserialisation
    var updatedLocallyAt: Date? = null,
    @IgnoreSerialisation
    @IgnoreDeserialisation
    var createdLocallyAt: Date? = null,

    @IgnoreSerialisation
    var user: User = User(),

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf(),

    var silent: Boolean = false,

    var shadowed: Boolean = false,

    @IgnoreSerialisation
    val i18n: Map<String, String> = mapOf(),

    @SerializedName("show_in_channel")
    var showInChannel: Boolean = false,

    @IgnoreSerialisation
    @SerializedName("channel")
    @InternalStreamChatApi
    var channelInfo: ChannelInfo? = null,

    @SerializedName("quoted_message")
    var replyTo: Message? = null,

    @SerializedName("quoted_message_id")
    var replyMessageId: String? = null,

    var pinned: Boolean = false,
    @SerializedName("pinned_at")
    var pinnedAt: Date? = null,
    @SerializedName("pin_expires")
    var pinExpires: Date? = null,
    @SerializedName("pinned_by")
    var pinnedBy: User? = null,

    @SerializedName("thread_participants")
    var threadParticipants: List<User> = emptyList(),
) : CustomObject
