package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName

class NewMessageEvent : ChatEvent() {
    @SerializedName("watcher_count")
    val watcherCount: Int = 0
}