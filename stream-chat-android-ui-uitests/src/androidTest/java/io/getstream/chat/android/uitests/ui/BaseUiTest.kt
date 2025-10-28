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

package io.getstream.chat.android.uitests.ui

import android.content.Context
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.uitests.app.login.LoginActivity
import io.getstream.chat.android.uitests.ui.util.CoroutineTaskExecutorRule
import io.getstream.chat.android.uitests.util.readFileContents
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import org.junit.After
import org.junit.Before
import org.junit.Rule

internal abstract class BaseUiTest {

    private val context: Context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    private val mockWebServer = MockWebServer()

    @get:Rule
    val composeTestRule = createAndroidComposeRule<LoginActivity>()

    @get:Rule
    val coroutineTaskExecutorRule = CoroutineTaskExecutorRule()

    private lateinit var webSocket: WebSocket

    @Before
    fun setup() {
        setupStreamSdk()
        setupMockWebServer()
    }

    @After
    fun tearDown() {
        ChatClient.instance().disconnect(flushPersistence = false).execute()
    }

    private fun setupStreamSdk() {
        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = false,
                userPresence = false,
            ),
            appContext = context,
        )

        ChatClient.Builder("hrwwzsgrzapv", context)
            .baseUrl(mockWebServer.url("/").toString())
            .withPlugins(statePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

    private fun setupMockWebServer() {
        mockWebServer.dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = requireNotNull(request.path)

                return if (path.startsWith("/connect?json")) {
                    MockResponse().withWebSocketUpgrade(
                        object : WebSocketListener() {
                            override fun onOpen(webSocket: WebSocket, response: Response) {
                                super.onOpen(webSocket, response)
                                this@BaseUiTest.webSocket = webSocket
                                webSocket.send(readFileContents(WS_HEALTH_CHECK))
                            }
                        },
                    )
                } else if (path.startsWith("/channels?")) {
                    okResponse(readFileContents(HTTP_CHANNELS))
                } else if (path.startsWith("/channels/messaging/general/query")) {
                    okResponse(readFileContents(HTTP_CHANNEL))
                } else if (path.startsWith("/channels/messaging/general/message")) {
                    val messageId = JSONObject(request.body.readUtf8()).getString("id")
                    webSocket.send(readFileContents(WS_MESSAGE_NEW).replace(":messageId", messageId))
                    okResponse(readFileContents(HTTP_MESSAGE).replace(":messageId", messageId))
                } else {
                    MockResponse().setResponseCode(404)
                }
            }
        }
    }

    private fun okResponse(body: String): MockResponse = MockResponse()
        .setResponseCode(200)
        .setBody(body)

    companion object {
        private const val WS_HEALTH_CHECK: String = "ws_health_check.json"
        private const val WS_MESSAGE_NEW: String = "ws_message_new.json"

        private const val HTTP_CHANNELS: String = "http_channels.json"
        private const val HTTP_CHANNEL: String = "http_channel.json"
        private const val HTTP_MESSAGE: String = "http_message.json"
    }
}
