package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.SerializedName


class Message {
    val id: String = ""


    private val cid: String? = null

    @SerializedName("text")
    private val text: String? = null

    @SerializedName("html")
    private val html: String? = null

    @SerializedName("type")
    private val type: String? = null

    private val syncStatus: Int? = null


    @SerializedName("user")
    private val user: User? = null


    @SerializedName("channel")
    private val channel: Channel? = null

    private val userID: String? = null

    private val attachments: List<Attachment>? = null

    @SerializedName("latest_reactions")
    private val latestReactions: List<Reaction>? = null

    @SerializedName("own_reactions")
    private val ownReactions: List<Reaction>? = null

    @SerializedName("reply_count")
    private val replyCount = 0

    @SerializedName("created_at")
    private val createdAt: Long = 0

    @SerializedName("updated_at")
    private val updatedAt: Long = 0


    private val deletedAt: Long = 0

    @SerializedName("mentioned_users")
    private val mentionedUsers: List<User>? =
        null


    private val mentionedUsersId: List<String>? = null

    private val reactionCounts: Map<String, Int>? = null

    private val parentId: String? = null

    private val command: String? = null

    private val commandInfo: Map<String, String>? = null


    private val extraData: HashMap<String, Any>? = null

    private val isStartDay = false
    private val isYesterday = false
    private val isToday = false
    private val date: String? = null
    private var time: String? = null
}