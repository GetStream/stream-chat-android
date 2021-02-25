package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class CommandDto(
    val name: String,
    val description: String,
    val args: String,
    val set: String,
)
