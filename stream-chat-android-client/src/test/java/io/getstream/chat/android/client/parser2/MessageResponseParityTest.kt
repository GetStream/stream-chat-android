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
import io.getstream.chat.android.client.parser2.testdata.MessageDtoTestData
import io.getstream.chat.android.client.parser2.testdata.MessageTestData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.MessageResponse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.lang.reflect.Modifier

/**
 * Parses the same message JSON through the hand-written [DownstreamMessageDto] and the generated
 * [MessageResponse], and requires the two domain [Message]s to match field by field.
 */
internal class MessageResponseParityTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "user-1" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    fun `The generated MessageResponse maps to the same Message as the hand-written DTO`(name: String, json: String) {
        val legacy = runCatching {
            with(mapping) { parser.fromJson(json, DownstreamMessageDto::class.java).toDomain() }
        }
        val generated = runCatching {
            with(mapping) { parser.fromJson(json, MessageResponse::class.java).toDomain() }
        }
        // The generated model may accept input the hand-written DTO rejected, never the reverse.
        if (legacy.isFailure) return
        assertTrue(generated.isSuccess) { "$name: only the generated path failed: ${generated.exceptionOrNull()}" }
        val differences = diff(legacy.getOrThrow(), generated.getOrThrow())
        if (differences.isNotEmpty()) fail<Unit>("$name differs:\n" + differences.joinToString("\n"))
    }

    private fun diff(legacy: Message, generated: Message): List<String> =
        Message::class.java.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
            .mapNotNull { field ->
                val a = field.get(legacy)
                val b = field.get(generated)
                if (a == b) null else "  ${field.name}:\n    legacy    = $a\n    generated = $b"
            }

    companion object {

        private val mapAdapter = Moshi.Builder().build().adapter<Map<String, Any?>>(
            Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java),
        )

        /** Every JSON fixture that describes a message, keyed by its property name. */
        @JvmStatic
        fun fixtures(): List<Arguments> {
            val messageTestData = MessageTestData::class.java.declaredFields
                .filter { it.type == String::class.java }
                .onEach { it.isAccessible = true }
                .mapNotNull { field ->
                    (field.get(MessageTestData) as? String)?.let { "MessageTestData.${field.name}" to it }
                }
                .filter { (_, json) -> json.trimStart().startsWith("{") }
            val dtoTestData = listOf(
                "MessageDtoTestData.downstreamJson" to MessageDtoTestData.downstreamJson,
                "MessageDtoTestData.downstreamJsonWithoutExtraData" to
                    MessageDtoTestData.downstreamJsonWithoutExtraData,
            )
            return (messageTestData + dtoTestData).map { (name, json) -> Arguments.of(name, wireShaped(json)) }
        }

        /**
         * Gives every user the fields the backend always sends (plain Go tags) and drops the root `channel`,
         * which the thread-replies and pinned-messages endpoints never send.
         */
        private fun wireShaped(json: String): String {
            val root = mapAdapter.fromJson(json) ?: return json

            @Suppress("UNCHECKED_CAST")
            val normalized = normalize(root.minus("channel")) as Map<String, Any?>
            return mapAdapter.toJson(normalized)
        }

        private fun normalize(value: Any?): Any? = when (value) {
            is Map<*, *> -> {
                val map = value.entries.associate { (k, v) -> k as String to normalize(v) }
                when {
                    map.looksLikeUser() -> map.minus(NOT_SENT_ON_MESSAGE_USERS).withDefaults(USER_WIRE_FIELDS)
                    map.looksLikeMessage() ->
                        map.withDefaults(MESSAGE_WIRE_FIELDS).withReminderIds().withLocationDates()
                    else -> map
                }
            }
            is List<*> -> value.map(::normalize)
            else -> value
        }

        private fun Map<String, Any?>.looksLikeUser() =
            containsKey("id") && containsKey("role") && containsKey("banned") && containsKey("online")

        private fun Map<String, Any?>.looksLikeMessage() = containsKey("id") && containsKey("html")

        /** Fills [defaults] for keys that are absent or null, since the backend always sends a value. */
        private fun Map<String, Any?>.withDefaults(defaults: Map<String, Any?>): Map<String, Any?> =
            this + defaults.filterKeys { this[it] == null }

        /** The shared location payload always sends its timestamps (plain Go tags, payload/shared_location.go). */
        private fun Map<String, Any?>.withLocationDates(): Map<String, Any?> {
            @Suppress("UNCHECKED_CAST")
            val location = this["shared_location"] as? Map<String, Any?> ?: return this
            val dates = mapOf("created_at" to "2020-01-01T00:00:00.000Z", "updated_at" to "2020-01-01T00:00:00.000Z")
            return this + ("shared_location" to location.withDefaults(dates))
        }

        /**
         * Keys a message user never carries for a client-side connection: `ignore_if_client_side` fields of Go
         * `commonpayloads.UserResponse`, plus own-user state that type does not have.
         */
        private val NOT_SENT_ON_MESSAGE_USERS = setOf(
            "devices", "privacy_settings", "invisible", "shadow_banned", "ban_expires", "push_notifications",
            "bypass_moderation", "mutes", "channel_mutes", "total_unread_count", "unread_channels", "unread_count",
            "unread_threads", "push_preferences",
        )

        /** The reminder payload repeats its message's ids, as plain Go tags. */
        private fun Map<String, Any?>.withReminderIds(): Map<String, Any?> {
            @Suppress("UNCHECKED_CAST")
            val reminder = this["reminder"] as? Map<String, Any?> ?: return this
            val userId = (this["user"] as? Map<*, *>)?.get("id")
            val ids = mapOf("channel_cid" to this["cid"], "message_id" to this["id"], "user_id" to userId)
            return this + ("reminder" to ids + reminder)
        }

        /** Plain (always emitted) tags on the Go user payload. */
        private val USER_WIRE_FIELDS = mapOf(
            "created_at" to "2020-01-01T00:00:00.000Z",
            "updated_at" to "2020-01-01T00:00:00.000Z",
            "language" to "en",
        )

        /** Plain (always emitted) boolean tags on the Go message payload (payload/message.go). */
        private val MESSAGE_WIRE_FIELDS = mapOf(
            "shadowed" to false,
            "mentioned_channel" to false,
            "mentioned_here" to false,
            "pinned" to false,
        )
    }
}
