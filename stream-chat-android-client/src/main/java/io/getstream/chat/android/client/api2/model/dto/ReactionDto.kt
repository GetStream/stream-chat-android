package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * See [io.getstream.chat.android.client.parser2.ReactionDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class ReactionDto(
    val message_id: String,
    val type: String,
    val score: Int,
    val user: UserDto?,
    val user_id: String,
    val created_at: Date?,
    val updated_at: Date?,
    val extraData: Map<String, Any>,
)
