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

package io.getstream.chat.android.ai.openui.components

import kotlin.test.Test
import kotlin.test.assertEquals

internal class CitationsTest {
    private val sources = mapOf(1 to "https://a.example/x (1)", 120 to "https://b.example")

    @Test
    fun `citations with a source become links`() {
        assertEquals(
            "See [[1]](https://a.example/x%20%281%29) and [[120]](https://b.example), not [7].",
            Citations.link("See [1] and [120], not [7].", sources),
        )
    }

    @Test
    fun `links and text without sources stay as written`() {
        assertEquals("[1](https://c.example)", Citations.link("[1](https://c.example)", sources))
        assertEquals("[1]", Citations.link("[1]", emptyMap()))
    }

    @Test
    fun `citations in code spans and fenced code stay as written`() {
        val text = listOf(
            "items[1] and `items[1]` and ``a`[1]`b``",
            "```kotlin",
            "val x = items[1]",
            "```",
            "~~~",
            "y[120]",
            "~~~",
            "after [120]",
        ).joinToString("\n")
        val expected = listOf(
            "items[[1]](https://a.example/x%20%281%29) and `items[1]` and ``a`[1]`b``",
            "```kotlin",
            "val x = items[1]",
            "```",
            "~~~",
            "y[120]",
            "~~~",
            "after [[120]](https://b.example)",
        ).joinToString("\n")
        assertEquals(expected, Citations.link(text, sources))
    }
}
