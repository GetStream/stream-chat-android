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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Model representing the result of a 'Query Message Reminders' operation.
 *
 * @property reminders The list of message reminders matching the query.
 * @property next The pagination token for fetching the next set of results.
 */
@Immutable
public data class QueryRemindersResult(
    val reminders: List<MessageReminder>,
    val next: String?,
)
