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

package io.getstream.chat.android.compose.state.channels.list

/**
 * Represent the search query.
 *
 * @property query The query to search for.
 */
public sealed class SearchQuery {
    public abstract val query: String

    /**
     * Represents the search query for channels.
     */
    public data class Channels(override val query: String) : SearchQuery()

    /**
     * Represents the search query for messages.
     */
    public data class Messages(override val query: String) : SearchQuery()

    /**
     * Represents an empty search query.
     */
    public data object Empty : SearchQuery() {
        override val query: String = ""
    }
}
