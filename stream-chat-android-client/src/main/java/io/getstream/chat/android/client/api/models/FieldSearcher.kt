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

import io.getstream.chat.android.client.extensions.lowerCamelCaseToGetter
import io.getstream.chat.android.client.extensions.snakeToLowerCamelCase
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

internal class FieldSearcher {

    internal fun <T : Any> findProperty(
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

    internal fun <T : Any> findMemberProperty(
        fieldName: String,
        kClass: KClass<T>,
    ): KProperty1<T, Comparable<*>?>? {
        val members = kClass.memberProperties.filterIsInstance<KProperty1<T, Comparable<*>?>>()
        val camelCaseName = fieldName.snakeToLowerCamelCase()

        return members.firstOrNull { property -> property.name == camelCaseName }
    }
}
