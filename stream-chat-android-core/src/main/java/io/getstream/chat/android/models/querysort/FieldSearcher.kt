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

import io.getstream.chat.android.extensions.lowerCamelCaseToGetter
import io.getstream.chat.android.extensions.snakeToLowerCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

internal class FieldSearcher {

    internal fun <T : Any> findComparableMemberProperty(
        fieldName: String,
        kClass: KClass<T>,
    ): KProperty1<T, Comparable<*>?>? {
        val members = kClass.members.filterIsInstance<KProperty1<T, Comparable<*>?>>()
        val camelCaseName = fieldName.snakeToLowerCamelCase()
        val getField = camelCaseName.lowerCamelCaseToGetter()

        return members.firstOrNull { property ->
            property.name == camelCaseName || property.name == getField
        }
    }

    internal fun findComparable(
        any: Any,
        fieldName: String,
    ): Comparable<Any>? {
        val members = any::class.members
        val camelCaseName = fieldName.snakeToLowerCamelCase()
        val getField = camelCaseName.lowerCamelCaseToGetter()

        return members.firstOrNull { property ->
            property.name == camelCaseName || property.name == getField
        }?.call(any) as? Comparable<Any>
    }
}
