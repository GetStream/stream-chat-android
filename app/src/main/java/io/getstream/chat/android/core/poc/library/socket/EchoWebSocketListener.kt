package io.getstream.chat.android.core.poc.library.socket

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import io.getstream.chat.android.core.poc.library.Event
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.*


class EchoWebSocketListener(val service: StreamWebSocketService) : WebSocketListener() {

    private val NORMAL_CLOSURE_STATUS = 1000

    @Synchronized
    override fun onOpen(webSocket: WebSocket?, response: Response) {
        if (service.shuttingDown) return
        service.setHealth(true)
        service.isConnecting = false
        service.resetConsecutiveFailures()
        if (service.wsId > 1) {
            service.eventThread?.handler?.post { service.webSocketListener.connectionRecovered() }
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
            Gson().fromJson(text, WsErrorMessage::class.java)
        } catch (ignored: JsonSyntaxException) {
            null
        }
        val isError = errorMessage != null && errorMessage.error != null
        if (isError) { // token expiration is handled separately (allowing you to refresh the token from your backend)
            if (errorMessage?.error!!.code == ErrorResponse.TOKEN_EXPIRED_CODE) { // the server closes the connection after sending an error, so we don't need to close it here
// webSocket.close(NORMAL_CLOSURE_STATUS, "token expired");
                service.eventThread?.handler?.post({ service.webSocketListener.tokenExpired() })
                return
            } else { // other errors are passed to the callback
// the server closes the connection after sending an error, so we don't need to close it here
// webSocket.close(NORMAL_CLOSURE_STATUS, String.format("error with code %d", errorMessage.getError().getCode()));
                service.eventThread?.handler?.post({ service.webSocketListener.onError(errorMessage) })
                return
            }
        }
        val event: Event
        try {
            event = Gson().fromJson(text, Event::class.java)
            // set received at, prevents clock issues from breaking our ability to remove old typing indicators
            val now = Date()
            event.receivedAt = now
            service.lastEvent = now
        } catch (e: JsonSyntaxException) {
            e.printStackTrace()
            return
        }
        //Log.d(TAG, java.lang.String.format("Received event of type %s", event.getType().toString()))
        // resolve on the first good message
        if (!service.connectionResolved) {
            service.eventThread?.handler?.post({
                service.webSocketListener.connectionResolved(event)
                service.setConnectionResolved(event.me!!)
            })
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

    override fun onFailure(webSocket: WebSocket?, t: Throwable, response: Response?) {
        if (service.shuttingDown) return
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
