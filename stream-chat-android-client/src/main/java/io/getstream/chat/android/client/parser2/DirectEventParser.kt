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
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.direct.AttachmentAdapter
import io.getstream.chat.android.client.parser2.direct.ChannelInfoAdapter
import io.getstream.chat.android.client.parser2.direct.DeviceAdapter
import io.getstream.chat.android.client.parser2.direct.LocationAdapter
import io.getstream.chat.android.client.parser2.direct.MessageAdapter
import io.getstream.chat.android.client.parser2.direct.MessageModerationDetailsAdapter
import io.getstream.chat.android.client.parser2.direct.MessageReminderInfoAdapter
import io.getstream.chat.android.client.parser2.direct.ModerationAdapter
import io.getstream.chat.android.client.parser2.direct.NewMessageEventAdapter
import io.getstream.chat.android.client.parser2.direct.OptionAdapter
import io.getstream.chat.android.client.parser2.direct.PollAdapter
import io.getstream.chat.android.client.parser2.direct.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.UserId
import io.getstream.chat.android.models.UserTransformer
import io.getstream.log.taggedLogger
import okio.Buffer
import java.util.Date

/**
 * Routes incoming JSON events to hand-written [JsonAdapter]s that parse directly into domain models,
 * bypassing the DTO intermediate layer. Returns `null` for unsupported event types so the caller
 * can fall back to the existing DTO path.
 */
internal class DirectEventParser(
    private val currentUserIdProvider: () -> UserId?,
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

    // region Leaf adapters

    private val moshi by lazy { Moshi.Builder().add(Date::class.java, DateAdapter()).build() }
    private val dateAdapter by lazy { moshi.adapter(Date::class.java) }
    private val deviceAdapter by lazy { DeviceAdapter() }
    private val privacySettingsAdapter by lazy { PrivacySettingsAdapter() }
    private val attachmentAdapter by lazy { AttachmentAdapter() }
    private val channelInfoAdapter by lazy { ChannelInfoAdapter() }
    private val moderationDetailsAdapter by lazy { MessageModerationDetailsAdapter() }
    private val moderationAdapter by lazy { ModerationAdapter() }
    private val optionAdapter by lazy { OptionAdapter() }
    private val locationAdapter by lazy { LocationAdapter(dateAdapter) }
    private val reactionGroupAdapter by lazy { ReactionGroupAdapter(dateAdapter) }

    // endregion

    // region Composed adapters

    private val userAdapter by lazy {
        UserAdapter(deviceAdapter, privacySettingsAdapter, dateAdapter, userTransformer)
    }
    private val reactionAdapter by lazy { ReactionAdapter(userAdapter, dateAdapter) }
    private val pollAdapter by lazy {
        PollAdapter(userAdapter, optionAdapter, dateAdapter, currentUserIdProvider)
    }
    private val reminderAdapter by lazy { MessageReminderInfoAdapter(dateAdapter) }
    private val messageAdapter by lazy {
        MessageAdapter(
            attachmentAdapter, channelInfoAdapter, reactionAdapter,
            reactionGroupAdapter, userAdapter, moderationDetailsAdapter, moderationAdapter,
            pollAdapter, reminderAdapter, locationAdapter, dateAdapter, messageTransformer,
        )
    }

    // endregion

    // region Event adapters

    private val newMessageEventAdapter by lazy {
        NewMessageEventAdapter(messageAdapter, userAdapter)
    }

    // endregion

    /** Registry mapping event type strings to their direct adapters. */
    private val adapterMap: Map<String, JsonAdapter<out ChatEvent>> by lazy {
        mapOf(EventType.MESSAGE_NEW to newMessageEventAdapter)
    }

    /**
     * Attempts to parse [raw] JSON into a [ChatEvent] using a direct adapter.
     * Returns `null` if the event type is not supported by any direct adapter.
     */
    fun parse(raw: String): ChatEvent? {
        val type = extractType(raw) ?: return null
        val adapter = adapterMap[type] ?: return null
        return adapter.fromJson(raw)
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
                logger.v { "extractType failed; falling back to DTO path: ${e.message}" }
                null
            }
        }
    }
}
