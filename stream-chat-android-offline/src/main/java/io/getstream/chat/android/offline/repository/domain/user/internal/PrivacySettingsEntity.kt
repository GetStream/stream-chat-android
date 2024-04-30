package io.getstream.chat.android.offline.repository.domain.user.internal

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class PrivacySettingsEntity(
    val typingIndicators: TypingIndicatorsEntity? = null,
    val readReceipts: ReadReceiptsEntity? = null,
)

@JsonClass(generateAdapter = true)
internal data class TypingIndicatorsEntity(
    val enabled: Boolean,
)

@JsonClass(generateAdapter = true)
internal data class ReadReceiptsEntity(
    val enabled: Boolean,
)