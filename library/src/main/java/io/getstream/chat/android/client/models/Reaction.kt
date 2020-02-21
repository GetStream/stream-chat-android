package io.getstream.chat.android.client.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreDeserialisation
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import java.util.*


data class Reaction(
    @SerializedName("message_id")
    val messageId: String
) {
    lateinit var user: User
    @SerializedName("user_id")
    var userId: String = ""
    var type: String = ""
    @SerializedName("created_at")
    val createdAt: Date? = null

    @IgnoreSerialisation
    @IgnoreDeserialisation
    val extraData: Map<String, Any> = emptyMap()
}