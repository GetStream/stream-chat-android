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
import io.getstream.chat.android.client.api2.model.dto.AnswerCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedByUserEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChannelUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectedEventDto
import io.getstream.chat.android.client.api2.model.dto.ConnectionErrorEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserBannedEventDto
import io.getstream.chat.android.client.api2.model.dto.GlobalUserUnbannedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationAddedToChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationChannelTruncatedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteAcceptedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInviteRejectedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationInvitedEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMarkUnreadEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationReminderDueEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationRemovedFromChannelEventDto
import io.getstream.chat.android.client.api2.model.dto.NotificationThreadMessageNewEventDto
import io.getstream.chat.android.client.api2.model.dto.PollClosedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollDeletedEventDto
import io.getstream.chat.android.client.api2.model.dto.PollUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.ReactionUpdateEventDto
import io.getstream.chat.android.client.api2.model.dto.ThreadUpdatedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteCastedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteChangedEventDto
import io.getstream.chat.android.client.api2.model.dto.VoteRemovedEventDto
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
    private val reactionUpdateEventAdapter = moshi.adapter(ReactionUpdateEventDto::class.java)
    private val channelUpdatedByUserEventAdapter = moshi.adapter(ChannelUpdatedByUserEventDto::class.java)
    private val channelUpdatedEventAdapter = moshi.adapter(ChannelUpdatedEventDto::class.java)
    private val notificationAddedToChannelEventAdapter = moshi.adapter(NotificationAddedToChannelEventDto::class.java)
    private val notificationMarkUnreadEventAdapter = moshi.adapter(NotificationMarkUnreadEventDto::class.java)
    private val notificationMessageNewEventAdapter = moshi.adapter(NotificationMessageNewEventDto::class.java)
    private val notificationThreadMessageNewEventAdapter =
        moshi.adapter(NotificationThreadMessageNewEventDto::class.java)
    private val threadUpdatedEventAdapter = moshi.adapter(ThreadUpdatedEventDto::class.java)
    private val notificationInvitedEventAdapter = moshi.adapter(NotificationInvitedEventDto::class.java)
    private val notificationInviteAcceptedEventAdapter = moshi.adapter(NotificationInviteAcceptedEventDto::class.java)
    private val notificationInviteRejectedEventAdapter = moshi.adapter(NotificationInviteRejectedEventDto::class.java)
    private val notificationRemovedFromChannelEventAdapter =
        moshi.adapter(NotificationRemovedFromChannelEventDto::class.java)
    private val notificationChannelDeletedEventAdapter = moshi.adapter(NotificationChannelDeletedEventDto::class.java)
    private val notificationChannelTruncatedEventAdapter =
        moshi.adapter(NotificationChannelTruncatedEventDto::class.java)
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
    private val notificationReminderDueEventAdapter = moshi.adapter(NotificationReminderDueEventDto::class.java)

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
                else -> return null
            }
            EventType.CONNECTION_ERROR -> connectionErrorEventAdapter
            EventType.REACTION_UPDATED -> reactionUpdateEventAdapter
            EventType.CHANNEL_UPDATED -> when {
                map.containsKey("user") -> channelUpdatedByUserEventAdapter
                else -> channelUpdatedEventAdapter
            }
            EventType.NOTIFICATION_ADDED_TO_CHANNEL -> notificationAddedToChannelEventAdapter
            EventType.NOTIFICATION_MARK_UNREAD -> notificationMarkUnreadEventAdapter
            EventType.NOTIFICATION_MESSAGE_NEW -> notificationMessageNewEventAdapter
            EventType.NOTIFICATION_THREAD_MESSAGE_NEW -> notificationThreadMessageNewEventAdapter
            EventType.THREAD_UPDATED -> threadUpdatedEventAdapter
            EventType.NOTIFICATION_INVITED -> notificationInvitedEventAdapter
            EventType.NOTIFICATION_INVITE_ACCEPTED -> notificationInviteAcceptedEventAdapter
            EventType.NOTIFICATION_INVITE_REJECTED -> notificationInviteRejectedEventAdapter
            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL -> notificationRemovedFromChannelEventAdapter
            EventType.NOTIFICATION_CHANNEL_DELETED -> notificationChannelDeletedEventAdapter
            EventType.NOTIFICATION_CHANNEL_TRUNCATED -> notificationChannelTruncatedEventAdapter
            EventType.USER_BANNED -> when {
                map.containsKey("cid") -> channelUserBannedEventAdapter
                else -> globalUserBannedEventAdapter
            }
            EventType.USER_UNBANNED -> when {
                map.containsKey("cid") -> channelUserUnbannedEventAdapter
                else -> globalUserUnbannedEventAdapter
            }
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
            EventType.NOTIFICATION_REMINDER_DUE -> notificationReminderDueEventAdapter
            else -> return null
        }

        return adapter.fromJsonValue(map)
    }

    private fun Map<String, Any?>.containsAnswer(): Boolean =
        (((this["poll_vote"] as? Map<String, Any?>)?.get("is_answer") as? Boolean) ?: false)

    override fun toJson(writer: JsonWriter, value: ChatEventDto?) {
        error("Can't convert this event to Json $value")
    }
}
