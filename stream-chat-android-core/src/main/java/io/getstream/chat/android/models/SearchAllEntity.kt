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

/**
 * The sections a unified search can return.
 */
public object SearchAllEntity {
    /** Messages whose text matches the query, newest first. */
    public const val MESSAGES: String = "messages"

    /** Users whose name or username starts with the query. */
    public const val USERS: String = "users"

    /** Channels the current user is a member of whose name starts with the query. */
    public const val CHANNELS: String = "channels"
}
