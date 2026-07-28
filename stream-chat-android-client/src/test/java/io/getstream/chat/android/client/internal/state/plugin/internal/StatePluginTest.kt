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

package io.getstream.chat.android.client.internal.state.plugin.internal

import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

internal class StatePluginTest {

    @Test
    fun `constructing the plugin wires the thread query listener to the state listener`() = runTest {
        val threadLogic: ThreadLogic = mock()
        val logic: LogicRegistry = mock {
            on(it.thread(any())) doReturn threadLogic
        }
        val statePlugin = StatePlugin(
            errorHandlerFactory = mock(),
            logic = logic,
            repositoryFacade = mock(),
            clientState = mock(),
            stateRegistry = mock(),
            syncManager = mock(),
            eventHandler = mock(),
            mutableGlobalState = mock(),
            groupedUnreadChannelsUpdater = mock(),
            queryingChannelsFree = MutableStateFlow(true),
            chatClientConfig = ChatClientConfig(),
        )

        val result = statePlugin.onGetRepliesPrecondition(randomString())

        result shouldBeInstanceOf Result.Success::class
        verify(threadLogic).isLoadingMessages()
    }
}
