package io.getstream.chat.android.client

import android.text.TextUtils
import io.getstream.chat.android.client.json.IgnoreSerialisation
import io.getstream.chat.android.client.utils.UndefinedDate
import java.util.*
import kotlin.collections.HashMap


class User : UserEntity {

    var id: String = ""
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

    /**
     * Constructor
     *
     * @param id        User id
     * @param extraData Custom user fields (ie: name, image, anything that json can serialize is ok)
     */
    constructor(
        id: String,
        extraData: Map<String, Any> = mutableMapOf()
    ) {
        this.id = id
        online = false
        this.extraData = HashMap(extraData)
        // since name and image are very common fields, we are going to promote them as
        val image = this.extraData.remove("image")
        if (image != null) {
            this.image = image.toString()
        }
        val name = this.extraData.remove("name")
        if (name != null) {
            this.name = name.toString()
        }
        this.extraData.remove("id")
    }

    fun shallowCopy(): User {
        val copy = User(id)
        copy.shallowUpdate(this)
        return copy
    }

    fun shallowUpdate(user: User) {
        name = user.name
        online = user.online
        image = user.image
        created_at = user.created_at
        last_active = user.last_active
        updated_at = user.updated_at
        extraData = HashMap(user.extraData)
    }

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