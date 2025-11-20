/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.message.internal

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase

/**
 * A [PollDao] implementation which lazily retrieves the original [PollDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverablePollDao(private val getDatabase: () -> ChatDatabase) : PollDao {

    private val delegate: PollDao
        get() = getDatabase().pollDao()

    override suspend fun insertPolls(polls: List<PollEntity>) {
        delegate.insertPolls(polls)
    }

    override suspend fun getPoll(pollId: String): PollEntity? {
        return delegate.getPoll(pollId)
    }

    override suspend fun deletePoll(pollId: String) {
        delegate.deletePoll(pollId)
    }
}
