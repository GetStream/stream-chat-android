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

package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistence.db.ChatClientDatabase

/**
 * Repository that aggregates all internal repositories used by [ChatClient].
 */
internal class ChatClientRepository(
    private val messageReceiptRepository: MessageReceiptRepository,
) : MessageReceiptRepository by messageReceiptRepository {

    suspend fun clear() {
        messageReceiptRepository.clearMessageReceipts()
    }

    companion object {
        fun from(database: ChatClientDatabase) = ChatClientRepository(
            messageReceiptRepository = MessageReceiptRepository(database),
        )
    }
}
