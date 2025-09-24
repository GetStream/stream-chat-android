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

package io.getstream.chat.android.compose.ui

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import kotlinx.coroutines.test.TestScope
import org.junit.After
import org.junit.Before
import org.mockito.Answers
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever

private val MockClientState = mock<ClientState>(defaultAnswer = Answers.RETURNS_MOCKS)
private val MockChatClient = mock<ChatClient>(defaultAnswer = Answers.RETURNS_MOCKS)

internal interface ComposeTest {

    /**
     * Mocked [ChatClient] available for configuration whenever needed. e.g.:
     *
     * ```kotlin
     * @Test
     * fun `my test case`() {
     *     whenever(mockChatClient.getCurrentUser()) doReturn PreviewUserData.user1
     *     // Your test code here
     * }
     * ```
     */
    val mockChatClient: ChatClient get() = MockChatClient
    val mockClientState: ClientState get() = MockClientState

    /**
     * Bind the mocked [ChatClient] instance to the singleton before each test case run.
     */
    @Before
    fun setUp() {
        object : ChatClient.ChatClientBuilder() {
            override fun internalBuild(): ChatClient = MockChatClient
        }.build()
        whenever(MockChatClient.clientState) doReturn MockClientState
        whenever(MockChatClient.inheritScope(any())) doReturn TestScope()
    }

    @After
    fun tearDown() {
        reset(MockChatClient, MockClientState)
    }
}
