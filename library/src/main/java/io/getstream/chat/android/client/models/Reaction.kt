package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.api.models.CustomObject
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.*


data class Reaction(
    @SerializedName("message_id")
    var messageId: String = "",
    var type: String = "",
    var score: Int = 0,
    var user: User? = null,
    @SerializedName("user_id")
    var userId: String = "",
    @SerializedName("created_at")
    var createdAt: Date? = null,

    @IgnoreSerialisation
    var syncStatus: SyncStatus = SyncStatus.COMPLETED,

    @IgnoreSerialisation
    @IgnoreDeserialisation
    override var extraData: MutableMap<String, Any> = mutableMapOf()
) : CustomObject