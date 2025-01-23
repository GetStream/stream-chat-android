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

package io.getstream.chat.android.extensions

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class IntExtensionsTest {

    @ParameterizedTest
    @MethodSource("intExtensionsLimitToArguments")
    fun testIntExtensionsLimitTo(
        value: Int,
        min: Int,
        max: Int,
        expected: Int,
    ) {
        value.limitTo(min, max) `should be equal to` expected
    }

    companion object {

        @JvmStatic
        fun intExtensionsLimitToArguments() = listOf(
            Arguments.of(1, 0, 2, 1),
            Arguments.of(1, 2, 3, 2),
            Arguments.of(3, 1, 2, 2),
        )
    }
}
