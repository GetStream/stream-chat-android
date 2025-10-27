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

package io.getstream.chat.android.client.persistence.repository

import io.getstream.chat.android.client.persistence.db.entity.MessageReceiptEntity
import io.getstream.chat.android.client.receipts.MessageReceipt

internal fun MessageReceipt.toEntity() = MessageReceiptEntity(
    messageId = messageId,
    type = type,
    createdAt = createdAt,
    cid = cid,
)

internal fun MessageReceiptEntity.toModel() = MessageReceipt(
    messageId = messageId,
    type = type,
    createdAt = createdAt,
    cid = cid,
)
