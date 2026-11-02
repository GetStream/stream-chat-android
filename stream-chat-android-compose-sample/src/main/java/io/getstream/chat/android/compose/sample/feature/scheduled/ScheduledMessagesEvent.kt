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

package io.getstream.chat.android.compose.sample.feature.scheduled

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import java.util.Date

/**
 * Something the scheduled messages screens show or do once.
 */
internal sealed interface ScheduledMessagesEvent {

    /** The server scheduled the message at [scheduledAt]. */
    data class Scheduled(val scheduledAt: Date) : ScheduledMessagesEvent

    /** The server is sending the message. */
    data object Sent : ScheduledMessagesEvent

    /** The server sent the message right away, as its date had passed. */
    data object SentRightAway : ScheduledMessagesEvent

    /** The device is offline: the change is sent once it is online. */
    data object Queued : ScheduledMessagesEvent

    /** The server is sending the message, so nothing can be done with it. */
    data object StillSending : ScheduledMessagesEvent

    /** The message was deleted. */
    data object Deleted : ScheduledMessagesEvent

    /** The server, or the device, could not save the change. */
    data class Failed(val failure: ScheduledMessageFailure?) : ScheduledMessagesEvent

    /** The change was refused before it was saved, or the request failed. */
    data class Error(val message: String) : ScheduledMessagesEvent

    /** The composer is emptied, after its content was scheduled, or the edit of a scheduled message ended. */
    data object ClearComposer : ScheduledMessagesEvent

    /** The composer is filled with the [scheduledMessage] to edit, and its [quotedMessage], if any. */
    data class EditInComposer(val scheduledMessage: ScheduledMessage, val quotedMessage: Message?) :
        ScheduledMessagesEvent

    /** The scheduled message being edited was sent, so the edit ended. */
    data object EditedMessageSent : ScheduledMessagesEvent

    /** The scheduled message being edited was deleted, for example on another device, so the edit ended. */
    data object EditedMessageDeleted : ScheduledMessagesEvent
}

/**
 * The event that tells the user the answer of the server to the latest change of the scheduled message with [id]:
 * [ScheduledMessagesEvent.Queued] right away when the device is offline, else once the server answered.
 *
 * @param isSendNow Whether the change was a send now, which succeeded unless it failed.
 */
internal suspend fun ChatClient.answerOf(id: String, isSendNow: Boolean = false): ScheduledMessagesEvent {
    if (clientState.connectionState.value is ConnectionState.Offline) return ScheduledMessagesEvent.Queued
    return when (val answer = scheduledMessages.awaitAnswer(id)) {
        is ScheduledMessageAnswer.Failed -> ScheduledMessagesEvent.Failed(answer.failure)
        is ScheduledMessageAnswer.Scheduled ->
            if (isSendNow) ScheduledMessagesEvent.Sent else ScheduledMessagesEvent.Scheduled(answer.scheduledAt)
        ScheduledMessageAnswer.Sent ->
            if (isSendNow) ScheduledMessagesEvent.Sent else ScheduledMessagesEvent.SentRightAway
    }
}
