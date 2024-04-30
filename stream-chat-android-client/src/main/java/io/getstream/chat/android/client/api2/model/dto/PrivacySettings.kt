package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PrivacySettingsDto(
    val typing_indicators: TypingIndicatorsDto? = null,
    val read_receipts: ReadReceiptsDto? = null,
)

@JsonClass(generateAdapter = true)
internal data class TypingIndicatorsDto(
    val enabled: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class ReadReceiptsDto(
    val enabled: Boolean,
)