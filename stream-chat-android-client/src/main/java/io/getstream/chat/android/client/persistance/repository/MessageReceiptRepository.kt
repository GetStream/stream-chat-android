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

package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.MessageReceipt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@InternalStreamChatApi
public interface MessageReceiptRepository {

    public suspend fun upsert(receipts: List<MessageReceipt>) { /* no-op */ }

    public fun getAllByType(type: String, limit: Int): Flow<List<MessageReceipt>> = emptyFlow()

    public suspend fun deleteByMessageIds(messageIds: List<String>) { /* no-op */ }

    public suspend fun clear() { /* no-op */ }
}
