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

package io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.internal

import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class MessageComposerCapabilitiesTest {

    @ParameterizedTest
    @MethodSource("canSendMessageArguments")
    fun `canSendMessage returns the expected result based on capabilities and message mode`(
        sendEnabled: Boolean,
        messageMode: MessageMode,
        ownCapabilities: Set<String>,
        expected: Boolean,
    ) {
        val state = MessageComposerState(
            sendEnabled = sendEnabled,
            messageMode = messageMode,
            ownCapabilities = ownCapabilities,
        )
        Assertions.assertEquals(expected, canSendMessage(state))
    }

    @ParameterizedTest
    @MethodSource("canUploadFileArguments")
    fun `canUploadFile returns the expected result based on capabilities`(
        ownCapabilities: Set<String>,
        expected: Boolean,
    ) {
        val state = MessageComposerState(ownCapabilities = ownCapabilities)
        Assertions.assertEquals(expected, canUploadFile(state))
    }

    companion object Companion {

        @Suppress("LongMethod")
        @JvmStatic
        fun canSendMessageArguments() = listOf(
            // When sendEnabled is false, should always return false regardless of capabilities or mode
            Arguments.of(
                false,
                MessageMode.Normal,
                setOf(ChannelCapabilities.SEND_MESSAGE),
                false,
            ),
            Arguments.of(
                false,
                MessageMode.Normal,
                emptySet<String>(),
                false,
            ),
            Arguments.of(
                false,
                MessageMode.MessageThread(Message()),
                setOf(ChannelCapabilities.SEND_REPLY),
                false,
            ),
            Arguments.of(
                false,
                MessageMode.MessageThread(Message()),
                emptySet<String>(),
                false,
            ),

            // Normal mode tests: requires SEND_MESSAGE capability
            // Normal mode + SEND_MESSAGE capability + sendEnabled = true -> true
            Arguments.of(
                true,
                MessageMode.Normal,
                setOf(ChannelCapabilities.SEND_MESSAGE),
                true,
            ),
            // Normal mode + SEND_MESSAGE + SEND_REPLY capabilities + sendEnabled = true -> true
            Arguments.of(
                true,
                MessageMode.Normal,
                setOf(ChannelCapabilities.SEND_MESSAGE, ChannelCapabilities.SEND_REPLY),
                true,
            ),
            // Normal mode + SEND_REPLY capability only (no SEND_MESSAGE) + sendEnabled = true -> false
            Arguments.of(
                true,
                MessageMode.Normal,
                setOf(ChannelCapabilities.SEND_REPLY),
                false,
            ),
            // Normal mode + no capabilities + sendEnabled = true -> false
            Arguments.of(
                true,
                MessageMode.Normal,
                emptySet<String>(),
                false,
            ),
            // Normal mode + other capabilities (no SEND_MESSAGE) + sendEnabled = true -> false
            Arguments.of(
                true,
                MessageMode.Normal,
                setOf(ChannelCapabilities.PIN_MESSAGE, ChannelCapabilities.FLAG_MESSAGE),
                false,
            ),

            // Thread mode tests: requires SEND_REPLY capability
            // Thread mode + SEND_REPLY capability + sendEnabled = true -> true
            Arguments.of(
                true,
                MessageMode.MessageThread(Message()),
                setOf(ChannelCapabilities.SEND_REPLY),
                true,
            ),
            // Thread mode + SEND_MESSAGE + SEND_REPLY capabilities + sendEnabled = true -> true
            Arguments.of(
                true,
                MessageMode.MessageThread(Message()),
                setOf(ChannelCapabilities.SEND_MESSAGE, ChannelCapabilities.SEND_REPLY),
                true,
            ),
            // Thread mode + SEND_MESSAGE capability only (no SEND_REPLY) + sendEnabled = true -> false
            Arguments.of(
                true,
                MessageMode.MessageThread(Message()),
                setOf(ChannelCapabilities.SEND_MESSAGE),
                false,
            ),
            // Thread mode + no capabilities + sendEnabled = true -> false
            Arguments.of(
                true,
                MessageMode.MessageThread(Message()),
                emptySet<String>(),
                false,
            ),
            // Thread mode + other capabilities (no SEND_REPLY) + sendEnabled = true -> false
            Arguments.of(
                true,
                MessageMode.MessageThread(Message()),
                setOf(ChannelCapabilities.DELETE_OWN_MESSAGE, ChannelCapabilities.QUOTE_MESSAGE),
                false,
            ),
        )

        @JvmStatic
        fun canUploadFileArguments() = listOf(
            // With UPLOAD_FILE capability -> true
            Arguments.of(setOf(ChannelCapabilities.UPLOAD_FILE), true),
            // With UPLOAD_FILE + other capabilities -> true
            Arguments.of(
                setOf(
                    ChannelCapabilities.UPLOAD_FILE,
                    ChannelCapabilities.SEND_MESSAGE,
                    ChannelCapabilities.PIN_MESSAGE,
                ),
                true,
            ),
            // Without UPLOAD_FILE capability -> false
            Arguments.of(emptySet<String>(), false),
            // With other capabilities but no UPLOAD_FILE -> false
            Arguments.of(
                setOf(ChannelCapabilities.SEND_MESSAGE, ChannelCapabilities.SEND_REPLY),
                false,
            ),
        )
    }
}
