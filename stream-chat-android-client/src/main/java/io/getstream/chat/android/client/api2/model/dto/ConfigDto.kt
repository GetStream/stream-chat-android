package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class ConfigDto(
    val created_at: Date?,
    val updated_at: Date?,
    val name: String,
    val typing_events: Boolean,
    val read_events: Boolean,
    val connect_events: Boolean,
    val search: Boolean,
    val reactions: Boolean,
    val replies: Boolean,
    val mutes: Boolean,
    val uploads: Boolean,
    val url_enrichment: Boolean,
    val custom_events: Boolean,
    val push_notifications: Boolean,
    val message_retention: String,
    val max_message_length: Int,
    val automod: String,
    val automod_behavior: String,
    val blocklist_behavior: String,
    val commands: List<CommandDto>,
)
