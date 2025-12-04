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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase

/**
 * A [ThreadOrderDao] implementation which lazily retrieves the original [ThreadOrderDao] from the currently active
 * [ChatDatabase] instance. The [ChatDatabase] instance can change in runtime if it becomes corrupted
 * and is manually recreated.
 *
 * @param getDatabase Method retrieving the current instance of [ChatDatabase].
 */
internal class RecoverableThreadOrderDao(private val getDatabase: () -> ChatDatabase) : ThreadOrderDao {

    private val delegate: ThreadOrderDao
        get() = getDatabase().threadOrderDao()

    override suspend fun insertThreadOrder(order: ThreadOrderEntity) {
        delegate.insertThreadOrder(order)
    }

    override suspend fun selectThreadOrder(id: String): ThreadOrderEntity? {
        return delegate.selectThreadOrder(id)
    }

    override suspend fun deleteAll() {
        delegate.deleteAll()
    }
}
