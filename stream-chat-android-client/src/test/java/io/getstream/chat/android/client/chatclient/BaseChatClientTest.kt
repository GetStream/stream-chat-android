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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.clientstate.SocketStateService
import io.getstream.chat.android.client.clientstate.UserStateService
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.utils.TokenUtils
import io.getstream.chat.android.client.utils.retry.NoRetryPolicy
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.TestCoroutineRule
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock

@ExtendWith(value = [TestCoroutineExtension::class])
internal open class BaseChatClientTest {
    @get:Rule
    val coroutineRule = TestCoroutineRule()

    @Mock
    protected lateinit var socketStateService: SocketStateService

    @Mock
    protected lateinit var userStateService: UserStateService

    @Mock
    protected lateinit var socket: ChatSocket

    @Mock
    protected lateinit var tokenManager: TokenManager

    @Mock
    protected lateinit var config: ChatClientConfig

    protected lateinit var plugins: MutableList<Plugin>

    @Mock
    protected lateinit var api: ChatApi

    protected val initializationCoordinator = InitializationCoordinator.create()

    protected lateinit var chatClient: ChatClient
    internal val tokenUtils: TokenUtils = mock()

    @BeforeEach
    fun before() {
        MockitoAnnotations.openMocks(this)
        plugins = mutableListOf()
        chatClient = ChatClient(
            config = config,
            api = api,
            socket = socket,
            notifications = mock(),
            tokenManager = tokenManager,
            socketStateService = socketStateService,
            queryChannelsPostponeHelper = mock(),
            userCredentialStorage = mock(),
            userStateService = userStateService,
            tokenUtils = tokenUtils,
            scope = coroutineRule.scope,
            retryPolicy = NoRetryPolicy(),
            initializationCoordinator = initializationCoordinator,
            appSettingsManager = mock(),
        )
    }
}
