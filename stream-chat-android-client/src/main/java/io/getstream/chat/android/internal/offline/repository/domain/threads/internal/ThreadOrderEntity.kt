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

package io.getstream.chat.android.internal.offline.repository.domain.threads.internal

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model holding info about the ordering in which the offline threads should be displayed.
 *
 */
@Entity(tableName = THREAD_ORDER_ENTITY_TABLE_NAME)
internal class ThreadOrderEntity(
    @PrimaryKey val id: String,
    val order: List<String>,
)

internal const val THREAD_ORDER_ENTITY_TABLE_NAME = "stream_chat_thread_order"
