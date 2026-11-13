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

package io.getstream.chat.android.client.api2

import io.getstream.chat.android.network.apis.ChatEndpoints
import io.getstream.chat.android.network.infrastructure.JsonQuery
import io.getstream.chat.android.network.infrastructure.QueryConverterFactory
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.QueryMembersPayload
import io.getstream.chat.android.network.models.QueryUsersPayload
import io.getstream.chat.android.network.models.SortParamRequest
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.Date
import java.util.concurrent.TimeUnit

/**
 * Checks what the generated [ChatEndpoints] put on the wire through the converters the SDK registers.
 */
internal class ChatEndpointsRequestTest {

    private val server = MockWebServer()
    private lateinit var api: ChatEndpoints

    @BeforeEach
    fun setUp() {
        server.start()
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            // The converters ChatModule registers on the v2 Retrofit, in the same order.
            .addConverterFactory(QueryConverterFactory(Serializer.moshi))
            .addConverterFactory(MoshiConverterFactory.create(Serializer.moshi))
            .build()
            .create(ChatEndpoints::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `A sort list is sent as one JSON array`() = runTest {
        val request = record {
            getReplies(
                parentId = "parent",
                sort = JsonQuery(listOf(SortParamRequest(field = "created_at", direction = 1))),
            )
        }

        request.requestUrl!!.queryParameterValues("sort") shouldBeEqualTo listOf("""[{"direction":1,"field":"created_at"}]""")
    }

    @Test
    fun `A payload is sent as one JSON object`() = runTest {
        val payload = QueryMembersPayload(type = "messaging", id = "general", limit = 5)

        val request = record { queryMembers(payload = JsonQuery(payload)) }

        request.requestUrl!!.queryParameterValues("payload") shouldBeEqualTo
            listOf("""{"type":"messaging","id":"general","limit":5}""")
    }

    @Test
    fun `A presence query sends its connection id next to the payload`() = runTest {
        val payload = QueryUsersPayload(filterConditions = emptyMap(), presence = true)

        val request = record { queryUsers(connectionId = "connection", payload = JsonQuery(payload)) }

        request.requestUrl!!.queryParameter("connection_id") shouldBeEqualTo "connection"
        request.requestUrl!!.queryParameterValues("payload") shouldBeEqualTo
            listOf("""{"filter_conditions":{},"presence":true}""")
    }

    @Test
    fun `A date is sent as an RFC 3339 timestamp in UTC`() = runTest {
        val request = record { getPinnedMessages(type = "messaging", id = "general", pinnedAtBefore = Date(1_762_778_096_789)) }

        request.requestUrl!!.queryParameter("pinned_at_before") shouldBeEqualTo "2025-11-10T12:34:56.789Z"
    }

    @Test
    fun `An empty request body is sent as an empty JSON object`() = runTest {
        val request = record { stopWatchingChannel(type = "messaging", id = "general", connectionId = "connection") }

        request.method shouldBeEqualTo "POST"
        request.requestUrl!!.queryParameter("connection_id") shouldBeEqualTo "connection"
        request.body.readUtf8() shouldBeEqualTo "{}"
    }

    @Test
    fun `Showing a channel sends an empty JSON object`() = runTest {
        val request = record { showChannel(type = "messaging", id = "general") }

        request.method shouldBeEqualTo "POST"
        request.body.readUtf8() shouldBeEqualTo "{}"
    }

    private suspend fun record(call: suspend ChatEndpoints.() -> Any): RecordedRequest {
        server.enqueue(MockResponse().setResponseCode(500))
        val failure = runCatching { api.call() }.exceptionOrNull()
        return server.takeRequest(5, TimeUnit.SECONDS) ?: error("No request was sent: $failure")
    }
}
