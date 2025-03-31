/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.domain.poll.internal

import io.getstream.chat.android.client.persistance.repository.PollRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.PollDao

/**
 * Implementation of the [PollRepository] backed by a local database.
 *
 * @param pollDao The DAO to perform operations on the local database.
 */
internal class DatabasePollRepository(private val pollDao: PollDao) : PollRepository {

    override suspend fun deletePoll(pollId: String) {
        pollDao.deletePoll(pollId)
    }
}
