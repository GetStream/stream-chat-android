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

package io.getstream.chat.android.extensions

private val snakeRegex = "_[a-zA-Z]".toRegex()
private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

/**
 * Converts string written in snake case to String in camel case with the first symbol in lower case.
 * For example string "created_at_some_time" is converted to "createdAtSomeTime".
 */
internal fun String.snakeToLowerCamelCase(): String {
    return snakeRegex.replace(this) { matchResult ->
        matchResult.value.replace("_", "").uppercase()
    }
}

/**
 * Converts string written in lower camel case to a getter method name.
 * For example string "createdAtSomeTime" is converted to "getCreatedAtSomeTime".
 */
internal fun String.lowerCamelCaseToGetter(): String = "get${this[0].uppercase()}${this.substring(1)}"

/**
 * Converts String written in camel case to String in snake case.
 * For example string "createdAtSomeTime" is converted to "created_at_some_time".
 */
internal fun String.camelCaseToSnakeCase(): String {
    return camelRegex.replace(this) { "_${it.value}" }.lowercase()
}
