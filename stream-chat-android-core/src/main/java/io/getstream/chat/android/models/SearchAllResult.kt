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
 * The result of a unified search across messages, users and channels.
 *
 * A section is null when it was not requested.
 *
 * @param messages The messages section.
 * @param users The users section.
 * @param channels The channels section. Its channels carry no messages and up to 4 members.
 */
@Immutable
public data class SearchAllResult(
    val messages: SearchAllSection<Message>? = null,
    val users: SearchAllSection<User>? = null,
    val channels: SearchAllSection<Channel>? = null,
)

/**
 * One section of a [SearchAllResult].
 *
 * @param items The results of this page. Empty when [error] is set.
 * @param next The cursor of the next page, or null when every result is loaded. Pass it back with the same query.
 * The channels section can give a cursor whose page is empty, when the last page was full.
 * @param error Set when the search of this section failed. The other sections are not affected.
 * @param isCursorRejected True when the cursor passed for this section was rejected, so [items] is the first page.
 * @param resultsWarning Set only on the messages section, when the search covered only part of the channels.
 */
@Immutable
public data class SearchAllSection<T>(
    val items: List<T> = emptyList(),
    val next: String? = null,
    val error: SearchAllSectionError? = null,
    val isCursorRejected: Boolean = false,
    val resultsWarning: SearchWarning? = null,
)

/**
 * The reason a section of a [SearchAllResult] failed.
 *
 * @param code The error code, for example [BACKEND_TIMEOUT].
 * @param message A description of the failure.
 */
@Immutable
public data class SearchAllSectionError(
    val code: String,
    val message: String,
) {
    public companion object {
        /** The search did not finish in time. Retrying can succeed. */
        public const val BACKEND_TIMEOUT: String = "backend_timeout"

        /** The search could not run because of load. Retrying later can succeed. */
        public const val BACKEND_UNAVAILABLE: String = "backend_unavailable"

        /** The app's configuration cannot serve this section. Retrying does not help. */
        public const val UNSUPPORTED: String = "unsupported"

        /** An unexpected server error. Retrying does not help. */
        public const val INTERNAL_ERROR: String = "internal_error"
    }
}
