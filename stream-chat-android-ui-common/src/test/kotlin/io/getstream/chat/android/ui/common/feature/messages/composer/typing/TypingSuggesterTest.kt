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

package io.getstream.chat.android.ui.common.feature.messages.composer.typing

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class TypingSuggesterTest {

    private val suggester = TypingSuggester(TypingSuggestionOptions(symbol = "@"))

    @ParameterizedTest(name = "[{index}] text=\"{0}\" -> {1}")
    @MethodSource("provideCases")
    fun `typingSuggestion resolves the mention token`(text: String, expected: TypingSuggestion?) {
        val suggestion = suggester.typingSuggestion(text)

        suggestion `should be equal to` expected
    }

    companion object {

        @JvmStatic
        fun provideCases(): List<Arguments> = listOf(
            Arguments.of("@", TypingSuggestion("", IntRange(1, 0))),
            Arguments.of("@john", TypingSuggestion("john", 1 until 5)),
            Arguments.of("Hello @john", TypingSuggestion("john", 7 until 11)),
            Arguments.of("@John Doe", TypingSuggestion("John Doe", 1 until 9)),
            Arguments.of("@john ", TypingSuggestion("john ", 1 until 6)),
            Arguments.of("@ ", null),
            Arguments.of("@  john", null),
            Arguments.of("Hello@john", null),
            Arguments.of("@john,@jane", TypingSuggestion("jane", 7 until 11)),
            Arguments.of("@john, @jane", TypingSuggestion("jane", 8 until 12)),
            Arguments.of("(@john", TypingSuggestion("john", 2 until 6)),
            Arguments.of("john@example.com", null),
        )
    }
}
