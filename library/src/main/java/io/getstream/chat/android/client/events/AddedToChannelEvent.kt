package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Channel

class AddedToChannelEvent : ChatEvent() {
    lateinit var channel: Channel
    @SerializedName("unread_count")
    val unreadCount: Number = 0
}