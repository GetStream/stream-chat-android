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

package io.getstream.chat.android.client

import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.client.api.ChatApiConfig
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.persistance.repository.noop.NoOpRepositoryFactory
import io.getstream.chat.android.client.persistence.repository.ChatClientRepository
import io.getstream.chat.android.client.receipts.MessageReceiptManager
import io.getstream.chat.android.client.scope.ClientTestScope
import io.getstream.chat.android.client.scope.UserTestScope
import io.getstream.chat.android.client.setup.state.internal.MutableClientState
import io.getstream.chat.android.client.socket.FakeChatSocket
import io.getstream.chat.android.client.token.FakeTokenManager
import io.getstream.chat.android.client.user.CredentialConfig
import io.getstream.chat.android.client.user.storage.UserCredentialStorage
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.internal.ServerClockOffset
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

internal class ChatClientRepositoriesTests {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var clientScope: ClientTestScope
    private lateinit var client: ChatClient
    private lateinit var fakeChatSocket: FakeChatSocket
    private val userId = randomString()
    private val jwt = randomString()
    private val user = User(id = userId)
    private val streamDateFormatter = StreamDateFormatter()
    private val config = mock<ChatApiConfig>()
    private val userCredentialStorage = mock<UserCredentialStorage>()
    private val repository = mock<ChatClientRepository>()
    private val messageReceiptManager = mock<MessageReceiptManager>()
    private val userRepository = mock<UserRepository>()
    private val usersWithRepositories = mutableListOf<String>()
    private val repositoryFactoryProvider = object : RepositoryFactory.Provider {
        override fun createRepositoryFactory(user: User): RepositoryFactory {
            usersWithRepositories += user.id
            return object : RepositoryFactory by NoOpRepositoryFactory {
                override fun createUserRepository(): UserRepository = userRepository
            }
        }
    }

    @BeforeEach
    fun setup() {
        val tokenUtils = mock<TokenUtils>()
        whenever(tokenUtils.getUserId(jwt)) doReturn userId
        whenever(config.notificationConfig) doReturn NotificationConfig()
        val lifecycleOwner = TestLifecycleOwner(coroutineDispatcher = testCoroutines.dispatcher)
        clientScope = ClientTestScope(testCoroutines.scope)
        val userScope = UserTestScope(clientScope)
        val tokenManager = FakeTokenManager("")
        val networkStateProvider = mock<NetworkStateProvider>()
        whenever(networkStateProvider.isConnected()) doReturn true
        fakeChatSocket = FakeChatSocket(
            userScope = userScope,
            lifecycleObserver = StreamLifecycleObserver(userScope, lifecycleOwner.lifecycle),
            tokenManager = tokenManager,
            networkStateProvider = networkStateProvider,
        )
        client = ChatClient(
            config = config,
            api = mock(),
            dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer),
            notifications = mock(),
            tokenManager = tokenManager,
            userCredentialStorage = userCredentialStorage,
            userStateService = UserStateService(),
            tokenUtils = tokenUtils,
            clientScope = clientScope,
            userScope = userScope,
            retryPolicy = mock(),
            appSettingsManager = mock(),
            chatSocket = fakeChatSocket,
            serverClockOffset = ServerClockOffset(),
            pluginFactories = emptyList(),
            repositoryFactoryProvider = repositoryFactoryProvider,
            mutableClientState = MutableClientState(mock()),
            currentUserFetcher = mock(),
            audioPlayer = mock(),
            repository = repository,
            messageReceiptReporter = mock(),
            messageReceiptManager = messageReceiptManager,
        ).apply {
            attachmentsSender = mock()
        }
    }

    @Test
    fun `Given stored credentials and no user set, the repositories should be empty and not created`() = runClientTest {
        givenStoredCredentials(userId)

        client.repositoryFacade.selectUser(userId)

        usersWithRepositories `should be equal to` emptyList()
        verify(userRepository, never()).selectUser(any())
    }

    @Test
    fun `Given a connected user, its repositories should be created once`() = runClientTest {
        connect { connectUser(user, jwt) }

        client.repositoryFacade.selectUser(userId)
        client.repositoryFacade.selectUser(userId)

        usersWithRepositories `should be equal to` listOf(userId)
        verify(userRepository, times(2)).selectUser(userId)
    }

    @Test
    fun `Given a user set without connecting, connecting another user should replace the repositories once`() =
        runClientTest {
            val storedUserId = randomString()
            givenStoredCredentials(storedUserId)
            client.setUserWithoutConnectingIfNeeded()

            connect { connectUser(user, jwt) }
            client.repositoryFacade.selectUser(userId)

            usersWithRepositories `should be equal to` listOf(storedUserId, userId)
        }

    @Test
    fun `Given a disconnected user with stored credentials, the repositories should be empty and not created again`() =
        runClientTest {
            connect { connectUser(user, jwt) }
            client.disconnect(flushPersistence = false).await()
            givenStoredCredentials(userId)

            client.repositoryFacade.selectUser(userId)

            usersWithRepositories `should be equal to` listOf(userId)
            verify(userRepository, never()).selectUser(any())
        }

    @Test
    fun `Given a connected user, disconnecting with flushPersistence should clear its repositories`() = runClientTest {
        connect { connectUser(user, jwt) }

        client.disconnect(flushPersistence = true).await()

        verify(userRepository).clear()
    }

    @Test
    fun `Given a connected user, disconnecting without flushPersistence should keep its repositories' data`() =
        runClientTest {
            connect { connectUser(user, jwt) }

            client.disconnect(flushPersistence = false).await()

            verify(userRepository, never()).clear()
        }

    @Test
    fun `Given stored credentials and no user set, switching to another user should clear the stored user's data`() =
        runClientTest {
            val storedUserId = randomString()
            givenStoredCredentials(storedUserId)

            connect { switchUser(user, jwt) }

            usersWithRepositories `should be equal to` listOf(storedUserId, userId)
            verify(userRepository).clear()
        }

    @Test
    fun `Given a user set while a disconnect is running, its repositories should stay`() = runClientTest {
        val otherUserId = randomString()
        connect { connectUser(user, jwt) }
        givenStoredCredentials(otherUserId)
        whenever(repository.clear()) doSuspendableAnswer { client.setUserWithoutConnectingIfNeeded() }

        client.disconnect(flushPersistence = false).await()
        client.repositoryFacade.selectUser(otherUserId)

        usersWithRepositories `should be equal to` listOf(userId, otherUserId)
        verify(userRepository).selectUser(otherUserId)
    }

    @Test
    fun `Given stored credentials and no user set, marking a message as delivered should set the stored user first`() =
        runClientTest {
            val messageId = randomString()
            givenStoredCredentials(userId)
            var usersWithRepositoriesOnCall = emptyList<String>()
            whenever(messageReceiptManager.markMessageAsDelivered(messageId)) doSuspendableAnswer {
                usersWithRepositoriesOnCall = usersWithRepositories.toList()
                true
            }

            val result = client.markMessageAsDelivered(messageId).await()

            result `should be equal to` Result.Success(true)
            usersWithRepositoriesOnCall `should be equal to` listOf(userId)
        }

    private fun givenStoredCredentials(userId: String) {
        whenever(userCredentialStorage.get())
            .doReturn(CredentialConfig(userId, randomString(), randomString(), isAnonymous = false))
    }

    private suspend fun connect(call: ChatClient.() -> Call<ConnectionData>) {
        val createdAt = Date()
        val rawCreatedAt = streamDateFormatter.format(createdAt)
        val event = ConnectedEvent(EventType.HEALTH_CHECK, createdAt, rawCreatedAt, user, randomString())
        val connection = testCoroutines.scope.async { client.call().await() }
        fakeChatSocket.mockEventReceived(event)
        connection.await().shouldBeInstanceOf(Result.Success::class)
    }

    private fun runClientTest(testBody: suspend TestScope.() -> Unit) {
        runTest {
            testBody()
            clientScope.cancel()
        }
    }
}
