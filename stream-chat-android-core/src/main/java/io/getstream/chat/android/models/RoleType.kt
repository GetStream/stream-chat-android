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
 * Whether a role is assignable to a user or to a channel member. Wraps the raw wire string so
 * unknown values round-trip without being lost.
 */
public data class RoleType(public val value: String) {

    public companion object {

        /** Assignable to a user. */
        public val User: RoleType = RoleType(value = "user")

        /** Assignable to a channel member. */
        public val Channel: RoleType = RoleType(value = "channel")

        public fun fromValue(value: String?): RoleType? = when (value) {
            null, "" -> null
            User.value -> User
            Channel.value -> Channel
            else -> RoleType(value)
        }
    }
}
