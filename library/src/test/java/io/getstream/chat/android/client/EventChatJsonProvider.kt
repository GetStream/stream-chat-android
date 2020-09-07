package io.getstream.chat.android.client

fun createChannelCreatedEventStringJson() =
    createChatEventStringJson(
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

fun createChannelDeletedEventStringJson() =
    createChatEventStringJson(
        "channel.deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createChannelHiddenEventStringJson() =
    createChatEventStringJson(
        "channel.hidden",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "clear_history": true
        """.trimIndent()
    )

fun createChannelMuteEventStringJson() =
    createChatEventStringJson(
        "channel.muted",
        """
            "mute": ${createChannelMuteJsonString()}
        """.trimIndent()
    )

fun createChannelsMuteEventStringJson() =
    createChatEventStringJson(
        "channel.muted",
        """
            "mutes": [
                ${createChannelMuteJsonString()}
            ]
        """.trimIndent()
    )

fun createChannelTruncatedEventStringJson() =
    createChatEventStringJson(
        "channel.truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createChannelUnmuteEventStringJson() =
    createChatEventStringJson(
        "channel.unmuted",
        """
            "mute": ${createChannelMuteJsonString()}
        """.trimIndent()
    )

fun createChannelsUnmuteEventStringJson() =
    createChatEventStringJson(
        "channel.unmuted",
        """
            "mutes": [ ${createChannelMuteJsonString()} ]
        """.trimIndent()
    )

fun createChannelUpdatedEventStringJson() =
    createChatEventStringJson(
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

fun createChannelVisibleEventStringJson() =
    createChatEventStringJson(
        "channel.visible",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createMemberAddedEventStringJson() =
    createChatEventStringJson(
        "member.added",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createMemberRemovedEventStringJson() =
    createChatEventStringJson(
        "member.removed",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createMemberUpdatedEventStringJson() =
    createChatEventStringJson(
        "member.updated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createMessageDeletedEventStringJson() =
    createChatEventStringJson(
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

fun createMessageReadEventStringJson() =
    createChatEventStringJson(
        "message.read",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3
        """.trimIndent()
    )

fun createMessageUpdatedEventStringJson() =
    createChatEventStringJson(
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

fun createNotificationAddedToChannelEventStringJson() =
    createChatEventStringJson(
        "notification.added_to_channel",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createNotificationChannelDeletedEventStringJson() =
    createChatEventStringJson(
        "notification.channel_deleted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createNotificationChannelTruncatedEventStringJson() =
    createChatEventStringJson(
        "notification.channel_truncated",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "channel": ${createChannelJsonString()}
        """.trimIndent()
    )

fun createNotificationInviteAcceptedEventStringJson() =
    createChatEventStringJson(
        "notification.invite_accepted",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createNotificationInvitedEventStringJson() =
    createChatEventStringJson(
        "notification.invited",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()},
            "member": ${createMemberJsonString()}
        """.trimIndent()
    )

fun createNotificationMarkReadEventStringJson() =
    createChatEventStringJson(
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

fun createNotificationMessageNewEventStringJson() =
    createChatEventStringJson(
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

fun createNotificationRemovedFromChannelEventStringJson() =
    createChatEventStringJson(
        "notification.removed_from_channel",
        """
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createReactionDeletedEventStringJson() =
    createChatEventStringJson(
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

fun createReactionNewEventStringJson() =
    createChatEventStringJson(
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

fun createReactionUpdateEventStringJson() =
    createChatEventStringJson(
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

fun createTypingStartEventStringJson() =
    createChatEventStringJson(
        "typing.start",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId"
        """.trimIndent()
    )

fun createTypingStopEventStringJson() =
    createChatEventStringJson(
        "typing.stop",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId"
        """.trimIndent()
    )

fun createChannelUserBannedEventStringJson() =
    createChatEventStringJson(
        "user.banned",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "expiration": "2020-06-29T06:14:28.000Z"
        """.trimIndent()
    )

fun createGlobalUserBannedEventStringJson() =
    createChatEventStringJson(
        "user.banned",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserDeletedEventStringJson() =
    createChatEventStringJson(
        "user.deleted",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserMutedEventStringJson() =
    createChatEventStringJson(
        "user.muted",
        """
            "user": ${createUserJsonString()},
            "target_user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUsersMutedEventStringJson() =
    createChatEventStringJson(
        "user.muted",
        """
            "user": ${createUserJsonString()},
            "target_users": [ ${createUserJsonString()} ]
        """.trimIndent()
    )

fun createUserPresenceChangedEventStringJson() =
    createChatEventStringJson(
        "user.presence.changed",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserStartWatchingEventStringJson() =
    createChatEventStringJson(
        "user.watching.start",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3
        """.trimIndent()
    )

fun createUserStopWatchingEventStringJson() =
    createChatEventStringJson(
        "user.watching.stop",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId",
            "watcher_count": 3
        """.trimIndent()
    )

fun createChannelUserUnbannedEventStringJson() =
    createChatEventStringJson(
        "user.unbanned",
        """
            "user": ${createUserJsonString()},
            "channel_type": "channelType",
            "channel_id": "channelId",
            "cid": "channelType:channelId"
        """.trimIndent()
    )

fun createGlobalUserUnbannedEventStringJson() =
    createChatEventStringJson(
        "user.unbanned",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUserUnmutedEventStringJson() =
    createChatEventStringJson(
        "user.unmuted",
        """
            "user": ${createUserJsonString()},
            "target_user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createUsersUnmutedEventStringJson() =
    createChatEventStringJson(
        "user.unmuted",
        """
            "user": ${createUserJsonString()},
            "target_users": [ ${createUserJsonString()} ]
        """.trimIndent()
    )

fun createUserUpdatedEventStringJson() =
    createChatEventStringJson(
        "user.updated",
        """
            "user": ${createUserJsonString()}
        """.trimIndent()
    )

fun createHealthEventStringJson() =
    createChatEventStringJson(
        "health.check",
        """
            "connection_id":"6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
        """.trimIndent()
    )

fun createConnectedEventStringJson(userJsonString: String? = createUserJsonString()) =
    createChatEventStringJson(
        "health.check",
        """
            "connection_id":"6cfffec7-40df-40ac-901a-6ea6c5b7fb83",
            "me": $userJsonString
        """.trimIndent()
    )

fun createNotificationChannelMutesUpdatedEventStringJson() =
    createChatEventStringJson(
        "notification.channel_mutes_updated",
        """
            "me": ${createUserJsonString()}
        """.trimIndent()
    )

fun createNotificationMutesUpdatedEventStringJson() =
    createChatEventStringJson(
        "notification.mutes_updated",
        """
            "me": ${createUserJsonString()}
        """.trimIndent()
    )

fun createNewMessageEventStringJson() =
    createChatEventStringJson(
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

fun createUnknownEventStringJson(type: String = "unknown_event") =
    createChatEventStringJson(type, null)

private fun createChatEventStringJson(type: String, payload: String?) =
    """
        {"type": "$type",
         "created_at": "2020-06-29T06:14:28.000Z"
         ${payload?.let { ", $it" } ?: ""}
         }
    """.trimIndent()

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
