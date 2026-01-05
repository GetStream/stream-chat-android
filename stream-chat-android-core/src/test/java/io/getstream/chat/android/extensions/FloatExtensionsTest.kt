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

package io.getstream.chat.android.extensions

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class FloatExtensionsTest {

    @ParameterizedTest
    @MethodSource("floatExtensionsLimitToArguments")
    fun testFloatExtensionsLimitTo(
        value: Float,
        min: Float,
        max: Float,
        expected: Float,
    ) {
        value.limitTo(min, max) `should be equal to` expected
    }

    @ParameterizedTest
    @MethodSource("floatExtensionsIsIntArguments")
    fun testFloatExtensionsIsInt(
        value: Float,
        expected: Boolean,
    ) {
        value.isInt() `should be equal to` expected
    }

    companion object {

        @JvmStatic
        fun floatExtensionsLimitToArguments() = listOf(
            Arguments.of(1f, 0f, 2f, 1f),
            Arguments.of(1f, 2f, 3f, 2f),
            Arguments.of(3f, 1f, 2f, 2f),
        )

        @JvmStatic
        fun floatExtensionsIsIntArguments() = listOf(
            Arguments.of(1.0f, true),
            Arguments.of(1.1f, false),
            Arguments.of(1.9f, false),
        )
    }
}
