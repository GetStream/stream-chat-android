package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


class Event : UserEntity {
    @SerializedName("connection_id")
    @Expose
    var connectionId: String = ""
    @SerializedName("cid")
    @Expose
    val cid: String? = null
    @SerializedName("client_id")
    @Expose
    var clientId: String? = null
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
    val totalUnreadCount: Number? = null
    @SerializedName("unread_channels")
    @Expose
    val unreadChannels: Number? = null
    @SerializedName("watcher_count")
    @Expose
    val watcherCount: Number? = null
    @SerializedName("created_at")
    @Expose
    val createdAt: Long = 0
    @SerializedName("clear_history")
    @Expose
    var clearHistory: Boolean? = null
    var receivedAt: Date? = null
    var online = false
        private set

    constructor() {}
    constructor(type: String?) {
        this.type = type
    }

    constructor(online: Boolean) {
        this.online = online
        setType(EventType.CONNECTION_CHANGED)
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

}