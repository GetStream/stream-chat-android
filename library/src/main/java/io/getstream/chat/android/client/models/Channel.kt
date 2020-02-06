package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.call.ChatCall
import io.getstream.chat.android.client.utils.UndefinedDate
import java.util.*


class Channel {
    var cid: String = ""
    var id: String = ""
    var type: String = ""

    @SerializedName("last_message_at")
    var lastMessageDate: Date = UndefinedDate
    @SerializedName("created_at")
    var createdAt: Date = UndefinedDate
    @SerializedName("deleted_at")
    var deletedAt: Date = UndefinedDate
    @SerializedName("updated_at")
    var updatedAt: Date = UndefinedDate
    @SerializedName("created_by")
    lateinit var createdByUser: User
    @SerializedName("member_count")
    val memberCount: Int = 0

    val frozen = false
    lateinit var config: Config

    var extraData = mutableMapOf<String, Any>()

    lateinit var messages: List<Message>
    lateinit var members: List<Member>
    lateinit var read: List<ChannelUserRead>
    var watcherCount: Int = 0

    internal lateinit var client: ChatClient

    fun watch(request: ChannelWatchRequest): ChatCall<Channel> {
        return client.queryChannel(type, id, request)
    }

    override fun toString(): String {
        return "Channel(cid='$cid')"
    }
}