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

package io.getstream.chat.android.client

import org.intellij.lang.annotations.Language

internal fun createChannelDeletedEventStringJson() =
    createChatEventStringJson(
        "channel.deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelHiddenEventStringJson() =
    createChatEventStringJson(
        "channel.hidden",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "clear_history": true,
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelTruncatedEventStringJson() =
    createChatEventStringJson(
        "channel.truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelTruncatedServerSideEventStringJson() =
    createChatEventStringJson(
        "channel.truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelUpdatedEventStringJson() =
    createChatEventStringJson(
        "channel.updated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelUpdatedByUserEventStringJson() =
    createChatEventStringJson(
        "channel.updated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "message": ${createMessageJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelVisibleEventStringJson() =
    createChatEventStringJson(
        "channel.visible",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createMemberAddedEventStringJson() =
    createChatEventStringJson(
        "member.added",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createMemberRemovedEventStringJson() =
    createChatEventStringJson(
        "member.removed",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createMemberUpdatedEventStringJson() =
    createChatEventStringJson(
        "member.updated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createMessageDeletedEventStringJson() =
    createChatEventStringJson(
        "message.deleted",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "message": ${createMessageJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z",
            "channel_message_count": 1
        """.trimIndent(),
    )

internal fun createMessageDeletedServerSideEventStringJson() =
    createChatEventStringJson(
        "message.deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "hard_delete": true,
            "channel_last_message_at": "2020-06-29T06:14:28.000Z",
            "channel_message_count": 1,
            "deleted_for_me": true
        """.trimIndent(),
    )

internal fun createMessageReadEventStringJson() =
    createChatEventStringJson(
        "message.read",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "channel_last_message_at": "2020-06-29T06:14:28.000Z",
            "last_read_message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755"
        """.trimIndent(),
    )

internal fun createMessageUpdatedEventStringJson() =
    createChatEventStringJson(
        "message.updated",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "message": ${createMessageJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationAddedToChannelEventStringJson() =
    createChatEventStringJson(
        "notification.added_to_channel",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "channel": ${createChannelJsonString()},
            "member": ${createMemberJsonString()},
            "total_unread_count": 4,
            "unread_channels": 5,
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationChannelDeletedEventStringJson() =
    createChatEventStringJson(
        "notification.channel_deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationChannelTruncatedEventStringJson() =
    createChatEventStringJson(
        "notification.channel_truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationInviteAcceptedEventStringJson() =
    createChatEventStringJson(
        "notification.invite_accepted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationInviteRejectedEventStringJson() =
    createChatEventStringJson(
        "notification.invite_rejected",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel": ${createChannelJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationInvitedEventStringJson() =
    createChatEventStringJson(
        "notification.invited",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationMarkReadEventStringJson() =
    createChatEventStringJson(
        "notification.mark_read",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "watcher_count": 3,
            "total_unread_count": 4,
            "unread_channels": 5,
            "channel_last_message_at": "2020-06-29T06:14:28.000Z",
            "last_read_message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755"
        """.trimIndent(),
    )

internal fun createNotificationMarkUnreadEventStringJson() =
    createChatEventStringJson(
        "notification.mark_unread",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "watcher_count": 3,
            "total_unread_count": 4,
            "unread_channels": 5,
            "unread_messages": 1,
            "first_unread_message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "last_read_at": "2020-06-29T06:14:28.000Z",
            "last_read_message_id": "parentMessageId",
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationMessageNewEventStringJson() =
    createChatEventStringJson(
        "notification.message_new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "channel": ${createChannelJsonString()},
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "total_unread_count": 4,
            "unread_channels": 5,
            "message": ${createMessageJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createNotificationRemovedFromChannelEventStringJson() =
    createChatEventStringJson(
        "notification.removed_from_channel",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "channel": ${createChannelJsonString()},
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createReactionDeletedEventStringJson() =
    createChatEventStringJson(
        "reaction.deleted",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "reaction": ${createReactionJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createReactionNewEventStringJson() =
    createChatEventStringJson(
        "reaction.new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "reaction": ${createReactionJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createReactionUpdateEventStringJson() =
    createChatEventStringJson(
        "reaction.updated",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "reaction": ${createReactionJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createTypingStartEventStringJson() =
    createChatEventStringJson(
        "typing.start",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "parent_id": "parentMessageId",
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createTypingStopEventStringJson() =
    createChatEventStringJson(
        "typing.stop",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "parent_id": "parentMessageId",
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelUserBannedEventStringJson() =
    createChatEventStringJson(
        "user.banned",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "expiration": "2020-06-29T06:14:28.000Z",
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createGlobalUserBannedEventStringJson() =
    createChatEventStringJson(
        "user.banned",
        """
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createUserDeletedEventStringJson() =
    createChatEventStringJson(
        "user.deleted",
        """
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createUserPresenceChangedEventStringJson() =
    createChatEventStringJson(
        "user.presence.changed",
        """
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createUserStartWatchingEventStringJson() =
    createChatEventStringJson(
        "user.watching.start",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createUserStopWatchingEventStringJson() =
    createChatEventStringJson(
        "user.watching.stop",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createChannelUserUnbannedEventStringJson() =
    createChatEventStringJson(
        "user.unbanned",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "channel_last_message_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent(),
    )

internal fun createGlobalUserUnbannedEventStringJson() =
    createChatEventStringJson(
        "user.unbanned",
        """
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createUserUpdatedEventStringJson() =
    createChatEventStringJson(
        "user.updated",
        """
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createHealthEventStringJson() =
    createChatEventStringJson(
        "health.check",
        """
            "connection_id":"6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
        """.trimIndent(),
    )

internal fun createConnectedEventStringJson(userJsonString: String? = createOwnUserJsonString()) =
    createChatEventStringJson(
        "health.check",
        """
            "connection_id":"6cfffec7-40df-40ac-901a-6ea6c5b7fb83",
            "me": $userJsonString
        """.trimIndent(),
    )

internal fun createNotificationChannelMutesUpdatedEventStringJson() =
    createChatEventStringJson(
        "notification.channel_mutes_updated",
        """
            "me": ${createOwnUserJsonString()}
        """.trimIndent(),
    )

internal fun createNotificationMutesUpdatedEventStringJson() =
    createChatEventStringJson(
        "notification.mutes_updated",
        """
            "me": ${createOwnUserJsonString()}
        """.trimIndent(),
    )

internal fun createNewMessageEventStringJson() =
    createChatEventStringJson(
        "message.new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "total_unread_count": 4,
            "unread_channels": 5,
            "message": ${createMessageJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z",
            "channel_message_count": 1
        """.trimIndent(),
    )

internal fun createNewMessageWithoutUnreadCountsEventStringJson() =
    createChatEventStringJson(
        "message.new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "message": ${createMessageJsonString()},
            "channel_last_message_at": "2020-06-29T06:14:28.000Z",
            "channel_message_count": 1
        """.trimIndent(),
    )

internal fun createConnectionErrorEventStringJson() =
    createChatEventStringJson(
        "connection.error",
        """
            "connection_id": "6cfffec7-40df-40ac-901a-6ea6c5b7fb83",
            "error": ${createErrorJsonString()}
        """.trimIndent(),
    )

internal fun createDraftMessageUpdatedEventStringJson() =
    createChatEventStringJson(
        "draft.updated",
        """
            "draft": ${createDraftJsonString()}
        """.trimIndent(),
    )

internal fun createDraftMessageDeletedEventStringJson() =
    createChatEventStringJson(
        "draft.deleted",
        """
            "draft": ${createDraftJsonString()}
        """.trimIndent(),
    )

internal fun createMessageDeliveredEventStringJson() =
    createChatEventStringJson(
        "message.delivered",
        """
            "user": ${createUserJsonString()},
            "cid": "channelType:channelId",
            "channel_type": "channelType",
            "channel_id": "channelId",
            "last_delivered_at": "2020-06-29T06:14:28.000Z",
            "last_delivered_message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755"
        """.trimIndent(),
    )

internal fun createNotificationThreadMessageNewEventStringJson() =
    createChatEventStringJson(
        "notification.thread_message_new",
        """
            "cid": "channelType:channelId",
            "channel_type": "channelType",
            "channel_id": "channelId",
            "message": ${createMessageJsonString()},
            "channel": ${createChannelJsonString()},
            "unread_threads": 1,
            "unread_thread_messages": 2
        """.trimIndent(),
    )

internal fun createPollUpdatedEventStringJson() =
    createChatEventStringJson(
        "poll.updated",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()}
        """.trimIndent(),
    )

internal fun createPollDeletedEventStringJson() =
    createChatEventStringJson(
        "poll.deleted",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()}
        """.trimIndent(),
    )

internal fun createPollClosedEventStringJson() =
    createChatEventStringJson(
        "poll.closed",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()}
        """.trimIndent(),
    )

internal fun createVoteCastedEventStringJson() =
    createChatEventStringJson(
        "poll.vote_casted",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()},
            "poll_vote": ${createPollVoteJsonString()}
        """.trimIndent(),
    )

internal fun createAnswerCastedEventStringJson() =
    createChatEventStringJson(
        "poll.vote_casted",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()},
            "poll_vote": ${createPollAnswerVoteJsonString()}
        """.trimIndent(),
    )

internal fun createVoteChangedEventStringJson() =
    createChatEventStringJson(
        "poll.vote_changed",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()},
            "poll_vote": ${createPollVoteJsonString()}
        """.trimIndent(),
    )

internal fun createVoteRemovedEventStringJson() =
    createChatEventStringJson(
        "poll.vote_removed",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "poll": ${createPollJsonString()},
            "poll_vote": ${createPollVoteJsonString()}
        """.trimIndent(),
    )

internal fun createReminderCreatedEventStringJson() =
    createChatEventStringJson(
        "reminder.created",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "user_id": "bender",
            "reminder": ${createReminderJsonString()}
        """.trimIndent(),
    )

internal fun createReminderUpdatedEventStringJson() =
    createChatEventStringJson(
        "reminder.updated",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "user_id": "bender",
            "reminder": ${createReminderJsonString()}
        """.trimIndent(),
    )

internal fun createReminderDeletedEventStringJson() =
    createChatEventStringJson(
        "reminder.deleted",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "user_id": "bender",
            "reminder": ${createReminderJsonString()}
        """.trimIndent(),
    )

internal fun createNotificationReminderDueEventStringJson() =
    createChatEventStringJson(
        "notification.reminder_due",
        """
            "cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "user_id": "bender",
            "reminder": ${createReminderJsonString()}
        """.trimIndent(),
    )

internal fun createAIIndicatorUpdatedEventStringJson() =
    createChatEventStringJson(
        "ai_indicator.update",
        """
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "ai_state": "AI_STATE_THINKING",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755"
        """.trimIndent(),
    )

internal fun createAIIndicatorClearEventStringJson() =
    createChatEventStringJson(
        "ai_indicator.clear",
        """
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createAIIndicatorStopEventStringJson() =
    createChatEventStringJson(
        "ai_indicator.stop",
        """
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent(),
    )

internal fun createUserMessagesDeletedEventStringJson() =
    createChatEventStringJson(
        "user.messages.deleted",
        """
            "user": ${createUserJsonString()},
            "cid": "channelType:channelId",
            "channel_type": "channelType",
            "channel_id": "channelId",
            "hard_delete": false
        """.trimIndent(),
    )

internal fun createUnknownEventStringJson(type: String = "unknown_event") =
    createChatEventStringJson(type, null)

@Language("JSON")
private fun createChatEventStringJson(type: String, payload: String?) =
    """
        {"type": "$type",
         "created_at": "2020-06-29T06:14:28.000Z"
         ${payload?.let { ", $it" } ?: ""}
         }
    """.trimIndent()

@Language("JSON")
private fun createUserJsonString() =
    """
        {
            "id": "bender",
            "role": "user",
            "language": "",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "last_active": "2020-06-29T06:14:28.000Z",
            "banned": false,
            "online": true,
            "image": "https://api.adorable.io/avatars/285/bender.png",
            "name": "Bender"
          }
    """.trimIndent()

@Language("JSON")
private fun createOwnUserJsonString() =
    """
        {
            "id": "bender",
            "role": "user",
            "language": "",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "last_active": "2020-06-29T06:14:28.000Z",
            "banned": false,
            "online": true,
            "invisible": false,
            "total_unread_count": 26,
            "unread_channels": 2,
            "unread_count": 0,
            "unread_threads": 0,
            "image": "https://api.adorable.io/avatars/285/bender.png",
            "name": "Bender"
          }
    """.trimIndent()

@Language("JSON")
private fun createMessageJsonString() =
    """
        {
            "id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "text": "Hello",
            "channel": ${createChannelInfoJsonString()},
            "user": ${createUserJsonString()},
            "html": "<p>Hello</p>",
            "attachments": [
                ${createMessageAttachmentJsonString()}
            ],
            "latest_reactions": [
                ${createMessageReactionJsonString()}
            ],
            "own_reactions": [
                ${createMessageReactionJsonString()}
            ],
            "reaction_counts": {"like": 1},
            "reaction_scores": {"like": 1},
            "reply_count": 1,
            "deleted_reply_count": 0,
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "mentioned_users": [
                ${createUserJsonString()}
            ],
            "thread_participants": [
                ${createUserJsonString()}
            ],
            "silent": false,
            "type": "regular",
            "cid": ""
        }
    """.trimIndent()

@Language("JSON")
private fun createMessageAttachmentJsonString() =
    """
        {
            "type": "image",
            "image_url": "https://example.com/image.png",
            "thumb_url": "https://example.com/thumb.png",
            "file_size": 1234,
            "mime_type": "image/png"
        }
    """.trimIndent()

@Language("JSON")
private fun createMessageReactionJsonString() =
    """
        {
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "type": "like",
            "score": 1,
            "user": ${createUserJsonString()},
            "user_id": "bender",
            "created_at": "2020-06-29T06:14:28.000Z"
        }
    """.trimIndent()

@Language("JSON")
private fun createChannelInfoJsonString() =
    """
        {
            "id": "channelId",
            "type": "channelType",
            "cid": "channelType:channelId",
            "member_count": 1,
            "name": "Channel Name",
            "image": "https://domain.io/avatars/285/channel.png"
        }
    """.trimIndent()

@Language("JSON")
private fun createMemberJsonString() =
    """
        {
            "user": ${createUserJsonString()},
            "channel_role": "channel_member",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z"
        }
    """.trimIndent()

@Language("JSON")
private fun createChannelJsonString() =
    """
        {
    "id": "channelId",
    "type": "channelType",
    "cid": "channelType:channelId",
    "last_message_at": "2020-06-29T06:14:28.000Z",
    "created_at": "2020-06-29T06:14:28.000Z",
    "updated_at": "2020-06-29T06:14:28.000Z",
    "created_by": ${createUserJsonString()},
    "frozen": false,
    "members": [
        ${createMemberJsonString()}
    ],
    "member_count": 1,
    "config": ${createConfigJsonString()}
  }
    """.trimIndent()

@Language("JSON")
private fun createConfigJsonString() =
    """
        {
          "created_at": "2020-06-29T06:14:28.000Z",
          "updated_at": "2020-06-29T06:14:28.000Z",
          "name": "team",
          "typing_events": true,
          "read_events": true,
          "delivery_events": true,
          "connect_events": true,
          "search": true,
          "reactions": true,
          "replies": true,
          "mutes": true,
          "uploads": true,
          "url_enrichment":true,
          "custom_events": true,
          "push_notifications":true,
          "skip_last_msg_update_for_system_msgs": false,
          "message_retention": "infinite",
          "max_message_length": 5000,
          "polls": false,
          "automod": "disabled",
          "automod_behavior":"flag",
          "blocklist_behavior":"flag",
          "commands": [
            {
              "name": "giphy",
              "description": "Post a random gif to the channel",
              "args": "[text]",
              "set": "fun_set"
            }
          ],
          "mark_messages_pending": false
        }
    """.trimIndent()

@Language("JSON")
private fun createReactionJsonString() =
    """
        {
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "type": "type",
            "score": 3,
            "user": ${createUserJsonString()},
            "user_id": "bender",
            "created_at": "2020-06-29T06:14:28.000Z"
        }
    """.trimIndent()

@Language("JSON")
private fun createPollJsonString() =
    """
        {
            "id": "poll-id",
            "name": "Test Poll",
            "description": "A test poll",
            "options": [
                {"id": "option-1", "text": "Option 1"}
            ],
            "voting_visibility": "public",
            "enforce_unique_vote": true,
            "max_votes_allowed": 1,
            "allow_user_suggested_options": false,
            "allow_answers": true,
            "vote_count": 1,
            "vote_counts_by_option": {"option-1": 1},
            "latest_votes_by_option": {},
            "own_votes": [],
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "is_closed": false,
            "answers_count": 0,
            "latest_answers": [],
            "created_by": ${createUserJsonString()},
            "created_by_id": "bender"
        }
    """.trimIndent()

@Language("JSON")
private fun createPollVoteJsonString() =
    """
        {
            "id": "vote-id",
            "poll_id": "poll-id",
            "option_id": "option-1",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "user": ${createUserJsonString()},
            "user_id": "bender"
        }
    """.trimIndent()

@Language("JSON")
private fun createPollAnswerVoteJsonString() =
    """
        {
            "id": "answer-id",
            "poll_id": "poll-id",
            "option_id": "",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "user": ${createUserJsonString()},
            "user_id": "bender",
            "is_answer": true,
            "answer_text": "My answer"
        }
    """.trimIndent()

@Language("JSON")
private fun createDraftJsonString() =
    """
        {
            "message": {
                "id": "draft-message-id",
                "text": "Draft text"
            },
            "channel_cid": "channelType:channelId"
        }
    """.trimIndent()

@Language("JSON")
private fun createReminderJsonString() =
    """
        {
            "remind_at": "2020-06-29T06:14:28.000Z",
            "channel_cid": "channelType:channelId",
            "message_id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z"
        }
    """.trimIndent()

@Language("JSON")
private fun createErrorJsonString() =
    """
        {
            "code": 4,
            "message": "Token expired",
            "StatusCode": 401,
            "duration": "0.5ms",
            "exception_fields": {},
            "more_info": "https://getstream.io/chat/docs/",
            "details": []
        }
    """.trimIndent()

@Language("JSON")
internal fun createMarkAllReadEventStringJson() =
    """
        {
           "unread_count":0,
           "unread_channels":0,
           "total_unread_count":0,
           "created_at":"2020-06-29T06:14:28.000Z",
           "type":"notification.mark_read",
           "user":${createUserJsonString()}
        }
    """.trimIndent()
