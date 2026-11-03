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

package io.getstream.chat.android.ai.openui.library

/** Structured form validation rules: `{required: true, email: true, minLength: 8, ...}`. */
internal val PropType.Companion.validationRules: PropType
    get() = objectOf(
        listOf("required", "email", "url", "numeric").map { PropField.optional(it, boolean) } +
            listOf("min", "max", "minLength", "maxLength").map { PropField.optional(it, number) } +
            PropField.optional("pattern", string),
    )

// The short DSL the built-in library definitions are written in.

internal val string: PropType get() = PropType.string
internal val number: PropType get() = PropType.number
internal val boolean: PropType get() = PropType.boolean
internal val any: PropType get() = PropType.any
internal val action: PropType get() = PropType.action
internal val validationRules: PropType get() = PropType.validationRules

internal fun enumeration(values: List<String>): PropType = PropType.enumeration(values)
internal fun literal(value: String): PropType = PropType.literal(value)
internal fun array(items: PropType, minItems: Int? = null): PropType = PropType.array(items, minItems)
internal fun obj(fields: List<PropField>): PropType = PropType.objectOf(fields)
internal fun record(value: PropType): PropType = PropType.record(value)
internal fun component(name: String): PropType = PropType.component(name)
internal fun components(names: List<String>): PropType = PropType.components(names)
internal fun union(options: List<PropType>): PropType = PropType.union(options)
internal fun required(name: String, type: PropType): PropField = PropField.required(name, type)
internal fun optional(name: String, type: PropType): PropField = PropField.optional(name, type)
internal fun defaulted(name: String, type: PropType, defaultValue: Any?): PropField =
    PropField.defaulted(name, type, defaultValue)
internal fun binding(name: String, type: PropType): PropField = PropField.reactive(name, type)
