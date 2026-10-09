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

package io.getstream.chat.android.ai.ondevice

import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("StringShouldBeRawString") // The line breaks are part of the prompt being checked.
internal class OnDevicePromptTest {

    @Test
    fun `a question on its own is the prompt`() {
        assertEquals("What's 2 + 2?", OnDevicePrompt.text(instructions = null, history = emptyList(), question = " What's 2 + 2? "))
    }

    @Test
    fun `the conversation so far frames the question`() {
        val prompt = OnDevicePrompt.text(
            instructions = "Answer briefly.",
            history = listOf(AIConversationTurn.user("Hi"), AIConversationTurn.assistant("Hello! ")),
            question = "What can you do?",
        )

        assertEquals(
            "Answer briefly.\n\nConversation so far:\nUser: Hi\nAssistant: Hello!\n\n" +
                "Answer the user's last message.\n\nUser: What can you do?",
            prompt,
        )
    }

    @Test
    fun `tokens are estimated from UTF-8 bytes`() {
        assertEquals(1, OnDevicePrompt.estimatedTokens("abc"))
        assertEquals(3, OnDevicePrompt.estimatedTokens("日本語"))
    }
}
