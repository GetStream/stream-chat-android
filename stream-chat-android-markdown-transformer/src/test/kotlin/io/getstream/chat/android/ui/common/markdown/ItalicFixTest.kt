/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.markdown

import io.getstream.chat.android.markdown.fixItalicAtEnd
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ItalicFixTest {
    @Test
    fun italicShouldHaveSpaceAddedToIt() {
        val italicText = "*ha*"
        val expected = "$italicText&#x200A;"
        val response = italicText.fixItalicAtEnd()

        assertEquals(expected, response)
    }

    @Test
    fun italicShouldHaveSpaceAddedToIt_ComplexScenario_Positive() {
        val italicText = "*_ha_ llalal _heey_ *ha!* *"
        val expected = "$italicText&#x200A;"
        val response = italicText.fixItalicAtEnd()

        assertEquals(expected, response)
    }

    @Test
    fun italicShouldHaveSpaceAddedToIt_ComplexScenario_Negative() {
        val italicText = "*_ha_ llalal _heey_ *ha!"
        val expected = italicText
        val response = italicText.fixItalicAtEnd()

        assertEquals(expected, response)
    }

    @Test
    fun emptyStringsShouldNotBeAffected() {
        val text = ""
        val response = text.fixItalicAtEnd()

        assertEquals(text, response)
    }
}
