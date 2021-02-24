package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.models.Device
import java.util.Date

/**
 * See [io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamUserDto(
    val banned: Boolean,
    val id: String,
    val invisible: Boolean,
    val role: String,
    val devices: List<Device> = mutableListOf(),
    val teams: List<String> = listOf(),

    val extraData: Map<String, Any>,
)

/**
 * See [io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamUserDto(
    val id: String,
    val role: String,
    val invisible: Boolean,
    val banned: Boolean,
    val devices: List<DeviceDto>,
    val online: Boolean,
    val created_at: Date?,
    val updated_at: Date?,
    val last_active: Date?,
    val total_unread_count: Int,
    val unread_channels: Int,
    val unread_count: Int,
    val mutes: List<DownstreamMuteDto>,
    val teams: List<String>,
    val channel_mutes: List<DownstreamChannelMute>,

    val extraData: Map<String, Any>,
)
