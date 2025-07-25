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

package io.getstream.chat.android.client.header

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class VersionPrefixHeaderTest {

    @ParameterizedTest
    @MethodSource("versionPrefixHeaderArguments")
    fun testVersionPrefixHeader(type: VersionPrefixHeader, expectedPrefix: String) {
        assertEquals(expectedPrefix, type.prefix)
    }

    companion object {

        @JvmStatic
        fun versionPrefixHeaderArguments() = listOf(
            Arguments.of(VersionPrefixHeader.Default, "stream-chat-android-"),
            Arguments.of(VersionPrefixHeader.UiComponents, "stream-chat-android-ui-components-"),
            Arguments.of(VersionPrefixHeader.Compose, "stream-chat-android-compose-"),
        )
    }
}
