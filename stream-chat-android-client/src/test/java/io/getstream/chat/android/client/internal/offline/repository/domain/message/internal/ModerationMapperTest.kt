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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.ModerationConverter
import io.getstream.chat.android.randomModeration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ModerationMapperTest {

    private val converter = ModerationConverter()

    @Test
    fun `Moderation survives a round trip through the database`() {
        val moderation = randomModeration(blocklistsMatched = listOf("custom_blocklist", "other_blocklist"))

        val stored = converter.moderationToString(moderation.toEntity())
        val result = converter.stringToModeration(stored)?.toDomain()

        assertEquals(moderation, result)
    }

    @Test
    fun `Moderation stored without blocklistsMatched reads back as an empty list`() {
        val stored = """{"action":"bounce","originalText":"Some text","textHarms":[],"imageHarms":[],""" +
            """"blocklistMatched":"custom_blocklist","platformCircumvented":false}"""

        val result = converter.stringToModeration(stored)

        assertEquals(emptyList<String>(), result?.blocklistsMatched)
        assertEquals("custom_blocklist", result?.blocklistMatched)
    }
}
