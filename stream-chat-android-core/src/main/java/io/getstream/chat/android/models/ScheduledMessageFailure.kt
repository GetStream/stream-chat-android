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

import androidx.compose.runtime.Immutable

/**
 * Why a [ScheduledMessage] could not be scheduled or sent, telling whether the server or the local device failed. More
 * kinds can be added in a minor version, so a `when` on it needs an `else` branch.
 */
@Immutable
public abstract class ScheduledMessageFailure internal constructor() {

    /**
     * Whether sending the same changes again can succeed without changing them.
     */
    public abstract val isRetryable: Boolean

    /**
     * The server rejected scheduling, editing or rescheduling the message, for example because the scheduling limit of
     * the channel is reached. The latest changes are kept.
     *
     * @property reason The reason of the rejection, or null when the server gives none this SDK knows.
     * @property message The message of the server, which describes the rejection.
     */
    public data class ServerRejectedScheduling(
        val reason: Reason?,
        val message: String,
    ) : ScheduledMessageFailure() {

        /**
         * False when the changes have to be changed first: the date is too soon or too far, a scheduling limit is
         * reached, the feature is off, the ID is taken, or the message can't be edited, was already sent or was
         * deleted.
         */
        override val isRetryable: Boolean
            get() = when (reason) {
                null, Reason.ScheduledMessageSending, Reason.ScheduledMessageConflict -> true
                else -> false
            }

        /**
         * The reason the server rejects scheduling, editing or rescheduling a message. More reasons can be added in a
         * minor version, so a `when` on it needs an `else` branch.
         */
        @Immutable
        public abstract class Reason internal constructor() {

            /**
             * The name of the reason, as the server gives it.
             */
            public abstract val value: String

            /** Scheduled messages are disabled for the app. */
            public data object ScheduledMessagesDisabled : Reason() {
                override val value: String = "scheduled_messages_disabled"
            }

            /** The new date of an edited message is in the past. */
            public data object ScheduledAtTooSoon : Reason() {
                override val value: String = "scheduled_at_too_soon"
            }

            /** The date is further away than the app allows. */
            public data object ScheduledAtTooFar : Reason() {
                override val value: String = "scheduled_at_too_far"
            }

            /** The current user has as many pending scheduled messages in the channel as the app allows. */
            public data object ScheduledLimitChannel : Reason() {
                override val value: String = "scheduled_limit_channel"
            }

            /** The current user has as many pending scheduled messages as the app allows. */
            public data object ScheduledLimitUser : Reason() {
                override val value: String = "scheduled_limit_user"
            }

            /** The ID of the message belongs to a scheduled message of another user or channel. */
            public data object ScheduledMessageIdTaken : Reason() {
                override val value: String = "scheduled_message_id_taken"
            }

            /** A failed scheduled message can't be edited or sent now. */
            public data object ScheduledMessageNotEditable : Reason() {
                override val value: String = "scheduled_message_not_editable"
            }

            /** The server is sending the message, or will try again shortly. */
            public data object ScheduledMessageSending : Reason() {
                override val value: String = "scheduled_message_sending"
            }

            /** Another edit was saved first. */
            public data object ScheduledMessageConflict : Reason() {
                override val value: String = "scheduled_message_conflict"
            }

            /** The message was already sent. */
            public data object ScheduledMessageAlreadySent : Reason() {
                override val value: String = "scheduled_message_already_sent"
            }

            /** The scheduled message was deleted, for example on another device. */
            public data object ScheduledMessageCanceled : Reason() {
                override val value: String = "scheduled_message_canceled"
            }
        }
    }

    /**
     * The server could not send the message at its date, for example because the user was banned. The message can only
     * be deleted, or scheduled again as a new message.
     *
     * @property reason The reason of the failure, or null when the server gives none.
     */
    public data class ServerFailedPublishing(
        val reason: Reason?,
    ) : ScheduledMessageFailure() {

        override val isRetryable: Boolean get() = false

        /**
         * The reason the server could not send a scheduled message. More reasons can be added in a minor version, so a
         * `when` on it needs an `else` branch.
         */
        @Immutable
        public abstract class Reason internal constructor() {

            /**
             * The name of the reason, as the server gives it.
             */
            public abstract val value: String

            /** The user is banned from the channel. */
            public data object Banned : Reason() {
                override val value: String = "banned"
            }

            /** The channel was deleted. */
            public data object ChannelDeleted : Reason() {
                override val value: String = "channel_deleted"
            }

            /** The channel is frozen. */
            public data object ChannelFrozen : Reason() {
                override val value: String = "channel_frozen"
            }

            /** The server was delayed and could not send the message in time. */
            public data object Expired : Reason() {
                override val value: String = "expired"
            }

            /** A hook of the app rejected the message. */
            public data object HookRejected : Reason() {
                override val value: String = "hook_rejected"
            }

            /** The server failed for another reason. */
            public data object InternalError : Reason() {
                override val value: String = "internal_error"
            }

            /** The message no longer meets the limits of the app, for example it is too long. */
            public data object InvalidMessage : Reason() {
                override val value: String = "invalid_message"
            }

            /** Moderation blocked the message. */
            public data object ModerationBlocked : Reason() {
                override val value: String = "moderation_blocked"
            }

            /** The user is no longer allowed to send messages in the channel. */
            public data object NoPermission : Reason() {
                override val value: String = "no_permission"
            }

            /** The channel now holds messages for approval, which scheduled messages don't support. */
            public data object PendingNotSupported : Reason() {
                override val value: String = "pending_not_supported"
            }

            /** The user was deleted. */
            public data object UserDeleted : Reason() {
                override val value: String = "user_deleted"
            }

            /**
             * A reason this version of the SDK does not know.
             *
             * @property value The name of the reason, as the server gives it.
             */
            public data class Unknown(override val value: String) : Reason()
        }
    }

    /**
     * The local device could not send the changes of the message to the server, for example because an attachment
     * could not be uploaded.
     *
     * @property reason The reason of the failure.
     * @property message A description of the underlying error, when there is one.
     */
    public data class LocalDeviceFailedScheduling(
        val reason: Reason,
        val message: String?,
    ) : ScheduledMessageFailure() {

        override val isRetryable: Boolean get() = true

        /**
         * The reason the local device could not send the changes of a scheduled message. More reasons can be added in
         * a minor version, so a `when` on it needs an `else` branch.
         */
        @Immutable
        public abstract class Reason internal constructor() {

            /**
             * The name of the reason.
             */
            public abstract val value: String

            /** An attachment of the message could not be uploaded. */
            public data object AttachmentUploadFailed : Reason() {
                override val value: String = "attachment_upload_failed"
            }

            /** The request failed without an answer from the server. */
            public data object RequestFailed : Reason() {
                override val value: String = "request_failed"
            }
        }
    }
}
