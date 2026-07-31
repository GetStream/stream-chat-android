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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.network.models.MessageOptions
import io.getstream.chat.android.network.models.SearchPayload
import io.getstream.chat.android.network.models.SortParamRequest
import org.intellij.lang.annotations.Language

internal object SearchPayloadTestData {

    @Language("JSON")
    val searchPayloadJson =
        """{
          "filter_conditions": {
            "type": "messaging"
          },
          "force_default_search": true,
          "force_sql_v2_backend": false,
          "limit": 30,
          "next": "next-token",
          "offset": 0,
          "query": "hello",
          "sort": [
            {
              "direction": -1,
              "field": "created_at"
            }
          ],
          "message_filter_conditions": {
            "text": "hello"
          },
          "message_options": {
            "include_thread_participants": true,
            "member_custom_include": [
              "role"
            ]
          }
        }""".withoutWhitespace()

    val searchPayload = SearchPayload(
        filterConditions = mapOf("type" to "messaging"),
        forceDefaultSearch = true,
        forceSqlV2Backend = false,
        limit = 30,
        next = "next-token",
        offset = 0,
        query = "hello",
        sort = listOf(
            SortParamRequest(
                direction = -1,
                field = "created_at",
            ),
        ),
        messageFilterConditions = mapOf("text" to "hello"),
        messageOptions = MessageOptions(
            includeThreadParticipants = true,
            memberCustomInclude = listOf("role"),
        ),
    )

    @Language("JSON")
    val searchPayloadJsonWithDefaults =
        """{
          "filter_conditions": {},
          "sort": [],
          "message_filter_conditions": {}
        }""".withoutWhitespace()

    val searchPayloadWithDefaults = SearchPayload()
}
