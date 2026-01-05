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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.initials
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class InitialsExtensionsTests {

    /** [provideNames] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.ui.common.utils.InitialsExtensionsTests#provideNames")
    fun `Should return initials of the user name`(name: String, initials: String) {
        val user = User(name = name)

        user.initials `should be equal to` initials
    }

    /** [provideNames] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.ui.common.utils.InitialsExtensionsTests#provideNames")
    fun `Should return initials of the channel name`(name: String, initials: String) {
        val channel = Channel(name = name)

        channel.initials `should be equal to` initials
    }

    companion object {

        @JvmStatic
        fun provideNames() = listOf(
            Arguments.of("    AaaA  A", "AA"),
            Arguments.of("", ""),
            Arguments.of(" ", ""),
            Arguments.of("    ", ""),
            Arguments.of("    a", "A"),
            Arguments.of("    A", "A"),
            Arguments.of("    Aaa", "A"),
            Arguments.of("    AaaA", "A"),
            Arguments.of("    AaaA A", "AA"),
            Arguments.of("    AaaA A   ", "AA"),
            Arguments.of("AaaA  A   ", "AA"),
            Arguments.of("AaaA  B   ", "AB"),
            Arguments.of("AaaA  b   ", "AB"),
            Arguments.of("baaA  b   ", "BB"),
            Arguments.of("caaA  b asdf  asdf asdf ", "CB"),
            Arguments.of("@   ", "@"),
            Arguments.of("$   ", "$"),
            Arguments.of(" #   ", "#"),
            Arguments.of("@  @ ", "@@"),
            Arguments.of("$   $", "$$"),
            Arguments.of(" #   #", "##"),
        )
    }
}
