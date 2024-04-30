/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
