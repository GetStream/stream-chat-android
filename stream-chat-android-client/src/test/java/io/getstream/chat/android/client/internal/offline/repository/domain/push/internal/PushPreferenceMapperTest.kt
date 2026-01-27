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

package io.getstream.chat.android.client.internal.offline.repository.domain.push.internal

import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class PushPreferenceMapperTest {

    @Test
    fun `PushPreference toEntity should map all fields correctly`() {
        // Given
        val disabledUntil = Date()
        val pushPreference = PushPreference(
            level = PushPreferenceLevel.all,
            disabledUntil = disabledUntil,
        )

        // When
        val entity = pushPreference.toEntity()

        // Then
        assertEquals("all", entity.level)
        assertEquals(disabledUntil, entity.disabledUntil)
    }

    @Test
    fun `PushPreference toEntity should handle null chatLevel`() {
        // Given
        val disabledUntil = Date()
        val pushPreference = PushPreference(
            level = null,
            disabledUntil = disabledUntil,
        )

        // When
        val entity = pushPreference.toEntity()

        // Then
        assertNull(entity.level)
        assertEquals(disabledUntil, entity.disabledUntil)
    }

    @Test
    fun `PushPreferences toEntity should handle all ChatPushLevel values`() {
        // Test ALL
        val allPreference = PushPreference(level = PushPreferenceLevel.all, disabledUntil = null)
        assertEquals("all", allPreference.toEntity().level)

        // Test MENTIONS
        val mentionsPreference = PushPreference(level = PushPreferenceLevel.mentions, disabledUntil = null)
        assertEquals("mentions", mentionsPreference.toEntity().level)

        // Test NONE
        val nonePreference = PushPreference(level = PushPreferenceLevel.none, disabledUntil = null)
        assertEquals("none", nonePreference.toEntity().level)
    }

    @Test
    fun `PushPreferenceEntity toModel should map all fields correctly`() {
        // Given
        val disabledUntil = Date()
        val entity = PushPreferenceEntity(
            level = "all",
            disabledUntil = disabledUntil,
        )

        // When
        val model = entity.toModel()

        // Then
        assertEquals(PushPreferenceLevel.all, model.level)
        assertEquals(disabledUntil, model.disabledUntil)
    }

    @Test
    fun `PushPreferenceEntity toModel should handle null chatLevel`() {
        // Given
        val disabledUntil = Date()
        val entity = PushPreferenceEntity(
            level = null,
            disabledUntil = disabledUntil,
        )

        // When
        val model = entity.toModel()

        // Then
        assertNull(model.level)
        assertEquals(disabledUntil, model.disabledUntil)
    }

    @Test
    fun `PushPreferencesEntity toModel should handle all valid chatLevel values`() {
        // Test "all"
        val allEntity = PushPreferenceEntity(level = "all", disabledUntil = null)
        assertEquals(PushPreferenceLevel.all, allEntity.toModel().level)

        // Test "mentions"
        val mentionsEntity = PushPreferenceEntity(level = "mentions", disabledUntil = null)
        assertEquals(PushPreferenceLevel.mentions, mentionsEntity.toModel().level)

        // Test "none"
        val noneEntity = PushPreferenceEntity(level = "none", disabledUntil = null)
        assertEquals(PushPreferenceLevel.none, noneEntity.toModel().level)
    }

    @Test
    fun `PushPreferencesEntity toModel should handle invalid chatLevel`() {
        // Given
        val entity = PushPreferenceEntity(
            level = "invalid",
            disabledUntil = null,
        )

        // When
        val model = entity.toModel()

        // Then
        assertEquals(PushPreferenceLevel("invalid"), model.level)
        assertNull(model.disabledUntil)
    }

    @Test
    fun `round trip conversion should preserve data for PushPreference`() {
        // Given
        val disabledUntil = Date()
        val originalPreference = PushPreference(
            level = PushPreferenceLevel.all,
            disabledUntil = disabledUntil,
        )

        // When
        val entity = originalPreference.toEntity()
        val convertedBack = entity.toModel()

        // Then
        assertEquals(originalPreference.level, convertedBack.level)
        assertEquals(originalPreference.disabledUntil, convertedBack.disabledUntil)
    }
}
