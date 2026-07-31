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

import io.getstream.chat.android.network.models.QueryDraftsRequest
import io.getstream.chat.android.network.models.QueryRemindersRequest
import io.getstream.chat.android.network.models.QueryThreadsRequest
import io.getstream.chat.android.network.models.SortParamRequest
import io.getstream.chat.android.network.models.UpdateThreadPartialRequest
import org.intellij.lang.annotations.Language

internal object QueryPayloadRequestTestData {

    private val sort = listOf(
        SortParamRequest(direction = -1, field = "created_at"),
    )

    // QueryDraftsRequest

    @Language("JSON")
    val queryDraftsRequestJson =
        """{
          "limit": 10,
          "next": "next-token",
          "prev": "prev-token",
          "sort": [
            {
              "direction": -1,
              "field": "created_at"
            }
          ],
          "filter": {
            "channel_cid": "messaging:123"
          }
        }""".withoutWhitespace()

    val queryDraftsRequest = QueryDraftsRequest(
        limit = 10,
        next = "next-token",
        prev = "prev-token",
        sort = sort,
        filter = mapOf("channel_cid" to "messaging:123"),
    )

    @Language("JSON")
    val queryDraftsRequestJsonWithDefaults =
        """{
          "sort": [],
          "filter": {}
        }""".withoutWhitespace()

    val queryDraftsRequestWithDefaults = QueryDraftsRequest()

    // QueryRemindersRequest

    @Language("JSON")
    val queryRemindersRequestJson =
        """{
          "limit": 10,
          "next": "next-token",
          "prev": "prev-token",
          "sort": [
            {
              "direction": -1,
              "field": "created_at"
            }
          ],
          "filter": {
            "channel_cid": "messaging:123"
          }
        }""".withoutWhitespace()

    val queryRemindersRequest = QueryRemindersRequest(
        limit = 10,
        next = "next-token",
        prev = "prev-token",
        sort = sort,
        filter = mapOf("channel_cid" to "messaging:123"),
    )

    @Language("JSON")
    val queryRemindersRequestJsonWithDefaults =
        """{
          "sort": [],
          "filter": {}
        }""".withoutWhitespace()

    val queryRemindersRequestWithDefaults = QueryRemindersRequest()

    // QueryThreadsRequest

    @Language("JSON")
    val queryThreadsRequestJson =
        """{
          "limit": 10,
          "member_limit": 5,
          "next": "next-token",
          "participant_limit": 3,
          "prev": "prev-token",
          "reply_limit": 2,
          "watch": true,
          "sort": [
            {
              "direction": -1,
              "field": "created_at"
            }
          ],
          "filter": {
            "channel_cid": "messaging:123"
          }
        }""".withoutWhitespace()

    val queryThreadsRequest = QueryThreadsRequest(
        limit = 10,
        memberLimit = 5,
        next = "next-token",
        participantLimit = 3,
        prev = "prev-token",
        replyLimit = 2,
        watch = true,
        sort = sort,
        filter = mapOf("channel_cid" to "messaging:123"),
    )

    @Language("JSON")
    val queryThreadsRequestJsonWithDefaults =
        """{
          "sort": [],
          "filter": {}
        }""".withoutWhitespace()

    val queryThreadsRequestWithDefaults = QueryThreadsRequest()

    // UpdateThreadPartialRequest

    @Language("JSON")
    val updateThreadPartialRequestJson =
        """{
          "unset": [
            "description"
          ],
          "set": {
            "title": "new-title"
          }
        }""".withoutWhitespace()

    val updateThreadPartialRequest = UpdateThreadPartialRequest(
        unset = listOf("description"),
        set = mapOf("title" to "new-title"),
    )

    @Language("JSON")
    val updateThreadPartialRequestJsonWithDefaults =
        """{
          "unset": [],
          "set": {}
        }""".withoutWhitespace()

    val updateThreadPartialRequestWithDefaults = UpdateThreadPartialRequest()
}
