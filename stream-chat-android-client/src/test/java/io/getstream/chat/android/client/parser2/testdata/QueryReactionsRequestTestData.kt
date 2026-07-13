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

import io.getstream.chat.android.network.models.QueryReactionsRequest
import io.getstream.chat.android.network.models.SortParamRequest
import org.intellij.lang.annotations.Language

internal object QueryReactionsRequestTestData {

    @Language("JSON")
    val queryReactionsRequestJson =
        """{
          "limit": 10,
          "next": "next-token",
          "prev": "prev-token",
          "sort": [
            {
              "direction": -1,
              "field": "created_at",
              "type": "reaction"
            }
          ],
          "filter": {
            "type": "like"
          }
        }""".withoutWhitespace()

    val queryReactionsRequest = QueryReactionsRequest(
        limit = 10,
        next = "next-token",
        prev = "prev-token",
        sort = listOf(
            SortParamRequest(
                direction = -1,
                field = "created_at",
                type = "reaction",
            ),
        ),
        filter = mapOf(
            "type" to "like",
        ),
    )

    @Language("JSON")
    val queryReactionsRequestJsonWithDefaults =
        """{
          "sort": [],
          "filter": {}
        }""".withoutWhitespace()

    val queryReactionsRequestWithDefaults = QueryReactionsRequest()
}
