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

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

@JsonClass(generateAdapter = true)
internal data class ManualStyleDto(
    val name: String,
    val weight: Int,
    val extraData: Map<String, Any>,
)

@JsonClass(generateAdapter = true)
internal data class GeneratedStyleDto(
    @Json(name = "first_name") val firstName: String,
    @Json(name = "weight_kg") val weightKg: Int,
    val extraData: Map<String, Any>,
)

private object ManualStyleAdapter : CustomObjectDtoAdapter<ManualStyleDto>(ManualStyleDto::class) {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<ManualStyleDto>,
    ): ManualStyleDto? = parseWithExtraData(reader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(writer: JsonWriter, value: ManualStyleDto): Unit = error("write not under test")
}

private object GeneratedStyleAdapter : CustomObjectDtoAdapter<GeneratedStyleDto>(GeneratedStyleDto::class) {
    @FromJson
    fun fromJson(
        reader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<GeneratedStyleDto>,
    ): GeneratedStyleDto? = parseWithExtraData(reader, mapAdapter, valueAdapter)

    @ToJson
    fun toJson(writer: JsonWriter, value: GeneratedStyleDto): Unit = error("write not under test")
}

internal class CustomObjectDtoAdapterTest {

    @Test
    fun `manual-style DTO with snake_case property names buckets unknown keys as extraData`() {
        val moshi = Moshi.Builder().add(ManualStyleAdapter).build()
        val json = """{"name":"alice","weight":42,"role":"admin","mood":"happy"}"""

        val dto = moshi.adapter(ManualStyleDto::class.java).fromJson(json)!!

        dto.name shouldBeEqualTo "alice"
        dto.weight shouldBeEqualTo 42
        dto.extraData shouldBeEqualTo mapOf("role" to "admin", "mood" to "happy")
    }

    @Test
    fun `generated-style DTO with @Json annotations buckets unknown keys as extraData`() {
        val moshi = Moshi.Builder().add(GeneratedStyleAdapter).build()
        val json = """{"first_name":"alice","weight_kg":42,"role":"admin","mood":"happy"}"""

        val dto = moshi.adapter(GeneratedStyleDto::class.java).fromJson(json)!!

        dto.firstName shouldBeEqualTo "alice"
        dto.weightKg shouldBeEqualTo 42
        dto.extraData shouldBeEqualTo mapOf("role" to "admin", "mood" to "happy")
    }
}
