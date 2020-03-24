package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.*


class Config {
    @SerializedName("created_at")
    var created_at: Date? = null

    @SerializedName("updated_at")
    var updated_at: Date? = null

    @SerializedName("typing_events")
    var isTypingEvents = false

    @SerializedName("read_events")
    var isReadEvents = false

    @SerializedName("connect_events")
    var isConnectEvents = false

    @SerializedName("search")
    var isSearch = false

    @SerializedName("reactions")
    var isReactionsEnabled = false

    @SerializedName("replies")
    var isRepliesEnabled = false

    @SerializedName("mutes")
    var isMutes = false

    @SerializedName("max_message_length")
    var maxMessageLength = 0

    var automod: String = ""
    var infinite: String = ""
    var name: String = ""

    var commands: List<Command> = mutableListOf()

}
