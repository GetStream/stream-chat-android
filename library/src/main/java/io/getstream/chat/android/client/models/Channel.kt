package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.*


data class Channel(
    var cid: String = "",
    var id: String = "",
    var type: String = "",
    var watcherCount: Int = 0,
    var frozen: Boolean = false,

    @SerializedName("last_message_at")
    var lastMessageAt: Date? = null,
    @SerializedName("created_at")
    var createdAt: Date? = null,
    @SerializedName("deleted_at")
    var deletedAt: Date? = null,
    @SerializedName("updated_at")
    var updatedAt: Date? = null,

    @IgnoreSerialisation
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    @SerializedName("member_count")
    val memberCount: Int = 0,
    var messages: List<Message> = mutableListOf(),
    var members: List<Member> = mutableListOf(),
    var watchers: List<Watcher> = mutableListOf(),
    var read: List<ChannelUserRead> = mutableListOf(),
    var config: Config = Config(),
    var createdBy: User = User(),

    @IgnoreSerialisation
    @IgnoreDeserialisation
    var unreadCount: Int? = null,

    val team:String = "",

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf()



) : CustomObject

