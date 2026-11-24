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

package io.getstream.chat.android.ui.common.state.scheduled

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.result.Error

/**
 * The state of a list of the current user's scheduled messages.
 *
 * @param items The scheduled messages, sorted by the date they are sent at, then by their text.
 * @param isLoading Whether the scheduled messages are loading, and none is known yet.
 * @param isLoadFailed Whether the scheduled messages could not be loaded, or can't be while offline, and none is
 * known yet.
 */
public data class ScheduledMessageListState(
    val items: List<ScheduledMessageItemState> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadFailed: Boolean = false,
)

/**
 * A scheduled message of the list.
 *
 * @param scheduledMessage The scheduled message.
 * @param channel The channel of the message, for its name and its avatar, built from [ScheduledMessage.channel], or
 * null when there is no summary.
 * @param quotedMessage The message it quotes, or null when it quotes none, or when the server did not send the quoted
 * message (for example, it is gone or hidden from the user) and this device does not know it.
 * @param actions The actions the status of the message allows, in the order to show them. Empty while the message
 * is sending or its attachments are uploading. When the channel saved on this device tells that the current user can
 * no longer schedule messages in it, for example once it is frozen, the message can't be edited, rescheduled or sent
 * now, and its text can be copied before it is deleted.
 */
public data class ScheduledMessageItemState(
    val scheduledMessage: ScheduledMessage,
    val channel: Channel?,
    val quotedMessage: Message?,
    val actions: List<ScheduledMessageAction>,
) {

    /**
     * Whether the message quotes a message that was deleted, so it can't be sent or rescheduled until it is edited to
     * remove the quote.
     */
    val isQuoteDeleted: Boolean
        get() = quotedMessage?.deletedAt != null
}

/**
 * An action on a scheduled message. More actions can be added in a minor version, so a `when` on it needs an `else`
 * branch.
 */
public abstract class ScheduledMessageAction internal constructor() {

    /** Edits the content of the message, and keeps its date. */
    public data object Edit : ScheduledMessageAction()

    /** Changes the date the message is sent at. */
    public data object Reschedule : ScheduledMessageAction()

    /** Sends the message right away. */
    public data object SendNow : ScheduledMessageAction()

    /**
     * Edits a message the server failed to send, and schedules it again as a new message, as the server can't change
     * it.
     */
    public data object EditAndReschedule : ScheduledMessageAction()

    /** Copies the text of a message that can only be deleted, as its channel was deleted. */
    public data object CopyText : ScheduledMessageAction()

    /** Deletes the message, so it is never sent. */
    public data object Delete : ScheduledMessageAction()
}

/**
 * A one-time event of a list of scheduled messages, to tell the user. More events can be added in a minor version, so
 * a `when` on it needs an `else` branch.
 */
public abstract class ScheduledMessageListEvent internal constructor() {

    /**
     * The message the user acted on was already sent, or deleted on another device, so it left the list.
     */
    public data object MessageGone : ScheduledMessageListEvent()

    /**
     * A message deleted from the list had already been sent, so it is in its channel.
     */
    public data object DeletedMessageAlreadySent : ScheduledMessageListEvent()

    /**
     * Send now was asked while offline: the message is sent once the device is back online.
     */
    public data object SendNowQueued : ScheduledMessageListEvent()

    /**
     * An action on a scheduled message failed.
     *
     * @property error Why the action failed.
     */
    public data class ActionFailed(val error: Error) : ScheduledMessageListEvent()
}
