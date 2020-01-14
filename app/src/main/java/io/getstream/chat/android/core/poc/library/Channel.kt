package io.getstream.chat.android.core.poc.library

import androidx.room.Embedded
import androidx.room.TypeConverters
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.api.ExtraDataConverter
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
    var lastMessageDate: Long = 0

    @get:Sync.Status
    var syncStatus: Int? = null

    var lastKeystrokeAt: Long = 0

    
    var lastStartTypingEvent: Long = 0
    @Embedded(prefix = "state_")
    val lastState: ChannelState? = null
    @SerializedName("created_at")
    @Expose
    val createdAt = Date()
    @SerializedName("deleted_at")
    @Expose
    val deletedAt = Date()
    @SerializedName("updated_at")
    @Expose
    val updatedAt = Date()
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