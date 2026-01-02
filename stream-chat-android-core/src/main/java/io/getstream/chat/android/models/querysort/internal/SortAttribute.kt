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

package io.getstream.chat.android.models.querysort.internal

import kotlin.reflect.KProperty1

/** Inner representation of sorting feature specification. */
public sealed class SortAttribute<T> {
    /** Name of attribute */
    public abstract val name: String

    /** KProperty referenced attribute. */
    public data class FieldSortAttribute<T>(val field: KProperty1<T, Comparable<*>?>, override val name: String) :
        SortAttribute<T>()

    /** Referenced by name attribute. */
    public data class FieldNameSortAttribute<T>(override val name: String) : SortAttribute<T>()
}
