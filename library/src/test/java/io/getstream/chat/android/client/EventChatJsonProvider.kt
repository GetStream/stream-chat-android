package io.getstream.chat.android.client

import java.io.StringReader

fun createChannelCreatedEventStringReader() =
    createChatEventStringReader(
        "channel.created",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "message": ${createMessageJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createChannelDeletedEventStringReader() =
    createChatEventStringReader(
        "channel.deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createChannelHiddenEventStringReader() =
    createChatEventStringReader(
        "channel.hidden",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "clear_history": true
        """.trimIndent()
    )

fun createChannelMuteEventStringReader() =
    createChatEventStringReader(
        "channel.muted",
        """
            "mute": ${createChannelMuteJsonString()}
        """.trimIndent()
    )

fun createChannelsMuteEventStringReader() =
    createChatEventStringReader(
        "channel.muted",
        """
            "mutes": [
                ${createChannelMuteJsonString()}
            ]
        """.trimIndent()
    )

fun createChannelTruncatedEventStringReader() =
    createChatEventStringReader(
        "channel.truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createChannelUnmuteEventStringReader() =
    createChatEventStringReader(
        "channel.unmuted",
        """
            "mute": ${createChannelMuteJsonString()}
        """.trimIndent()
    )

fun createChannelsUnmuteEventStringReader() =
    createChatEventStringReader(
        "channel.unmuted",
        """
            "mutes": [ ${createChannelMuteJsonString()} ]
        """.trimIndent()
    )

fun createChannelUpdatedEventStringReader() =
    createChatEventStringReader(
        "channel.updated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "message": ${createMessageJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createChannelVisibleEventStringReader() =
    createChatEventStringReader(
        "channel.visible",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createMemberAddedEventStringReader() =
    createChatEventStringReader(
        "member.added",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createMemberRemovedEventStringReader() =
    createChatEventStringReader(
        "member.removed",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createMemberUpdatedEventStringReader() =
    createChatEventStringReader(
        "member.updated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createMessageDeletedEventStringReader() =
    createChatEventStringReader(
        "message.deleted",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "message": ${createMessageJsonString()},
            "deleted_at": "2020-06-29T06:14:28.000Z"
        """.trimIndent()
    )

fun createMessageReadEventStringReader() =
    createChatEventStringReader(
        "message.read",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3
        """.trimIndent()
    )

fun createMessageUpdatedEventStringReader() =
    createChatEventStringReader(
        "message.updated",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "message": ${createMessageJsonString()}
        """.trimIndent()
    )

fun createNotificationAddedToChannelEventStringReader() =
    createChatEventStringReader(
        "notification.added_to_channel",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createNotificationChannelDeletedEventStringReader() =
    createChatEventStringReader(
        "notification.channel_deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createNotificationChannelTruncatedEventStringReader() =
    createChatEventStringReader(
        "notification.channel_truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createNotificationInviteAcceptedEventStringReader() =
    createChatEventStringReader(
        "notification.invite_accepted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createNotificationInvitedEventStringReader() =
    createChatEventStringReader(
        "notification.invited",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createNotificationMarkReadEventStringReader() =
    createChatEventStringReader(
        "notification.mark_read",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "watcher_count": 3,
            "unread_messages": 5,
            "total_unread_count": 4
        """.trimIndent()
    )

fun createNotificationMessageNewEventStringReader() =
    createChatEventStringReader(
        "notification.message_new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "unread_messages": 5,
            "total_unread_count": 4,
            "message": ${createMessageJsonString()}
        """.trimIndent()
    )

fun createNotificationRemovedFromChannelEventStringReader() =
    createChatEventStringReader(
        "notification.removed_from_channel",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createReactionDeletedEventStringReader() =
    createChatEventStringReader(
        "reaction.deleted",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "reaction": ${createReactionJsonString()}
        """.trimIndent()
    )

fun createReactionNewEventStringReader() =
    createChatEventStringReader(
        "reaction.new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "reaction": ${createReactionJsonString()}
        """.trimIndent()
    )

fun createReactionUpdateEventStringReader() =
    createChatEventStringReader(
        "reaction.updated",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "message": ${createMessageJsonString()},
            "reaction": ${createReactionJsonString()}
        """.trimIndent()
    )

fun createTypingStartEventStringReader() =
    createChatEventStringReader(
        "typing.start",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId"
        """.trimIndent()
    )

fun createTypingStopEventStringReader() =
    createChatEventStringReader(
        "typing.stop",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId"
        """.trimIndent()
    )

fun createChannelUserBannedEventStringReader() =
    createChatEventStringReader(
        "user.banned",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "expiration": "2020-06-29T06:14:28.000Z"
        """.trimIndent()
    )

fun createGlobalUserBannedEventStringReader() =
    createChatEventStringReader(
        "user.banned",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserDeletedEventStringReader() =
    createChatEventStringReader(
        "user.deleted",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserMutedEventStringReader() =
    createChatEventStringReader(
        "user.muted",
        """
            "user": ${createUserJsonString()},
            "target_user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUsersMutedEventStringReader() =
    createChatEventStringReader(
        "user.muted",
        """
            "user": ${createUserJsonString()},
            "target_users": [ ${createUserJsonString()} ]
        """.trimIndent()
    )

fun createUserPresenceChangedEventStringReader() =
    createChatEventStringReader(
        "user.presence.changed",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserStartWatchingEventStringReader() =
    createChatEventStringReader(
        "user.watching.start",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3
        """.trimIndent()
    )

fun createUserStopWatchingEventStringReader() =
    createChatEventStringReader(
        "user.watching.stop",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3
        """.trimIndent()
    )

fun createChannelUserUnbannedEventStringReader() =
    createChatEventStringReader(
        "user.unbanned",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId"
        """.trimIndent()
    )

fun createGlobalUserUnbannedEventStringReader() =
    createChatEventStringReader(
        "user.unbanned",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserUnmutedEventStringReader() =
    createChatEventStringReader(
        "user.unmuted",
        """
            "user": ${createUserJsonString()},
            "target_user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUsersUnmutedEventStringReader() =
    createChatEventStringReader(
        "user.unmuted",
        """
            "user": ${createUserJsonString()},
            "target_users": [ ${createUserJsonString()} ]
        """.trimIndent()
    )

fun createUserUpdatedEventStringReader() =
    createChatEventStringReader(
        "user.updated",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createHealthEventStringReader() =
    createChatEventStringReader(
        "health.check",
        """
            "connection_id":"6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
        """.trimIndent()
    )

fun createConnectedEventStringReader() =
    createChatEventStringReader(
        "health.check",
        """
            "connection_id":"6cfffec7-40df-40ac-901a-6ea6c5b7fb83",
            "me": ${createUserJsonString()}
        """.trimIndent()
    )

fun createNotificationChannelMutesUpdatedEventStringReader() =
    createChatEventStringReader(
        "notification.channel_mutes_updated",
        """
            "me": ${createUserJsonString()}
        """.trimIndent()
    )

fun createNotificationMutesUpdatedEventStringReader() =
    createChatEventStringReader(
        "notification.mutes_updated",
        """
            "me": ${createUserJsonString()}
        """.trimIndent()
    )

fun createNewMessageEventStringReader() =
    createChatEventStringReader(
        "message.new",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3,
            "unread_messages": 5,
            "total_unread_count": 4,
            "message": ${createMessageJsonString()}
        """.trimIndent()
    )

private fun createChatEventStringReader(type: String, payload: String) = StringReader(
    """
        {"type": "$type",
         "created_at": "2020-06-29T06:14:28.000Z",
         $payload
         }
    """.trimIndent()
)

private fun createUserJsonString() =
    """
        {
            "id": "bender",
            "role": "user",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "last_active": "2020-06-29T06:14:28.000Z",
            "banned": false,
            "online": true,
            "invisible": false,
            "devices": [ ],
            "mutes": [ ],
            "channel_mutes": [ ],
            "unread_count": 26,
            "total_unread_count": 26,
            "unread_channels": 2,
            "image": "https://api.adorable.io/avatars/285/bender.png",
            "name": "Bender"
          }
    """.trimIndent()

private fun createMessageJsonString() =
    """
        {
            "id": "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
            "text": "Hello",
            "user": ${createUserJsonString()},
            "attachments": [ ],
            "latest_reactions": [ ],
            "own_reactions": [ ],
            "reaction_counts": { },
            "reaction_scores": { },
            "reply_count": 0,
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z",
            "mentioned_users": [ ],
            "silent": false,
            "isToday": false,
            "isStartDay": false,
            "commandInfo": { },
            "isYesterday": false,
            "cid": "",
            "date": "",
            "time": ""
        }
    """.trimIndent()

private fun createMemberJsonString() =
    """
        {
            "user": ${createUserJsonString()},
            "role": "user",
            "created_at": "2020-06-29T06:14:28.000Z",
            "updated_at": "2020-06-29T06:14:28.000Z"
        }
    """.trimIndent()

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

private fun createConfigJsonString() =
    """
        {
          "created_at": "2020-06-29T06:14:28.000Z",
          "updated_at": "2020-06-29T06:14:28.000Z",
          "name": "team",
          "typing_events": true,
          "read_events": true,
          "connect_events": true,
          "search": true,
          "reactions": true,
          "replies": true,
          "mutes": true,
          "uploads": true,
          "url_enrichment": true,
          "message_retention": "infinite",
          "max_message_length": 5000,
          "automod": "disabled",
          "automod_behavior": "flag",
          "commands": [
            {
              "name": "giphy",
              "description": "Post a random gif to the channel",
              "args": "[text]",
              "set": "fun_set"
            }
          ]
        }
    """.trimIndent()

private fun createChannelMuteJsonString() =
    """
        {
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()},
            "created_at": "2020-06-29T06:14:28.000Z"
        }
    """.trimIndent()

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
