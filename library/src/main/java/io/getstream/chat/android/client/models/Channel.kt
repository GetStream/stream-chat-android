package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.ChatCall
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.utils.UndefinedDate
import java.util.*


class Channel {
    var cid: String = ""
    var id: String = ""
    var type: String = ""

    @SerializedName("last_message_at")
    var lastMessageDate: Date = UndefinedDate
    var lastKeystrokeAt: Date = UndefinedDate
    var lastStartTypingEvent: Date = UndefinedDate
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
    val frozen = false
    val config: Config? = null

    var extraData = mutableMapOf<String, Any>()

    val reactionTypes = mutableMapOf<String, String>()

    internal lateinit var client: ChatClient

    //TODO: check if field is filled/used
    lateinit var channelState: ChannelState

    fun getName(): String {
        val name = extraData["name"]
        return if (name is String) {
            name
        } else ""
    }

    fun watch(request: ChannelWatchRequest): ChatCall<Channel> {
        return client.queryChannel(type, id, request)
    }

    override fun toString(): String {
        return "Channel(cid='$cid')"
    }
}