/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.util

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class TextUtilsKtTest {

    @ParameterizedTest
    @MethodSource("urlArguments")
    fun `Verify that only the scheme should be lowercase`(
        url: String,
        schemes: List<String>,
        expectedResult: String,
    ) {
        url.ensureLowercaseScheme(schemes) `should be equal to` expectedResult
    }

    companion object {

        @JvmStatic
        fun urlArguments() = listOf(
            Arguments.of(
                "http://www.getstream.io",
                listOf("https://", "http://"),
                "http://www.getstream.io",
            ),
            Arguments.of(
                "https://www.getstream.io",
                listOf("https://", "http://"),
                "https://www.getstream.io",
            ),
            Arguments.of(
                "HTTPS://www.getstream.io",
                listOf("https://", "http://"),
                "https://www.getstream.io",
            ),
            Arguments.of(
                "HTtPS://www.getstream.io",
                listOf("https://", "http://"),
                "https://www.getstream.io",
            ),
            Arguments.of(
                "HTtPS://www.getstream.io/SomePath",
                listOf("https://", "http://"),
                "https://www.getstream.io/SomePath",
            ),
            Arguments.of(
                "www.getstream.io/SomePath",
                listOf("https://", "http://"),
                "https://www.getstream.io/SomePath",
            ),
        )
    }
}
