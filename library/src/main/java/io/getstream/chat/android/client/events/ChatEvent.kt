package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.*
import java.util.*


open class ChatEvent(val type: String = "") {

    var cid: String? = null

    @SerializedName("created_at")
    var createdAt: Date? = null

    @SerializedName("total_unread_count")
    var totalUnreadCount: Int? = null

    @SerializedName("unread_channels")
    var unreadChannels: Int? = null

    var user: User? = null

    lateinit var message: Message
    var receivedAt: Date = Date()

    var reaction: Reaction?= null
    var member: Member? = null

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