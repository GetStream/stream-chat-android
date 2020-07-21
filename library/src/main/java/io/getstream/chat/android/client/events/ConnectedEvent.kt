package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User

class ConnectedEvent : ChatEvent() {

    lateinit var me: User
    var online = false

    @SerializedName("connection_id")
    var connectionId: String = ""

    @SerializedName("client_id")
    var clientId: String = ""

    @SerializedName("watcher_count")
    val watcherCount: Number = 0

    val isChannelEvent: Boolean
        get() = cid != "*"

    val isAnonymous: Boolean
        get() = me.id == "!anon"

    /**
     * Backend doesn't have dedicated type for connection (first) event, but it's the same type [EventType.HEALTH_CHECK]
     * Also backend doesn't guarantee that first message is connection, but not regular health check, so it must be checked if [me] exits
     */
    fun isValid(): Boolean {
        return ::me.isInitialized
    }
}
