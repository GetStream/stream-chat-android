package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

public data class Channel(
    var cid: String = "",
    var id: String = "",
    var type: String = "",
    var watcherCount: Int = 0,
    var frozen: Boolean = false,

    var lastMessageAt: Date? = null,
    var createdAt: Date? = null,
    var deletedAt: Date? = null,
    var updatedAt: Date? = null,

    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    val memberCount: Int = 0,
    var messages: List<Message> = mutableListOf(),
    var members: List<Member> = mutableListOf(),
    var watchers: List<User> = mutableListOf(),
    var read: List<ChannelUserRead> = mutableListOf(),
    var config: Config = Config(),
    var createdBy: User = User(),

    var unreadCount: Int? = null,

    val team: String = "",

    override var extraData: MutableMap<String, Any> = mutableMapOf(),

    var hidden: Boolean? = null,

    var hiddenMessagesBefore: Date? = null,

    /**
     *  Cooldown period after sending each message in seconds
     */
    val cooldown: Int = 0,

    var pinnedMessages: List<Message> = mutableListOf(),
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

    val lastUpdated: Date?
        get() = lastMessageAt?.takeIf { createdAt == null || it.after(createdAt) } ?: createdAt

    val hasUnread: Boolean
        get() = unreadCount?.let { it > 0 } ?: false
}
