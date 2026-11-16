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

package io.getstream.chat.android.ui.common.feature.messages.composer.scheduled

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * The messages the server sent right away from these scheduled messages, while this flow is collected.
 *
 * A message scheduled while offline waits on the device in [ScheduledMessageStatus.PreparingToSchedule]. When its date
 * passed before the device is back online, the server sends it right away as a normal message with the same ID, and it
 * leaves the scheduled messages. A message sent at its date gets a new ID, and a deleted one has no message, so
 * [findMessage] tells the messages sent right away apart.
 *
 * @param findMessage Finds the message with the given ID, or returns null when there is none.
 */
internal fun Flow<List<ScheduledMessage>>.sentNow(findMessage: suspend (id: String) -> Message?): Flow<Message> = flow {
    var previous = emptyList<ScheduledMessage>()
    collect { current ->
        val currentIds = current.mapTo(HashSet(), ScheduledMessage::id)
        val removed = previous.filter { scheduledMessage ->
            scheduledMessage.status == ScheduledMessageStatus.PreparingToSchedule && scheduledMessage.id !in currentIds
        }
        previous = current
        removed.forEach { scheduledMessage -> findMessage(scheduledMessage.id)?.let { emit(it) } }
    }
}
