package io.getstream.chat.android.core.poc.library

import androidx.room.*
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.api.ExtraDataConverter


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

    @get:Sync.Status n
    var syncStatus: Int? = null

    var lastKeystrokeAt: Long = 0

    
    var lastStartTypingEvent: Long = 0
    @Embedded(prefix = "state_")
    private val lastState: ChannelState? = null
    @SerializedName("created_at")
    @Expose
    private val createdAt: Long = 0
    @SerializedName("deleted_at")
    @Expose
    private val deletedAt: Long = 0
    @SerializedName("updated_at")
    @Expose
    private val updatedAt: Long = 0
    @SerializedName("created_by")
    @Expose
    
    private val createdByUser: User? = null
    private val createdByUserID: String? = null
    @SerializedName("frozen")
    @Expose
    private val frozen = false
    @SerializedName("config")
    @Expose
    @Embedded(prefix = "config_")
    private val config: Config? = null
    @TypeConverters(ExtraDataConverter::class)
    private val extraData: HashMap<String, Any>? = null
    
    private val reactionTypes: Map<String, String>? = null
    
    private val subRegistery: EventSubscriberRegistry<ChatChannelEventHandler>? = null
    
    lateinit var client: StreamChatClient
    
    private val channelState: ChannelState? = null

    
    var isInitialized = false

    companion object {
        private val TAG = Channel::class.java.simpleName
    }
}