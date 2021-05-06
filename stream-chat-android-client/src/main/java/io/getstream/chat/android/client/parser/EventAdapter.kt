package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.models.EventType

internal class EventAdapter(
    private val gson: Gson,
    private val chatEventAdapter: TypeAdapter<ChatEvent>,
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

        val mapData = (mapAdapter.read(reader) as HashMap<*, *>).filterNot { it.value == null }
        val type = mapData["type"] as? String
        val data = gson.toJson(mapData)

        return when (type) {

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
                gson.fromJson(data, NewMessageEvent::class.java).apply { message.enrichWithCid(cid) }
            }
            EventType.MESSAGE_DELETED -> {
                gson.fromJson(data, MessageDeletedEvent::class.java).apply { message.enrichWithCid(cid) }
            }
            EventType.MESSAGE_UPDATED -> {
                gson.fromJson(data, MessageUpdatedEvent::class.java).apply { message.enrichWithCid(cid) }
            }
            EventType.MESSAGE_READ -> when {
                mapData.containsKey("cid") -> gson.fromJson(data, MessageReadEvent::class.java)
                else -> gson.fromJson(data, MarkAllReadEvent::class.java)
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
                gson.fromJson(data, ReactionNewEvent::class.java).apply { message.enrichWithCid(cid) }
            }
            EventType.REACTION_UPDATED -> {
                gson.fromJson(data, ReactionUpdateEvent::class.java).apply { message.enrichWithCid(cid) }
            }
            EventType.REACTION_DELETED -> {
                gson.fromJson(data, ReactionDeletedEvent::class.java).apply { message.enrichWithCid(cid) }
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
                if (mapData.containsKey("user")) {
                    gson.fromJson(data, ChannelUpdatedByUserEvent::class.java).apply { message?.enrichWithCid(cid) }
                } else {
                    gson.fromJson(data, ChannelUpdatedEvent::class.java).apply { message?.enrichWithCid(cid) }
                }
            }
            EventType.CHANNEL_HIDDEN -> {
                gson.fromJson(data, ChannelHiddenEvent::class.java)
            }
            EventType.CHANNEL_DELETED -> {
                gson.fromJson(data, ChannelDeletedEvent::class.java)
            }

            EventType.CHANNEL_VISIBLE -> {
                gson.fromJson(data, ChannelVisibleEvent::class.java)
            }

            EventType.CHANNEL_TRUNCATED -> {
                gson.fromJson(data, ChannelTruncatedEvent::class.java)
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

            EventType.NOTIFICATION_MARK_READ -> when {
                mapData.containsKey("cid") -> gson.fromJson(data, NotificationMarkReadEvent::class.java)
                else -> gson.fromJson(data, MarkAllReadEvent::class.java)
            }

            EventType.NOTIFICATION_MESSAGE_NEW -> {
                gson.fromJson(data, NotificationMessageNewEvent::class.java).apply { message.enrichWithCid(cid) }
            }

            EventType.NOTIFICATION_INVITED -> {
                gson.fromJson(data, NotificationInvitedEvent::class.java)
            }

            EventType.NOTIFICATION_INVITE_ACCEPTED -> {
                gson.fromJson(data, NotificationInviteAcceptedEvent::class.java)
            }

            EventType.NOTIFICATION_INVITE_REJECTED -> {
                gson.fromJson(data, NotificationInviteRejectedEvent::class.java)
            }

            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> {
                gson.fromJson(data, NotificationRemovedFromChannelEvent::class.java)
            }

            EventType.NOTIFICATION_MUTES_UPDATED -> {
                gson.fromJson(data, NotificationMutesUpdatedEvent::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED -> {
                gson.fromJson(data, NotificationChannelMutesUpdatedEvent::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_DELETED -> {
                gson.fromJson(data, NotificationChannelDeletedEvent::class.java)
            }

            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> {
                gson.fromJson(data, NotificationChannelTruncatedEvent::class.java)
            }

            EventType.USER_PRESENCE_CHANGED -> {
                gson.fromJson(data, UserPresenceChangedEvent::class.java)
            }

            EventType.USER_UPDATED -> {
                gson.fromJson(data, UserUpdatedEvent::class.java)
            }

            EventType.USER_DELETED -> {
                gson.fromJson(data, UserDeletedEvent::class.java)
            }

            EventType.USER_BANNED -> {
                if (mapData.containsKey("cid")) {
                    gson.fromJson(data, ChannelUserBannedEvent::class.java)
                } else {
                    gson.fromJson(data, GlobalUserBannedEvent::class.java)
                }
            }

            EventType.USER_UNBANNED -> {
                if (mapData.containsKey("cid")) {
                    gson.fromJson(data, ChannelUserUnbannedEvent::class.java)
                } else {
                    gson.fromJson(data, GlobalUserUnbannedEvent::class.java)
                }
            }
            else -> {
                gson.fromJson(data, UnknownEvent::class.java)
                    .copy(
                        type = mapData["type"]?.toString() ?: EventType.UNKNOWN,
                        rawData = mapData
                    )
            }
        }
    }
}
