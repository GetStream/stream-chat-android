package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Channel

class AddedToChannelEvent : RemoteEvent() {
    lateinit var channel: Channel
    @SerializedName("total_unread_count")
    val totalUnreadCount: Number = 0
    @SerializedName("unread_count")
    val unreadCount: Number = 0
}