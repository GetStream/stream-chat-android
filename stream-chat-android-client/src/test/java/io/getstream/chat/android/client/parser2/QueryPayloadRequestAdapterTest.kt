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

import io.getstream.chat.android.client.parser2.testdata.QueryPayloadRequestTestData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class QueryPayloadRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize QueryDraftsRequest with all fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.queryDraftsRequest)
        Assertions.assertEquals(QueryPayloadRequestTestData.queryDraftsRequestJson, json)
    }

    @Test
    fun `Serialize QueryDraftsRequest with default fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.queryDraftsRequestWithDefaults)
        Assertions.assertEquals(QueryPayloadRequestTestData.queryDraftsRequestJsonWithDefaults, json)
    }

    @Test
    fun `Serialize QueryRemindersRequest with all fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.queryRemindersRequest)
        Assertions.assertEquals(QueryPayloadRequestTestData.queryRemindersRequestJson, json)
    }

    @Test
    fun `Serialize QueryRemindersRequest with default fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.queryRemindersRequestWithDefaults)
        Assertions.assertEquals(QueryPayloadRequestTestData.queryRemindersRequestJsonWithDefaults, json)
    }

    @Test
    fun `Serialize QueryThreadsRequest with all fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.queryThreadsRequest)
        Assertions.assertEquals(QueryPayloadRequestTestData.queryThreadsRequestJson, json)
    }

    @Test
    fun `Serialize QueryThreadsRequest with default fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.queryThreadsRequestWithDefaults)
        Assertions.assertEquals(QueryPayloadRequestTestData.queryThreadsRequestJsonWithDefaults, json)
    }

    @Test
    fun `Serialize UpdateThreadPartialRequest with all fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.updateThreadPartialRequest)
        Assertions.assertEquals(QueryPayloadRequestTestData.updateThreadPartialRequestJson, json)
    }

    @Test
    fun `Serialize UpdateThreadPartialRequest with default fields`() {
        val json = parser.toJson(QueryPayloadRequestTestData.updateThreadPartialRequestWithDefaults)
        Assertions.assertEquals(QueryPayloadRequestTestData.updateThreadPartialRequestJsonWithDefaults, json)
    }
}
