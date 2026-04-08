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

package io.getstream.chat.android.client.parser2.event

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import java.util.Date

internal class ConfigAdapter(
    private val dateAdapter: JsonAdapter<Date>,
    private val commandAdapter: JsonAdapter<Command>,
) : JsonAdapter<Config>() {

    @Suppress("LongMethod")
    override fun fromJson(reader: JsonReader): Config? {
        if (reader.peek() == JsonReader.Token.NULL) return reader.nextNull()

        reader.beginObject()
        var createdAt: Date? = null
        var updatedAt: Date? = null
        var name: String? = null
        var typingEvents: Boolean? = null
        var readEvents: Boolean? = null
        var deliveryEvents: Boolean = true
        var connectEvents: Boolean? = null
        var search: Boolean? = null
        var reactions: Boolean? = null
        var replies: Boolean? = null
        var mutes: Boolean? = null
        var uploads: Boolean? = null
        var urlEnrichment: Boolean? = null
        var customEvents: Boolean? = null
        var pushNotifications: Boolean? = null
        var skipLastMsgUpdateForSystemMsgs: Boolean? = null
        var polls: Boolean? = null
        var messageRetention: String? = null
        var maxMessageLength: Int? = null
        var automod: String? = null
        var automodBehavior: String? = null
        var blocklistBehavior: String? = null
        var commands: List<Command>? = null
        var userMessageReminders: Boolean? = null
        var sharedLocations: Boolean? = null
        var markMessagesPending: Boolean? = null

        while (reader.hasNext()) {
            when (reader.nextName()) {
                "created_at" -> createdAt = dateAdapter.fromJson(reader)
                "updated_at" -> updatedAt = dateAdapter.fromJson(reader)
                "name" -> name = reader.nextString()
                "typing_events" -> typingEvents = reader.nextBoolean()
                "read_events" -> readEvents = reader.nextBoolean()
                "delivery_events" -> deliveryEvents = reader.nextBoolean()
                "connect_events" -> connectEvents = reader.nextBoolean()
                "search" -> search = reader.nextBoolean()
                "reactions" -> reactions = reader.nextBoolean()
                "replies" -> replies = reader.nextBoolean()
                "mutes" -> mutes = reader.nextBoolean()
                "uploads" -> uploads = reader.nextBoolean()
                "url_enrichment" -> urlEnrichment = reader.nextBoolean()
                "custom_events" -> customEvents = reader.nextBoolean()
                "push_notifications" -> pushNotifications = reader.nextBoolean()
                "skip_last_msg_update_for_system_msgs" -> skipLastMsgUpdateForSystemMsgs = reader.nextBoolean()
                "polls" -> polls = reader.nextBoolean()
                "message_retention" -> messageRetention = reader.nextString()
                "max_message_length" -> maxMessageLength = reader.nextInt()
                "automod" -> automod = reader.nextString()
                "automod_behavior" -> automodBehavior = reader.nextString()
                "blocklist_behavior" -> blocklistBehavior = reader.nextString()
                "commands" -> commands = JsonParsingUtils.parseList(reader, commandAdapter)
                "user_message_reminders" -> userMessageReminders = reader.nextBoolean()
                "shared_locations" -> sharedLocations = reader.nextBoolean()
                "mark_messages_pending" -> markMessagesPending = reader.nextBoolean()
                else -> reader.skipValue()
            }
        }
        reader.endObject()

        JsonParsingUtils.requireField(typingEvents, "typing_events", reader)
        JsonParsingUtils.requireField(readEvents, "read_events", reader)
        JsonParsingUtils.requireField(connectEvents, "connect_events", reader)
        JsonParsingUtils.requireField(search, "search", reader)
        JsonParsingUtils.requireField(reactions, "reactions", reader)
        JsonParsingUtils.requireField(replies, "replies", reader)
        JsonParsingUtils.requireField(mutes, "mutes", reader)
        JsonParsingUtils.requireField(uploads, "uploads", reader)
        JsonParsingUtils.requireField(urlEnrichment, "url_enrichment", reader)
        JsonParsingUtils.requireField(customEvents, "custom_events", reader)
        JsonParsingUtils.requireField(pushNotifications, "push_notifications", reader)
        JsonParsingUtils.requireField(polls, "polls", reader)
        JsonParsingUtils.requireField(messageRetention, "message_retention", reader)
        JsonParsingUtils.requireField(maxMessageLength, "max_message_length", reader)
        JsonParsingUtils.requireField(automod, "automod", reader)
        JsonParsingUtils.requireField(automodBehavior, "automod_behavior", reader)
        JsonParsingUtils.requireField(commands, "commands", reader)
        JsonParsingUtils.requireField(markMessagesPending, "mark_messages_pending", reader)

        return Config(
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = name ?: "",
            typingEventsEnabled = typingEvents,
            readEventsEnabled = readEvents,
            deliveryEventsEnabled = deliveryEvents,
            connectEventsEnabled = connectEvents,
            searchEnabled = search,
            isReactionsEnabled = reactions,
            isThreadEnabled = replies,
            muteEnabled = mutes,
            uploadsEnabled = uploads,
            urlEnrichmentEnabled = urlEnrichment,
            customEventsEnabled = customEvents,
            pushNotificationsEnabled = pushNotifications,
            skipLastMsgUpdateForSystemMsgs = skipLastMsgUpdateForSystemMsgs ?: false,
            pollsEnabled = polls,
            messageRetention = messageRetention,
            maxMessageLength = maxMessageLength,
            automod = automod,
            automodBehavior = automodBehavior,
            blocklistBehavior = blocklistBehavior ?: "",
            commands = commands,
            messageRemindersEnabled = userMessageReminders ?: false,
            sharedLocationsEnabled = sharedLocations ?: false,
            markMessagesPending = markMessagesPending,
        )
    }

    override fun toJson(p0: JsonWriter, p1: Config?) {
        error("Serialization not supported for direct-to-domain path")
    }
}
