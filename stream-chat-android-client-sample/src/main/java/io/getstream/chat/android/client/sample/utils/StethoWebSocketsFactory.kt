package io.getstream.chat.android.client.sample.utils

import com.facebook.stetho.inspector.network.NetworkEventReporterImpl
import com.facebook.stetho.inspector.network.SimpleBinaryInspectorWebSocketFrame
import com.facebook.stetho.inspector.network.SimpleTextInspectorWebSocketFrame
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class StethoWebSocketsFactory(private val httpClient: OkHttpClient) : WebSocket.Factory {
    private val reporter = NetworkEventReporterImpl.get()

    override fun newWebSocket(request: Request, listener: WebSocketListener): WebSocket {
        val requestId = reporter.nextRequestId()
        val newListener = StethoWebSocketListener(listener, requestId)
        val wrappedSocket = httpClient.newWebSocket(request, newListener)
        return StethoWebSocket(wrappedSocket, requestId)
    }

    inner class StethoWebSocketListener(
        private val listener: WebSocketListener,
        private val requestId: String
    ) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            listener.onOpen(webSocket, response)
            reporter.webSocketCreated(requestId, webSocket.request().url.toString())
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            listener.onClosed(webSocket, code, reason)
            reporter.webSocketClosed(requestId)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            listener.onFailure(webSocket, t, response)
            reporter.webSocketFrameError(requestId, t.message)
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            listener.onMessage(webSocket, bytes)
            reporter.webSocketFrameReceived(
                SimpleBinaryInspectorWebSocketFrame(requestId, bytes.toByteArray())
            )
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            listener.onMessage(webSocket, text)
            reporter.webSocketFrameReceived(
                SimpleTextInspectorWebSocketFrame(requestId, text)
            )
        }
    }

    inner class StethoWebSocket(
        private val wrappedSocket: WebSocket,
        private val requestId: String
    ) : WebSocket {
        private val reporter = NetworkEventReporterImpl.get()

        override fun queueSize() = wrappedSocket.queueSize()

        override fun send(text: String): Boolean {
            reporter.webSocketFrameSent(SimpleTextInspectorWebSocketFrame(requestId, text))
            return wrappedSocket.send(text)
        }

        override fun send(bytes: ByteString): Boolean {
            reporter.webSocketFrameSent(SimpleBinaryInspectorWebSocketFrame(requestId, bytes.toByteArray()))
            return wrappedSocket.send(bytes)
        }

        override fun close(code: Int, reason: String?) = wrappedSocket.close(code, reason)

        override fun cancel() = wrappedSocket.cancel()

        override fun request() = wrappedSocket.request()
    }
}
