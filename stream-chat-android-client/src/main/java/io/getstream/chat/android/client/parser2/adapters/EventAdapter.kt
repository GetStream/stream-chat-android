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
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.rawType
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorClearEventDto
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorStopEventDto
import io.getstream.chat.android.client.api2.model.dto.AIIndicatorUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.AnswerCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelHiddenEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedByUserEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelVisibleEventDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.DraftMessageDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.DraftMessageUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.HealthEventDto
import io.getstream.chat.android.client.api2.model.dto.MarkAllReadEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberAddedEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberRemovedEventDto
import io.getstream.chat.android.client.api2.model.dto.MemberUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageDeliveredEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageReadEventDto
import io.getstream.chat.android.client.api2.model.dto.MessageUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NewMessageEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteAcceptedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteRejectedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInvitedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMarkReadEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMarkUnreadEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMutesUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationReminderDueEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationThreadMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.PollClosedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionNewEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionUpdateEventDto
import io.getstream.chat.android.client.api2.model.dto.ReminderCreatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReminderDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReminderUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStartEventDto
import io.getstream.chat.android.client.api2.model.dto.TypingStopEventDto
import io.getstream.chat.android.client.api2.model.dto.UnknownEventDto
import io.getstream.chat.android.client.api2.model.dto.UserDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserMessagesDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserPresenceChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStartWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserStopWatchingEventDto
import io.getstream.chat.android.client.api2.model.dto.UserUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteRemovedEventDto
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.models.EventType
import java.lang.reflect.Type

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
    private val draftMessageUpdatedEventAdapter = moshi.adapter(DraftMessageUpdatedEventDto::class.java)
    private val draftMessageDeletedEventAdapter = moshi.adapter(DraftMessageDeletedEventDto::class.java)
    private val newMessageEventAdapter = moshi.adapter(NewMessageEventDto::class.java)
    private val messageDeletedEventAdapter = moshi.adapter(MessageDeletedEventDto::class.java)
    private val messageUpdatedEventAdapter = moshi.adapter(MessageUpdatedEventDto::class.java)
    private val messageReadEventAdapter = moshi.adapter(MessageReadEventDto::class.java)
    private val messageDeliveredEventAdapter = moshi.adapter(MessageDeliveredEventDto::class.java)
    private val typingStartEventAdapter = moshi.adapter(TypingStartEventDto::class.java)
    private val typingStopEventAdapter = moshi.adapter(TypingStopEventDto::class.java)
    private val reactionNewEventAdapter = moshi.adapter(ReactionNewEventDto::class.java)
    private val reactionUpdateEventAdapter = moshi.adapter(ReactionUpdateEventDto::class.java)
    private val reactionDeletedEventAdapter = moshi.adapter(ReactionDeletedEventDto::class.java)
    private val memberAddedEventAdapter = moshi.adapter(MemberAddedEventDto::class.java)
    private val memberRemovedEventAdapter = moshi.adapter(MemberRemovedEventDto::class.java)
    private val memberUpdatedEventAdapter = moshi.adapter(MemberUpdatedEventDto::class.java)
    private val channelUpdatedByUserEventAdapter = moshi.adapter(ChannelUpdatedByUserEventDto::class.java)
    private val channelUpdatedEventAdapter = moshi.adapter(ChannelUpdatedEventDto::class.java)
    private val channelHiddenEventAdapter = moshi.adapter(ChannelHiddenEventDto::class.java)
    private val channelDeletedEventAdapter = moshi.adapter(ChannelDeletedEventDto::class.java)
    private val channelVisibleEventAdapter = moshi.adapter(ChannelVisibleEventDto::class.java)
    private val channelTruncatedEventAdapter = moshi.adapter(ChannelTruncatedEventDto::class.java)
    private val userStartWatchingEventAdapter = moshi.adapter(UserStartWatchingEventDto::class.java)
    private val userStopWatchingEventAdapter = moshi.adapter(UserStopWatchingEventDto::class.java)
    private val notificationAddedToChannelEventAdapter = moshi.adapter(NotificationAddedToChannelEventDto::class.java)
    private val notificationMarkReadEventAdapter = moshi.adapter(NotificationMarkReadEventDto::class.java)
    private val notificationMarkUnreadEventAdapter = moshi.adapter(NotificationMarkUnreadEventDto::class.java)
    private val markAllReadEventAdapter = moshi.adapter(MarkAllReadEventDto::class.java)
    private val notificationMessageNewEventAdapter = moshi.adapter(NotificationMessageNewEventDto::class.java)
    private val notificationThreadMessageNewEventAdapter =
        moshi.adapter(NotificationThreadMessageNewEventDto::class.java)
    private val notificationInvitedEventAdapter = moshi.adapter(NotificationInvitedEventDto::class.java)
    private val notificationInviteAcceptedEventAdapter = moshi.adapter(NotificationInviteAcceptedEventDto::class.java)
    private val notificationInviteRejectedEventAdapter = moshi.adapter(NotificationInviteRejectedEventDto::class.java)
    private val notificationRemovedFromChannelEventAdapter =
        moshi.adapter(NotificationRemovedFromChannelEventDto::class.java)
    private val notificationMutesUpdatedEventAdapter = moshi.adapter(NotificationMutesUpdatedEventDto::class.java)
    private val notificationChannelMutesUpdatedEventAdapter =
        moshi.adapter(NotificationChannelMutesUpdatedEventDto::class.java)
    private val notificationChannelDeletedEventAdapter = moshi.adapter(NotificationChannelDeletedEventDto::class.java)
    private val notificationChannelTruncatedEventAdapter =
        moshi.adapter(NotificationChannelTruncatedEventDto::class.java)
    private val userPresenceChangedEventAdapter = moshi.adapter(UserPresenceChangedEventDto::class.java)
    private val userUpdatedEventAdapter = moshi.adapter(UserUpdatedEventDto::class.java)
    private val userDeletedEventAdapter = moshi.adapter(UserDeletedEventDto::class.java)
    private val channelUserBannedEventAdapter = moshi.adapter(ChannelUserBannedEventDto::class.java)
    private val globalUserBannedEventAdapter = moshi.adapter(GlobalUserBannedEventDto::class.java)
    private val channelUserUnbannedEventAdapter = moshi.adapter(ChannelUserUnbannedEventDto::class.java)
    private val globalUserUnbannedEventAdapter = moshi.adapter(GlobalUserUnbannedEventDto::class.java)
    private val pollUpdatedEventAdapter = moshi.adapter(PollUpdatedEventDto::class.java)
    private val pollDeletedEventAdapter = moshi.adapter(PollDeletedEventDto::class.java)
    private val pollClosedEventAdapter = moshi.adapter(PollClosedEventDto::class.java)
    private val voteCastedEventAdapter = moshi.adapter(VoteCastedEventDto::class.java)
    private val voteChangedEventAdapter = moshi.adapter(VoteChangedEventDto::class.java)
    private val answerCastedEventAdapter = moshi.adapter(AnswerCastedEventDto::class.java)
    private val voteRemovedEventAdapter = moshi.adapter(VoteRemovedEventDto::class.java)
    private val reminderCreatedEventAdapter = moshi.adapter(ReminderCreatedEventDto::class.java)
    private val reminderUpdatedEventAdapter = moshi.adapter(ReminderUpdatedEventDto::class.java)
    private val reminderDeletedEventAdapter = moshi.adapter(ReminderDeletedEventDto::class.java)
    private val notificationReminderDueEventAdapter = moshi.adapter(NotificationReminderDueEventDto::class.java)
    private val userMessagesDeletedEventAdapter = moshi.adapter(UserMessagesDeletedEventDto::class.java)
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
            EventType.MESSAGE_READ -> when {
                map.containsKey("cid") -> messageReadEventAdapter
                else -> markAllReadEventAdapter
            }
            EventType.MESSAGE_DELIVERED -> messageDeliveredEventAdapter
            EventType.TYPING_START -> typingStartEventAdapter
            EventType.TYPING_STOP -> typingStopEventAdapter
            EventType.REACTION_NEW -> reactionNewEventAdapter
            EventType.REACTION_UPDATED -> reactionUpdateEventAdapter
            EventType.REACTION_DELETED -> reactionDeletedEventAdapter
            EventType.MEMBER_ADDED -> memberAddedEventAdapter
            EventType.MEMBER_REMOVED -> memberRemovedEventAdapter
            EventType.MEMBER_UPDATED -> memberUpdatedEventAdapter
            EventType.CHANNEL_UPDATED -> when {
                map.containsKey("user") -> channelUpdatedByUserEventAdapter
                else -> channelUpdatedEventAdapter
            }
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
            EventType.USER_BANNED -> when {
                map.containsKey("cid") -> channelUserBannedEventAdapter
                else -> globalUserBannedEventAdapter
            }
            EventType.USER_UNBANNED -> when {
                map.containsKey("cid") -> channelUserUnbannedEventAdapter
                else -> globalUserUnbannedEventAdapter
            }
            EventType.USER_MESSAGES_DELETED -> userMessagesDeletedEventAdapter
            EventType.POLL_UPDATED -> pollUpdatedEventAdapter
            EventType.POLL_DELETED -> pollDeletedEventAdapter
            EventType.POLL_CLOSED -> pollClosedEventAdapter
            EventType.POLL_VOTE_CASTED -> when (map.containsAnswer()) {
                true -> answerCastedEventAdapter
                else -> voteCastedEventAdapter
            }
            EventType.POLL_VOTE_CHANGED -> when (map.containsAnswer()) {
                true -> answerCastedEventAdapter
                else -> voteChangedEventAdapter
            }
            EventType.POLL_VOTE_REMOVED -> voteRemovedEventAdapter
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

    private fun Map<String, Any?>.containsAnswer(): Boolean =
        (((this["poll_vote"] as? Map<String, Any?>)?.get("is_answer") as? Boolean) ?: false)

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this event to Json $value")
    }
}
