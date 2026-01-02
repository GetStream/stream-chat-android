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

package io.getstream.chat.android.models.querysort

/** Sort order which can be ascending or descending. */
public enum class SortDirection(public val value: Int) {
    /** Descending sort order. */
    DESC(-1),

    /** Ascending sort order. */
    ASC(1),
    ;

    public companion object {
        /**
         * Returns the [SortDirection] from the number that represents the direction.
         *
         * @param value Int the number of the direction.
         */
        public fun fromNumber(value: Int): SortDirection =
            when (value) {
                1 -> ASC
                -1 -> DESC
                else -> throw IllegalArgumentException("Unsupported sort direction")
            }
    }
}
