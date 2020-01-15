package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


class Event : UserEntity {
    @SerializedName("connection_id")
    @Expose
    var connectionId: String = ""
    @SerializedName("cid")
    @Expose
    val cid: String = ""
    @SerializedName("client_id")
    @Expose
    var clientId: String = ""
    @SerializedName("type")
    @Expose
    private var type: String? = null
    @SerializedName("user")
    @Expose
    val user: User? = null
    @SerializedName("me")
    @Expose
    val me: User? = null
    @SerializedName("member")
    @Expose
    val member: Member? = null
    @SerializedName("message")
    @Expose
    var message: Message? = null
    @SerializedName("reaction")
    @Expose
    val reaction: Reaction? = null
    @SerializedName("channel")
    @Expose
    val channel: Channel? = null
    @SerializedName("total_unread_count")
    @Expose
    val totalUnreadCount: Number = 0
    @SerializedName("unread_channels")
    @Expose
    val unreadChannels: Number = 0
    @SerializedName("watcher_count")
    @Expose
    val watcherCount: Number = 0
    @SerializedName("created_at")
    @Expose
    val createdAt: Date = UndefinedDate
    @SerializedName("clear_history")
    @Expose
    var clearHistory: Boolean = false
    var receivedAt: Date = UndefinedDate
    var online = false

    constructor() {

    }

    constructor(type: EventType) {
        this.type = type.label
    }

    constructor(type: EventType, online: Boolean) {
        this.online = online
        this.type = type.label
    }

    val isChannelEvent: Boolean
        get() = cid != null && cid != "*"

    fun getType(): EventType {
        return EventType.findByString(type)
    }

    fun setType(type: String?) {
        this.type = type
    }

    fun setType(type: EventType) {
        this.type = type.label
    }

    val isAnonymous: Boolean
        get() = if (me != null) {
            me.id == "!anon"
        } else true

    override fun getUserId(): String {
        return user!!.id
    }

    override fun toString(): String {
        return "Event(type=$type, online=$online)"
    }


}