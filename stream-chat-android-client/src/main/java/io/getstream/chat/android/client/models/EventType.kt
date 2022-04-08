/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.models

/**
 * https://getstream.io/chat/docs/js/#event_object
 */
public object EventType {

    /**
     * Remote
     */
    public const val USER_PRESENCE_CHANGED: String = "user.presence.changed"
    public const val USER_WATCHING_START: String = "user.watching.start"
    public const val USER_WATCHING_STOP: String = "user.watching.stop"
    public const val USER_UPDATED: String = "user.updated"
    public const val USER_BANNED: String = "user.banned"
    public const val USER_DELETED: String = "user.deleted"
    public const val USER_UNBANNED: String = "user.unbanned"
    public const val TYPING_START: String = "typing.start"
    public const val TYPING_STOP: String = "typing.stop"
    public const val MESSAGE_NEW: String = "message.new"
    public const val MESSAGE_UPDATED: String = "message.updated"
    public const val MESSAGE_DELETED: String = "message.deleted"
    public const val MESSAGE_READ: String = "message.read"
    public const val REACTION_NEW: String = "reaction.new"
    public const val REACTION_DELETED: String = "reaction.deleted"
    public const val REACTION_UPDATED: String = "reaction.updated"
    public const val MEMBER_ADDED: String = "member.added"
    public const val MEMBER_REMOVED: String = "member.removed"
    public const val MEMBER_UPDATED: String = "member.updated"
    public const val CHANNEL_UPDATED: String = "channel.updated"
    public const val CHANNEL_HIDDEN: String = "channel.hidden"
    public const val CHANNEL_DELETED: String = "channel.deleted"
    public const val CHANNEL_VISIBLE: String = "channel.visible"
    public const val CHANNEL_TRUNCATED: String = "channel.truncated"
    public const val HEALTH_CHECK: String = "health.check"
    public const val NOTIFICATION_MESSAGE_NEW: String = "notification.message_new"
    public const val NOTIFICATION_CHANNEL_TRUNCATED: String = "notification.channel_truncated"
    public const val NOTIFICATION_CHANNEL_DELETED: String = "notification.channel_deleted"
    public const val NOTIFICATION_MARK_READ: String = "notification.mark_read"
    public const val NOTIFICATION_INVITED: String = "notification.invited"
    public const val NOTIFICATION_INVITE_ACCEPTED: String = "notification.invite_accepted"
    public const val NOTIFICATION_INVITE_REJECTED: String = "notification.invite_rejected"
    public const val NOTIFICATION_ADDED_TO_CHANNEL: String = "notification.added_to_channel"
    public const val NOTIFICATION_REMOVED_FROM_CHANNEL: String = "notification.removed_from_channel"
    public const val NOTIFICATION_MUTES_UPDATED: String = "notification.mutes_updated"
    public const val NOTIFICATION_CHANNEL_MUTES_UPDATED: String = "notification.channel_mutes_updated"

    /**
     * Local
     */
    public const val CONNECTION_CONNECTING: String = "connection.connecting"
    public const val CONNECTION_DISCONNECTED: String = "connection.disconnected"
    public const val CONNECTION_ERROR: String = "connection.error"
    /**
     * Unknown
     */
    public const val UNKNOWN: String = "unknown_event"
}
