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

import io.getstream.chat.android.network.models.TranslateMessageRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class TranslateMessageRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize TranslateMessageRequest with a language the models do not know`() {
        val language = TranslateMessageRequest.Language.fromString("xx")

        val json = parser.toJson(TranslateMessageRequest(language))

        Assertions.assertEquals("""{"language":"xx"}""", json)
    }

    @ParameterizedTest
    @MethodSource("languages")
    fun `Serialize TranslateMessageRequest for every language the API accepts`(code: String) {
        val json = parser.toJson(TranslateMessageRequest(TranslateMessageRequest.Language.fromString(code)))

        Assertions.assertEquals("""{"language":"$code"}""", json)
    }

    companion object {
        @JvmStatic
        fun languages(): List<String> = listOf(
            "af", "am", "ar", "az", "bg", "bn", "bs", "cs", "da", "de", "el", "en", "es", "es-MX", "et", "fa",
            "fa-AF", "fi", "fr", "fr-CA", "ha", "he", "hi", "hr", "ht", "hu", "id", "it", "ja", "ka", "ko",
            "lt", "lv", "ms", "nl", "no", "pl", "ps", "pt", "ro", "ru", "sk", "sl", "so", "sq", "sr", "sv",
            "sw", "ta", "th", "tl", "tr", "uk", "ur", "vi", "zh", "zh-TW",
        )
    }
}
