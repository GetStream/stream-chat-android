package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.api.models.CustomObject
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.*


data class Message(
    var id: String = "",
    var cid: String = "",
    var text: String = "",
    val html: String = "",
    @SerializedName("parent_id")
    var parentId: String? = null,
    var command: String? = null,
    var isStartDay: Boolean = false,
    var isYesterday: Boolean = false,
    var isToday: Boolean = false,
    var date: String = "",
    var time: String = "",
    var commandInfo: Map<String, String> = mutableMapOf(),
    var attachments: MutableList<Attachment> = mutableListOf(),
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
    var user: User = User(),

    @IgnoreSerialisation
    var channel: Channel = Channel(),

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf()

) : CustomObject