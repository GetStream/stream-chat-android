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

import io.getstream.chat.android.client.internal.offline.randomOptionEntity
import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.OptionConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.OptionEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class OptionConverterTest {

    private val sut = OptionConverter()

    @Test
    fun `stringToOption should return null for null input`() {
        assertNull(sut.stringToOption(null))
    }

    @Test
    fun `optionToString should return null for null input`() {
        assertNull(sut.optionToString(null))
    }

    @Test
    fun `option should survive a round trip`() {
        val option = randomOptionEntity()

        val result = requireNotNull(sut.stringToOption(sut.optionToString(option)))

        assertEquals(option.id, result.id)
        assertEquals(option.text, result.text)
        assertEquals(option.extraData, result.extraData)
    }

    @Test
    fun `stringToOptionList should return empty list for null or blank input`() {
        assertEquals(emptyList<OptionEntity>(), sut.stringToOptionList(null))
        assertEquals(emptyList<OptionEntity>(), sut.stringToOptionList(""))
        assertEquals(emptyList<OptionEntity>(), sut.stringToOptionList("null"))
    }

    @Test
    fun `optionListToString should return null for null input`() {
        assertNull(sut.optionListToString(null))
    }

    @Test
    fun `option list should survive a round trip`() {
        val option = randomOptionEntity()

        val result = requireNotNull(sut.stringToOptionList(sut.optionListToString(listOf(option))))

        assertEquals(1, result.size)
        assertEquals(option.id, result.first().id)
        assertEquals(option.text, result.first().text)
        assertEquals(option.extraData, result.first().extraData)
    }
}
