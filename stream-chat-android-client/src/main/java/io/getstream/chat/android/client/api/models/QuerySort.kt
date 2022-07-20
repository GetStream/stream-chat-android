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

@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.api.models.querysort.QuerySortByReflection
import io.getstream.chat.android.client.api.models.querysort.internal.SortSpecification
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Sort specification for api queries. You can specify QuerySort by referencing kotlin class property or passing field
 * name as string instance.
 * QuerySort.asc(Channel::memberCount) and QuerySort.asc<Channel>("member_count") mean the same.
 */
@Suppress("TooManyFunctions")
@Deprecated(
    message = "Use QuerySortByReflection.",
    replaceWith = ReplaceWith("QuerySortByReflection"),
    level = DeprecationLevel.ERROR
)
public class QuerySort<T : Any> : QuerySortByReflection<T>() {

    override fun add(sortSpecification: SortSpecification<T>): QuerySort<T> {
        super.add(sortSpecification)
        return this
    }

    public override fun asc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return super.asc(field) as QuerySort<T>
    }

    public override fun desc(field: KProperty1<T, Comparable<*>?>): QuerySort<T> {
        return super.desc(field) as QuerySort<T>
    }

    public override fun asc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return super.asc(fieldName, javaClass) as QuerySort<T>
    }

    public override fun desc(fieldName: String, javaClass: Class<T>): QuerySort<T> {
        return super.desc(fieldName, javaClass) as QuerySort<T>
    }

    public override fun asc(fieldName: String): QuerySort<T> {
        return super.asc(fieldName) as QuerySort<T>
    }

    public override fun desc(fieldName: String): QuerySort<T> {
        return super.desc(fieldName) as QuerySort<T>
    }

    public override fun asc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return super.asc(fieldName, kClass) as QuerySort<T>
    }

    public override fun desc(fieldName: String, kClass: KClass<T>): QuerySort<T> {
        return super.desc(fieldName, kClass) as QuerySort<T>
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
