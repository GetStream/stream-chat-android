package io.getstream.chat.android.client.models

import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

public data class Reaction(
    var messageId: String = "",
    var type: String = "",
    var score: Int = 0,
    var user: User? = null,
    var userId: String = "",
    var createdAt: Date? = null,

    var updatedAt: Date? = null,

    var deletedAt: Date? = null,

    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    override var extraData: MutableMap<String, Any> = mutableMapOf(),

    var enforceUnique: Boolean = false,

) : CustomObject {
    // this is a workaround around a backend issue
    // for some reason we sometimes only get the user id and not the user object
    // this needs more investigation on the backend side of things
    public fun fetchUserId(): String {
        return user?.id ?: userId
    }
}
