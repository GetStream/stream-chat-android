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

package io.getstream.chat.android.models

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PushPreferenceLevelTest {

    @ParameterizedTest
    @MethodSource("pushPreferenceLevelFromValueArguments")
    fun `test ChatLevel fromValue with valid values`(value: String, expected: PushPreferenceLevel) {
        Assertions.assertEquals(expected, PushPreferenceLevel.fromValue(value))
    }

    @ParameterizedTest
    @MethodSource("invalidPushPreferenceLevelFromValueArguments")
    fun `test ChatLevel fromValue with invalid values`(value: String?, expected: PushPreferenceLevel?) {
        Assertions.assertEquals(expected, PushPreferenceLevel.fromValue(value))
    }

    companion object Companion {

        @JvmStatic
        fun pushPreferenceLevelFromValueArguments() = listOf(
            Arguments.of("all", PushPreferenceLevel.all),
            Arguments.of("mentions", PushPreferenceLevel.mentions),
            Arguments.of("none", PushPreferenceLevel.none),
        )

        @JvmStatic
        fun invalidPushPreferenceLevelFromValueArguments() = listOf(
            Arguments.of(null, null),
            Arguments.of("", PushPreferenceLevel("")),
            Arguments.of("invalid", PushPreferenceLevel("invalid")),
            Arguments.of("ALL", PushPreferenceLevel("ALL")),
            Arguments.of("Mentions", PushPreferenceLevel("Mentions")),
            Arguments.of("MENTIONS", PushPreferenceLevel("MENTIONS")),
            Arguments.of("NONE", PushPreferenceLevel("NONE")),
            Arguments.of("all ", PushPreferenceLevel("all ")),
            Arguments.of(" all", PushPreferenceLevel(" all")),
            Arguments.of("unknown", PushPreferenceLevel("unknown")),
            Arguments.of("123", PushPreferenceLevel("123")),
        )
    }
}
