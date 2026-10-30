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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.PushPreferenceConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.PushPreferenceEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.PushScheduleEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.QuietHoursRuleEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PushPreferenceConverterTest {

    private val converter = PushPreferenceConverter()

    @Test
    fun `stored preference without a schedule is read with a null schedule`() {
        val stored = "{\"level\":\"mentions\"}"

        val entity = converter.stringToPushPreference(stored)

        assertEquals(PushPreferenceEntity(level = "mentions", disabledUntil = null, schedule = null), entity)
    }

    @Test
    fun `preference with a schedule survives a round trip`() {
        val entity = PushPreferenceEntity(
            level = null,
            disabledUntil = null,
            schedule = PushScheduleEntity(
                timezone = "Europe/Amsterdam",
                quietHours = listOf(
                    QuietHoursRuleEntity(start = "22:00", end = "07:00", days = listOf("fri")),
                    QuietHoursRuleEntity(start = "00:00", end = "24:00", days = emptyList()),
                ),
            ),
        )

        val restored = converter.stringToPushPreference(converter.pushPreferenceToString(entity))

        assertEquals(entity, restored)
    }
}
