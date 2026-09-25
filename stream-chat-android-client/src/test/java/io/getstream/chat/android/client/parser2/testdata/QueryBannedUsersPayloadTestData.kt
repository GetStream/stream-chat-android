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

import io.getstream.chat.android.network.models.QueryBannedUsersPayload
import io.getstream.chat.android.network.models.SortParamRequest
import org.intellij.lang.annotations.Language
import java.util.Date

internal object QueryBannedUsersPayloadTestData {

    @Language("JSON")
    val payloadJson =
        """{
          "filter_conditions": {
            "channel_cid": "messaging:abc"
          },
          "created_at_after": "2023-11-14T22:13:20.123Z",
          "created_at_after_or_equal": "2023-11-14T22:13:21.000Z",
          "created_at_before": "2027-01-15T08:00:00.000Z",
          "created_at_before_or_equal": "1970-01-01T00:00:00.001Z",
          "limit": 10,
          "offset": 5,
          "sort": [
            {
              "direction": -1,
              "field": "created_at"
            }
          ]
        }""".withoutWhitespace()

    val payload = QueryBannedUsersPayload(
        filterConditions = mapOf("channel_cid" to "messaging:abc"),
        createdAtAfter = Date(1_700_000_000_123),
        createdAtAfterOrEqual = Date(1_700_000_001_000),
        createdAtBefore = Date(1_800_000_000_000),
        createdAtBeforeOrEqual = Date(1),
        limit = 10,
        offset = 5,
        sort = listOf(SortParamRequest(direction = -1, field = "created_at")),
    )

    @Language("JSON")
    val payloadJsonWithDefaults = """{"filter_conditions":{}}"""

    val payloadWithDefaults = QueryBannedUsersPayload()
}
