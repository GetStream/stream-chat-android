package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.User

class NewMessageEvent : ChatEvent() {
    lateinit var cid: String
    @SerializedName("watcher_count")
    val watcherCount: Int = 0
}