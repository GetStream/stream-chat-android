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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.concurrent.atomic.AtomicInteger

internal class ChatClientExtensionTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val MESSAGE_LIMIT = 30
        private const val ONE_SEC = 1000L
    }

    private lateinit var channel: Channel
    private lateinit var userFlow: MutableStateFlow<User?>
    private lateinit var initializationStateFlow: MutableStateFlow<InitializationState>
    private lateinit var chatClient: ChatClient
    private lateinit var pluginFactories: List<PluginFactory>
    private lateinit var plugins: List<Plugin>

    @BeforeEach
    fun setUp() {
        channel = randomChannel()

        val statePluginConfig = StatePluginConfig()
        val globalState: GlobalState = mock() {
        }
        val channelState: ChannelState = mock {
            on(it.cid) doReturn channel.cid
            on(it.channelType) doReturn channel.type
            on(it.channelId) doReturn channel.id
        }
        val stateRegistry: StateRegistry = mock { stateRegistry ->
            on(stateRegistry.channel(channel.type, channel.id)) doReturn channelState
        }

        val request = QueryChannelPaginationRequest(MESSAGE_LIMIT)
            .toWatchChannelRequest(statePluginConfig.userPresence)
            .apply {
                this.shouldRefresh = false
                this.isWatchChannel = true
            }

        pluginFactories = listOf<PluginFactory>(
            mock<StreamStatePluginFactory> {
                on(it.resolveDependency(StatePluginConfig::class)) doReturn statePluginConfig
            },
        )

        plugins = listOf(
            mock<StatePlugin> {
                on(it.resolveDependency(StatePluginConfig::class)) doReturn statePluginConfig
                on(it.resolveDependency(GlobalState::class)) doReturn globalState
                on(it.resolveDependency(StateRegistry::class)) doReturn stateRegistry
            },
        )

        userFlow = MutableStateFlow(null)
        initializationStateFlow = MutableStateFlow(
            InitializationState.NOT_INITIALIZED,
        )

        val clientState: ClientState = mock() {
            on(it.user) doReturn userFlow
            on(it.initializationState) doReturn initializationStateFlow
        }

        val connectionData = ConnectionData(
            user = randomUser(),
            connectionId = "connectionId",
        )

        chatClient = mock {
            on(it.clientState) doReturn clientState
            on(it.pluginFactories) doReturn pluginFactories
            on(it.plugins) doReturn plugins
            on(it.queryChannel(channel.type, channel.id, request)) doReturn channel.asCall()
            on(it.connectAnonymousUser()) doAnswer {
                userFlow.value = connectionData.user
                initializationStateFlow.value = InitializationState.COMPLETE
                connectionData.asCall()
            }
            on(it.disconnect(flushPersistence = true)) doAnswer {
                initializationStateFlow.value = InitializationState.NOT_INITIALIZED
                userFlow.value = null
                Unit.asCall()
            }
            on(it.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
        }
    }

    @Test
    fun `watchChannelAsState should emit when InitializationState becomes COMPLETE`() = runTest {
        val channelStateEmissions = AtomicInteger(0)
        val channelStateFlow = MutableStateFlow<ChannelState?>(null)
        val localScope = testCoroutines.scope + Job()
        localScope.launch {
            chatClient.watchChannelAsState(channel.cid, MESSAGE_LIMIT).collect {
                channelStateFlow.value = it
                channelStateEmissions.incrementAndGet()
            }
        }
        delay(ONE_SEC)

        chatClient.connectAnonymousUser().await()

        delay(ONE_SEC)

        val channelState = channelStateFlow.first { it != null }
        channelState?.cid shouldBeEqualTo channel.cid
        channelStateEmissions.get() shouldBeEqualTo 2
    }

    @Test
    fun `watchChannelAsState should emit when InitializationState becomes NOT_INITIALIZED`() = runTest {
        userFlow.value = randomUser()
        initializationStateFlow.value = InitializationState.COMPLETE
        val channelStateEmissions = AtomicInteger(0)
        val channelStateFlow = MutableStateFlow<ChannelState?>(null)
        val localScope = testCoroutines.scope + Job()
        localScope.launch {
            chatClient.watchChannelAsState(channel.cid, MESSAGE_LIMIT).collect {
                channelStateFlow.value = it
                channelStateEmissions.incrementAndGet()
            }
        }
        delay(ONE_SEC)
        val channelState = channelStateFlow.first { it != null }
        channelState?.cid shouldBeEqualTo channel.cid
        channelStateEmissions.get() shouldBeEqualTo 2

        chatClient.disconnect(flushPersistence = true).await()

        delay(ONE_SEC)

        channelStateFlow.value shouldBeEqualTo null
        channelStateEmissions.get() shouldBeEqualTo 3
    }

    @Test
    fun `watchChannelAsState shouldn't queryChannel twice when the user is the same with but some properties has changed`() = runTest {
        userFlow.value = randomUser(id = "jc")
        initializationStateFlow.value = InitializationState.COMPLETE
        val localScope = testCoroutines.scope + Job()
        localScope.launch {
            chatClient.watchChannelAsState(channel.cid, MESSAGE_LIMIT).collect { }
        }
        userFlow.value = randomUser(id = "jc")
        verify(chatClient, times(1)).queryChannel(any(), any(), any(), any())
    }

    @Test
    fun `watchChannelAsState should queryChannel again when the user is a different one`() = runTest {
        userFlow.value = randomUser(id = "jc")
        initializationStateFlow.value = InitializationState.COMPLETE
        val localScope = testCoroutines.scope + Job()
        localScope.launch {
            chatClient.watchChannelAsState(channel.cid, MESSAGE_LIMIT).collect { }
        }
        userFlow.value = randomUser(id = "123")
        verify(chatClient, times(2)).queryChannel(any(), any(), any(), any())
    }
}
