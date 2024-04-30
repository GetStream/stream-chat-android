package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.api2.model.dto.PrivacySettingsDto
import io.getstream.chat.android.client.api2.model.dto.ReadReceiptsDto
import io.getstream.chat.android.client.api2.model.dto.TypingIndicatorsDto

internal fun PrivacySettings.toDto(): PrivacySettingsDto = PrivacySettingsDto(
    typing_indicators = typingIndicators?.toDto(),
    read_receipts = readReceipts?.toDto(),
)

internal fun TypingIndicators.toDto(): TypingIndicatorsDto = TypingIndicatorsDto(
    enabled = enabled,
)

internal fun ReadReceipts.toDto(): ReadReceiptsDto = ReadReceiptsDto(
    enabled = enabled,
)

internal fun PrivacySettingsDto.toDomain(): PrivacySettings = PrivacySettings(
    typingIndicators = typing_indicators?.toDomain(),
    readReceipts = read_receipts?.toDomain(),
)

internal fun TypingIndicatorsDto.toDomain(): TypingIndicators = TypingIndicators(
    enabled = enabled,
)

internal fun ReadReceiptsDto.toDomain(): ReadReceipts = ReadReceipts(
    enabled = enabled,
)