package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName

class NewMessageEvent : ChatEvent() {
    val cid: String = ""
    @SerializedName("watcher_count")
    val watcherCount: Int = 0
}