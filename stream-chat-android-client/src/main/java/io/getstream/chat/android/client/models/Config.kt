package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import java.util.Date

public data class Config(
    @SerializedName("created_at")
    var created_at: Date? = null,

    @SerializedName("updated_at")
    var updated_at: Date? = null,

    @SerializedName("typing_events")
    var isTypingEvents: Boolean = false,

    @SerializedName("read_events")
    var isReadEvents: Boolean = false,

    @SerializedName("connect_events")
    var isConnectEvents: Boolean = false,

    @SerializedName("search")
    var isSearch: Boolean = false,

    @SerializedName("reactions")
    var isReactionsEnabled: Boolean = false,

    @SerializedName("replies")
    var isRepliesEnabled: Boolean = false,

    @SerializedName("mutes")
    var isMutes: Boolean = false,

    @SerializedName("max_message_length")
    var maxMessageLength: Int = 0,

    var automod: String = "",
    var infinite: String = "",
    var name: String = "",

    var commands: List<Command> = mutableListOf()
)
