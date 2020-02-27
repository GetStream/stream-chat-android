package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.api.models.CustomObject
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import java.util.*


class Channel : CustomObject {

    var cid: String = ""
    var id: String = ""
    var type: String = ""
    var watcherCount: Int = 0
    val frozen = false

    @SerializedName("last_message_at")
    var lastMessageAt: Date? = null
    @SerializedName("created_at")
    var createdAt: Date? = null
    @SerializedName("deleted_at")
    var deletedAt: Date? = null
    @SerializedName("updated_at")
    var updatedAt: Date? = null
    @SerializedName("created_by")
    lateinit var createdBy: User
    @SerializedName("member_count")
    val memberCount: Int = 0
    lateinit var config: Config
    var messages: List<Message> = emptyList()
    var members: List<Member> = emptyList()
    var watchers: List<Watcher> = emptyList()
    var read: List<ChannelUserRead> = emptyList()

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData = mutableMapOf<String, Any>()

    @IgnoreSerialisation
    @IgnoreDeserialisation
    internal lateinit var client: ChatClient

    fun watch(request: ChannelWatchRequest): Call<Channel> {
        return client.queryChannel(type, id, request)
    }

    override fun toString(): String {
        return "Channel(cid='$cid')"
    }
}