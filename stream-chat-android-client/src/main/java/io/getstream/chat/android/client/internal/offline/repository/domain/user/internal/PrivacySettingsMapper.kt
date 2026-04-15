/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.offline.repository.domain.user.internal

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators

internal fun PrivacySettings.toEntity(): PrivacySettingsEntity {
    return PrivacySettingsEntity(
        typingIndicators = typingIndicators?.let {
            TypingIndicatorsEntity(
                enabled = it.enabled,
            )
        },
        readReceipts = readReceipts?.let {
            ReadReceiptsEntity(
                enabled = it.enabled,
            )
        },
        deliveryReceipts = deliveryReceipts?.let {
            DeliveryReceiptsEntity(
                enabled = it.enabled,
            )
        },
    )
}

internal fun PrivacySettingsEntity.toModel(): PrivacySettings {
    return PrivacySettings(
        typingIndicators = typingIndicators?.let {
            TypingIndicators(
                enabled = it.enabled,
            )
        },
        readReceipts = readReceipts?.let {
            ReadReceipts(
                enabled = it.enabled,
            )
        },
        deliveryReceipts = deliveryReceipts?.let {
            DeliveryReceipts(
                enabled = it.enabled,
            )
        },
    )
}
