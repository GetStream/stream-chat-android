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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.mapping.toDomain
import io.getstream.chat.android.client.api2.model.dto.ErrorDto
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectionErrorEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.extensions.internal.enrichIfNeeded
import io.getstream.chat.android.client.parser2.adapters.generatedEventAdapter
import io.getstream.chat.android.client.parser2.adapters.generatedEventAdapters
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.network.infrastructure.ExactDate
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.ConnectedEvent
import io.getstream.chat.android.network.models.HealthCheckEvent
import io.getstream.chat.android.network.models.NotificationMarkReadEvent
import io.getstream.chat.android.network.models.UserResponseCommonFields
import io.getstream.log.taggedLogger
import okio.Buffer

/**
 * Parses the frames of the v2 websocket. Their dates are unix nanoseconds and their custom data sits under `custom`,
 * so they are read straight into the generated models with [Serializer.moshi]: a generic map would turn the dates into
 * doubles. [domainMapping] is the v2 mapping, which gives every object the `extraData` the v1 parser gives it.
 */
internal class V2EventParser(
    private val domainMapping: DomainMapping,
    private val directEventParser: DirectEventParser? = null,
) {

    private val logger by taggedLogger("Chat:V2EventParser")

    // Built on the first frame, not with the client: the generated adapters are many.
    private val moshi by lazy { Serializer.moshi }
    private val eventMapping by lazy { EventMapping(domainMapping) }

    private val generatedAdapters by lazy { generatedEventAdapters(moshi) }
    private val connectedEventAdapter by lazy { generatedEventAdapter<ConnectedEvent>(moshi) { emptyMap() } }
    private val healthEventAdapter by lazy { generatedEventAdapter<HealthCheckEvent>(moshi) { emptyMap() } }
    private val notificationMarkReadEventAdapter by lazy {
        generatedEventAdapter<NotificationMarkReadEvent>(moshi) {
            mapOf("cid" to cid, "channel_type" to channelType, "channel_id" to channelId, "user" to user)
        }
    }
    private val markAllReadEventAdapter by lazy {
        generatedEventAdapter<NotificationMarkReadEvent>(moshi) { mapOf("user" to user) }
    }
    private val connectionErrorAdapter by lazy { moshi.adapter(V2ConnectionErrorEvent::class.java) }
    private val exactDateAdapter by lazy { moshi.adapter(ExactDate::class.java) }
    private val userAdapter by lazy { moshi.adapter(UserResponseCommonFields::class.java) }
    private val valueAdapter by lazy { moshi.adapter(Any::class.java) }
    private val keyAdapter by lazy { moshi.adapter(String::class.java) }

    /**
     * Parses [raw] into its domain event, or returns null for a frame the SDK ignores.
     */
    @Suppress("ReturnCount")
    fun parse(raw: String): ChatEvent? {
        val head = readHead(raw)
        val type = head.type
        if (type != null && type.startsWith(FEEDS_EVENT_PREFIX)) {
            // The v2 socket also delivers the user's Feeds events, which v1 never did.
            logger.v { "[parse] ignored: $type" }
            return null
        }
        // The fast path, when enabled; it returns null for a frame it can't read, which the generated path then reads.
        if (type == EventType.MESSAGE_NEW) directEventParser?.parse(raw, type)?.let { return it.enrichedFast() }
        val adapter = when (type) {
            CONNECTION_OK -> connectedEventAdapter
            EventType.HEALTH_CHECK -> healthEventAdapter
            EventType.CONNECTION_ERROR -> return parseConnectionError(raw)
            EventType.NOTIFICATION_MARK_READ ->
                if (head.hasCid) notificationMarkReadEventAdapter else markAllReadEventAdapter
            else -> generatedAdapters[type] ?: return parseUnknownEvent(raw)
        }
        val dto = adapter.fromJson(raw) ?: throw JsonDataException("Null event: $type")
        return with(eventMapping) { dto.toDomain() }.enrichIfNeeded()
    }

    /**
     * [enrichIfNeeded] copies the event and its message chain to set the event's cid. A fast-path `message.new` already
     * carries it throughout, so the copies, which the fast path exists to avoid, are skipped.
     */
    private fun ChatEvent.enrichedFast(): ChatEvent =
        if (this is NewMessageEvent && message.hasCidThroughout(cid)) this else enrichIfNeeded()

    private fun Message.hasCidThroughout(cid: String): Boolean =
        this.cid == cid && (replyTo?.hasCidThroughout(cid) ?: true)

    /**
     * Parses the event a send-event response carries. The response holds the event as sent, its custom data under
     * `custom`, while the copy the socket delivers has the backend's fields at the root (an AI indicator's `ai_state`,
     * say), so the `custom` entries the root lacks are lifted to it first. Values are copied as raw JSON, so dates
     * keep their exact value.
     */
    fun parseSentEvent(body: String): ChatEvent? {
        val event = rawFields(body)["event"] ?: throw JsonDataException("Required value 'event' missing at $")
        return parse(withCustomAtRoot(event))
    }

    private fun withCustomAtRoot(event: String): String {
        val fields = rawFields(event)
        val custom = fields["custom"]?.takeIf { it.trimStart().startsWith("{") }?.let(::rawFields).orEmpty()
        return jsonObject(fields + custom.filterKeys { it !in fields })
    }

    /** The fields of a JSON object, each value as its raw JSON. */
    private fun rawFields(json: String): Map<String, String> {
        val fields = linkedMapOf<String, String>()
        reader(json).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) fields[reader.nextName()] = reader.nextSource().use { it.readUtf8() }
        }
        return fields
    }

    private fun jsonObject(rawFields: Map<String, String>): String =
        rawFields.entries.joinToString(prefix = "{", postfix = "}") { (name, raw) -> "${keyAdapter.toJson(name)}:$raw" }

    private fun readHead(raw: String): Head {
        var type: String? = null
        var hasCid = false
        reader(raw).use { reader ->
            reader.beginObject()
            // `cid` only matters for notification.mark_read, so the scan stops once the type rules it out.
            while (reader.hasNext() && (type == null || type == EventType.NOTIFICATION_MARK_READ)) {
                when (reader.nextName()) {
                    "type" -> type = valueAdapter.fromJson(reader) as? String
                    "cid" -> hasCid = valueAdapter.fromJson(reader) != null
                    else -> reader.skipValue()
                }
            }
        }
        return Head(type, hasCid)
    }

    private fun parseConnectionError(raw: String): ConnectionErrorEvent {
        val event = connectionErrorAdapter.fromJson(raw) ?: throw JsonDataException("Null connection error")
        return ConnectionErrorEvent(
            type = event.type,
            createdAt = event.createdAt.date,
            rawCreatedAt = event.createdAt.raw,
            connectionId = event.connectionId,
            error = event.error.toDomain(),
        )
    }

    @Suppress("DEPRECATION")
    private fun parseUnknownEvent(raw: String): UnknownEvent {
        val frame = UnknownFrame()
        reader(raw).use { reader ->
            reader.beginObject()
            while (reader.hasNext()) frame.read(reader.nextName(), reader)
        }
        val createdAt = frame.dates["created_at"] ?: throw JsonDataException("Required value 'created_at' missing")
        return UnknownEvent(
            type = frame.fields["type"] as? String ?: EventType.UNKNOWN,
            createdAt = createdAt.date,
            rawCreatedAt = createdAt.raw,
            user = frame.user?.let { with(domainMapping) { it.toDomain() } },
            rawData = frame.fields.withV1CustomEventShape(frame.dates),
        )
    }

    /** An unknown event's fields as read: its root dates exactly, its user as the generated model and as raw data. */
    private inner class UnknownFrame {
        val fields = mutableMapOf<String, Any?>()
        val dates = mutableMapOf<String, ExactDate>()
        var user: UserResponseCommonFields? = null

        fun read(name: String, reader: JsonReader) {
            when {
                name in ENVELOPE_DATE_KEYS && reader.peek() != JsonReader.Token.NULL ->
                    dates[name] = requireNotNull(exactDateAdapter.fromJson(reader))
                name == "user" -> {
                    user = reader.peekJson().use { userAdapter.fromJson(it) }
                    fields[name] = valueAdapter.fromJson(reader)
                }
                else -> fields[name] = valueAdapter.fromJson(reader)
            }
        }
    }

    private fun reader(raw: String): JsonReader = JsonReader.of(Buffer().writeUtf8(raw))

    private class Head(val type: String?, val hasCid: Boolean)

    private companion object {
        private const val CONNECTION_OK = "connection.ok"
        private const val FEEDS_EVENT_PREFIX = "feeds."

        /** The dates the backend declares at the root of every event; nested ones stay as read. */
        private val ENVELOPE_DATE_KEYS = setOf("created_at", "channel_last_message_at")
    }
}

/**
 * Gives an unknown event's raw data the v1 shape apps read their custom event fields from: the `custom` entries at
 * the root (the declared keys win, as v1's encoder skips custom keys that collide with them; `custom` itself is kept
 * unless the app sent a field of that name) and the root [dates] as their ISO strings. Nested objects keep the v2
 * shape: their dates stay unix nanoseconds, read as numbers, and their custom data stays under their own `custom`.
 */
@Deprecated(
    "v1 compatibility: v1 frames carry custom event fields at the root, v2 nests them under `custom`, so " +
        "UnknownEvent.rawData is reshaped to keep apps reading rawData[\"field\"] working. Remove in the next major, " +
        "so rawData carries the frame as sent, and document the change.",
)
internal fun Map<String, Any?>.withV1CustomEventShape(dates: Map<String, ExactDate>): Map<String, Any> {
    val custom = (this["custom"] as? Map<*, *>).orEmpty().entries.associate { (key, value) -> key.toString() to value }
    val envelope = mapOf("custom" to this["custom"])
    val flattened = envelope + custom + (this - "custom") + dates.mapValues { (_, date) -> date.raw }
    return flattened.mapNotNull { (key, value) -> value?.let { key to it } }.toMap()
}

/** The v2 connection error. Its error keeps the hand-written model: the backend sends `details` as raw JSON. */
@JsonClass(generateAdapter = true)
internal data class V2ConnectionErrorEvent(
    val type: String,
    @Json(name = "created_at") val createdAt: ExactDate,
    @Json(name = "connection_id") val connectionId: String,
    val error: ErrorDto,
)
