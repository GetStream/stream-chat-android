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

package io.getstream.chat.android.state.extensions

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChatClientExtensionsTest {

    @Test
    fun `Given ChatClient not initialized, When collecting globalStateFlow, There are no emissions()`() = runTest {
        // given
        val initializationState = InitializationState.NOT_INITIALIZED
        val globalState = mock<GlobalState>()
        val sut = Fixture().get(initializationState, globalState)
        // when
        sut.globalStateFlow.test {
            // then
            expectNoEvents()
        }
    }

    @Test
    fun `Given ChatClient initializing, When collecting globalStateFlow, There are no emissions()`() = runTest {
        // given
        val initializationState = InitializationState.INITIALIZING
        val globalState = mock<GlobalState>()
        val sut = Fixture().get(initializationState, globalState)
        // when / then
        sut.globalStateFlow.test {
            // then
            expectNoEvents()
        }
    }

    @Test
    fun `Given ChatClient initialized, When collecting globalStateFlow, Then globalState is emitted()`() = runTest {
        // given
        val initializationState = InitializationState.COMPLETE
        val globalState = mock<GlobalState>()
        val sut = Fixture().get(initializationState, globalState)
        // when / then
        sut.globalStateFlow.test {
            // then
            val emission = awaitItem()
            emission shouldBeEqualTo globalState
            expectNoEvents()
        }
    }

    private class Fixture {

        fun get(
            initializationState: InitializationState,
            globalState: GlobalState,
        ): ChatClient {
            val client = mock<ChatClient>()
            // Prepare initialization state
            val clientState = mock<ClientState>()
            whenever(clientState.initializationState)
                .thenReturn(MutableStateFlow(initializationState))
            whenever(client.awaitInitializationState(any()))
                .thenReturn(initializationState)
            // Prepare StatePlugin
            val statePlugin: StatePlugin = mock()
            whenever(statePlugin.resolveDependency(eq(GlobalState::class))) doReturn globalState
            whenever(client.plugins).thenReturn(listOf(statePlugin))
            whenever(client.clientState).thenReturn(clientState)
            return client
        }
    }
}
