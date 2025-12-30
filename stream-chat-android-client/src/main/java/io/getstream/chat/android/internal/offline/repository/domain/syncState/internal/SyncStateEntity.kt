/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.internal.offline.repository.domain.syncState.internal

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = SYNC_STATE_ENTITY_TABLE_NAME)
internal data class SyncStateEntity(
    @PrimaryKey var userId: String,
    var activeChannelIds: List<String> = mutableListOf(),
    var lastSyncedAt: Date? = null,
    var rawLastSyncedAt: String? = null,
    var markedAllReadAt: Date? = null,
)

internal const val SYNC_STATE_ENTITY_TABLE_NAME = "stream_sync_state"
