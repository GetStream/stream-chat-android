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

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.GeneratedEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.network.models.AIIndicatorClearEvent
import io.getstream.chat.android.network.models.AIIndicatorStopEvent
import io.getstream.chat.android.network.models.AIIndicatorUpdateEvent
import io.getstream.chat.android.network.models.ChannelDeletedEvent
import io.getstream.chat.android.network.models.ChannelHiddenEvent
import io.getstream.chat.android.network.models.ChannelTruncatedEvent
import io.getstream.chat.android.network.models.ChannelUpdatedEvent
import io.getstream.chat.android.network.models.ChannelVisibleEvent
import io.getstream.chat.android.network.models.ConnectedEvent
import io.getstream.chat.android.network.models.DraftDeletedEvent
import io.getstream.chat.android.network.models.DraftUpdatedEvent
import io.getstream.chat.android.network.models.HealthCheckEvent
import io.getstream.chat.android.network.models.MemberAddedEvent
import io.getstream.chat.android.network.models.MemberRemovedEvent
import io.getstream.chat.android.network.models.MemberUpdatedEvent
import io.getstream.chat.android.network.models.MessageDeletedEvent
import io.getstream.chat.android.network.models.MessageDeliveredEvent
import io.getstream.chat.android.network.models.MessageNewEvent
import io.getstream.chat.android.network.models.MessageReadEvent
import io.getstream.chat.android.network.models.MessageUpdatedEvent
import io.getstream.chat.android.network.models.NotificationAddedToChannelEvent
import io.getstream.chat.android.network.models.NotificationChannelDeletedEvent
import io.getstream.chat.android.network.models.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.network.models.NotificationChannelTruncatedEvent
import io.getstream.chat.android.network.models.NotificationInviteAcceptedEvent
import io.getstream.chat.android.network.models.NotificationInviteRejectedEvent
import io.getstream.chat.android.network.models.NotificationInvitedEvent
import io.getstream.chat.android.network.models.NotificationMarkReadEvent
import io.getstream.chat.android.network.models.NotificationMarkUnreadEvent
import io.getstream.chat.android.network.models.NotificationMutesUpdatedEvent
import io.getstream.chat.android.network.models.NotificationNewMessageEvent
import io.getstream.chat.android.network.models.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.network.models.NotificationThreadMessageNewEvent
import io.getstream.chat.android.network.models.PollClosedEvent
import io.getstream.chat.android.network.models.PollDeletedEvent
import io.getstream.chat.android.network.models.PollUpdatedEvent
import io.getstream.chat.android.network.models.PollVoteCastedEvent
import io.getstream.chat.android.network.models.PollVoteChangedEvent
import io.getstream.chat.android.network.models.PollVoteRemovedEvent
import io.getstream.chat.android.network.models.ReactionDeletedEvent
import io.getstream.chat.android.network.models.ReactionNewEvent
import io.getstream.chat.android.network.models.ReactionUpdatedEvent
import io.getstream.chat.android.network.models.ReminderCreatedEvent
import io.getstream.chat.android.network.models.ReminderDeletedEvent
import io.getstream.chat.android.network.models.ReminderNotificationEvent
import io.getstream.chat.android.network.models.ReminderUpdatedEvent
import io.getstream.chat.android.network.models.ThreadUpdatedEvent
import io.getstream.chat.android.network.models.TypingStartEvent
import io.getstream.chat.android.network.models.TypingStopEvent
import io.getstream.chat.android.network.models.UserBannedEvent
import io.getstream.chat.android.network.models.UserDeletedEvent
import io.getstream.chat.android.network.models.UserMessagesDeletedEvent
import io.getstream.chat.android.network.models.UserPresenceChangedEvent
import io.getstream.chat.android.network.models.UserUnbannedEvent
import io.getstream.chat.android.network.models.UserUpdatedEvent
import io.getstream.chat.android.network.models.UserWatchingStartEvent
import io.getstream.chat.android.network.models.UserWatchingStopEvent
import io.getstream.chat.android.network.models.WSEvent
import java.lang.reflect.Type
import io.getstream.chat.android.network.infrastructure.ExactDate as GeneratedExactDate

internal class EventAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return when (type.rawType) {
            ChatEventDto::class.java -> EventDtoAdapter(moshi)
            else -> null
        }
    }
}

/**
 * The generated adapter of every event type that always maps to one model, shared by the v1 and v2 socket parsers.
 * Types whose model depends on the payload, connection errors and unknown types are left to each parser.
 */
@Suppress("LongMethod")
internal fun generatedEventAdapters(moshi: Moshi): Map<String, JsonAdapter<ChatEventDto>> = mapOf(
    EventType.DRAFT_MESSAGE_UPDATED to generatedEventAdapter<DraftUpdatedEvent>(moshi) { mapOf("draft" to draft) },
    EventType.DRAFT_MESSAGE_DELETED to generatedEventAdapter<DraftDeletedEvent>(moshi) { mapOf("draft" to draft) },
    EventType.MESSAGE_NEW to generatedEventAdapter<MessageNewEvent>(moshi) { mapOf("cid" to cid, "user" to user) },
    EventType.MESSAGE_DELETED to generatedEventAdapter<MessageDeletedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.MESSAGE_UPDATED to generatedEventAdapter<MessageUpdatedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.MESSAGE_READ to generatedEventAdapter<MessageReadEvent>(moshi) {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "user" to user)
    },
    EventType.MESSAGE_DELIVERED to generatedEventAdapter<MessageDeliveredEvent>(moshi) {
        mapOf(
            "cid" to cid,
            "channel_type" to channelType,
            "channel_id" to channelId,
            "user" to user,
            // The spec types it as a string; an unparseable value counts as missing.
            "last_delivered_at" to lastDeliveredAt?.let(GeneratedExactDate::parseOrNull),
            "last_delivered_message_id" to lastDeliveredMessageId,
        )
    },
    EventType.TYPING_START to generatedEventAdapter<TypingStartEvent>(moshi) { mapOf("cid" to cid, "user" to user) },
    EventType.TYPING_STOP to generatedEventAdapter<TypingStopEvent>(moshi) { mapOf("cid" to cid, "user" to user) },
    EventType.REACTION_NEW to generatedEventAdapter<ReactionNewEvent>(moshi) {
        mapOf("cid" to cid, "user" to user, "message" to message, "reaction" to reaction)
    },
    EventType.REACTION_UPDATED to generatedEventAdapter<ReactionUpdatedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user, "reaction" to reaction)
    },
    EventType.REACTION_DELETED to generatedEventAdapter<ReactionDeletedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user, "message" to message, "reaction" to reaction)
    },
    EventType.MEMBER_ADDED to generatedEventAdapter<MemberAddedEvent>(moshi) { mapOf("cid" to cid, "user" to user) },
    EventType.MEMBER_REMOVED to generatedEventAdapter<MemberRemovedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.MEMBER_UPDATED to generatedEventAdapter<MemberUpdatedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.CHANNEL_UPDATED to generatedEventAdapter<ChannelUpdatedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.CHANNEL_HIDDEN to generatedEventAdapter<ChannelHiddenEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.CHANNEL_DELETED to generatedEventAdapter<ChannelDeletedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.CHANNEL_VISIBLE to generatedEventAdapter<ChannelVisibleEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.CHANNEL_TRUNCATED to generatedEventAdapter<ChannelTruncatedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.USER_WATCHING_START to generatedEventAdapter<UserWatchingStartEvent>(moshi) { mapOf("cid" to cid) },
    EventType.USER_WATCHING_STOP to generatedEventAdapter<UserWatchingStopEvent>(moshi) { mapOf("cid" to cid) },
    EventType.NOTIFICATION_ADDED_TO_CHANNEL to generatedEventAdapter<NotificationAddedToChannelEvent>(moshi) {
        mapOf("cid" to cid)
    },
    EventType.NOTIFICATION_MARK_UNREAD to generatedEventAdapter<NotificationMarkUnreadEvent>(moshi) {
        mapOf(
            "cid" to cid,
            "user" to user,
            "first_unread_message_id" to firstUnreadMessageId,
            "last_read_at" to lastReadAt,
            "unread_messages" to unreadMessages,
        )
    },
    EventType.NOTIFICATION_MESSAGE_NEW to generatedEventAdapter<NotificationNewMessageEvent>(moshi) {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId)
    },
    EventType.NOTIFICATION_THREAD_MESSAGE_NEW to generatedEventAdapter<NotificationThreadMessageNewEvent>(moshi) {
        mapOf("cid" to cid, "unread_threads" to unreadThreads, "unread_thread_messages" to unreadThreadMessages)
    },
    EventType.THREAD_UPDATED to generatedEventAdapter<ThreadUpdatedEvent>(moshi) {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "thread" to thread)
    },
    EventType.NOTIFICATION_INVITED to generatedEventAdapter<NotificationInvitedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.NOTIFICATION_INVITE_ACCEPTED to generatedEventAdapter<NotificationInviteAcceptedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.NOTIFICATION_INVITE_REJECTED to generatedEventAdapter<NotificationInviteRejectedEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.NOTIFICATION_REMOVED_FROM_CHANNEL to generatedEventAdapter<NotificationRemovedFromChannelEvent>(moshi) {
        mapOf("cid" to cid)
    },
    EventType.NOTIFICATION_MUTES_UPDATED to generatedEventAdapter<NotificationMutesUpdatedEvent>(moshi) { emptyMap() },
    EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED to generatedEventAdapter<NotificationChannelMutesUpdatedEvent>(moshi) {
        emptyMap()
    },
    EventType.NOTIFICATION_CHANNEL_DELETED to generatedEventAdapter<NotificationChannelDeletedEvent>(moshi) {
        mapOf("cid" to cid)
    },
    EventType.NOTIFICATION_CHANNEL_TRUNCATED to generatedEventAdapter<NotificationChannelTruncatedEvent>(moshi) {
        mapOf("cid" to cid)
    },
    EventType.USER_PRESENCE_CHANGED to generatedEventAdapter<UserPresenceChangedEvent>(moshi) { emptyMap() },
    EventType.USER_UPDATED to generatedEventAdapter<UserUpdatedEvent>(moshi) { emptyMap() },
    EventType.USER_DELETED to generatedEventAdapter<UserDeletedEvent>(moshi) { emptyMap() },
    EventType.USER_BANNED to generatedEventAdapter<UserBannedEvent>(moshi) { emptyMap() },
    EventType.USER_UNBANNED to generatedEventAdapter<UserUnbannedEvent>(moshi) { emptyMap() },
    EventType.POLL_UPDATED to generatedEventAdapter<PollUpdatedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.POLL_DELETED to generatedEventAdapter<PollDeletedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.POLL_CLOSED to generatedEventAdapter<PollClosedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.POLL_VOTE_CASTED to generatedEventAdapter<PollVoteCastedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.POLL_VOTE_CHANGED to generatedEventAdapter<PollVoteChangedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.POLL_VOTE_REMOVED to generatedEventAdapter<PollVoteRemovedEvent>(moshi) { mapOf("cid" to cid) },
    EventType.REMINDER_CREATED to generatedEventAdapter<ReminderCreatedEvent>(moshi) { emptyMap() },
    EventType.REMINDER_UPDATED to generatedEventAdapter<ReminderUpdatedEvent>(moshi) { emptyMap() },
    EventType.REMINDER_DELETED to generatedEventAdapter<ReminderDeletedEvent>(moshi) { emptyMap() },
    EventType.NOTIFICATION_REMINDER_DUE to generatedEventAdapter<ReminderNotificationEvent>(moshi) { emptyMap() },
    EventType.USER_MESSAGES_DELETED to generatedEventAdapter<UserMessagesDeletedEvent>(moshi) { emptyMap() },
    EventType.AI_TYPING_INDICATOR_UPDATED to generatedEventAdapter<AIIndicatorUpdateEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.AI_TYPING_INDICATOR_CLEAR to generatedEventAdapter<AIIndicatorClearEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
    EventType.AI_TYPING_INDICATOR_STOP to generatedEventAdapter<AIIndicatorStopEvent>(moshi) {
        mapOf("cid" to cid, "user" to user)
    },
)

internal inline fun <reified T : WSEvent> generatedEventAdapter(
    moshi: Moshi,
    noinline required: T.() -> Map<String, Any?>,
): JsonAdapter<ChatEventDto> = GeneratedEventAdapter(moshi.adapter(T::class.java), required)

/**
 * Parses an event with its generated model. The spec makes some fields optional that the domain event
 * requires (e.g. the cid, since feeds shares these events without one); an event missing any of the
 * [required] fields is rejected.
 */
internal class GeneratedEventAdapter<T : WSEvent>(
    private val delegate: JsonAdapter<T>,
    private val required: T.() -> Map<String, Any?>,
) : JsonAdapter<ChatEventDto>() {
    override fun fromJson(reader: JsonReader): ChatEventDto? = delegate.fromJson(reader)?.let { event ->
        event.required().entries.firstOrNull { it.value == null }?.let { (name, _) ->
            throw JsonDataException("Required value '$name' missing for ${event.getWSEventType()}")
        }
        GeneratedEventDto(event)
    }

    override fun toJson(writer: JsonWriter, value: ChatEventDto?): Unit = error("Can't convert this event to Json")
}

internal class EventDtoAdapter(
    private val moshi: Moshi,
) : JsonAdapter<ChatEventDto>() {

    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))

    private val generatedAdapters = generatedEventAdapters(moshi)
    private val connectedEventAdapter = generatedEventAdapter<ConnectedEvent>(moshi) { emptyMap() }
    private val connectionErrorEventAdapter = moshi.adapter(ConnectionErrorEventDto::class.java)
    private val healthEventAdapter = generatedEventAdapter<HealthCheckEvent>(moshi) { emptyMap() }
    private val notificationMarkReadEventAdapter = generatedEventAdapter<NotificationMarkReadEvent>(moshi) {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "user" to user)
    }
    private val markAllReadEventAdapter =
        generatedEventAdapter<NotificationMarkReadEvent>(moshi) { mapOf("user" to user) }

    @Suppress("ReturnCount")
    override fun fromJson(reader: JsonReader): ChatEventDto? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Nothing?>()
            return null
        }

        val map: Map<String, Any?> = mapAdapter.fromJson(reader)!!.filterValues { it != null }

        val adapter = when (val type = map["type"] as? String) {
            EventType.HEALTH_CHECK -> when {
                map.containsKey("me") -> connectedEventAdapter
                else -> healthEventAdapter
            }
            EventType.CONNECTION_ERROR -> connectionErrorEventAdapter
            EventType.NOTIFICATION_MARK_READ -> when {
                map.containsKey("cid") -> notificationMarkReadEventAdapter
                else -> markAllReadEventAdapter
            }
            else -> generatedAdapters[type] ?: // Custom case, early return
                return UnknownEventDto(
                    type = type ?: EventType.UNKNOWN,
                    created_at = moshi.adapter(ExactDate::class.java).fromJsonValue(map["created_at"])!!,
                    user = moshi.adapter(DownstreamUserDto::class.java).fromJsonValue(map["user"]),
                    rawData = map,
                )
        }

        return adapter.fromJsonValue(map)
    }

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this event to Json $value")
    }
}
