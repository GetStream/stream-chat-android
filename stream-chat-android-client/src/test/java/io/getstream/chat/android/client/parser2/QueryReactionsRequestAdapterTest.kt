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

import io.getstream.chat.android.client.parser2.testdata.QueryReactionsRequestTestData
import io.getstream.chat.android.network.models.QueryReactionsRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class QueryReactionsRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize QueryReactionsRequest with all fields`() {
        val json = parser.toJson(QueryReactionsRequestTestData.queryReactionsRequest)
        Assertions.assertEquals(QueryReactionsRequestTestData.queryReactionsRequestJson, json)
    }

    @Test
    fun `Serialize QueryReactionsRequest with default fields`() {
        val json = parser.toJson(QueryReactionsRequestTestData.queryReactionsRequestWithDefaults)
        Assertions.assertEquals(QueryReactionsRequestTestData.queryReactionsRequestJsonWithDefaults, json)
    }

    @Test
    fun `Deserialize QueryReactionsRequest with all fields`() {
        val request = parser.fromJson(
            QueryReactionsRequestTestData.queryReactionsRequestJson,
            QueryReactionsRequest::class.java,
        )
        Assertions.assertEquals(QueryReactionsRequestTestData.queryReactionsRequest, request)
    }
}
