/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.debugger

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.Mother
import io.getstream.chat.android.client.StreamLifecycleObserver
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.observable.FakeSocket
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.internal.verification.Times
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChatClientDebuggerTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        val createdAt = Date()
    }

    lateinit var api: ChatApi
    lateinit var sendMessageInterceptor: SendMessageInterceptor
    lateinit var sendMessageDebugger: SendMessageDebugger
    lateinit var client: ChatClient
    lateinit var fakeChatSocket: FakeSocket
    lateinit var result: MutableList<ChatEvent>
    val token = randomString()
    val userId = randomString()
    val user = Mother.randomUser { id = userId }
    val tokenUtils: TokenUtils = mock()
    var pluginFactories: List<PluginFactory> = emptyList()

    @BeforeEach
    fun setUp() {
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        val apiKey = "api-key"
        val wssUrl = "socket.url"
        val config = ChatClientConfig(
            apiKey,
            "hello.http",
            "cdn.http",
            wssUrl,
            false,
            Mother.chatLoggerConfig(),
            false,
            false
        )
        whenever(tokenUtils.getUserId(token)) doReturn userId
        api = mock()
        sendMessageInterceptor = object : SendMessageInterceptor {
            override suspend fun interceptMessage(
                channelType: String,
                channelId: String,
                message: Message,
                isRetrying: Boolean,
                onUpdate: (Message) -> Unit,
            ): Result<Message> {
                onUpdate(message)
                return Result.success(message)
            }
        }
        sendMessageDebugger = mock()
        fakeChatSocket = FakeSocket()
        val socketStateService = SocketStateService()
        val userStateService = UserStateService()
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)

        val debugger = object : ChatClientDebugger {
            override fun debugSendMessage(
                channelType: String,
                channelId: String,
                message: Message,
                isRetrying: Boolean,
            ): SendMessageDebugger = sendMessageDebugger
        }
        client = ChatClient(
            config = config,
            api = api,
            socket = fakeChatSocket,
            notifications = mock(),
            tokenManager = FakeTokenManager(""),
            socketStateService = socketStateService,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            clientDebugger = debugger,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocketExperimental = mock(),
            lifecycleObserver = StreamLifecycleObserver(lifecycleOwner.lifecycle),
            pluginFactories = pluginFactories,
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            clientState = Mother.mockedClientState(),
            currentUserFetcher = mock(),
        ).apply {
            addInterceptor(sendMessageInterceptor)
            connectUser(user, token).enqueue()
        }

        result = mutableListOf()
    }

    @Test
    fun `Verify that sendMessageDebugger was invoked on message sending`() = runTest {
        /* Given */
        val channelType = "messaging"
        val channelId = "general"
        val message = Message().apply { text = "test-message" }
        val isRetrying = false
        whenever(api.sendMessage(any(), any(), any())) doReturn message.asCall()

        /* When */
        client.sendMessage(channelType, channelId, message, isRetrying).await()

        /* Then */
        verify(sendMessageDebugger, Times(1)).onStart(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionStart(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionUpdate(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionStop(any())
        verify(sendMessageDebugger, Times(1)).onSendStart(any())
        verify(sendMessageDebugger, Times(1)).onSendStop(any())
        verify(sendMessageDebugger, Times(1)).onStop(any())
    }
}
