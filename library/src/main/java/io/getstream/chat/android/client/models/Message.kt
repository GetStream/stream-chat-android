package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.*


class Message : UserEntity {

    var id: String = ""
    var cid: String = ""
    var text: String = ""
    val html: String = ""

    @IgnoreSerialisation
    lateinit var user: User

    /** if the message has been synced to the servers */
    @IgnoreSerialisation
    var syncStatus: SyncStatus = SyncStatus.SYNCED

    @IgnoreSerialisation
    lateinit var channel: Channel

    var attachments = mutableListOf<Attachment>()

    @IgnoreSerialisation
    var type: String = ""
    @IgnoreSerialisation
    @SerializedName("latest_reactions")
    var latestReactions = mutableListOf<Reaction>()
    @IgnoreSerialisation
    @SerializedName("own_reactions")
    var ownReactions = mutableListOf<Reaction>()
    @IgnoreSerialisation
    @SerializedName("reply_count")
    var replyCount = 0

    @IgnoreSerialisation
    @SerializedName("created_at")
    var createdAt: Date? = null
    @IgnoreSerialisation
    @SerializedName("updated_at")
    var updatedAt: Date? = null
    @IgnoreSerialisation
    @SerializedName("deleted_at")
    var deletedAt: Date? = null
    @SerializedName("mentioned_users")
    var mentionedUsers = mutableListOf<User>()
    @SerializedName("parent_id")
    var parentId: String? = null

    @SerializedName("reaction_counts")
    @IgnoreSerialisation
    var reactionCounts = mutableMapOf<String, Int>()

    var command: String? = null

    var commandInfo: Map<String, String>? = null

    @IgnoreSerialisation
    @IgnoreDeserialisation
    var extraData = mutableMapOf<String, Any>()

    var isStartDay = false
    var isYesterday = false
    var isToday = false
    var date: String = ""
    var time: String = ""

    override fun getUserId(): String {
        return user!!.id
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (cid != other.cid) return false
        if (text != other.text) return false
        if (html != other.html) return false
        if (user != other.user) return false
        if (attachments != other.attachments) return false
        if (type != other.type) return false
        if (latestReactions != other.latestReactions) return false
        if (ownReactions != other.ownReactions) return false
        if (replyCount != other.replyCount) return false
        if (createdAt != other.createdAt) return false
        if (updatedAt != other.updatedAt) return false
        if (deletedAt != other.deletedAt) return false
        if (mentionedUsers != other.mentionedUsers) return false
        if (parentId != other.parentId) return false
        if (command != other.command) return false
        if (commandInfo != other.commandInfo) return false
        if (extraData != other.extraData) return false
        if (isStartDay != other.isStartDay) return false
        if (isYesterday != other.isYesterday) return false
        if (isToday != other.isToday) return false
        if (date != other.date) return false
        if (time != other.time) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cid.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + html.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + attachments.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + latestReactions.hashCode()
        result = 31 * result + ownReactions.hashCode()
        result = 31 * result + replyCount
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + updatedAt.hashCode()
        result = 31 * result + deletedAt.hashCode()
        result = 31 * result + mentionedUsers.hashCode()
        result = 31 * result + (parentId?.hashCode() ?: 0)
        result = 31 * result + (command?.hashCode() ?: 0)
        result = 31 * result + (commandInfo?.hashCode() ?: 0)
        result = 31 * result + extraData.hashCode()
        result = 31 * result + isStartDay.hashCode()
        result = 31 * result + isYesterday.hashCode()
        result = 31 * result + isToday.hashCode()
        result = 31 * result + date.hashCode()
        result = 31 * result + time.hashCode()
        return result
    }


}