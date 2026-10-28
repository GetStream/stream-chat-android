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

package io.getstream.chat.android.client.internal.offline.repository.domain.scheduled.internal

import io.getstream.chat.android.client.persistance.repository.ScheduledMessageRepository
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage
import io.getstream.chat.android.models.User

/**
 * [ScheduledMessageRepository] that keeps the scheduled messages in the database.
 */
internal class DatabaseScheduledMessageRepository(
    private val scheduledMessageDao: ScheduledMessageDao,
    private val getUser: suspend (userId: String) -> User,
) : ScheduledMessageRepository {

    override suspend fun selectScheduledMessages(): List<LocalScheduledMessage> =
        scheduledMessageDao.selectAll().map { it.toModel(getUser) }

    override suspend fun insertScheduledMessage(message: LocalScheduledMessage) {
        scheduledMessageDao.insert(message.toEntity())
    }

    override suspend fun deleteScheduledMessage(id: String) {
        scheduledMessageDao.delete(id)
    }

    override suspend fun clear() {
        scheduledMessageDao.deleteAll()
    }
}
