package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class ConfigDto(
    val created_at: Date?,
    val updated_at: Date?,
    val typing_events: Boolean,
    val read_events: Boolean,
    val connect_events: Boolean,
    val search: Boolean,
    val reactions: Boolean,
    val replies: Boolean,
    val mutes: Boolean,
    val max_message_length: Int,
    val automod: String,
    val infinite: String,
    val name: String,
    val commands: List<CommandDto>,
)
