package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


data class Event(
    private var type: String = ""
) : UserEntity {

    val cid: String = ""
    val user: User? = null
    val me: User? = null
    val member: Member? = null
    var message: Message? = null
    val reaction: Reaction? = null
    val channel: Channel? = null
    var receivedAt: Date = UndefinedDate
    var online = false

    @SerializedName("connection_id")
    var connectionId: String = ""
    @SerializedName("client_id")
    var clientId: String = ""
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

    constructor(type: EventType) : this(type.label) {
        this.type = type.label
    }

    constructor(type: EventType, online: Boolean) : this(type.label) {
        this.online = online
        this.type = type.label
    }

    val isChannelEvent: Boolean
        get() = cid != "*"

    fun getType(): EventType {
        return EventType.values().firstOrNull {
            it.label == type
        } ?: EventType.UNKNOWN
    }

    val isAnonymous: Boolean
        get() = if (me != null) {
            me.id == "!anon"
        } else true

    override fun getUserId(): String {
        return user!!.id
    }



}