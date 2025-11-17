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
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.attachment.AttachmentsSender
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.persistence.repository.ChatClientRepository
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
    lateinit var sendMessageDebugger: SendMessageDebugger
    lateinit var attachmentsSender: AttachmentsSender
    lateinit var client: ChatClient
    lateinit var fakeChatSocket: FakeChatSocket
    lateinit var result: MutableList<ChatEvent>
    val token = randomString()
    val userId = randomString()
    val user = randomUser(id = userId)
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
            false,
            NotificationConfig(),
        )
        whenever(tokenUtils.getUserId(token)) doReturn userId
        api = mock()
        sendMessageDebugger = mock()
        attachmentsSender = mock()
        val userStateService = UserStateService()
        val clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle)
        val tokenManager = FakeTokenManager("")
        val networkStateProvider: NetworkStateProvider = mock()
        whenever(networkStateProvider.isConnected()) doReturn true
        fakeChatSocket = FakeChatSocket(
            userScope = userScope,
            lifecycleObserver = lifecycleObserver,
            tokenManager = tokenManager,
            apiKey = apiKey,
            wssUrl = wssUrl,
            networkStateProvider = networkStateProvider,
        )
        val debugger = object : ChatClientDebugger {
            override fun debugSendMessage(
                channelType: String,
                channelId: String,
                message: Message,
                isRetrying: Boolean,
            ): SendMessageDebugger = sendMessageDebugger
        }
        val mockRepository = mock<ChatClientRepository> {
            onBlocking { selectMessageReceipts(limit = any()) } doReturn emptyList()
        }
        client = ChatClient(
            config = config,
            api = api,
            dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer),
            notifications = mock(),
            tokenManager = tokenManager,
            userCredentialStorage = mock(),
            userStateService = userStateService,
            clientDebugger = debugger,
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = NoRetryPolicy(),
            appSettingsManager = mock(),
            chatSocket = fakeChatSocket,
            pluginFactories = pluginFactories,
            mutableClientState = Mother.mockedClientState(),
            repositoryFactoryProvider = NoOpRepositoryFactory.Provider,
            currentUserFetcher = mock(),
            audioPlayer = mock(),
            repository = mockRepository,
            messageReceiptReporter = mock(),
            messageReceiptManager = mock(),
        ).apply {
            attachmentsSender = this@ChatClientDebuggerTest.attachmentsSender
            connectUser(user, token).enqueue()
        }

        result = mutableListOf()
    }

    @Test
    fun `Verify that sendMessageDebugger was invoked on message sending`() = runTest {
        /* Given */
        val channelType = "messaging"
        val channelId = "general"
        val message = Message(text = "test-message")
        val isRetrying = false
        whenever(attachmentsSender.sendAttachments(any(), any(), any(), any())) doReturn Result.Success(message)
        whenever(api.sendMessage(any(), any(), any())) doReturn message.asCall()

        /* When */
        client.sendMessage(channelType, channelId, message, isRetrying).await()

        /* Then */
        verify(sendMessageDebugger, Times(1)).onStart(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionStart(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionUpdate(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionStop(any(), any())
        verify(sendMessageDebugger, Times(1)).onSendStart(any())
        verify(sendMessageDebugger, Times(1)).onSendStop(any(), any())
        verify(sendMessageDebugger, Times(1)).onStop(any(), any())
    }

    @Test
    fun `Verify that sendMessageDebugger was invoked once on sending the same message 2 times`() = runTest {
        /* Given */
        val channelType = "messaging"
        val channelId = "general"
        val message = Message(id = "id_1", text = "test-message")
        val isRetrying = false
        whenever(attachmentsSender.sendAttachments(any(), any(), any(), any())) doReturn Result.Success(message)
        whenever(api.sendMessage(any(), any(), any())) doReturn message.asCall()

        /* When */
        listOf(
            async { client.sendMessage(channelType, channelId, message, isRetrying).await() },
            async { client.sendMessage(channelType, channelId, message, isRetrying).await() },
        ).awaitAll()

        /* Then */
        verify(sendMessageDebugger, Times(1)).onStart(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionStart(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionUpdate(any())
        verify(sendMessageDebugger, Times(1)).onInterceptionStop(any(), any())
        verify(sendMessageDebugger, Times(1)).onSendStart(any())
        verify(sendMessageDebugger, Times(1)).onSendStop(any(), any())
        verify(sendMessageDebugger, Times(1)).onStop(any(), any())
    }
}
