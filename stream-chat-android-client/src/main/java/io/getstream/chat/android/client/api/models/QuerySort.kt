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

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.api.models.querysort.CompositeComparator
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.api.models.querysort.QuerySorter.Companion.KEY_DIRECTION
import io.getstream.chat.android.client.api.models.querysort.QuerySorter.Companion.KEY_FIELD_NAME
import io.getstream.chat.android.client.api.models.querysort.SortAttribute
import io.getstream.chat.android.client.api.models.querysort.SortDirection
import io.getstream.chat.android.client.api.models.querysort.SortSpecification
import io.getstream.chat.android.client.extensions.camelCaseToSnakeCase
import io.getstream.logging.StreamLog
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Sort specification for api queries. You can specify QuerySort by referencing kotlin class property or passing field
 * name as string instance.
 * QuerySort.asc(Channel::memberCount) and QuerySort.asc<Channel>("member_count") mean the same.
 */
@Suppress("TooManyFunctions")
public open class QuerySort<T : Any> : QuerySorter<T> {
    private val logger = StreamLog.getLogger("QuerySort")

    private var sortSpecifications: List<SortSpecification<T>> = emptyList()
    private val fieldSearcher: FieldSearcher = FieldSearcher()

    /** Composite comparator based on sort attributes. */
    public override val comparator: Comparator<in T>
        get() = CompositeComparator(sortSpecifications.map { it.comparator })

    private val SortSpecification<T>.comparator: Comparator<T>
        get() {
            return when (this.sortAttribute) {
                is SortAttribute.FieldSortAttribute<T> -> this.sortAttribute.field.comparator(this.sortDirection)

                is SortAttribute.FieldNameSortAttribute -> this.sortAttribute.name.comparator(this.sortDirection)
            }
        }

    private fun add(sortSpecification: SortSpecification<T>): QuerySort<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public open fun asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldSortAttribute(field, field.name.camelCaseToSnakeCase()),
                SortDirection.ASC
            )
        )
    }

    public open fun desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldSortAttribute(
                    field,
                    field.name.camelCaseToSnakeCase()
                ),
                SortDirection.DESC
            )
        )
    }

    public open fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.ASC))
    }

    public open fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.DESC))
    }

    public open fun asc(fieldName: String): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldNameSortAttribute(fieldName),
                SortDirection.ASC
            )
        )
    }

    public open fun desc(fieldName: String): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldNameSortAttribute(fieldName),
                SortDirection.DESC
            )
        )
    }

    public open fun asc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.ASC))
    }

    public open fun desc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.DESC))
    }

    override fun toDto(): List<Map<String, Any>> = sortSpecifications.map { sortSpec ->
        listOf(KEY_FIELD_NAME to sortSpec.sortAttribute.name, KEY_DIRECTION to sortSpec.sortDirection.value).toMap()
    }

    internal fun toList(): List<Pair<String, SortDirection>> =
        sortSpecifications.map { it.sortAttribute.name to it.sortDirection }

    private fun getSortFeature(fieldName: String, javaClass: Class<T>): SortAttribute<T> {
        @Suppress("UNCHECKED_CAST")
        val kClass = Reflection.createKotlinClass(javaClass) as KClass<T>
        return getSortFeature(fieldName, kClass)
    }

    private fun getSortFeature(fieldName: String, kClass: KClass<T>): SortAttribute<T> {
        return fieldSearcher.findComparableMemberProperty(fieldName, kClass)
            ?.let { FieldSortAttribute(it, fieldName) }
            .also { fieldSortAttribute ->
                logger.d { "[getSortFeature] A field to sort was found. Using field: $fieldSortAttribute" }
            }
            ?: SortAttribute.FieldNameSortAttribute(fieldName)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuerySort<*>

        if (sortSpecifications != other.sortSpecifications) return false

        return true
    }

    override fun hashCode(): Int {
        return sortSpecifications.hashCode()
    }

    override fun toString(): String {
        return sortSpecifications.toString()
    }

    public companion object {
        public inline fun <reified T : Any> QuerySort<T>.ascByName(
            fieldName: String
        ): QuerySort<T> = asc(fieldName, T::class)

        public inline fun <reified T : Any> QuerySort<T>.descByName(
            fieldName: String
        ): QuerySort<T> = desc(fieldName, T::class)

        public inline fun <reified T : Any> asc(fieldName: String): QuerySort<T> =
            QuerySort<T>().ascByName(fieldName)

        public inline fun <reified R : Any> desc(fieldName: String): QuerySort<R> =
            QuerySort<R>().descByName(fieldName)

        public fun <T : Any> asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> =
            QuerySort<T>().asc(field)

        public fun <T : Any> desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> =
            QuerySort<T>().desc(field)
    }
}
