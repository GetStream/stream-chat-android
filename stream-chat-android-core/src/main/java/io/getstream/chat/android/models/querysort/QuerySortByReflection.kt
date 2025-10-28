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

package io.getstream.chat.android.models.querysort

import io.getstream.chat.android.extensions.camelCaseToSnakeCase
import io.getstream.chat.android.models.CustomObject
import io.getstream.chat.android.models.querysort.internal.SortAttribute
import io.getstream.chat.android.models.querysort.internal.SortSpecification
import io.getstream.chat.android.models.querysort.internal.compare
import io.getstream.log.taggedLogger
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Sort specification for api queries. You can specify QuerySortByReflection by referencing kotlin class property
 * or passing field name as string instance.
 * QuerySortByReflection.asc(Channel::memberCount) and QuerySortByReflection.asc<Channel>("member_count") mean the same.
 */
@Suppress("TooManyFunctions")
public open class QuerySortByReflection<T : Any> : BaseQuerySort<T>() {
    private val logger by taggedLogger("QuerySort")

    private val fieldSearcher: FieldSearcher = FieldSearcher()

    /**
     * Comparator from [SortAttribute.FieldSortAttribute]
     */
    override fun comparatorFromFieldSort(
        firstSort: SortAttribute.FieldSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T> = firstSort.field.comparator(sortDirection)

    /**
     * Comparator from [SortAttribute.FieldNameSortAttribute]
     */
    override fun comparatorFromNameAttribute(
        name: SortAttribute.FieldNameSortAttribute<T>,
        sortDirection: SortDirection,
    ): Comparator<T> = name.name.comparator(sortDirection)

    @Suppress("UNCHECKED_CAST")
    private fun KProperty1<T, Comparable<*>?>.comparator(sortDirection: SortDirection): Comparator<T> = this.let { compareProperty ->
        Comparator { c0, c1 ->
            compare(
                (compareProperty.getter.call(c0) as? Comparable<Any>),
                (compareProperty.getter.call(c1) as? Comparable<Any>),
                sortDirection,
            )
        }
    }

    private fun String.comparator(sortDirection: SortDirection): Comparator<T> = Comparator { o1, o2 ->
        compare(
            comparableFromExtraData(o1, this) ?: fieldSearcher.findComparable(o1, this),
            comparableFromExtraData(o2, this) ?: fieldSearcher.findComparable(o2, this),
            sortDirection,
        )
    }

    private fun comparableFromExtraData(any: Any, field: String): Comparable<Any>? = (any as? CustomObject)?.extraData?.get(field) as? Comparable<Any>

    internal open fun add(sortSpecification: SortSpecification<T>): QuerySortByReflection<T> {
        sortSpecifications = sortSpecifications + sortSpecification
        return this
    }

    /**
     * Creates a [QuerySortByReflection] with ASC as [SortDirection]
     */
    public open fun asc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> = add(
        SortSpecification(
            SortAttribute.FieldSortAttribute(field, field.name.camelCaseToSnakeCase()),
            SortDirection.ASC,
        ),
    )

    /**
     * Creates a [QuerySortByReflection] with DESC as [SortDirection]
     */
    public open fun desc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> = add(
        SortSpecification(
            SortAttribute.FieldSortAttribute(
                field,
                field.name.camelCaseToSnakeCase(),
            ),
            SortDirection.DESC,
        ),
    )

    /**
     * Creates a [QuerySortByReflection] with ASC as [SortDirection]
     */
    public open fun asc(fieldName: String, javaClass: Class<T>): QuerySortByReflection<T> = add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.ASC))

    /**
     * Creates a [QuerySortByReflection] with DESC as [SortDirection]
     */
    public open fun desc(fieldName: String, javaClass: Class<T>): QuerySortByReflection<T> = add(SortSpecification(getSortFeature(fieldName, javaClass), SortDirection.DESC))

    /**
     * Creates a [QuerySortByReflection] with ASC as [SortDirection]
     */
    public open fun asc(fieldName: String): QuerySortByReflection<T> = add(
        SortSpecification(
            SortAttribute.FieldNameSortAttribute(fieldName),
            SortDirection.ASC,
        ),
    )

    /**
     * Creates a [QuerySortByReflection] with DESC as [SortDirection]
     */
    public open fun desc(fieldName: String): QuerySortByReflection<T> = add(
        SortSpecification(
            SortAttribute.FieldNameSortAttribute(fieldName),
            SortDirection.DESC,
        ),
    )

    /**
     * Creates a [QuerySortByReflection] with ASC as [SortDirection]
     */
    public open fun asc(fieldName: String, kClass: KClass<T>): QuerySortByReflection<T> = add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.ASC))

    /**
     * Creates a [QuerySortByReflection] with DESC as [SortDirection]
     */
    public open fun desc(fieldName: String, kClass: KClass<T>): QuerySortByReflection<T> = add(SortSpecification(getSortFeature(fieldName, kClass), SortDirection.DESC))

    internal fun toList(): List<Pair<String, SortDirection>> = sortSpecifications.map { it.sortAttribute.name to it.sortDirection }

    private fun getSortFeature(fieldName: String, javaClass: Class<T>): SortAttribute<T> {
        @Suppress("UNCHECKED_CAST")
        val kClass = Reflection.createKotlinClass(javaClass) as KClass<T>
        return getSortFeature(fieldName, kClass)
    }

    private fun getSortFeature(fieldName: String, kClass: KClass<T>): SortAttribute<T> = fieldSearcher.findComparableMemberProperty(fieldName, kClass)
        ?.let { SortAttribute.FieldSortAttribute(it, fieldName) }
        .also { fieldSortAttribute ->
            logger.d { "[getSortFeature] A field to sort was found. Using field: $fieldSortAttribute" }
        }
        ?: SortAttribute.FieldNameSortAttribute(fieldName)

    public companion object {
        /**
         * Adds a field to [QuerySortByReflection] using the name of field in the direction ASC.
         *
         * @param fieldName Field name.
         */
        public inline fun <reified T : Any> QuerySortByReflection<T>.ascByName(
            fieldName: String,
        ): QuerySortByReflection<T> = asc(fieldName, T::class)

        /**
         * Adds a field to [QuerySortByReflection] using the name of field in the direction DESC.
         *
         * @param fieldName Field name.
         */
        public inline fun <reified T : Any> QuerySortByReflection<T>.descByName(
            fieldName: String,
        ): QuerySortByReflection<T> = desc(fieldName, T::class)

        /**
         * Creates a [QuerySortByReflection] using the name of field in the direction ASC.
         *
         * @param fieldName Field name.
         */
        public inline fun <reified T : Any> asc(fieldName: String): QuerySortByReflection<T> = QuerySortByReflection<T>().ascByName(fieldName)

        /**
         * Creates a [QuerySortByReflection] using the name of field in the direction DESC.
         *
         * @param fieldName Field name.
         */
        public inline fun <reified R : Any> desc(fieldName: String): QuerySortByReflection<R> = QuerySortByReflection<R>().descByName(fieldName)

        /**
         * Creates a [QuerySortByReflection] using the property of field in the direction ASC.
         *
         * @param field [KProperty1] from the class.
         */
        public fun <T : Any> asc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> = QuerySortByReflection<T>().asc(field)

        /**
         * Creates a [QuerySortByReflection] using the property of field in the direction DESC.
         *
         * @param field [KProperty1] from the class.
         */
        public fun <T : Any> desc(field: KProperty1<T, Comparable<*>?>): QuerySortByReflection<T> = QuerySortByReflection<T>().desc(field)
    }
}
