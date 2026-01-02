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

import io.getstream.chat.android.models.querysort.internal.SortSpecification

/**
 * Interface for the sorter of the SDK. Its implementations must provide a comparator to be used for
 * sorting collections of data in the SDK.
 */
public interface QuerySorter<T : Any> {

    /**
     * Sort specifications that compose this QuerySorter
     */
    public var sortSpecifications: List<SortSpecification<T>>

    /**
     * Comparator class.
     */
    public val comparator: Comparator<in T>

    /**
     * Converts the sorter to a DTO.
     */
    public fun toDto(): List<Map<String, Any>>

    public companion object {
        public const val KEY_DIRECTION: String = "direction"
        public const val KEY_FIELD_NAME: String = "field"
        public const val MORE_ON_COMPARISON: Int = 1
        public const val EQUAL_ON_COMPARISON: Int = 0
        public const val LESS_ON_COMPARISON: Int = -1
    }
}
