package io.getstream.chat.android.client.models

import android.text.TextUtils
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.UndefinedDate
import java.util.*
import kotlin.collections.HashMap


class User (var id: String = ""): UserEntity {

    var name: String = ""
    var image: String = ""
    var role: String = ""
    @IgnoreSerialisation
    var created_at: Date = UndefinedDate
    @IgnoreSerialisation
    var updated_at: Date = UndefinedDate
    @IgnoreSerialisation
    var last_active: Date = UndefinedDate
    var online: Boolean = false
    var invisible: Boolean = false
    var banned: Boolean = false
    var mutes: List<Mute> = mutableListOf()
    var devices: List<Device> = mutableListOf()
    var totalUnreadCount: Int = 0
    var unreadChannels: Int = 0
    var extraData = mutableMapOf<String, Any>()


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

    val initials: String?
        get() {
            val name = name
            val names = name.split(" ").toTypedArray()
            val firstName = names[0]
            var lastName: String? = null
            try {
                lastName = names[1]
            } catch (e: Exception) {
            }
            if (!TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName)) return firstName.substring(
                0,
                1
            ).toUpperCase()
            if (TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) return lastName!!.substring(
                0,
                1
            ).toUpperCase()
            return if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) firstName.substring(
                0,
                1
            ).toUpperCase() + lastName!!.substring(0, 1).toUpperCase() else null
        }

    // TODO: populate this from API
    val isMe: Boolean
        get() = false

    override fun getUserId(): String {
        return id
    }

    /**
     * Returns true if the other user is muted
     */
    fun hasMuted(user: User): Boolean {
        if (mutes == null || mutes!!.size == 0) return false
        for (mute in mutes!!) {
            if (mute.target!!.id.equals(user.id)) return true
        }
        return false
    }

}