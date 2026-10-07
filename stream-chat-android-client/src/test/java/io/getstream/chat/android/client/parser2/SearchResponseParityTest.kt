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

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.MessageResponse
import io.getstream.chat.android.network.models.SearchResponse
import io.getstream.chat.android.network.models.SearchResultMessage
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

/**
 * Search results parsed through the generated [SearchResultMessage], against the generated [MessageResponse] it
 * extends and the hand-written [DownstreamMessageDto] search used before.
 */
internal class SearchResponseParityTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "jaewoong" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("io.getstream.chat.android.client.parser2.MessageResponseParityTest#fixtures")
    fun `A search result maps like the MessageResponse it extends`(name: String, json: String) {
        val message = runCatching { with(mapping) { parser.fromJson(json, MessageResponse::class.java).toDomain() } }
        val searchResult = runCatching {
            with(mapping) { parser.fromJson(json, SearchResultMessage::class.java).toDomain() }
        }

        // Fixtures missing a required field must be rejected by both.
        searchResult.isSuccess shouldBeEqualTo message.isSuccess
        if (message.isSuccess) assertFieldsEqual(name, message.getOrThrow(), searchResult.getOrThrow())
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("io.getstream.chat.android.client.parser2.MessageResponseParityTest#fixtures")
    fun `A search result converts to the MessageResponse of its JSON`(name: String, json: String) {
        val message = runCatching { parser.fromJson(json, MessageResponse::class.java) }.getOrNull() ?: return
        val searchResult = parser.fromJson(json, SearchResultMessage::class.java)

        // Compares every field, including the ones the message mapper doesn't read yet.
        assertFieldsEqual(name, message, with(mapping) { searchResult.toMessageResponse() })
    }

    @Test
    fun `A search result message keeps the fields the message mapper only reads through custom`() {
        val json = recordedMessages().first().replaceFirst(
            "{",
            """{"mml": "<mml/>", "poll_id": "poll-1", "mentioned_group_ids": ["group-1"],
            "image_labels": {"a.png": ["cat"]},
            "draft": {"channel_cid": "messaging:search-parity", "created_at": "2026-09-30T09:00:00.000Z",
            "message": {"id": "draft-1", "text": "draft"}},""",
        )
        val searchResult = parser.fromJson(json, SearchResultMessage::class.java)
        // Without the channel, which MessageResponse doesn't declare and would collect into custom.
        val message = parser.fromJson(mapAdapter.toJson(mapAdapter.fromJson(json)!! - "channel"), MessageResponse::class.java)

        assertFieldsEqual("extra fields", message, with(mapping) { searchResult.toMessageResponse() })
    }

    @Test
    fun `Recorded search results map to the same Message as the hand-written DTO`() {
        recordedMessages().forEachIndexed { index, json ->
            val legacy = with(mapping) { parser.fromJson(json, DownstreamMessageDto::class.java).toDomain() }
            val generated = with(mapping) { parser.fromJson(json, SearchResultMessage::class.java).toDomain() }

            assertFieldsEqual("result $index", legacy, generated)
        }
    }

    @Test
    fun `A search result takes its channel info from the channel it carries`() {
        val message = with(mapping) {
            parser.fromJson(recordedMessages().first(), SearchResultMessage::class.java).toDomain()
        }

        message.channelInfo?.cid shouldBeEqualTo "messaging:search-parity"
        message.channelInfo?.id shouldBeEqualTo "search-parity"
        message.channelInfo?.type shouldBeEqualTo "messaging"
        message.channelInfo?.memberCount shouldBeEqualTo 2
    }

    @Test
    fun `The search response keeps the results warning and the pagination cursor`() {
        val response = parser.fromJson(RECORDED_RESPONSE, SearchResponse::class.java)

        response.results.size shouldBeEqualTo 4
        response.next shouldBeEqualTo "W3sibmFtZSI6ImNyZWF0ZWRfYXQiLCJ2YWx1ZSI6IjIwMjYtMDktMzAiLCJkaXJlY3Rpb24iOi0xfV0="
        val warning = with(mapping) { response.resultsWarning?.toDomain() }
        warning?.warningCode shouldBeEqualTo 1
        warning?.channelSearchCount shouldBeEqualTo 57
        warning?.channelSearchCids shouldBeEqualTo
            listOf("messaging:search-parity", "messaging:general", "messaging:random")
    }

    private fun recordedMessages(): List<String> {
        val response = mapAdapter.fromJson(RECORDED_RESPONSE)!!

        @Suppress("UNCHECKED_CAST")
        val results = response["results"] as List<Map<String, Any?>>
        return results.map { mapAdapter.toJson(it["message"] as Map<String, Any?>) }
    }

    private companion object {
        private val mapAdapter = Moshi.Builder().build().adapter<Map<String, Any?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
        )

        /** A search response recorded from the backend, with its warning and cursor added. */
        private val RECORDED_RESPONSE = SearchResponseParityTest::class.java
            .getResource("/parity/search_results.json")!!
            .readText()
    }
}
