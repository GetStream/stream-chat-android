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

import org.junit.jupiter.api.Test
import java.io.File

/**
 * Guards against silently dropping custom data.
 *
 * The v1 endpoints flatten custom data to the root of the enclosing object, so a generated model that
 * declares `custom` needs a [io.getstream.chat.android.client.parser2.adapters.CustomObjectDtoAdapter]
 * registered in [MoshiChatParser] to collect it. Without one the field stays empty and the data is lost
 * with no error, which is how member `extraData` was dropped by the queryMembers slice.
 *
 * Models whose `custom` is genuinely a nested object on the wire (the Go struct declares a plain map
 * rather than `jsonextra.ExtraFields`) must be listed in [NESTED_CUSTOM] with a reason instead.
 */
internal class CustomAdapterCoverageTest {

    @Test
    fun `every generated model declaring custom is either adapted or documented as nested`() {
        val declaring = modelsDeclaringCustom()
        check(declaring.isNotEmpty()) { "Found no generated models declaring `custom`; is $MODELS_DIR correct?" }

        val unprotected = declaring - adaptedModels() - NESTED_CUSTOM.keys

        check(unprotected.isEmpty()) {
            "These generated models declare `custom` but have no adapter registered in MoshiChatParser:\n" +
                unprotected.sorted().joinToString("\n") { "  - $it" } +
                "\n\nAdd a CustomObjectDtoAdapter (extraDataPropertyName = \"custom\") and register it, or " +
                "list the model in NESTED_CUSTOM if its custom really is a nested object on the wire."
        }
    }

    private fun modelsDeclaringCustom(): Set<String> =
        File(MODELS_DIR).listFiles { f -> f.extension == "kt" }.orEmpty()
            .filter { it.readText().contains(CUSTOM_PROPERTY) }
            .map { it.nameWithoutExtension }
            .toSet()

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
        private const val MODELS_DIR = "src/main/java/io/getstream/chat/android/network/models"
        private const val PARSER_FILE = "src/main/java/io/getstream/chat/android/client/parser2/MoshiChatParser.kt"
        private const val ADAPTERS_DIR = "src/main/java/io/getstream/chat/android/client/parser2/adapters"

        /** Read from the type parameter, since an adapter's name need not match the model it adapts. */
        private val ADAPTER_DECLARATION = Regex("""object\s+(\w+)\s*:\s*CustomObjectDtoAdapter<(\w+)>""")

        /** The colon matters: without it this also matches unrelated properties like `customEvents`. */
        private const val CUSTOM_PROPERTY = "internal val custom:"

        /** Models whose `custom` is a nested object on the wire, so no collecting adapter applies. */
        private val NESTED_CUSTOM = mapOf(
            "ThreadParticipant" to "Go declares a plain map, not jsonextra.ExtraFields, so custom stays nested",
        )
    }
}
