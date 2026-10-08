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
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorClearEventDto
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorStopEventDto
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelHiddenEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelVisibleEventDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.GeneratedEventDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.NewMessageEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.network.models.ChannelDeletedEvent
import io.getstream.chat.android.network.models.ChannelTruncatedEvent
import io.getstream.chat.android.network.models.ChannelUpdatedEvent
import io.getstream.chat.android.network.models.DraftDeletedEvent
import io.getstream.chat.android.network.models.DraftUpdatedEvent
import io.getstream.chat.android.network.models.MemberAddedEvent
import io.getstream.chat.android.network.models.MemberRemovedEvent
import io.getstream.chat.android.network.models.MemberUpdatedEvent
import io.getstream.chat.android.network.models.MessageDeletedEvent
import io.getstream.chat.android.network.models.MessageDeliveredEvent
import io.getstream.chat.android.network.models.MessageReadEvent
import io.getstream.chat.android.network.models.MessageUpdatedEvent
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

internal class EventDtoAdapter(
    private val moshi: Moshi,
) : JsonAdapter<ChatEventDto>() {

    private val mapAdapter: JsonAdapter<MutableMap<String, Any?>> =
        moshi.adapter(Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java))

    private val connectedEventAdapter = moshi.adapter(ConnectedEventDto::class.java)
    private val connectionErrorEventAdapter = moshi.adapter(ConnectionErrorEventDto::class.java)
    private val healthEventAdapter = moshi.adapter(HealthEventDto::class.java)
    private val draftMessageUpdatedEventAdapter = generatedEventAdapter<DraftUpdatedEvent> { mapOf("draft" to draft) }
    private val draftMessageDeletedEventAdapter = generatedEventAdapter<DraftDeletedEvent> { mapOf("draft" to draft) }
    private val newMessageEventAdapter = moshi.adapter(NewMessageEventDto::class.java)
    private val messageDeletedEventAdapter = generatedEventAdapter<MessageDeletedEvent> { mapOf("cid" to cid) }
    private val messageUpdatedEventAdapter =
        generatedEventAdapter<MessageUpdatedEvent> { mapOf("cid" to cid, "user" to user) }
    private val messageReadEventAdapter = generatedEventAdapter<MessageReadEvent> {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "user" to user)
    }
    private val messageDeliveredEventAdapter = generatedEventAdapter<MessageDeliveredEvent> {
        mapOf(
            "cid" to cid,
            "channel_type" to channelType,
            "channel_id" to channelId,
            "user" to user,
            // The spec types it as a string; an unparseable value counts as missing.
            "last_delivered_at" to lastDeliveredAt?.let(GeneratedExactDate::parseOrNull),
            "last_delivered_message_id" to lastDeliveredMessageId,
        )
    }
    private val typingStartEventAdapter =
        generatedEventAdapter<TypingStartEvent> { mapOf("cid" to cid, "user" to user) }
    private val typingStopEventAdapter =
        generatedEventAdapter<TypingStopEvent> { mapOf("cid" to cid, "user" to user) }
    private val reactionNewEventAdapter = generatedEventAdapter<ReactionNewEvent> {
        mapOf("cid" to cid, "user" to user, "message" to message, "reaction" to reaction)
    }
    private val reactionUpdatedEventAdapter = generatedEventAdapter<ReactionUpdatedEvent> {
        mapOf("cid" to cid, "user" to user, "reaction" to reaction)
    }
    private val reactionDeletedEventAdapter = generatedEventAdapter<ReactionDeletedEvent> {
        mapOf("cid" to cid, "user" to user, "message" to message, "reaction" to reaction)
    }
    private val memberAddedEventAdapter =
        generatedEventAdapter<MemberAddedEvent> { mapOf("cid" to cid, "user" to user) }
    private val memberRemovedEventAdapter =
        generatedEventAdapter<MemberRemovedEvent> { mapOf("cid" to cid, "user" to user) }
    private val memberUpdatedEventAdapter =
        generatedEventAdapter<MemberUpdatedEvent> { mapOf("cid" to cid, "user" to user) }
    private val channelUpdatedEventAdapter = generatedEventAdapter<ChannelUpdatedEvent> { mapOf("cid" to cid) }
    private val channelHiddenEventAdapter = moshi.adapter(ChannelHiddenEventDto::class.java)
    private val channelDeletedEventAdapter = generatedEventAdapter<ChannelDeletedEvent> { mapOf("cid" to cid) }
    private val channelVisibleEventAdapter = moshi.adapter(ChannelVisibleEventDto::class.java)
    private val channelTruncatedEventAdapter = generatedEventAdapter<ChannelTruncatedEvent> { mapOf("cid" to cid) }
    private val userStartWatchingEventAdapter = generatedEventAdapter<UserWatchingStartEvent> { mapOf("cid" to cid) }
    private val userStopWatchingEventAdapter = generatedEventAdapter<UserWatchingStopEvent> { mapOf("cid" to cid) }
    private val notificationAddedToChannelEventAdapter = moshi.adapter(NotificationAddedToChannelEventDto::class.java)
    private val notificationMarkReadEventAdapter = generatedEventAdapter<NotificationMarkReadEvent> {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "user" to user)
    }
    private val notificationMarkUnreadEventAdapter = generatedEventAdapter<NotificationMarkUnreadEvent> {
        mapOf(
            "cid" to cid,
            "user" to user,
            "first_unread_message_id" to firstUnreadMessageId,
            "last_read_at" to lastReadAt,
            "unread_messages" to unreadMessages,
        )
    }
    private val markAllReadEventAdapter = generatedEventAdapter<NotificationMarkReadEvent> { mapOf("user" to user) }
    private val notificationMessageNewEventAdapter = generatedEventAdapter<NotificationNewMessageEvent> {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId)
    }
    private val notificationThreadMessageNewEventAdapter = generatedEventAdapter<NotificationThreadMessageNewEvent> {
        mapOf("cid" to cid, "unread_threads" to unreadThreads, "unread_thread_messages" to unreadThreadMessages)
    }
    private val threadUpdatedEventAdapter = generatedEventAdapter<ThreadUpdatedEvent> {
        mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "thread" to thread)
    }
    private val notificationInvitedEventAdapter = generatedEventAdapter<NotificationInvitedEvent> {
        mapOf("cid" to cid, "user" to user)
    }
    private val notificationInviteAcceptedEventAdapter = generatedEventAdapter<NotificationInviteAcceptedEvent> {
        mapOf("cid" to cid, "user" to user)
    }
    private val notificationInviteRejectedEventAdapter = generatedEventAdapter<NotificationInviteRejectedEvent> {
        mapOf("cid" to cid, "user" to user)
    }
    private val notificationRemovedFromChannelEventAdapter =
        generatedEventAdapter<NotificationRemovedFromChannelEvent> { mapOf("cid" to cid) }
    private val notificationMutesUpdatedEventAdapter =
        generatedEventAdapter<NotificationMutesUpdatedEvent> { emptyMap() }
    private val notificationChannelMutesUpdatedEventAdapter =
        generatedEventAdapter<NotificationChannelMutesUpdatedEvent> { emptyMap() }
    private val notificationChannelDeletedEventAdapter =
        generatedEventAdapter<NotificationChannelDeletedEvent> { mapOf("cid" to cid) }
    private val notificationChannelTruncatedEventAdapter =
        generatedEventAdapter<NotificationChannelTruncatedEvent> { mapOf("cid" to cid) }
    private val userPresenceChangedEventAdapter = generatedEventAdapter<UserPresenceChangedEvent> { emptyMap() }
    private val userUpdatedEventAdapter = generatedEventAdapter<UserUpdatedEvent> { emptyMap() }
    private val userDeletedEventAdapter = generatedEventAdapter<UserDeletedEvent> { emptyMap() }
    private val userBannedEventAdapter = generatedEventAdapter<UserBannedEvent> { emptyMap() }
    private val userUnbannedEventAdapter = generatedEventAdapter<UserUnbannedEvent> { emptyMap() }
    private val pollUpdatedEventAdapter = generatedEventAdapter<PollUpdatedEvent> { mapOf("cid" to cid) }
    private val pollDeletedEventAdapter = generatedEventAdapter<PollDeletedEvent> { mapOf("cid" to cid) }
    private val pollClosedEventAdapter = generatedEventAdapter<PollClosedEvent> { mapOf("cid" to cid) }
    private val pollVoteCastedEventAdapter = generatedEventAdapter<PollVoteCastedEvent> { mapOf("cid" to cid) }
    private val pollVoteChangedEventAdapter = generatedEventAdapter<PollVoteChangedEvent> { mapOf("cid" to cid) }
    private val pollVoteRemovedEventAdapter = generatedEventAdapter<PollVoteRemovedEvent> { mapOf("cid" to cid) }
    private val reminderCreatedEventAdapter = generatedEventAdapter<ReminderCreatedEvent> { emptyMap() }
    private val reminderUpdatedEventAdapter = generatedEventAdapter<ReminderUpdatedEvent> { emptyMap() }
    private val reminderDeletedEventAdapter = generatedEventAdapter<ReminderDeletedEvent> { emptyMap() }
    private val notificationReminderDueEventAdapter = generatedEventAdapter<ReminderNotificationEvent> { emptyMap() }
    private val userMessagesDeletedEventAdapter = generatedEventAdapter<UserMessagesDeletedEvent> { emptyMap() }
    private val aiTypingIndicatorUpdatedEventAdapter = moshi.adapter(AIIndicatorUpdatedEventDto::class.java)
    private val aiTypingIndicatorClearEventAdapter = moshi.adapter(AIIndicatorClearEventDto::class.java)
    private val aiTypingIndicatorStopEventAdapter = moshi.adapter(AIIndicatorStopEventDto::class.java)

    @Suppress("LongMethod", "ComplexMethod", "ReturnCount")
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
            EventType.DRAFT_MESSAGE_UPDATED -> draftMessageUpdatedEventAdapter
            EventType.DRAFT_MESSAGE_DELETED -> draftMessageDeletedEventAdapter
            EventType.MESSAGE_NEW -> newMessageEventAdapter
            EventType.MESSAGE_DELETED -> messageDeletedEventAdapter
            EventType.MESSAGE_UPDATED -> messageUpdatedEventAdapter
            EventType.MESSAGE_READ -> messageReadEventAdapter
            EventType.MESSAGE_DELIVERED -> messageDeliveredEventAdapter
            EventType.TYPING_START -> typingStartEventAdapter
            EventType.TYPING_STOP -> typingStopEventAdapter
            EventType.REACTION_NEW -> reactionNewEventAdapter
            EventType.REACTION_UPDATED -> reactionUpdatedEventAdapter
            EventType.REACTION_DELETED -> reactionDeletedEventAdapter
            EventType.MEMBER_ADDED -> memberAddedEventAdapter
            EventType.MEMBER_REMOVED -> memberRemovedEventAdapter
            EventType.MEMBER_UPDATED -> memberUpdatedEventAdapter
            EventType.CHANNEL_UPDATED -> channelUpdatedEventAdapter
            EventType.CHANNEL_HIDDEN -> channelHiddenEventAdapter
            EventType.CHANNEL_DELETED -> channelDeletedEventAdapter
            EventType.CHANNEL_VISIBLE -> channelVisibleEventAdapter
            EventType.CHANNEL_TRUNCATED -> channelTruncatedEventAdapter
            EventType.USER_WATCHING_START -> userStartWatchingEventAdapter
            EventType.USER_WATCHING_STOP -> userStopWatchingEventAdapter
            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> notificationAddedToChannelEventAdapter
            EventType.NOTIFICATION_MARK_READ -> when {
                map.containsKey("cid") -> notificationMarkReadEventAdapter
                else -> markAllReadEventAdapter
            }
            EventType.NOTIFICATION_MARK_UNREAD -> notificationMarkUnreadEventAdapter
            EventType.NOTIFICATION_MESSAGE_NEW -> notificationMessageNewEventAdapter
            EventType.NOTIFICATION_THREAD_MESSAGE_NEW -> notificationThreadMessageNewEventAdapter
            EventType.THREAD_UPDATED -> threadUpdatedEventAdapter
            EventType.NOTIFICATION_INVITED -> notificationInvitedEventAdapter
            EventType.NOTIFICATION_INVITE_ACCEPTED -> notificationInviteAcceptedEventAdapter
            EventType.NOTIFICATION_INVITE_REJECTED -> notificationInviteRejectedEventAdapter
            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> notificationRemovedFromChannelEventAdapter
            EventType.NOTIFICATION_MUTES_UPDATED -> notificationMutesUpdatedEventAdapter
            EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED -> notificationChannelMutesUpdatedEventAdapter
            EventType.NOTIFICATION_CHANNEL_DELETED -> notificationChannelDeletedEventAdapter
            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> notificationChannelTruncatedEventAdapter
            EventType.USER_PRESENCE_CHANGED -> userPresenceChangedEventAdapter
            EventType.USER_UPDATED -> userUpdatedEventAdapter
            EventType.USER_DELETED -> userDeletedEventAdapter
            EventType.USER_BANNED -> userBannedEventAdapter
            EventType.USER_UNBANNED -> userUnbannedEventAdapter
            EventType.USER_MESSAGES_DELETED -> userMessagesDeletedEventAdapter
            EventType.POLL_UPDATED -> pollUpdatedEventAdapter
            EventType.POLL_DELETED -> pollDeletedEventAdapter
            EventType.POLL_CLOSED -> pollClosedEventAdapter
            EventType.POLL_VOTE_CASTED -> pollVoteCastedEventAdapter
            EventType.POLL_VOTE_CHANGED -> pollVoteChangedEventAdapter
            EventType.POLL_VOTE_REMOVED -> pollVoteRemovedEventAdapter
            EventType.REMINDER_CREATED -> reminderCreatedEventAdapter
            EventType.REMINDER_UPDATED -> reminderUpdatedEventAdapter
            EventType.REMINDER_DELETED -> reminderDeletedEventAdapter
            EventType.NOTIFICATION_REMINDER_DUE -> notificationReminderDueEventAdapter
            EventType.AI_TYPING_INDICATOR_UPDATED -> aiTypingIndicatorUpdatedEventAdapter
            EventType.AI_TYPING_INDICATOR_CLEAR -> aiTypingIndicatorClearEventAdapter
            EventType.AI_TYPING_INDICATOR_STOP -> aiTypingIndicatorStopEventAdapter
            else -> // Custom case, early return
                return UnknownEventDto(
                    type = type ?: EventType.UNKNOWN,
                    created_at = moshi.adapter(ExactDate::class.java).fromJsonValue(map["created_at"])!!,
                    user = moshi.adapter(DownstreamUserDto::class.java).fromJsonValue(map["user"]),
                    rawData = map,
                )
        }

        return adapter.fromJsonValue(map)
    }

    private inline fun <reified T : WSEvent> generatedEventAdapter(
        noinline required: T.() -> Map<String, Any?>,
    ): JsonAdapter<ChatEventDto> = GeneratedEventAdapter(moshi.adapter(T::class.java), required)

    /**
     * Parses an event with its generated model. The spec makes some fields optional that the domain event
     * requires (e.g. the cid, since feeds shares these events without one); an event missing any of the
     * [required] fields is rejected.
     */
    private class GeneratedEventAdapter<T : WSEvent>(
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

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this event to Json $value")
    }
}
