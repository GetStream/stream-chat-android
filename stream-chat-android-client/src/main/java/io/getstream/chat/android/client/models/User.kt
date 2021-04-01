package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
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

    @IgnoreSerialisation
    var online: Boolean = false,

    @IgnoreSerialisation
    @SerializedName("created_at")
    var createdAt: Date? = null,
    @IgnoreSerialisation
    @SerializedName("updated_at")
    var updatedAt: Date? = null,
    @IgnoreSerialisation
    @SerializedName("last_active")
    var lastActive: Date? = null,

    @IgnoreSerialisation
    @SerializedName("total_unread_count")
    var totalUnreadCount: Int = 0,

    @IgnoreSerialisation
    @SerializedName("unread_channels")
    var unreadChannels: Int = 0,

    @IgnoreSerialisation
    @SerializedName("mutes")
    var mutes: List<Mute> = mutableListOf(),

    val teams: List<String> = listOf(),

    @IgnoreSerialisation
    @SerializedName("channel_mutes")
    val channelMutes: List<ChannelMute> = emptyList(),

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf()

) : CustomObject
