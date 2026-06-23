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

package io.getstream.chat.android.ui.common.state.channel.info

import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState.Content
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState.Content.Option
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ChannelInfoViewStateTest {

    @ParameterizedTest(name = "isMuted is {1} when {0}")
    @MethodSource("isMutedArguments")
    fun `Content isMuted reflects the mute options`(testData: IsMutedTestData) {
        Content(options = testData.options).isMuted `should be equal to` testData.expected
    }

    internal data class IsMutedTestData(
        val description: String,
        val options: List<Option>,
        val expected: Boolean,
    ) {
        override fun toString(): String = description
    }

    private companion object {
        @JvmStatic
        fun isMutedArguments() = listOf(
            IsMutedTestData(
                description = "there are no options",
                options = emptyList(),
                expected = false,
            ),
            IsMutedTestData(
                description = "there is no mute option",
                options = listOf(Option.PinnedMessages, Option.MediaAttachments),
                expected = false,
            ),
            IsMutedTestData(
                description = "the channel is muted",
                options = listOf(Option.MuteChannel(isMuted = true)),
                expected = true,
            ),
            IsMutedTestData(
                description = "the channel is not muted",
                options = listOf(Option.MuteChannel(isMuted = false)),
                expected = false,
            ),
            IsMutedTestData(
                description = "only the other user is muted in a direct message",
                options = listOf(Option.MuteChannel(isMuted = false), Option.MuteUser(isMuted = true)),
                expected = true,
            ),
            IsMutedTestData(
                description = "only the channel is muted in a direct message",
                options = listOf(Option.MuteChannel(isMuted = true), Option.MuteUser(isMuted = false)),
                expected = true,
            ),
            IsMutedTestData(
                description = "neither the channel nor the other user is muted in a direct message",
                options = listOf(Option.MuteChannel(isMuted = false), Option.MuteUser(isMuted = false)),
                expected = false,
            ),
        ).map { testData -> Arguments.of(testData) }
    }
}
