package io.getstream.chat.android.core.poc.library

import androidx.room.Embedded
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.api.ExtraDataConverter
import io.getstream.chat.android.core.poc.library.utils.UndefinedDate
import java.util.*


class Channel {

    @SerializedName("cid")
    @Expose
    var cid: String = ""

    @SerializedName("id")
    @Expose
    var id: String = ""

    @SerializedName("type")
    @Expose
    var type: String = ""

    @SerializedName("last_message_at")
    @Expose
    var lastMessageDate:Date = UndefinedDate

    @get:Sync.Status
    var syncStatus: Int? = null

    var lastKeystrokeAt:Date = UndefinedDate

    var lastStartTypingEvent:Date = UndefinedDate

    @Embedded(prefix = "state_")
    var lastState: ChannelState? = null

    @SerializedName("created_at")
    @Expose
    var createdAt:Date = UndefinedDate

    @SerializedName("deleted_at")
    @Expose
    var deletedAt:Date = UndefinedDate

    @SerializedName("updated_at")
    @Expose
    var updatedAt:Date = UndefinedDate

    @SerializedName("created_by")
    @Expose
    val createdByUser: User? = null

    val createdByUserID: String? = null

    @SerializedName("frozen")
    @Expose
    val frozen = false

    @SerializedName("config")
    @Expose
    @Embedded(prefix = "config_")
    val config: Config? = null

    @TypeConverters(ExtraDataConverter::class)
    val extraData: HashMap<String, Any>? = null
    
    val reactionTypes: Map<String, String>? = null
    
    val subRegistery: EventSubscriberRegistry<ChatChannelEventHandler>? = null
    
    lateinit var client: StreamChatClient
    
    lateinit var channelState: ChannelState

    
    var isInitialized = false

    fun getName(): String {
        val name = extraData!!["name"]
        return if (name is String) {
            name
        } else ""
    }

    companion object {
        private val TAG = Channel::class.java.simpleName
    }
}