package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncated
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelVisible
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeleted
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdated
import io.getstream.chat.android.client.events.NotificationChannelTruncated
import io.getstream.chat.android.client.events.NotificationInviteAccepted
import io.getstream.chat.android.client.events.NotificationInviteRejected
import io.getstream.chat.android.client.events.NotificationInvited
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNew
import io.getstream.chat.android.client.events.NotificationMutesUpdated
import io.getstream.chat.android.client.events.NotificationRemovedFromChannel
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserBanned
import io.getstream.chat.android.client.events.UserPresenceChanged
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUnbanned
import io.getstream.chat.android.client.events.UserUpdated
import io.getstream.chat.android.client.models.EventType
import java.util.Date

internal class EventAdapter(
    private val gson: Gson,
    private val chatEventAdapter: TypeAdapter<ChatEvent>
) : TypeAdapter<ChatEvent>() {

    override fun write(out: JsonWriter, value: ChatEvent?) {
        chatEventAdapter.write(out, value)
    }

    override fun read(reader: JsonReader): ChatEvent {

        /**
         * A workaround
         *
         * JsonReader can be read only once
         * But it's required to read type of event before parsing actual event
         * Hence:
         * 1. read as [HashMap]
         * 2. get event.type
         * 3. convert [HashMap] to [String]
         * 4. parse actual event from [String]
         */

        val mapAdapter = gson.getAdapter(HashMap::class.java)

        val mapData = mapAdapter.read(reader) as HashMap<*, *>
        val type = mapData["type"] as String
        val data = gson.toJson(mapData)
        val result = when (type) {

            EventType.HEALTH_CHECK -> {

                /**
                 * [HealthEvent] and [ConnectedEvent] have the same type [EventType.HEALTH_CHECK]
                 */

                if (mapData.containsKey("me")) {
                    gson.fromJson(data, ConnectedEvent::class.java)
                } else {
                    gson.fromJson(data, HealthEvent::class.java)
                }
            }

            //region Messages

            EventType.MESSAGE_NEW -> {
                gson.fromJson(data, NewMessageEvent::class.java)
            }
            EventType.MESSAGE_DELETED -> {
                gson.fromJson(data, MessageDeletedEvent::class.java)
            }
            EventType.MESSAGE_UPDATED -> {
                gson.fromJson(data, MessageUpdatedEvent::class.java)
            }
            EventType.MESSAGE_READ -> {
                gson.fromJson(data, MessageReadEvent::class.java)
            }

            //region Typing

            EventType.TYPING_START -> {
                gson.fromJson(data, TypingStartEvent::class.java)
            }
            EventType.TYPING_STOP -> {
                gson.fromJson(data, TypingStopEvent::class.java)
            }

            //region Reactions

            EventType.REACTION_NEW -> {
                gson.fromJson(data, ReactionNewEvent::class.java)
            }
            EventType.REACTION_DELETED -> {
                gson.fromJson(data, ReactionDeletedEvent::class.java)
            }

            //region Members

            EventType.MEMBER_ADDED -> {
                gson.fromJson(data, MemberAddedEvent::class.java)
            }
            EventType.MEMBER_REMOVED -> {
                gson.fromJson(data, MemberRemovedEvent::class.java)
            }
            EventType.MEMBER_UPDATED -> {
                gson.fromJson(data, MemberUpdatedEvent::class.java)
            }

            //region Channels

            EventType.CHANNEL_UPDATED -> {
                gson.fromJson(data, ChannelUpdatedEvent::class.java)
            }
            EventType.CHANNEL_HIDDEN -> {
                gson.fromJson(data, ChannelHiddenEvent::class.java)
            }
            EventType.CHANNEL_DELETED -> {
                gson.fromJson(data, ChannelDeletedEvent::class.java)
            }

            EventType.CHANNEL_VISIBLE -> {
                gson.fromJson(data, ChannelVisible::class.java)
            }

            EventType.CHANNEL_TRUNCATED -> {
                gson.fromJson(data, ChannelTruncated::class.java)
            }

            //region Watching

            EventType.USER_WATCHING_START -> {
                gson.fromJson(data, UserStartWatchingEvent::class.java)
            }
            EventType.USER_WATCHING_STOP -> {
                gson.fromJson(data, UserStopWatchingEvent::class.java)
            }

            //region Notifications

            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> {
                gson.fromJson(data, NotificationAddedToChannelEvent::class.java)
            }

            EventType.NOTIFICATION_MARK_READ -> {
                gson.fromJson(data, NotificationMarkReadEvent::class.java)
            }

            EventType.NOTIFICATION_MESSAGE_NEW -> {
                gson.fromJson(data, NotificationMessageNew::class.java)
            }

            EventType.NOTIFICATION_INVITED -> {
                gson.fromJson(data, NotificationInvited::class.java)
            }

            EventType.NOTIFICATION_INVITE_ACCEPTED -> {
                gson.fromJson(data, NotificationInviteAccepted::class.java)
            }

            EventType.NOTIFICATION_INVITE_REJECTED -> {
                gson.fromJson(data, NotificationInviteRejected::class.java)
            }

            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> {
                gson.fromJson(data, NotificationRemovedFromChannel::class.java)
            }

            EventType.NOTIFICATION_MUTES_UPDATED -> {
                gson.fromJson(data, NotificationMutesUpdated::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED -> {
                gson.fromJson(data, NotificationChannelMutesUpdated::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_DELETED -> {
                gson.fromJson(data, NotificationChannelDeleted::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> {
                gson.fromJson(data, NotificationChannelTruncated::class.java)
            }

            EventType.USER_PRESENCE_CHANGED -> {
                gson.fromJson(data, UserPresenceChanged::class.java)
            }

            EventType.USER_UPDATED -> {
                gson.fromJson(data, UserUpdated::class.java)
            }

            EventType.USER_BANNED -> {
                gson.fromJson(data, UserBanned::class.java)
            }

            EventType.USER_UNBANNED -> {
                gson.fromJson(data, UserUnbanned::class.java)
            }
            else -> {
                ChatEvent(type)
            }
        }

        result.receivedAt = Date()

        return result
    }
}
