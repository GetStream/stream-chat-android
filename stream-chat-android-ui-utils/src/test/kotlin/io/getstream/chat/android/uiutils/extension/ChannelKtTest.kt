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

package io.getstream.chat.android.uiutils.extension

import android.content.Context
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.ui.utils.R
import io.getstream.chat.android.uiutils.extension.ChannelKtTest.Companion.arguments
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
import java.util.Date

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
        argSetNum: Int,
        channel: Channel,
        maxMembers: Int,
        expectedName: String,
    ) {
        try {
            channel.getDisplayName(context, currentUser, fallbackResource, maxMembers) `should be equal to` expectedName
        } catch (e: Throwable) {
            System.err.println("Failed on test #$argSetNum")
            throw e
        }
    }

    @Test
    fun `Should return silent regular message as preview when it is the latest`() {
        // Given
        val user = randomUser()
        val olderMessage = randomMessage(
            text = "Older message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val latestSilentMessage = randomMessage(
            text = "Latest silent message",
            type = MessageType.REGULAR,
            silent = true,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(isInsideSearch = false, messages = listOf(olderMessage, latestSilentMessage))

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(latestSilentMessage, previewMessage)
    }

    @Test
    fun `Should return non-silent regular message as preview when it is the latest`() {
        // Given
        val user = randomUser()
        val olderSilentMessage = randomMessage(
            text = "Older silent message",
            type = MessageType.REGULAR,
            silent = true,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val latestNonSilentMessage = randomMessage(
            text = "Latest non-silent message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(olderSilentMessage, latestNonSilentMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(latestNonSilentMessage, previewMessage)
    }

    @Test
    fun `Should return system message as preview when it is the latest`() {
        // Given
        val user = randomUser()
        val olderRegularMessage = randomMessage(
            text = "Older regular message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val latestSystemMessage = randomMessage(
            text = "User joined the channel",
            type = MessageType.SYSTEM,
            silent = false,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(olderRegularMessage, latestSystemMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(latestSystemMessage, previewMessage)
    }

    @Test
    fun `Should ignore deleted messages for preview`() {
        // Given
        val user = randomUser()
        val validMessage = randomMessage(
            text = "Valid message",
            type = MessageType.REGULAR,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = null,
            deletedForMe = false,
            user = user,
            shadowed = false,
        )
        val deletedSilentMessage = randomMessage(
            text = "Deleted silent message",
            type = MessageType.REGULAR,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = Date(2500L),
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(validMessage, deletedSilentMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertEquals(validMessage, previewMessage)
    }

    @Test
    fun `Should return null when no valid messages exist`() {
        // Given
        val user = randomUser()
        val deletedMessage = randomMessage(
            text = "Deleted message",
            type = MessageType.REGULAR,
            silent = false,
            createdAt = Date(1000L),
            createdLocallyAt = null,
            deletedAt = Date(1500L),
            user = user,
            shadowed = false,
        )
        val ephemeralMessage = randomMessage(
            text = "Ephemeral message",
            type = MessageType.EPHEMERAL,
            silent = false,
            createdAt = Date(2000L),
            createdLocallyAt = null,
            deletedAt = null,
            user = user,
            shadowed = false,
        )
        val channel = randomChannel(
            isInsideSearch = false,
            messages = listOf(deletedMessage, ephemeralMessage),
        )

        // When
        val previewMessage = channel.getPreviewMessage(user)

        // Then
        Assertions.assertNull(previewMessage)
    }

    companion object {

        private const val ARG_SET_1 = 1
        private const val ARG_SET_2 = 2
        private const val ARG_SET_3 = 3
        private const val ARG_SET_4 = 4
        private const val ARG_SET_5 = 5
        private const val ARG_SET_6 = 6

        private val context: Context = mock()
        private val fallbackResource: Int = positiveRandomInt()
        private val fallbackText: String = randomString()
        private val currentUser = randomUser()

        @JvmStatic
        fun arguments() = listOf(
            randomChannel().let {
                Arguments.of(
                    ARG_SET_1,
                    it,
                    positiveRandomInt(),
                    it.name,
                )
            },
            randomUser().let {
                Arguments.of(
                    ARG_SET_2,
                    randomChannel(name = "", members = listOf(randomMember(user = it))),
                    positiveRandomInt(),
                    it.name,
                )
            },
            List(positiveRandomInt(4)) { randomUser() }.let {
                Arguments.of(
                    ARG_SET_3,
                    randomChannel(name = "", members = it.map { user -> randomMember(user = user) }),
                    4,
                    it.sortedBy(User::name)
                        .joinToString(", ") { user -> user.name },
                )
            },
            List(5) { randomUser() }.let {
                val maxMembers = positiveRandomInt(4)
                Arguments.of(
                    ARG_SET_4,
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
                ARG_SET_5,
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
                ARG_SET_6,
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
