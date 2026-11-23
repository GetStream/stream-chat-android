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

import android.os.SystemClock
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A chat backend on localhost. It accepts the socket connection, answers the channel query and the requests that
 * send a message or a reaction, and sends the events a benchmark gives it over the real socket, so they are parsed
 * and handled like the ones of the backend. Like the backend, it also sends the event of each message and
 * reaction the client sends.
 */
internal class MockChatServer {

    private val server = MockWebServer()
    private val socketOpened = CountDownLatch(1)
    private val messages = ConcurrentHashMap<String, MockMessage>()

    @Volatile
    private var socket: WebSocket? = null

    /** The paths the client asked for that are not mocked, to tell a missing mock from a slow SDK. */
    val unmockedPaths = CopyOnWriteArrayList<String>()

    /** When the request that sent a message arrived, in `elapsedRealtimeNanos`, by message id. */
    val messageRequestAt: MutableMap<String, Long> = ConcurrentHashMap()

    /** When the request that sent a reaction arrived, in `elapsedRealtimeNanos`, by the id of its message. */
    val reactionRequestAt: MutableMap<String, Long> = ConcurrentHashMap()

    // Resolved in [start]: the lookup is a network call, which the main thread building the client cannot make.
    lateinit var httpUrl: String
        private set
    val wsUrl: String get() = httpUrl.replaceFirst("http", "ws")

    fun start() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                val arrivedAt = SystemClock.elapsedRealtimeNanos()
                val path = request.requestUrl?.encodedPath.orEmpty()
                val queriedChannelId = channelQuery.matchEntire(path)?.groupValues?.get(1)
                val messageChannelId = messagePath.matchEntire(path)?.groupValues?.get(1)
                val reactionMessageId = reactionPath.matchEntire(path)?.groupValues?.get(1)
                return when {
                    request.getHeader("Upgrade").equals("websocket", ignoreCase = true) ->
                        MockResponse().withWebSocketUpgrade(socketListener)
                    queriedChannelId != null -> json(BenchmarkJson.channelState(queriedChannelId, now()))
                    messageChannelId != null -> messageSent(messageChannelId, request, arrivedAt)
                    reactionMessageId != null -> reactionSent(reactionMessageId, request, arrivedAt)
                    path == SYNC_PATH -> json(BenchmarkJson.SYNC)
                    else -> notMocked("${request.method} $path")
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

    /**
     * Sends the events of [newMessages] at once: they are all built before the first is sent, so building them
     * does not spread the burst. [onSend] gets each message right before its event is sent. The client can react
     * to the messages afterwards.
     */
    fun sendNewMessages(newMessages: List<MockMessage>, onSend: (MockMessage) -> Unit = {}) {
        val events = newMessages.map { message ->
            messages[message.id] = message
            message to BenchmarkJson.newMessage(message, now())
        }
        events.forEach { (message, event) ->
            onSend(message)
            send(event)
        }
    }

    fun shutdown() {
        socket?.close(NORMAL_CLOSURE, null)
        server.shutdown()
    }

    private fun send(event: String) {
        checkNotNull(socket) { "The client has not connected" }.send(event)
    }

    private fun messageSent(channelId: String, request: RecordedRequest, arrivedAt: Long): MockResponse {
        val sent = JSONObject(request.body.readUtf8()).getJSONObject("message")
        val message = MockMessage(
            channelId = channelId,
            id = sent.getString("id"),
            text = sent.getString("text"),
            userId = BenchmarkJson.currentUserId,
        )
        messages[message.id] = message
        messageRequestAt[message.id] = arrivedAt
        send(BenchmarkJson.newMessage(message, now()))
        return json(BenchmarkJson.sentMessage(message, now()))
    }

    private fun reactionSent(messageId: String, request: RecordedRequest, arrivedAt: Long): MockResponse {
        val message = messages[messageId] ?: return notMocked("reaction to the unknown message $messageId")
        val type = JSONObject(request.body.readUtf8()).getJSONObject("reaction").getString("type")
        reactionRequestAt[messageId] = arrivedAt
        send(BenchmarkJson.newReaction(message, type, now()))
        return json(BenchmarkJson.sentReaction(message, type, now()))
    }

    private fun notMocked(request: String): MockResponse {
        unmockedPaths += request
        return json(BenchmarkJson.error()).setResponseCode(HTTP_NOT_FOUND)
    }

    private val socketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            socket = webSocket
            webSocket.send(BenchmarkJson.connected(now()))
            socketOpened.countDown()
        }

        // The client's health checks: answered, so it does not take the connection as lost.
        override fun onMessage(webSocket: WebSocket, text: String) {
            webSocket.send(BenchmarkJson.health(now()))
        }
    }

    private fun now() = System.currentTimeMillis()

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private companion object {
        private const val HTTP_NOT_FOUND = 404
        private const val NORMAL_CLOSURE = 1000
        private const val POLL_MILLIS = 10L
        private const val SYNC_PATH = "/api/v2/chat/sync"
        private const val CHANNEL = "/api/v2/chat/channels/${BenchmarkJson.CHANNEL_TYPE}/([^/]+)"
        private val channelQuery = Regex("$CHANNEL/query")
        private val messagePath = Regex("$CHANNEL/message")
        private val reactionPath = Regex("/api/v2/chat/messages/([^/]+)/reaction")
    }
}
