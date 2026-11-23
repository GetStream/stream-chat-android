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

package io.getstream.chat.android.client.benchmark

import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A chat backend on localhost: it accepts the socket connection, answers the channel query, and sends the events
 * a benchmark gives it over the real socket, so they are parsed and handled like the ones of the backend.
 */
internal class MockChatServer {

    private val server = MockWebServer()
    private val socketOpened = CountDownLatch(1)

    @Volatile
    private var socket: WebSocket? = null

    /** The paths the client asked for that are not mocked, to tell a missing mock from a slow SDK. */
    val unmockedPaths = CopyOnWriteArrayList<String>()

    // Resolved in [start]: the lookup is a network call, which the main thread building the client cannot make.
    lateinit var httpUrl: String
        private set
    val wsUrl: String get() = httpUrl.replaceFirst("http", "ws")

    fun start() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val path = request.requestUrl?.encodedPath.orEmpty()
                val channelId = channelQuery.find(path)?.groupValues?.get(1)
                return when {
                    request.getHeader("Upgrade").equals("websocket", ignoreCase = true) ->
                        MockResponse().withWebSocketUpgrade(socketListener)
                    channelId != null -> json(BenchmarkJson.channelState(channelId, System.currentTimeMillis()))
                    path == SYNC_PATH -> json(BenchmarkJson.SYNC)
                    else -> {
                        unmockedPaths += "${request.method} $path"
                        json(BenchmarkJson.error()).setResponseCode(HTTP_NOT_FOUND)
                    }
                }
            }
        }
        server.start()
        httpUrl = server.url("/").toString()
    }

    fun awaitSocket(timeoutSeconds: Long): Boolean = socketOpened.await(timeoutSeconds, TimeUnit.SECONDS)

    /** Waits until the client has asked for [request], a method and a path that is not mocked. */
    fun awaitUnmocked(request: String, timeoutSeconds: Long): Boolean {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(timeoutSeconds)
        while (request !in unmockedPaths) {
            if (System.nanoTime() > deadline) return false
            Thread.sleep(POLL_MILLIS)
        }
        return true
    }

    fun send(event: String) {
        checkNotNull(socket) { "The client has not connected" }.send(event)
    }

    fun shutdown() {
        socket?.close(NORMAL_CLOSURE, null)
        server.shutdown()
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            socket = webSocket
            webSocket.send(BenchmarkJson.connected(System.currentTimeMillis()))
            socketOpened.countDown()
        }

        // The client's health checks: answered, so it does not take the connection as lost.
        override fun onMessage(webSocket: WebSocket, text: String) {
            webSocket.send(BenchmarkJson.health(System.currentTimeMillis()))
        }
    }

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private companion object {
        private const val HTTP_NOT_FOUND = 404
        private const val NORMAL_CLOSURE = 1000
        private const val POLL_MILLIS = 10L
        private const val SYNC_PATH = "/api/v2/chat/sync"
        private val channelQuery = Regex("/api/v2/chat/channels/${BenchmarkJson.CHANNEL_TYPE}/([^/]+)/query")
    }
}
