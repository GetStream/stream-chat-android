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

import io.getstream.chat.android.client.api.models.querysort.BaseQuerySort
import io.getstream.chat.android.client.api.models.querysort.SortDirection
import io.getstream.chat.android.client.api.models.querysort.compare
import io.getstream.chat.android.client.api.models.querysort.internal.SortAttribute
import io.getstream.chat.android.client.api.models.querysort.internal.SortSpecification
import io.getstream.chat.android.client.extensions.camelCaseToSnakeCase
import io.getstream.chat.android.client.models.CustomObject
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
public class QuerySort<T : Any> : BaseQuerySort<T>() {
    private val logger = StreamLog.getLogger("QuerySort")

    private val fieldSearcher: FieldSearcher = FieldSearcher()

    override fun comparatorFromFieldSort(
        firstSort: SortAttribute.FieldSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T> =
        firstSort.field.comparator(sortDirection)

    override fun comparatorFromNameAttribute(
        name: SortAttribute.FieldNameSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T> =
        name.name.comparator(sortDirection)

    @Suppress("UNCHECKED_CAST")
    private fun KProperty1<T, Comparable<*>?>.comparator(sortDirection: SortDirection): Comparator<T> =
        this.let { compareProperty ->
            Comparator { c0, c1 ->
                compare(
                    (compareProperty.getter.call(c0) as? Comparable<Any>),
                    (compareProperty.getter.call(c1) as? Comparable<Any>),
                    sortDirection
                )
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun String.comparator(sortDirection: SortDirection): Comparator<T> =
        Comparator { o1, o2 ->
            compare(
                comparableFromExtraData(o1, this) ?: fieldSearcher.findComparable(o1, this),
                comparableFromExtraData(o2, this) ?: fieldSearcher.findComparable(o2, this),
                sortDirection
            )
        }

    private fun comparableFromExtraData(any: Any, field: String): Comparable<Any>? {
        return (any as? CustomObject)?.extraData?.get(field) as? Comparable<Any>
    }

    private fun add(sortSpecification: SortSpecification<T>): QuerySort<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    public fun asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldSortAttribute(field, field.name.camelCaseToSnakeCase()),
                SortDirection.ASC
            )
        )
    }

    public fun desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
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

    public fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.ASC))
    }

    public fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.DESC))
    }

    public fun asc(fieldName: String): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldNameSortAttribute(fieldName),
                SortDirection.ASC
            )
        )
    }

    public fun desc(fieldName: String): QuerySort<T> {
        return add(
            SortSpecification(
                SortAttribute.FieldNameSortAttribute(fieldName),
                SortDirection.DESC
            )
        )
    }

    public fun asc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.ASC))
    }

    public fun desc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.DESC))
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
            ?.let { SortAttribute.FieldSortAttribute(it, fieldName) }
            .also { fieldSortAttribute ->
                logger.d { "[getSortFeature] A field to sort was found. Using field: $fieldSortAttribute" }
            }
            ?: SortAttribute.FieldNameSortAttribute(fieldName)
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
