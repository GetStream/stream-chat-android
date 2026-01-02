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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * DB entity holding limited data about a reminder.
 *
 * @property remindAt The date when the user should be reminded about this message. If null, this is a bookmark type
 * reminder without a notification.
 * @property createdAt Date when the reminder was created.
 * @property updatedAt Date when the reminder was last updated.
 */
@JsonClass(generateAdapter = true)
internal data class ReminderInfoEntity(
    val remindAt: Date?,
    val createdAt: Date,
    val updatedAt: Date,
)
