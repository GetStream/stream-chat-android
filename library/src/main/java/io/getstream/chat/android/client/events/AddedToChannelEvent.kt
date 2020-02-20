package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Channel

class AddedToChannelEvent : ChatEvent() {
    @SerializedName("unread_count")
    val unreadCount: Number = 0
}