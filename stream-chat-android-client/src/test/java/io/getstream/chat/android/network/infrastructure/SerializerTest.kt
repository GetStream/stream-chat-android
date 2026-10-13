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

package io.getstream.chat.android.network.infrastructure

import com.squareup.moshi.JsonAdapter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.net.JarURLConnection

internal class SerializerTest {

    @Test
    fun `Every generated enum parses through Serializer`() {
        val enums = generatedEnumTypes()
        assertTrue(enums.isNotEmpty()) { "no generated enums found" }

        // A value no enum declares: the registered adapter maps it to the enum's Unknown case. Without the
        // adapter, Moshi has no way to build the sealed class at all.
        val failures = enums.mapNotNull { type ->
            val parsed = runCatching { Serializer.moshi.adapter(type).fromJson("\"not-a-declared-value\"") }
            parsed.exceptionOrNull()?.let { "${type.name}: $it" }
                ?: "${type.name}: parsed to null".takeIf { parsed.getOrNull() == null }
        }
        assertTrue(failures.isEmpty()) { failures.joinToString("\n") }
    }

    /** The sealed types in the generated models package that declare a nested [JsonAdapter], as the enums do. */
    private fun generatedEnumTypes(): List<Class<*>> {
        val packagePath = "io/getstream/chat/android/network/models/"
        val location = javaClass.classLoader!!.getResource(packagePath)!!
        val classFiles = when (val connection = location.openConnection()) {
            is JarURLConnection -> connection.jarFile.entries().asSequence().map { it.name }
                .filter { it.startsWith(packagePath) && it.count { c -> c == '/' } == packagePath.count { c -> c == '/' } }
                .toList()
            else -> File(location.toURI()).list().orEmpty().map { packagePath + it }
        }
        return classFiles
            .filter { it.endsWith("Adapter.class") }
            .map { Class.forName(it.removeSuffix(".class").replace('/', '.')) }
            .filter { JsonAdapter::class.java.isAssignableFrom(it) }
            .mapNotNull { it.enclosingClass }
            .filter { it.kotlin.isSealed }
            .distinct()
    }
}
