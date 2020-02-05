package io.getstream.chat.android.client.socket

import android.util.Log
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.parser.JsonParser
import okhttp3.Response
import okhttp3.WebSocket
import java.util.*


class EventsParser(
    private val service: ChatSocketServiceImpl,
    private val parser: JsonParser
) : okhttp3.WebSocketListener() {

    private var firstReceivedMessage = false
    private val TAG = javaClass.simpleName

    override fun onOpen(webSocket: WebSocket, response: Response) {
        firstReceivedMessage = true
    }

    override fun onMessage(webSocket: WebSocket, text: String) {

        Log.d(TAG, "onMessage: $text")

        val errorMessage = parser.fromJsonOrError(text, SocketErrorMessage::class.java)
        val errorData = errorMessage.data()

        if (errorMessage.isSuccess && errorData.error != null) {
            handleErrorEvent(errorData.error)
        } else {
            handleEvent(text)
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // Treat as failure and reconnect, socket shouldn't be closed by server
        onFailure(webSocket, ChatNetworkError("server closed connection"), null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        // Called when socket is disconnected by client also (client.disconnect())
        // See issue here https://stream-io.atlassian.net/browse/CAS-88
        service.onSocketError(ChatNetworkError("listener.onFailure error. reconnecting", t))
    }

    private fun handleEvent(text: String) {
        val eventMessage = parser.fromJsonOrError(text, TypedEvent::class.java)

        if (eventMessage.isSuccess) {
            val event = eventMessage.data()

            if (firstReceivedMessage) {
                firstReceivedMessage = false
                val connection = parser.fromJsonOrError(text, ConnectedEvent::class.java)

                if (connection.isSuccess) {
                    service.onConnectionResolved(connection.data())
                } else {
                    service.onSocketError(
                        ChatNetworkError("unable to parse connection event", connection.error())
                    )
                }

            } else {
                service.onEvent(parseEvent(event.type, text))
            }
        } else {
            service.onSocketError(
                ChatNetworkError("Unable to parse message: $text")
            )
        }
    }

    private fun handleErrorEvent(error: ErrorResponse) {
        service.onSocketError(
            ChatNetworkError(
                "$TAG error. Error code: ${error.code}. Message: ${error.message}",
                streamCode = error.code
            )
        )
    }

    private fun parseEvent(type: String, data: String): ChatEvent {
        val result = when (type) {

            //region Messages

            EventType.MESSAGE_NEW.label -> {
                parser.fromJson(data, NewMessageEvent::class.java)
            }
            EventType.MESSAGE_DELETED.label -> {
                parser.fromJson(data, MessageDeletedEvent::class.java)
            }
            EventType.MESSAGE_UPDATED.label -> {
                parser.fromJson(data, MessageUpdatedEvent::class.java)
            }
            EventType.MESSAGE_READ.label -> {
                parser.fromJson(data, MessageReadEvent::class.java)
            }

            //region Typing

            EventType.TYPING_START.label -> {
                parser.fromJson(data, TypingStartEvent::class.java)
            }
            EventType.TYPING_STOP.label -> {
                parser.fromJson(data, TypingStopEvent::class.java)
            }

            //region Reactions

            EventType.REACTION_NEW.label -> {
                parser.fromJson(data, ReactionNewEvent::class.java)
            }
            EventType.REACTION_DELETED.label -> {
                parser.fromJson(data, ReactionDeletedEvent::class.java)
            }

            //region Members

            EventType.MEMBER_ADDED.label -> {
                parser.fromJson(data, MemberAddedEvent::class.java)
            }
            EventType.MEMBER_REMOVED.label -> {
                parser.fromJson(data, MemberRemovedEvent::class.java)
            }
            EventType.MEMBER_UPDATED.label -> {
                parser.fromJson(data, MemberUpdatedEvent::class.java)
            }

            //region Channels

            EventType.CHANNEL_UPDATED.label -> {
                parser.fromJson(data, ChannelUpdatedEvent::class.java)
            }
            EventType.CHANNEL_HIDDEN.label -> {
                parser.fromJson(data, ChannelHiddenEvent::class.java)
            }
            EventType.CHANNEL_DELETED.label -> {
                parser.fromJson(data, ChannelDeletedEvent::class.java)
            }

            //region Watching

            EventType.USER_WATCHING_START.label -> {
                parser.fromJson(data, UserStartWatchingEvent::class.java)
            }
            EventType.USER_WATCHING_STOP.label -> {
                parser.fromJson(data, UserStopWatchingEvent::class.java)
            }

            //region Notifications

            EventType.NOTIFICATION_ADDED_TO_CHANNEL.label -> {
                parser.fromJson(data, AddedToChannelEvent::class.java)
            }
            else -> {
                parser.fromJson(data, ChatEvent::class.java)
            }
        }

        val now = Date()
        result.receivedAt = now
        service.setLastEventDate(now)

        return result
    }

    private data class TypedEvent(val type: String)
}
