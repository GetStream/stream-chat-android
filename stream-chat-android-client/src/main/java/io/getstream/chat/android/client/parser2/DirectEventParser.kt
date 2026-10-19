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

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.EVENT_USER_HIDDEN_CUSTOM_KEYS
import io.getstream.chat.android.client.api2.mapping.USER_HIDDEN_CUSTOM_KEYS
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser2.direct.AttachmentAdapter
import io.getstream.chat.android.client.parser2.direct.MessageAdapter
import io.getstream.chat.android.client.parser2.direct.NewMessageEventAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import io.getstream.log.taggedLogger
import okio.Buffer
import java.io.IOException
import java.util.Date

/**
 * Routes incoming JSON events to hand-written [JsonAdapter]s that parse directly into domain models, bypassing the
 * generated models. Returns `null` for unsupported event types, and for frames it can't read, so the caller can fall
 * back to the generated path. [mapping] maps them as the generated path does.
 */
internal class DirectEventParser(
    private val currentUserIdProvider: () -> UserId?,
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
    private val mapping: DomainMapping,
) {

    // region Leaf adapters

    private val moshi by lazy { Moshi.Builder().add(IsoDateAdapter()).build() }
    private val dateAdapter by lazy { moshi.adapter(Date::class.java) }
    private val attachmentAdapter by lazy { AttachmentAdapter(mapping) }
    private val reactionGroupAdapter by lazy { ReactionGroupAdapter(dateAdapter) }

    // endregion

    // region Composed adapters

    private val userAdapter by lazy { UserAdapter(dateAdapter, userTransformer, mapping, USER_HIDDEN_CUSTOM_KEYS) }

    // The user an event carries maps its custom data differently from the users of its message.
    private val eventUserAdapter by lazy {
        UserAdapter(dateAdapter, userTransformer, mapping, EVENT_USER_HIDDEN_CUSTOM_KEYS)
    }
    private val reactionAdapter by lazy { ReactionAdapter(userAdapter, dateAdapter, mapping) }
    private val messageAdapter by lazy {
        MessageAdapter(
            attachmentAdapter,
            reactionAdapter,
            reactionGroupAdapter,
            userAdapter,
            dateAdapter,
            messageTransformer,
            mapping,
        )
    }

    // endregion

    // region Event adapters

    private val newMessageEventAdapter by lazy { NewMessageEventAdapter(messageAdapter, eventUserAdapter) }

    // endregion

    /** Registry mapping event type strings to their direct adapters. */
    private val adapterMap: Map<String, JsonAdapter<out ChatEvent>> by lazy {
        mapOf(EventType.MESSAGE_NEW to newMessageEventAdapter)
    }

    /**
     * Attempts to parse [raw] JSON into a [ChatEvent] using a direct adapter.
     * Returns `null` if the event type is not supported by any direct adapter, or if the frame can't be read.
     */
    fun parse(raw: String): ChatEvent? = parse(raw, extractType(raw))

    /** [parse] for a frame whose [type] the caller has already read, so the frame isn't scanned for it again. */
    fun parse(raw: String, type: String?): ChatEvent? {
        val adapter = adapterMap[type] ?: return null
        // Only a frame it can't read is left to the generated path. Anything else, such as an app transformer
        // throwing, fails as it would there, rather than running the transformers a second time.
        return try {
            adapter.fromJson(raw)
        } catch (e: JsonDataException) {
            logger.w { "Direct parse failed for '$type'; falling back to the generated path: ${e.message}" }
            null
        } catch (e: IOException) {
            logger.w { "Direct parse failed for '$type'; falling back to the generated path: ${e.message}" }
            null
        }
    }

    companion object {

        private val logger by taggedLogger("DirectEventParser")

        /**
         * Extracts the `"type"` field value from the top level of a JSON object
         * using a streaming [JsonReader]. Stops as soon as the field is found.
         */
        @Suppress("NestedBlockDepth")
        internal fun extractType(raw: String): String? {
            if (raw.isBlank()) return null
            val reader = JsonReader.of(Buffer().writeUtf8(raw))
            return try {
                reader.use {
                    if (it.peek() != JsonReader.Token.BEGIN_OBJECT) return null
                    it.beginObject()
                    while (it.hasNext()) {
                        if (it.nextName() == "type") {
                            return if (it.peek() == JsonReader.Token.NULL) {
                                it.nextNull<String>()
                            } else {
                                it.nextString()
                            }
                        } else {
                            it.skipValue()
                        }
                    }
                    null
                }
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                logger.v { "extractType failed; falling back to the generated path: ${e.message}" }
                null
            }
        }
    }
}
