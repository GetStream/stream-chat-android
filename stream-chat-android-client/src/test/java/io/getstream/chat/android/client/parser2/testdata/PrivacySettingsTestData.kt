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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import org.intellij.lang.annotations.Language

internal object PrivacySettingsTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"typing_indicators":{"enabled":true},"delivery_receipts":{"enabled":false},"read_receipts":{"enabled":true}}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{}"""

    val expectedAllFields = PrivacySettings(
        typingIndicators = TypingIndicators(enabled = true),
        deliveryReceipts = DeliveryReceipts(enabled = false),
        readReceipts = ReadReceipts(enabled = true),
    )

    val expectedOptionalFieldsMissing = PrivacySettings(
        typingIndicators = null,
        deliveryReceipts = null,
        readReceipts = null,
    )

    @Language("JSON")
    val jsonTypingIndicatorsMissingEnabled =
        """{"typing_indicators":{}}"""

    @Language("JSON")
    val jsonDeliveryReceiptsMissingEnabled =
        """{"delivery_receipts":{}}"""

    @Language("JSON")
    val jsonReadReceiptsMissingEnabled =
        """{"read_receipts":{}}"""
}
