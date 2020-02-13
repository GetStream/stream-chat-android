package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import java.util.*


open class ChatEvent(val type: String = "") {

    @SerializedName("created_at")
    val createdAt: Date? = null

    @SerializedName("total_unread_count")
    val totalUnreadCount: Int? = null

    @SerializedName("unread_channels")
    val unreadChannels: Int? = null

    lateinit var message: Message
    var receivedAt: Date = Date()
}