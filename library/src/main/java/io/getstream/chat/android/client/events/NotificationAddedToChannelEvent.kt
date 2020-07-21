package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName

class NotificationAddedToChannelEvent : ChatEvent() {
    @SerializedName("unread_count")
    val unreadCount: Number = 0
}
