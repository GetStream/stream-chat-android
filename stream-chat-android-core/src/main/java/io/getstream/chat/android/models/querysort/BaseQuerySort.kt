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

import io.getstream.chat.android.models.querysort.internal.CompositeComparator
import io.getstream.chat.android.models.querysort.internal.SortAttribute
import io.getstream.chat.android.models.querysort.internal.SortSpecification

/**
 * Base class for implementing [QuerySorter]. This class holds common code for [QuerySortByField] and
 * [QuerySortByReflection].
 */
public abstract class BaseQuerySort<T : Any> : QuerySorter<T> {

    override var sortSpecifications: List<SortSpecification<T>> = emptyList()

    /**
     * Comparator class that will be generator by the sort specifications.
     */
    override val comparator: Comparator<in T>
        get() = CompositeComparator(sortSpecifications.map { it.comparator })

    private val SortSpecification<T>.comparator: Comparator<T>
        get() {
            return when (this.sortAttribute) {
                is SortAttribute.FieldSortAttribute<T> -> comparatorFromFieldSort(this.sortAttribute, sortDirection)

                is SortAttribute.FieldNameSortAttribute<T> ->
                    comparatorFromNameAttribute(this.sortAttribute, sortDirection)
            }
        }

    /**
     * Comparator from [SortAttribute.FieldSortAttribute]
     */
    public abstract fun comparatorFromFieldSort(
        firstSort: SortAttribute.FieldSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T>

    /**
     * Comparator from [SortAttribute.FieldNameSortAttribute]
     */
    public abstract fun comparatorFromNameAttribute(
        name: SortAttribute.FieldNameSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T>

    public override fun toDto(): List<Map<String, Any>> = sortSpecifications.map { sortSpec ->
        listOf(
            QuerySorter.KEY_FIELD_NAME to sortSpec.sortAttribute.name,
            QuerySorter.KEY_DIRECTION to sortSpec.sortDirection.value,
        ).toMap()
    }

    override fun hashCode(): Int = sortSpecifications.hashCode()

    override fun toString(): String = sortSpecifications.toString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseQuerySort<*>

        if (sortSpecifications != other.sortSpecifications) return false

        return true
    }
}
