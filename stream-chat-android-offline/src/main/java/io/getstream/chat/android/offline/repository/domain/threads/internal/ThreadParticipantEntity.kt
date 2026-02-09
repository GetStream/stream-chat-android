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

package io.getstream.chat.android.offline.repository.domain.threads.internal

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Database entity for a Thread Participant.
 *
 * @param userId The ID of the user (thread participant).
 * @param threadId The ID of the thread.
 * @param createdAt The date when the user joined the thread.
 * @param lastReadAt The date when the user last read the thread.
 */
@JsonClass(generateAdapter = true)
internal data class ThreadParticipantEntity(
    val userId: String,
    val threadId: String?,
    val createdAt: Date?,
    val lastReadAt: Date?,
)
