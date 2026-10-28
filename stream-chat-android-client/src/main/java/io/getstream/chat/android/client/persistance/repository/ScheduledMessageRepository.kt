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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.scheduled.LocalScheduledMessage

/**
 * Repository to read and write the current user's scheduled messages. They are kept apart from the messages of the
 * channels, so they never show in a channel, a thread or a preview until the server sends them.
 */
internal interface ScheduledMessageRepository {

    /**
     * Selects all the scheduled messages.
     */
    suspend fun selectScheduledMessages(): List<LocalScheduledMessage>

    /**
     * Inserts or replaces a scheduled message.
     *
     * @param message The scheduled message.
     */
    suspend fun insertScheduledMessage(message: LocalScheduledMessage)

    /**
     * Deletes a scheduled message.
     *
     * @param id The ID of the scheduled message.
     */
    suspend fun deleteScheduledMessage(id: String)

    /**
     * Deletes all the scheduled messages.
     */
    suspend fun clear()
}
