package io.getstream.chat.android.client.models

import java.util.Date

/**
 * The only required field on the User data class is the user id.
 *
 * You can also store custom data as you'd like.
 *
 * @code
 *
 * val user = User("summer-brook-2").apply {
 *     extraData["name"] = "Paranoid Android"
 *     extraData["image"] = "https://bit.ly/2TIt8NR"
 * }
 *
 */
public data class User(
    /** the user id, this field is the only required field */
    var id: String = "",
    var role: String = "",

    var invisible: Boolean = false,
    var banned: Boolean = false,

    var devices: List<Device> = mutableListOf(),

    var online: Boolean = false,

    var createdAt: Date? = null,
    var updatedAt: Date? = null,
    var lastActive: Date? = null,

    var totalUnreadCount: Int = 0,

    var unreadChannels: Int = 0,

    var mutes: List<Mute> = mutableListOf(),

    val teams: List<String> = listOf(),

    val channelMutes: List<ChannelMute> = emptyList(),

    override var extraData: MutableMap<String, Any> = mutableMapOf()

) : CustomObject {
    var name: String
        get() = getExternalField(this, EXTRA_NAME)
        set(value) {
            extraData[EXTRA_NAME] = value
        }

    var image: String
        get() = getExternalField(this, EXTRA_IMAGE)
        set(value) {
            extraData[EXTRA_IMAGE] = value
        }
}
