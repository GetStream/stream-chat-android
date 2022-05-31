/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api.models.querysort

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.api.models.querysort.QuerySorter.Companion.KEY_DIRECTION
import io.getstream.chat.android.client.api.models.querysort.QuerySorter.Companion.KEY_FIELD_NAME
import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

private const val DONT_USE_ASC_METHOD_MESSAGE = "Don't use this version of asc. Use asc(fieldName: String) instead."
private const val DONT_USE_DESC_METHOD_MESSAGE = "Don't use this version of desc. Use desc(fieldName: String) instead."

public class QuerySortByMap<T : QueryableByMap> : QuerySorter<T>, QuerySort<T>() {

    private var sortSpecifications: List<SortSpecification<T>> = emptyList()

    override val comparator: Comparator<in T>
        get() = CompositeComparator(sortSpecifications.map { it.comparator })

    override fun toDto(): List<Map<String, Any>> = sortSpecifications.map { sortSpec ->
        listOf(KEY_FIELD_NAME to sortSpec.sortAttribute.name, KEY_DIRECTION to sortSpec.sortDirection.value).toMap()
    }

    private val SortSpecification<T>.comparator: Comparator<T>
        get() {
            return when (this.sortAttribute) {
                is SortAttribute.FieldSortAttribute<T> -> throw IllegalArgumentException(
                    "FieldSortAttribute can't be used with QuerySortByMap"
                )

                is SortAttribute.FieldNameSortAttribute<T> -> this.sortAttribute.name.comparator(this.sortDirection)
            }
        }

    private fun String.comparator(sortDirection: SortDirection): Comparator<T> =
        Comparator { o1, o2 ->
            val field = this.snakeToLowerCamelCase()

            val fieldOne = o1.toMap()[field] as? Comparable<Any>
            val fieldTwo = o2.toMap()[field] as? Comparable<Any>

            compare(fieldOne, fieldTwo, sortDirection)
        }

    override fun hashCode(): Int {
        return sortSpecifications.hashCode()
    }

    override fun toString(): String {
        return sortSpecifications.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuerySortByMap<*>

        if (sortSpecifications != other.sortSpecifications) return false

        return true
    }

    private fun add(sortSpecification: SortSpecification<T>): QuerySortByMap<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    override fun asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        throw IllegalArgumentException(DONT_USE_ASC_METHOD_MESSAGE)
    }

    override fun desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        throw IllegalArgumentException(DONT_USE_DESC_METHOD_MESSAGE)
    }

    override fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        throw IllegalArgumentException(DONT_USE_ASC_METHOD_MESSAGE)
    }

    override fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        throw IllegalArgumentException(DONT_USE_DESC_METHOD_MESSAGE)
    }

    override fun asc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        throw IllegalArgumentException(DONT_USE_ASC_METHOD_MESSAGE)
    }

    override fun desc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        throw IllegalArgumentException(DONT_USE_DESC_METHOD_MESSAGE)
    }

    override fun asc(fieldName: String): QuerySortByMap<T> {
        return add(SortSpecification(SortAttribute.FieldNameSortAttribute(fieldName), SortDirection.ASC))
    }

    override fun desc(fieldName: String): QuerySortByMap<T> {
        return add(SortSpecification(SortAttribute.FieldNameSortAttribute(fieldName), SortDirection.DESC))
    }

    public companion object {
        public fun <R : QueryableByMap> ascByName(fieldName: String): QuerySortByMap<R> =
            QuerySortByMap<R>().asc(fieldName)

        public fun <R : QueryableByMap> descByName(fieldName: String): QuerySortByMap<R> =
            QuerySortByMap<R>().desc(fieldName)

        public fun <R : QueryableByMap> QuerySortByMap<R>.ascByName(fieldName: String): QuerySortByMap<R> =
            asc(fieldName)

        public fun <R : QueryableByMap> QuerySortByMap<R>.descByName(fieldName: String): QuerySortByMap<R> =
            desc(fieldName)
    }
}
