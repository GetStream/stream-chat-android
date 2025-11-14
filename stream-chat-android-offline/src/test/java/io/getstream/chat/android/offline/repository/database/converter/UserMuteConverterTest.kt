/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.database.converter

import io.getstream.chat.android.offline.randomUserMuteEntity
import io.getstream.chat.android.offline.repository.database.converter.internal.UserMuteConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class UserMuteConverterTest {
    private val sut = UserMuteConverter()

    @Test
    fun `toJson with null entity returns null`() {
        val result = sut.toJson(null)

        assertNull(result)
    }

    @Test
    fun `fromJson with null string returns null`() {
        val result = sut.fromJson(null)

        assertNull(result)
    }

    @Test
    fun `round-trip conversion`() {
        val entity = randomUserMuteEntity()
        val json = sut.toJson(entity)
        val converted = sut.fromJson(json)

        assertEquals(entity, converted)
    }
}
