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

import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date

/**
 * What the server answered to the latest local change of a scheduled message.
 */
internal sealed interface ScheduledMessageAnswer {

    /** The server has the change, and sends the message at [scheduledAt], which it rounds up to the minute. */
    data class Scheduled(val scheduledAt: Date) : ScheduledMessageAnswer

    /** The server is sending the message, or sent it: it is no longer in the scheduled messages. */
    data object Sent : ScheduledMessageAnswer

    /** The change failed. */
    data class Failed(val failure: ScheduledMessageFailure?) : ScheduledMessageAnswer
}

/**
 * Waits until the scheduled message with [id] has no local change the server does not have yet. Offline, it waits
 * until the device is back online.
 */
internal suspend fun Flow<List<ScheduledMessage>>.awaitAnswer(id: String): ScheduledMessageAnswer =
    map { scheduledMessages -> scheduledMessages.find { it.id == id } }
        .first { it?.status != ScheduledMessageStatus.PreparingToSchedule }
        .let { scheduledMessage ->
            when (scheduledMessage?.status) {
                null, ScheduledMessageStatus.Sending -> ScheduledMessageAnswer.Sent
                ScheduledMessageStatus.Failed -> ScheduledMessageAnswer.Failed(scheduledMessage.failure)
                else -> ScheduledMessageAnswer.Scheduled(scheduledMessage.scheduledAt)
            }
        }
