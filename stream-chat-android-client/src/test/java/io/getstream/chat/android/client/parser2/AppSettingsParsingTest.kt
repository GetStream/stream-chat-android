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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.models.AppSettings
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.GetApplicationResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AppSettingsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Language("JSON")
    private val json =
        """{
          "duration": "1ms",
          "app": {
            "name": "Stream SDK - Android",
            "async_url_enrich_enabled": false,
            "auto_translation_enabled": false,
            "id": 1,
            "placement": "eu-west",
            "file_upload_config": {
              "size_limit": 1024,
              "allowed_file_extensions": [".png"],
              "allowed_mime_types": ["image/png"],
              "blocked_file_extensions": [".json"],
              "blocked_mime_types": ["application/json"]
            },
            "image_upload_config": {
              "size_limit": 0,
              "allowed_file_extensions": [],
              "allowed_mime_types": [],
              "blocked_file_extensions": [],
              "blocked_mime_types": []
            }
          }
        }"""

    @Test
    fun `deserializes the app settings wire shape and maps it to AppSettings`() {
        val dto = parser.fromJson(json, GetApplicationResponse::class.java)

        val appSettings = with(domainMapping) { dto.toDomain() }

        assertEquals("Stream SDK - Android", appSettings.app.name)
        with(appSettings.app.fileUploadConfig) {
            assertEquals(listOf(".png"), allowedFileExtensions)
            assertEquals(listOf("image/png"), allowedMimeTypes)
            assertEquals(listOf(".json"), blockedFileExtensions)
            assertEquals(listOf("application/json"), blockedMimeTypes)
            assertEquals(1024L, sizeLimitInBytes)
        }
        // size_limit 0 on the wire falls back to the domain default.
        assertEquals(
            AppSettings.DEFAULT_SIZE_LIMIT_IN_BYTES,
            appSettings.app.imageUploadConfig.sizeLimitInBytes,
        )
    }
}
