package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.*

class ConnectedEvent : ChatEvent() {

    val cid: String = ""
    lateinit var user: User
    lateinit var me: User
    val member: Member? = null
    val reaction: Reaction? = null
    val channel: Channel? = null
    var online = false

    @SerializedName("connection_id")
    var connectionId: String = ""
    @SerializedName("client_id")
    var clientId: String = ""
    @SerializedName("watcher_count")
    val watcherCount: Number = 0
    @SerializedName("clear_history")
    var clearHistory: Boolean = false

    val isChannelEvent: Boolean
        get() = cid != "*"

    val isAnonymous: Boolean
        get() = if (me != null) {
            me.id == "!anon"
        } else true
}