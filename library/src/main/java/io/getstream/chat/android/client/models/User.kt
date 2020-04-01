package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.api.models.CustomObject
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import java.util.*


class User(var id: String = "") : UserEntity, CustomObject {

    var role: String = ""

    @IgnoreSerialisation
    var online: Boolean = false

    var invisible: Boolean = false
    var banned: Boolean = false
    @SerializedName("total_unread_count")
    var totalUnreadCount: Int = 0
    @SerializedName("unread_channel")
    var unreadChannels: Int = 0
    @SerializedName("unread_count")
    var unreadCount: Int = 0
    var mutes: List<Mute> = mutableListOf()
    var devices: List<Device> = mutableListOf()

    @IgnoreSerialisation
    @SerializedName("created_at")
    var createdAt: Date? = null
    @IgnoreSerialisation
    @SerializedName("updated_at")
    var updatedAt: Date? = null
    @IgnoreSerialisation
    @SerializedName("last_active")
    var lastActive: Date? = null

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData = mutableMapOf<String, Any>()

    override fun equals(obj: Any?): Boolean {
        if (this === obj) return true
        if (obj == null || javaClass != obj.javaClass) return false
        // we compare based on the CID
        val otherUser = obj as User
        return Objects.equals(id, otherUser.id)
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    override fun getUserId(): String {
        return id
    }

}