package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


class Config {
    @SerializedName("created_at")
    
    var created_at:Date = UndefinedDate
    @SerializedName("updated_at")
    
    var updated_at:Date = UndefinedDate
    @SerializedName("name")
    
    var name: String = ""
    @SerializedName("typing_events")
    
    var isTypingEvents = false
    @SerializedName("read_events")
    
    var isReadEvents = false
    @SerializedName("connect_events")
    
    var isConnect_events = false
    @SerializedName("search")
    
    var isSearch = false
    @SerializedName("reactions")
    
    var isReactionsEnabled = false
    @SerializedName("replies")
    
    var isRepliesEnabled = false
    @SerializedName("mutes")
    
    var isMutes = false
    @SerializedName("infinite")
    
    var infinite: String = ""
    @SerializedName("max_message_length")
    
    var max_message_length = 0
    @SerializedName("automod")
    
    var automod: String = ""
    @SerializedName("commands")
    
    var commands: List<Command> = mutableListOf()

}
