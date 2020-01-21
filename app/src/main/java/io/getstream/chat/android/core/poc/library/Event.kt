package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


class Event : UserEntity {
    @SerializedName("connection_id")
    
    var connectionId: String = ""
    @SerializedName("cid")
    
    val cid: String = ""
    @SerializedName("client_id")
    
    var clientId: String = ""
    @SerializedName("type")
    
    private var type: String = ""
    @SerializedName("user")
    
    val user: User? = null
    @SerializedName("me")
    
    val me: User? = null
    @SerializedName("member")
    
    val member: Member? = null
    @SerializedName("message")
    
    var message: Message? = null
    @SerializedName("reaction")
    
    val reaction: Reaction? = null
    @SerializedName("channel")
    
    val channel: Channel? = null

    @SerializedName("total_unread_count")
    
    val totalUnreadCount: Number = 0

    @SerializedName("unread_channels")
    
    val unreadChannels: Number = 0

    @SerializedName("watcher_count")
    
    val watcherCount: Number = 0

    @SerializedName("created_at")
    
    val createdAt: Date = UndefinedDate

    @SerializedName("clear_history")
    
    var clearHistory: Boolean = false

    var receivedAt: Date = UndefinedDate

    var online = false

    constructor(type: EventType) {
        this.type = type.label
    }

    constructor(type: EventType, online: Boolean) {
        this.online = online
        this.type = type.label
    }

    val isChannelEvent: Boolean
        get() = cid != "*"

    fun getType(): EventType {
        return EventType.findByString(type)
    }

    fun setType(type: String) {
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
        return "Event(type=$type)"
    }


}