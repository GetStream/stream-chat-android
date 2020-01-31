package io.getstream.chat.android.client

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.call.ChatCall
import io.getstream.chat.android.client.rest.ChannelWatchRequest
import io.getstream.chat.android.client.utils.UndefinedDate
import java.util.*


class Channel {

    @SerializedName("cid")

    var cid: String = ""

    @SerializedName("id")

    var id: String = ""

    @SerializedName("type")

    var type: String = ""

    @SerializedName("last_message_at")
    var lastMessageDate: Date = UndefinedDate

    var lastKeystrokeAt: Date = UndefinedDate

    var lastStartTypingEvent: Date = UndefinedDate

    @Embedded(prefix = "state_")
    var lastState: ChannelState? = null

    @SerializedName("created_at")

    var createdAt: Date = UndefinedDate

    @SerializedName("deleted_at")

    var deletedAt: Date = UndefinedDate

    @SerializedName("updated_at")

    var updatedAt: Date = UndefinedDate

    @SerializedName("created_by")

    val createdByUser: User? = null

    val createdByUserID: String? = null

    @SerializedName("frozen")

    val frozen = false

    @SerializedName("config")

    @Embedded(prefix = "config_")
    val config: Config? = null

    var extraData = mutableMapOf<String, Any>()

    val reactionTypes: Map<String, String>? = null

    val subRegistery: EventSubscriberRegistry<ChatChannelEventHandler>? = null

    lateinit var client: ChatClient

    lateinit var channelState: ChannelState

    var isInitialized = false

    fun getName(): String {
        val name = extraData!!["name"]
        return if (name is String) {
            name
        } else ""
    }

    fun watch(
        request: ChannelWatchRequest
    ): ChatCall<Channel> {
        return client.queryChannel(type, id, request)
    }

    override fun toString(): String {
        return "Channel(cid='$cid')"
    }


    companion object {
        private val TAG = Channel::class.java.simpleName
    }
}