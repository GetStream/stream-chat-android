package io.getstream.chat.android.client.parser.adapters

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.parser.ChatParserImpl
import org.amshove.kluent.shouldBeEqualTo
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CustomObjectAdapterTest {
    private val parser = ChatParserImpl()

    @BeforeEach
    fun `check prerequisites`() {
        val jsonObject = JsonParser.parseString(json).asJsonObject
        checkJsonStructureAndValues(jsonObject)
    }

    @Test
    fun `should deserialize json object with custom fields`() {
        val message = parser.fromJson(json, Message::class.java)

        message.extraData.apply {
            get("extraData") shouldBeEqualTo mapOf("key1" to "value1", "key2" to true, "key3" to mapOf("key4" to "val4"))
            get("customKey1") shouldBeEqualTo "customVal1"
            get("customKey2") shouldBeEqualTo "customVal2"
            get("customKey3") shouldBeEqualTo true
            get("customKey4") shouldBeEqualTo listOf("a", "b", "c")
        }
    }

    @Test
    fun `should serialize json object with custom fields`() {
        val jsonString: String = parser.toJson(testMessage)
        val jsonObject = JsonParser.parseString(jsonString).asJsonObject
        checkJsonStructureAndValues(jsonObject)
    }

    private fun checkJsonStructureAndValues(jsonObject: JsonObject) {
        jsonObject.apply {
            get("id").asString shouldBeEqualTo "8584452-6d711169-0224-41c2-b9aa-1adbe624521b"
            get("extraData").asJsonObject.apply {
                get("key1").asString shouldBeEqualTo "value1"
                get("key2").asBoolean shouldBeEqualTo true
                get("key3").asJsonObject.apply {
                    get("key4").asString shouldBeEqualTo "val4"
                }
            }
            get("customKey1").asString shouldBeEqualTo "customVal1"
            get("customKey2").asString shouldBeEqualTo "customVal2"
            get("customKey3").asBoolean shouldBeEqualTo true
            get("customKey4").asJsonArray.apply {
                get(0).asString shouldBeEqualTo "a"
                get(1).asString shouldBeEqualTo "b"
                get(2).asString shouldBeEqualTo "c"
            }
        }
    }

    companion object {
        @Language("JSON")
        const val json = "{\n  \"id\": \"8584452-6d711169-0224-41c2-b9aa-1adbe624521b\",\n  \"extraData\": {\n    \"key1\": \"value1\",\n    \"key2\": true,\n    \"key3\": {\n      \"key4\": \"val4\"\n    }\n  },\n  \"customKey1\": \"customVal1\",\n  \"customKey2\": \"customVal2\",\n  \"customKey3\": true,\n  \"customKey4\": [\n    \"a\",\n    \"b\",\n    \"c\"\n  ]\n}"
        val testMessage = Message().apply {
            id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b"
            extraData["extraData"] = mapOf("key1" to "value1", "key2" to true, "key3" to mapOf("key4" to "val4"))
            extraData["customKey1"] = "customVal1"
            extraData["customKey2"] = "customVal2"
            extraData["customKey3"] = true
            extraData["customKey4"] = listOf("a", "b", "c")
        }
    }
}
