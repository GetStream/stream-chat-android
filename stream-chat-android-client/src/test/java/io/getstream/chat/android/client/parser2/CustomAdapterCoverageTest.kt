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

import io.getstream.chat.android.client.api2.endpoint.ModerationV1Api
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Guards against silently dropping custom data on the endpoints still on v1.
 *
 * Those endpoints flatten custom data to the root of the enclosing object, so a model they return that declares
 * `custom` needs a [io.getstream.chat.android.client.parser2.adapters.CustomObjectDtoAdapter] registered in
 * [MoshiChatParser] to collect it. Without one the field stays empty and the data is lost with no error.
 */
internal class CustomAdapterCoverageTest {

    @Test
    fun `every model the v1 endpoints return that declares custom is adapted`() {
        val declaring = reachableModels().filter(::declaresCustom).map { it.simpleName }.toSet()
        check(declaring.isNotEmpty()) { "Found no model declaring `custom` in the ModerationV1Api responses" }

        val unprotected = declaring - adaptedModels()

        check(unprotected.isEmpty()) {
            "These models returned by ModerationV1Api declare `custom` but have no adapter registered in " +
                "MoshiChatParser:\n" + unprotected.sorted().joinToString("\n") { "  - $it" } +
                "\n\nAdd a CustomObjectDtoAdapter (extraDataPropertyName = \"custom\") and register it."
        }
    }

    /** The response models of [ModerationV1Api] and every model their fields reach. */
    private fun reachableModels(): Set<Class<*>> {
        val seen = mutableSetOf<Class<*>>()
        val pending = ArrayDeque(ModerationV1Api::class.java.methods.flatMap { classesOf(it.genericReturnType) })
        while (pending.isNotEmpty()) {
            val model = pending.removeFirst()
            if (!model.isModel() || !seen.add(model)) continue
            model.declaredFields.filterNot { Modifier.isStatic(it.modifiers) }
                .forEach { field -> pending.addAll(classesOf(field.genericType)) }
        }
        return seen
    }

    private fun classesOf(type: Type): List<Class<*>> = when (type) {
        is Class<*> -> listOf(type)
        is ParameterizedType -> classesOf(type.rawType) + type.actualTypeArguments.flatMap(::classesOf)
        else -> emptyList()
    }

    private fun Class<*>.isModel(): Boolean = MODEL_PACKAGES.any { name.startsWith(it) }

    private fun declaresCustom(model: Class<*>): Boolean =
        model.declaredFields.any { it.isCustomMap() }

    private fun Field.isCustomMap(): Boolean = name == "custom" && Map::class.java.isAssignableFrom(type)

    private fun adaptedModels(): Set<String> {
        val registered = Regex("""\.add\((\w+)\)""").findAll(File(PARSER_FILE).readText())
            .map { it.groupValues[1] }
            .toSet()
        return File(ADAPTERS_DIR).listFiles { f -> f.extension == "kt" }.orEmpty()
            .flatMap { file -> ADAPTER_DECLARATION.findAll(file.readText()).toList() }
            .filter { it.groupValues[1] in registered }
            .map { it.groupValues[2] }
            .toSet()
    }

    private companion object {
        private const val PARSER_FILE = "src/main/java/io/getstream/chat/android/client/parser2/MoshiChatParser.kt"
        private const val ADAPTERS_DIR = "src/main/java/io/getstream/chat/android/client/parser2/adapters"

        private val MODEL_PACKAGES = listOf(
            "io.getstream.chat.android.network.models.",
            "io.getstream.chat.android.client.api2.model.",
        )

        /** Read from the type parameter, since an adapter's name need not match the model it adapts. */
        private val ADAPTER_DECLARATION = Regex("""object\s+(\w+)\s*:\s*CustomObjectDtoAdapter<(\w+)>""")
    }
}
