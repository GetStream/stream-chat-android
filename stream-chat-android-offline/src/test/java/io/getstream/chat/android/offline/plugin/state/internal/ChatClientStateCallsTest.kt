/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.plugin.state.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class ChatClientStateCallsTest {

    private val chatClient: ChatClient = mock {
        on(it.queryChannels(any())) doReturn TestCall(Result(emptyList()))
    }

    private val state: StateRegistry = mock()
    private val scope = TestScope()

    private val stateCalls: ChatClientStateCalls = ChatClientStateCalls(chatClient, state, scope)

    @Test
    fun `query channels should not trigger many times in sequence`() {
        val request = QueryChannelsRequest(filter = Filters.neutral(), limit = 1)

        repeat(5) {
            stateCalls.queryChannels(request, false)
        }

        verify(chatClient, times(1)).queryChannels(any())
    }
}
