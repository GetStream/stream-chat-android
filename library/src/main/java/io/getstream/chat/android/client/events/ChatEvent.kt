package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import java.util.*


open class ChatEvent(val type: String = "") {

    var cid: String? = null

    @SerializedName("created_at")
    val createdAt: Date? = null

    @SerializedName("total_unread_count")
    val totalUnreadCount: Int? = null

    @SerializedName("unread_channels")
    val unreadChannels: Int? = null

    val user: User? = null

    lateinit var message: Message
    var receivedAt: Date = Date()

    var channel: Channel? = null

    fun isFrom(cid: String): Boolean {
        return this.cid == cid
    }

    fun isFrom(channelType: String, channelId: String): Boolean {
        return this.cid == "$channelType:$channelId"
    }

    override fun toString(): String {
        return "ChatEvent(type='$type')"
    }
}