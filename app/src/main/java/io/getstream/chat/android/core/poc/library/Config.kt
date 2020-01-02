package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


class Config {
    @SerializedName("created_at")
    @Expose
    var created_at: Long = 0
    @SerializedName("updated_at")
    @Expose
    var updated_at: Long = 0
    @SerializedName("name")
    @Expose
    var name: String = ""
    @SerializedName("typing_events")
    @Expose
    var isTypingEvents = false
    @SerializedName("read_events")
    @Expose
    var isReadEvents = false
    @SerializedName("connect_events")
    @Expose
    var isConnect_events = false
    @SerializedName("search")
    @Expose
    var isSearch = false
    @SerializedName("reactions")
    @Expose
    var isReactionsEnabled = false
    @SerializedName("replies")
    @Expose
    var isRepliesEnabled = false
    @SerializedName("mutes")
    @Expose
    var isMutes = false
    @SerializedName("infinite")
    @Expose
    var infinite: String = ""
    @SerializedName("max_message_length")
    @Expose
    var max_message_length = 0
    @SerializedName("automod")
    @Expose
    var automod: String = ""
    @SerializedName("commands")
    @Expose
    var commands: List<Command> = mutableListOf()

}
