package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

public data class Reaction(
    @SerializedName("message_id")
    var messageId: String = "",
    var type: String = "",
    var score: Int = 0,
    var user: User? = null,
    @SerializedName("user_id")
    var userId: String = "",
    @SerializedName("created_at")
    var createdAt: Date? = null,

    @SerializedName("updated_at")
    var updatedAt: Date? = null,

    @IgnoreSerialisation
    @IgnoreDeserialisation
    var deletedAt: Date? = null,

    @IgnoreSerialisation
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf(),

    @IgnoreSerialisation
    @IgnoreDeserialisation
    var enforceUnique: Boolean = false,

) : CustomObject {
    // this is a workaround around a backend issue
    // for some reason we sometimes only get the user id and not the user object
    // this needs more investigation on the backend side of things
    public fun fetchUserId(): String {
        return user?.id ?: userId
    }
}
