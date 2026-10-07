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

package io.getstream.chat.android.client.parser2

import org.junit.jupiter.api.Assertions.fail
import java.lang.reflect.Modifier

/**
 * Compares every field of [expected] and [actual] and fails listing each one that differs. An empty collection on
 * one side matches null on the other: generated models default optional collections to empty or to null depending
 * on the model, and the mappers read both with `orEmpty()`.
 */
internal fun <T : Any> assertFieldsEqual(name: String, expected: T, actual: T) {
    val differences = expected.javaClass.declaredFields
        .filterNot { Modifier.isStatic(it.modifiers) }
        .onEach { it.isAccessible = true }
        .mapNotNull { field ->
            val a = field.get(expected)
            val b = field.get(actual)
            if (a == b || a.isEmptyCollection() && b == null) {
                null
            } else {
                "  ${field.name}:\n    expected = $a\n    actual   = $b"
            }
        }
    if (differences.isNotEmpty()) fail<Unit>("$name differs:\n" + differences.joinToString("\n"))
}

private fun Any?.isEmptyCollection() = this is Collection<*> && isEmpty() || this is Map<*, *> && isEmpty()
