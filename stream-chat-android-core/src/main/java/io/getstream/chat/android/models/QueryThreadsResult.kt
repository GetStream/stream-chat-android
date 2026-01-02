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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Model representing the result of a 'Query Threads' operation.
 *
 * @param threads: The list of threads.
 * @param prev: The identifier for the previous page of threads.
 * @param next: The identifier for the next page of threads.
 */
@Immutable
public data class QueryThreadsResult(
    val threads: List<Thread>,
    val prev: String?,
    val next: String?,
)
