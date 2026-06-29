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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

public interface WSEvent {
    fun getWSEventType(): kotlin.String
}

class WSEventAdapter : JsonAdapter<WSEvent>() {

    @FromJson
    override fun fromJson(reader: JsonReader): WSEvent? {
        val peek = reader.peekJson()
        var eventType: String? = null
        reader.beginObject()
        while (reader.hasNext()) {
            if (reader.nextName() == "type") {
                eventType = reader.nextString()
            } else {
                reader.skipValue()
            }
        }
        reader.endObject()

        return eventType?.let {
            peek.use { peekedReader ->
                io.getstream.chat.android.network.infrastructure.Serializer.moshi.adapter(getSubclass(eventType)).fromJson(peekedReader)
            }
        }
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: WSEvent?) {
        throw UnsupportedOperationException("toJson not implemented")
    }

    private fun getSubclass(type: String): Class<out WSEvent> {
        return when (type) {
            "*" -> io.getstream.chat.android.network.models.CustomEvent::class.java
            "ai_indicator.clear" -> io.getstream.chat.android.network.models.AIIndicatorClearEvent::class.java
            "ai_indicator.stop" -> io.getstream.chat.android.network.models.AIIndicatorStopEvent::class.java
            "ai_indicator.update" -> io.getstream.chat.android.network.models.AIIndicatorUpdateEvent::class.java
            "app.updated" -> io.getstream.chat.android.network.models.AppUpdatedEvent::class.java
            "channel.created" -> io.getstream.chat.android.network.models.ChannelCreatedEvent::class.java
            "channel.deleted" -> io.getstream.chat.android.network.models.ChannelDeletedEvent::class.java
            "channel.frozen" -> io.getstream.chat.android.network.models.ChannelFrozenEvent::class.java
            "channel.hidden" -> io.getstream.chat.android.network.models.ChannelHiddenEvent::class.java
            "channel.kicked" -> io.getstream.chat.android.network.models.ChannelKickedEvent::class.java
            "channel.max_streak_changed" -> io.getstream.chat.android.network.models.MaxStreakChangedEvent::class.java
            "channel.truncated" -> io.getstream.chat.android.network.models.ChannelTruncatedEvent::class.java
            "channel.unfrozen" -> io.getstream.chat.android.network.models.ChannelUnFrozenEvent::class.java
            "channel.updated" -> io.getstream.chat.android.network.models.ChannelUpdatedEvent::class.java
            "channel.visible" -> io.getstream.chat.android.network.models.ChannelVisibleEvent::class.java
            "draft.deleted" -> io.getstream.chat.android.network.models.DraftDeletedEvent::class.java
            "draft.updated" -> io.getstream.chat.android.network.models.DraftUpdatedEvent::class.java
            "health.check" -> io.getstream.chat.android.network.models.HealthCheckEvent::class.java
            "member.added" -> io.getstream.chat.android.network.models.MemberAddedEvent::class.java
            "member.removed" -> io.getstream.chat.android.network.models.MemberRemovedEvent::class.java
            "member.updated" -> io.getstream.chat.android.network.models.MemberUpdatedEvent::class.java
            "message.deleted" -> io.getstream.chat.android.network.models.MessageDeletedEvent::class.java
            "message.delivered" -> io.getstream.chat.android.network.models.MessageDeliveredEvent::class.java
            "message.new" -> io.getstream.chat.android.network.models.MessageNewEvent::class.java
            "message.pending" -> io.getstream.chat.android.network.models.PendingMessageEvent::class.java
            "message.read" -> io.getstream.chat.android.network.models.MessageReadEvent::class.java
            "message.undeleted" -> io.getstream.chat.android.network.models.MessageUndeletedEvent::class.java
            "message.updated" -> io.getstream.chat.android.network.models.MessageUpdatedEvent::class.java
            "notification.added_to_channel" -> io.getstream.chat.android.network.models.NotificationAddedToChannelEvent::class.java
            "notification.channel_deleted" -> io.getstream.chat.android.network.models.NotificationChannelDeletedEvent::class.java
            "notification.channel_mutes_updated" -> io.getstream.chat.android.network.models.NotificationChannelMutesUpdatedEvent::class.java
            "notification.channel_truncated" -> io.getstream.chat.android.network.models.NotificationChannelTruncatedEvent::class.java
            "notification.invite_accepted" -> io.getstream.chat.android.network.models.NotificationInviteAcceptedEvent::class.java
            "notification.invite_rejected" -> io.getstream.chat.android.network.models.NotificationInviteRejectedEvent::class.java
            "notification.invited" -> io.getstream.chat.android.network.models.NotificationInvitedEvent::class.java
            "notification.mark_read" -> io.getstream.chat.android.network.models.NotificationMarkReadEvent::class.java
            "notification.mark_unread" -> io.getstream.chat.android.network.models.NotificationMarkUnreadEvent::class.java
            "notification.message_new" -> io.getstream.chat.android.network.models.NotificationNewMessageEvent::class.java
            "notification.mutes_updated" -> io.getstream.chat.android.network.models.NotificationMutesUpdatedEvent::class.java
            "notification.reminder_due" -> io.getstream.chat.android.network.models.ReminderNotificationEvent::class.java
            "notification.removed_from_channel" -> io.getstream.chat.android.network.models.NotificationRemovedFromChannelEvent::class.java
            "notification.thread_message_new" -> io.getstream.chat.android.network.models.NotificationThreadMessageNewEvent::class.java
            "poll.closed" -> io.getstream.chat.android.network.models.PollClosedEvent::class.java
            "poll.deleted" -> io.getstream.chat.android.network.models.PollDeletedEvent::class.java
            "poll.updated" -> io.getstream.chat.android.network.models.PollUpdatedEvent::class.java
            "poll.vote_casted" -> io.getstream.chat.android.network.models.PollVoteCastedEvent::class.java
            "poll.vote_changed" -> io.getstream.chat.android.network.models.PollVoteChangedEvent::class.java
            "poll.vote_removed" -> io.getstream.chat.android.network.models.PollVoteRemovedEvent::class.java
            "reaction.deleted" -> io.getstream.chat.android.network.models.ReactionDeletedEvent::class.java
            "reaction.new" -> io.getstream.chat.android.network.models.ReactionNewEvent::class.java
            "reaction.updated" -> io.getstream.chat.android.network.models.ReactionUpdatedEvent::class.java
            "reminder.created" -> io.getstream.chat.android.network.models.ReminderCreatedEvent::class.java
            "reminder.deleted" -> io.getstream.chat.android.network.models.ReminderDeletedEvent::class.java
            "reminder.updated" -> io.getstream.chat.android.network.models.ReminderUpdatedEvent::class.java
            "thread.updated" -> io.getstream.chat.android.network.models.ThreadUpdatedEvent::class.java
            "typing.start" -> io.getstream.chat.android.network.models.TypingStartEvent::class.java
            "typing.stop" -> io.getstream.chat.android.network.models.TypingStopEvent::class.java
            "user.banned" -> io.getstream.chat.android.network.models.UserBannedEvent::class.java
            "user.deactivated" -> io.getstream.chat.android.network.models.UserDeactivatedEvent::class.java
            "user.deleted" -> io.getstream.chat.android.network.models.UserDeletedEvent::class.java
            "user.messages.deleted" -> io.getstream.chat.android.network.models.UserMessagesDeletedEvent::class.java
            "user.muted" -> io.getstream.chat.android.network.models.UserMutedEvent::class.java
            "user.presence.changed" -> io.getstream.chat.android.network.models.UserPresenceChangedEvent::class.java
            "user.reactivated" -> io.getstream.chat.android.network.models.UserReactivatedEvent::class.java
            "user.unbanned" -> io.getstream.chat.android.network.models.UserUnbannedEvent::class.java
            "user.updated" -> io.getstream.chat.android.network.models.UserUpdatedEvent::class.java
            "user.watching.start" -> io.getstream.chat.android.network.models.UserWatchingStartEvent::class.java
            "user.watching.stop" -> io.getstream.chat.android.network.models.UserWatchingStopEvent::class.java
            "user_group.created" -> io.getstream.chat.android.network.models.UserGroupCreatedEvent::class.java
            "user_group.deleted" -> io.getstream.chat.android.network.models.UserGroupDeletedEvent::class.java
            "user_group.member_added" -> io.getstream.chat.android.network.models.UserGroupMemberAddedEvent::class.java
            "user_group.member_removed" -> io.getstream.chat.android.network.models.UserGroupMemberRemovedEvent::class.java
            "user_group.updated" -> io.getstream.chat.android.network.models.UserGroupUpdatedEvent::class.java
            else -> UnsupportedWSEvent::class.java
        }
    }
}

class UnsupportedWSEvent(val type: String) : WSEvent {
    override fun getWSEventType(): kotlin.String {
        return type
    }
}

class UnsupportedWSEventException(val type: String) : Exception()
