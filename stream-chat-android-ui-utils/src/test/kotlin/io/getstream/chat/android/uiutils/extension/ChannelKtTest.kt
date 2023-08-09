/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.uiutils.extension

import android.content.Context
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.utils.R
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelKtTest {
    @BeforeEach
    fun setup() {
        whenever(context.getString(fallbackResource)) doReturn fallbackText
        whenever(
            context.getString(
                eq(R.string.stream_ui_channel_list_untitled_channel_plus_more),
                anyString(),
                anyInt(),
            ),
        ) doAnswer {
            val users = it.arguments[1] as String
            val count = it.arguments[2] as Int
            "$users +$count more"
        }
    }

    /**
     * Test that the channel name is correctly generated for a channel.
     * This method use [arguments] as a source of arguments.
     */
    @ParameterizedTest
    @MethodSource("arguments")
    fun `Should return proper channel name`(
        channel: Channel,
        maxMembers: Int,
        expectedName: String,
    ) {
        channel.getDisplayName(context, currentUser, fallbackResource, maxMembers) `should be equal to` expectedName
    }

    companion object {

        private val context: Context = mock()
        private val fallbackResource: Int = positiveRandomInt()
        private val fallbackText: String = randomString()
        private val currentUser = randomUser()

        @JvmStatic
        fun arguments() = listOf(
            randomChannel().let { Arguments.of(it, positiveRandomInt(), it.name) },
            randomUser().let {
                Arguments.of(
                    randomChannel(name = "", members = listOf(randomMember(user = it))),
                    positiveRandomInt(),
                    it.name,
                )
            },
            List(positiveRandomInt(4)) { randomUser() }.let {
                Arguments.of(
                    randomChannel(name = "", members = it.map { user -> randomMember(user = user) }),
                    4,
                    it.joinToString(", ") { user -> user.name },
                )
            },
            List(5) { randomUser() }.let {
                val maxMembers = positiveRandomInt(4)
                Arguments.of(
                    randomChannel(name = "", members = it.map { user -> randomMember(user = user) }),
                    maxMembers,
                    "${
                        it.sortedBy(User::name)
                            .take(maxMembers)
                            .joinToString(separator = ", ") { user -> user.name }
                    } +${it.size - maxMembers} more",
                )
            },
            Arguments.of(
                randomChannel(
                    name = "",
                    members = listOf(
                        randomMember(user = currentUser),
                    ),
                ),
                randomInt(),
                currentUser.name,
            ),
            Arguments.of(
                randomChannel(
                    name = "",
                    members = emptyList(),
                ),
                positiveRandomInt(),
                fallbackText,
            ),
        )
    }
}
