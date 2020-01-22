package io.getstream.chat.android.core.poc.library.socket

import com.google.gson.JsonSyntaxException
import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.json.ChatGson
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.*


class EchoWebSocketListener(val service: StreamWebSocketService) : WebSocketListener() {

    private val NORMAL_CLOSURE_STATUS = 1000


    @Synchronized
    override fun onOpen(webSocket: WebSocket, response: Response) {
        if (service.shuttingDown) return
        service.setHealth(true)
        service.isConnecting = false
        service.resetConsecutiveFailures()
        if (service.wsId > 1) {
            service.eventHandler.post { service.connectionRecovered() }
        }
        //Log.d(TAG, "WebSocket #" + wsId.toString() + " Connected : " + response)
    }

    @Synchronized
    override fun onMessage(
        webSocket: WebSocket,
        text: String
    ) { // TODO: synchronized onMessage is not great for performance when receiving many messages at once. Minor concern since its pretty fast at handling a message
        //Log.d(TAG, "WebSocket # " + wsId.toString() + " Response : " + text)
        if (service.shuttingDown) return
        val errorMessage = try {
            ChatGson.instance.fromJson(text, WsErrorMessage::class.java)
        } catch (ignored: JsonSyntaxException) {
            null
        }
        val isError = errorMessage != null && errorMessage.error != null
        if (isError) { // token expiration is handled separately (allowing you to refresh the token from your backend)
            if (errorMessage?.error!!.code == ErrorResponse.TOKEN_EXPIRED_CODE) { // the server closes the connection after sending an error, so we don't need to close it here
// webSocket.close(NORMAL_CLOSURE_STATUS, "token expired");
                service.eventHandler.post { service.tokenExpired() }
                return
            } else { // other errors are passed to the callback
                //TODO: should be delivered to setUser
// the server closes the connection after sending an error, so we don't need to close it here
// webSocket.close(NORMAL_CLOSURE_STATUS, String.format("error with code %d", errorMessage.getError().getCode()));
                service.eventHandler.post { service.onError(errorMessage) }
                return
            }
        }
        val event: Event
        try {
            event = ChatGson.instance.fromJson(text, Event::class.java)
            // set received at, prevents clock issues from breaking our ability to remove old typing indicators
            val now = Date()
            event.receivedAt = now
            service.lastEvent = now
        } catch (e: JsonSyntaxException) {
            //TODO: log error
            e.printStackTrace()
            return
        }

        val name = Thread.currentThread().name

        if (name.isNotEmpty()) {

        }

        //Log.d(TAG, java.lang.String.format("Received event of type %s", event.getType().toString()))
        // resolve on the first good message
        if (!service.connectionResolved) {
            service.eventHandler.post {
                service.setConnectionResolved(event.connectionId, event.me!!)
            }
        }
        service.sendEventToHandlerThread(event)
    }

    @Synchronized
    override fun onClosing(
        webSocket: WebSocket,
        code: Int,
        reason: String
    ) {
        if (service.shuttingDown) return
//        Log.d(
//            TAG,
//            "WebSocket # " + wsId.toString() + " Closing : " + code.toString() + " / " + reason
//        )
        // this usually happens only when the connection fails for auth reasons
        if (code == NORMAL_CLOSURE_STATUS) {
            webSocket.close(code, reason)
        } else {
            service.consecutiveFailures++
            service.isConnecting = false
            service.setHealth(false)
            service.reconnect(true)
            webSocket.close(code, reason)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        if (code == 1000) {

        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        if (service.shuttingDown) {
            service.shuttingDown = false
            service.isConnecting = false
            service.isHealthy = false
            service.connected = false
            service.resetConsecutiveFailures()
            return
        }
        try {
            //Log.i(TAG, "WebSocket # " + wsId.toString() + " Error: " + t.message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        service.consecutiveFailures++
        service.isConnecting = false
        service.setHealth(false)
        service.reconnect(true)
    }
}
