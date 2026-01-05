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

package io.getstream.chat.android.models

/**
 * Predefined list of channel capabilities constants.
 * Channel capabilities provide you information on which features are available for the current user.
 * Channel capabilities are affected by a number of factors including:
 * * User Permissions
 * * Channel Type settings
 * * Channel-level settings
 *
 * Capabilities are the preferred way of deciding which actions should be available in the user interface.
 */
public object ChannelCapabilities {
    /** Ability to ban channel members. */
    public const val BAN_CHANNEL_MEMBERS: String = "ban-channel-members"

    /** Ability to receive connect events. */
    public const val CONNECT_EVENTS: String = "connect-events"

    /** Ability to delete any message from the channel. */
    public const val DELETE_ANY_MESSAGE: String = "delete-any-message"

    /** Ability to delete the channel. */
    public const val DELETE_CHANNEL: String = "delete-channel"

    /** Ability to delete own messages from the channel. */
    public const val DELETE_OWN_MESSAGE: String = "delete-own-message"

    /** Ability to flag a message. */
    public const val FLAG_MESSAGE: String = "flag-message"

    /** Ability to freeze or unfreeze the channel. */
    public const val FREEZE_CHANNEL: String = "freeze-channel"

    /** Ability to leave the channel (remove own membership). */
    public const val LEAVE_CHANNEL: String = "leave-channel"

    /** Ability to join channel (add own membership). */
    public const val JOIN_CHANNEL: String = "join-channel"

    /** Ability to mute the channel. */
    public const val MUTE_CHANNEL: String = "mute-channel"

    /** Ability to pin a message. */
    public const val PIN_MESSAGE: String = "pin-message"

    /** Ability to quote a message. */
    public const val QUOTE_MESSAGE: String = "quote-message"

    /** Ability to receive read events. */
    public const val READ_EVENTS: String = "read-events"

    /** Ability to receive delivery events. */
    public const val DELIVERY_EVENTS: String = "delivery-events"

    /** Ability to use message search. */
    public const val SEARCH_MESSAGES: String = "search-messages"

    /** Ability to send custom events. */
    public const val SEND_CUSTOM_EVENTS: String = "send-custom-events"

    /** Ability to attach links to messages. */
    public const val SEND_LINKS: String = "send-links"

    /** Ability to send a message. */
    public const val SEND_MESSAGE: String = "send-message"

    /** Ability to send reactions. */
    public const val SEND_REACTION: String = "send-reaction"

    /** Ability to thread reply to a message. */
    public const val SEND_REPLY: String = "send-reply"

    /** Ability to enable or disable slow mode. */
    public const val SET_CHANNEL_COOLDOWN: String = "set-channel-cooldown"

    /** Ability to send and receive typing events. */
    @Deprecated(
        "Use TYPING_EVENTS instead.",
    )
    public const val SEND_TYPING_EVENTS: String = "send-typing-events"

    /** Ability to update any message in the channel. */
    public const val UPDATE_ANY_MESSAGE: String = "update-any-message"

    /** Ability to update channel data. */
    public const val UPDATE_CHANNEL: String = "update-channel"

    /** Ability to update channel members. */
    public const val UPDATE_CHANNEL_MEMBERS: String = "update-channel-members"

    /** Ability to update own messages in the channel. */
    public const val UPDATE_OWN_MESSAGE: String = "update-own-message"

    /** Ability to upload message attachments. */
    public const val UPLOAD_FILE: String = "upload-file"

    /** Ability to send and receive typing events. */
    public const val TYPING_EVENTS: String = "typing-events"

    /** Indicates that channel slow mode is active. */
    public const val SLOW_MODE: String = "slow-mode"

    /** Indicates that slow-mode should be skipped. */
    public const val SKIP_SLOW_MODE: String = "skip-slow-mode"

    /** Ability to join a call. */
    public const val JOIN_CALL: String = "join-call"

    /** "Ability to create a call. */
    public const val CREATE_CALL: String = "create-call"

    /** Ability to cast a vote in a poll. */
    public const val CAST_POLL_VOTE: String = "cast-poll-vote"

    /** Ability to send a poll. */
    public const val SEND_POLL: String = "send-poll"
}
