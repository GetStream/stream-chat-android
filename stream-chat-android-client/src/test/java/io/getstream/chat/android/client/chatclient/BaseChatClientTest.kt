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

package io.getstream.chat.android.client.chatclient

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.attachment.AttachmentsSender
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.receipts.MessageReceiptManager
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.user.CurrentUserFetcher
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

internal open class BaseChatClientTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Mock
    protected lateinit var userStateService: UserStateService

    @Mock
    protected lateinit var socket: ChatSocket

    @Mock
    protected lateinit var tokenManager: TokenManager
    private lateinit var clientScope: ClientTestScope

    @Mock
    protected lateinit var config: ChatClientConfig

    protected val plugins: MutableList<Plugin> = mutableListOf()

    @Mock
    protected lateinit var api: ChatApi

    @Mock
    protected lateinit var attachmentsSender: AttachmentsSender

    @Mock
    protected lateinit var currentUserFetcher: CurrentUserFetcher

    protected val mutableClientState = mock<MutableClientState>()

    protected lateinit var chatClient: ChatClient
    protected lateinit var fakeChatSocket: FakeChatSocket
    protected val tokenUtils: TokenUtils = mock()
    protected val pluginFactories: MutableList<PluginFactory> = mutableListOf()
    protected val now = Date()
    protected val mockMessageReceiptManager = mock<MessageReceiptManager>()

    @BeforeEach
    fun before() {
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle)
        val networkStateProvider: NetworkStateProvider = mock()
        MockitoAnnotations.openMocks(this)
        whenever(networkStateProvider.isConnected()) doReturn true
        fakeChatSocket = FakeChatSocket(
            userScope = userScope,
            lifecycleObserver = lifecycleObserver,
            tokenManager = tokenManager,
            networkStateProvider = networkStateProvider,
        )
        chatClient = ChatClient(
            config = config,
            api = api,
            dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer),
            notifications = mock(),
            tokenManager = tokenManager,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocket = getChatSocket(),
            pluginFactories = pluginFactories,
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            mutableClientState = mutableClientState,
            currentUserFetcher = currentUserFetcher,
            audioPlayer = mock(),
            now = { now },
            repository = mock(),
            messageReceiptReporter = mock(),
            messageReceiptManager = mockMessageReceiptManager,
        )
        chatClient.attachmentsSender = attachmentsSender
        chatClient.plugins = plugins

        Mockito.reset(
            userStateService,
            socket,
            tokenManager,
            config,
            api,
        )
    }

    protected open fun getChatSocket(): ChatSocket {
        return fakeChatSocket
    }

    fun runCancellableTest(testBody: suspend TestScope.() -> Unit) {
        runTest {
            testBody()
            clientScope.cancel()
        }
    }
}
