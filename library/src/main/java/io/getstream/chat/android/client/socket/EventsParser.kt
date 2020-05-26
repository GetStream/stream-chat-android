package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.parser.ChatParser
import okhttp3.Response
import okhttp3.WebSocket
import java.util.*


internal class EventsParser(
    private val service: ChatSocketService,
    private val parser: ChatParser
) : okhttp3.WebSocketListener() {

    private var firstMessageReceived = false
    private val logger = ChatLogger.get("Events")

    override fun onOpen(webSocket: WebSocket, response: Response) {
        logger.logI("onOpen")
        firstMessageReceived = false
    }

    override fun onMessage(webSocket: WebSocket, text: String) {

        try {
            val errorMessage = parser.fromJsonOrError(text, SocketErrorMessage::class.java)
            val errorData = errorMessage.data()

            if (errorMessage.isSuccess && errorData.error != null) {
                handleErrorEvent(errorData.error)
            } else {
                handleEvent(text)
            }
        } catch (t: Throwable) {
            logger.logE("onMessage", t)
            service.onSocketError(ChatNetworkError.create(ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT))
        }
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {

    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        // Treat as failure and reconnect, socket shouldn't be closed by server
        onFailure(webSocket, ChatNetworkError.create(ChatErrorCode.SOCKET_CLOSED), null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        logger.logE("onFailure", t)
        // Called when socket is disconnected by client also (client.disconnect())
        // See issue here https://stream-io.atlassian.net/browse/CAS-88
        service.onSocketError(ChatNetworkError.create(ChatErrorCode.SOCKET_FAILURE, t))
    }

    private fun handleEvent(text: String) {
        val eventMessage = parser.fromJsonOrError(text, TypedEvent::class.java)

        if (eventMessage.isSuccess) {
            val event = eventMessage.data()

            if (!firstMessageReceived) {

                val connection = parser.fromJsonOrError(text, ConnectedEvent::class.java)
                val data = connection.data()

                if (connection.isSuccess && data.isValid()) {
                    firstMessageReceived = true
                    service.onConnectionResolved(data)
                } else {
                    service.onSocketError(
                        ChatNetworkError.create(ChatErrorCode.CANT_PARSE_CONNECTION_EVENT, connection.error())
                    )
                }

            } else {
                val parsedEvent = parseEvent(event.type, text)
                service.onEvent(parsedEvent)
            }


        } else {
            service.onSocketError(
                ChatNetworkError.create(ChatErrorCode.CANT_PARSE_EVENT, eventMessage.error())
            )
        }
    }

    private fun handleErrorEvent(error: ErrorResponse) {
        service.onSocketError(
            ChatNetworkError.create(error.code, error.message, error.statusCode)
        )
    }

    private fun parseEvent(type: String, data: String): ChatEvent {
        val result = when (type) {

            //region Messages

            EventType.MESSAGE_NEW -> {
                parser.fromJson(data, NewMessageEvent::class.java)
            }
            EventType.MESSAGE_DELETED -> {
                parser.fromJson(data, MessageDeletedEvent::class.java)
            }
            EventType.MESSAGE_UPDATED -> {
                parser.fromJson(data, MessageUpdatedEvent::class.java)
            }
            EventType.MESSAGE_READ -> {
                parser.fromJson(data, MessageReadEvent::class.java)
            }

            //region Typing

            EventType.TYPING_START -> {
                parser.fromJson(data, TypingStartEvent::class.java)
            }
            EventType.TYPING_STOP -> {
                parser.fromJson(data, TypingStopEvent::class.java)
            }

            //region Reactions

            EventType.REACTION_NEW -> {
                parser.fromJson(data, ReactionNewEvent::class.java)
            }
            EventType.REACTION_DELETED -> {
                parser.fromJson(data, ReactionDeletedEvent::class.java)
            }

            //region Members

            EventType.MEMBER_ADDED -> {
                parser.fromJson(data, MemberAddedEvent::class.java)
            }
            EventType.MEMBER_REMOVED -> {
                parser.fromJson(data, MemberRemovedEvent::class.java)
            }
            EventType.MEMBER_UPDATED -> {
                parser.fromJson(data, MemberUpdatedEvent::class.java)
            }

            //region Channels

            EventType.CHANNEL_UPDATED -> {
                parser.fromJson(data, ChannelUpdatedEvent::class.java)
            }
            EventType.CHANNEL_HIDDEN -> {
                parser.fromJson(data, ChannelHiddenEvent::class.java)
            }
            EventType.CHANNEL_DELETED -> {
                parser.fromJson(data, ChannelDeletedEvent::class.java)
            }

            EventType.CHANNEL_VISIBLE -> {
                parser.fromJson(data, ChannelVisible::class.java)
            }

            EventType.CHANNEL_TRUNCATED -> {
                parser.fromJson(data, ChannelTruncated::class.java)
            }

            //region Watching

            EventType.USER_WATCHING_START -> {
                parser.fromJson(data, UserStartWatchingEvent::class.java)
            }
            EventType.USER_WATCHING_STOP -> {
                parser.fromJson(data, UserStopWatchingEvent::class.java)
            }

            //region Notifications

            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> {
                parser.fromJson(data, NotificationAddedToChannelEvent::class.java)
            }

            EventType.NOTIFICATION_MARK_READ -> {
                parser.fromJson(data, NotificationMarkReadEvent::class.java)
            }

            EventType.NOTIFICATION_MESSAGE_NEW -> {
                parser.fromJson(data, NotificationMessageNew::class.java)
            }

            EventType.NOTIFICATION_INVITED -> {
                parser.fromJson(data, NotificationInvited::class.java)
            }

            EventType.NOTIFICATION_INVITE_ACCEPTED -> {
                parser.fromJson(data, NotificationInviteAccepted::class.java)
            }

            EventType.NOTIFICATION_INVITE_REJECTED -> {
                parser.fromJson(data, NotificationInviteRejected::class.java)
            }

            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> {
                parser.fromJson(data, NotificationRemovedFromChannel::class.java)
            }

            EventType.NOTIFICATION_MUTES_UPDATED -> {
                parser.fromJson(data, NotificationMutesUpdated::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_DELETED -> {
                parser.fromJson(data, NotificationChannelDeleted::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> {
                parser.fromJson(data, NotificationChannelTruncated::class.java)
            }

            EventType.HEALTH_CHECK -> {
                parser.fromJson(data, HealthEvent::class.java)
            }

            EventType.USER_PRESENCE_CHANGED -> {
                parser.fromJson(data, UserPresenceChanged::class.java)
            }

            EventType.USER_UPDATED -> {
                parser.fromJson(data, UserUpdated::class.java)
            }

            EventType.USER_BANNED -> {
                parser.fromJson(data, UserBanned::class.java)
            }

            EventType.USER_UNBANNED -> {
                parser.fromJson(data, UserUnbanned::class.java)
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
