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
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
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
import org.junit.Before
import org.junit.Rule

internal open class MockWebServerTest {

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
        setupSdk()
        setupServer()
    }

    private fun setupSdk() {
        val offlinePluginFactory = StreamOfflinePluginFactory(
            config = Config(),
            appContext = context
        )

        ChatClient.Builder("hrwwzsgrzapv", context)
            .baseUrl(mockWebServer.url("/").toString())
            .withPlugin(offlinePluginFactory)
            .logLevel(ChatLogLevel.ALL)
            .build()
    }

    private fun setupServer() {
        mockWebServer.dispatcher = object : Dispatcher() {
            @Throws(InterruptedException::class)
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (request.requestLine.contains("connect?json")) {
                    return MockResponse().withWebSocketUpgrade(
                        object : WebSocketListener() {
                            override fun onOpen(webSocket: WebSocket, response: Response) {
                                super.onOpen(webSocket, response)
                                this@MockWebServerTest.webSocket = webSocket
                                webSocket.send(readFileContents(WS_HEALTH))
                            }
                        }
                    )
                }

                if (request.path!!.startsWith("/channels?")) {
                    return MockResponse().setResponseCode(200).setBody(readFileContents(HTTP_CHANNELS))
                }

                if (request.path!!.startsWith("/channels/messaging/general/query")) {
                    val contents = readFileContents(HTTP_CHANNEL)
                    return MockResponse().setResponseCode(200).setBody(contents)
                }

                if (request.path!!.startsWith("/channels/messaging/general/message")) {
                    sendEvent(readFileContents(WS_MESSAGE_NEW))
                    return MockResponse().setResponseCode(200).setBody(readFileContents(HTTP_MESSAGE))
                }

                return MockResponse().setResponseCode(404)
            }
        }
    }

    fun sendEvent(text: String) {
        webSocket.send(text)
    }

    companion object {
        private const val HTTP_CHANNELS: String = "http_channels.json"
        private const val HTTP_CHANNEL: String = "http_channel.json"
        private const val HTTP_MESSAGE: String = "http_message.json"

        private const val WS_HEALTH: String = "ws_health_check.json"
        private const val WS_MESSAGE_NEW: String = "ws_message_new.json"
    }
}
