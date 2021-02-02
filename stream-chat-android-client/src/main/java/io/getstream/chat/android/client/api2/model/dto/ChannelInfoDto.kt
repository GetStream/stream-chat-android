package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class ChannelInfoDto(
    val cid: String?,
    val id: String?,
    val member_count: Int,
    val name: String?,
    val type: String?,
)
