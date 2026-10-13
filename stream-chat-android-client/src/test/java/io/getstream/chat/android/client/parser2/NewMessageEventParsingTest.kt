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

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.api2.model.dto.ChatEventDto
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
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
import io.getstream.chat.android.client.parser2.direct.ReactionAdapter
import io.getstream.chat.android.client.parser2.direct.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.direct.UserGroupAdapter
import io.getstream.chat.android.client.parser2.direct.UserGroupMemberAdapter
import io.getstream.chat.android.client.parser2.testdata.NewMessageEventTestData
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class NewMessageEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val eventMapping = EventMapping(domainMapping)

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)

    private val deviceAdapter = DeviceAdapter()
    private val userAdapter = UserAdapter(
        deviceAdapter = deviceAdapter,
        dateAdapter = dateAdapter,
        userTransformer = NoOpUserTransformer,
    )
    private val reactionAdapter = ReactionAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
    )
    private val reactionGroupAdapter = ReactionGroupAdapter(
        dateAdapter = dateAdapter,
    )
    private val userGroupMemberAdapter = UserGroupMemberAdapter(
        dateAdapter = dateAdapter,
    )
    private val userGroupAdapter = UserGroupAdapter(
        memberAdapter = userGroupMemberAdapter,
        dateAdapter = dateAdapter,
    )
    private val attachmentAdapter = AttachmentAdapter()
    private val channelInfoAdapter = ChannelInfoAdapter()
    private val moderationDetailsAdapter = MessageModerationDetailsAdapter()
    private val moderationAdapter = ModerationAdapter()
    private val optionAdapter = OptionAdapter()
    private val pollAdapter = PollAdapter(
        userAdapter = userAdapter,
        optionAdapter = optionAdapter,
        dateAdapter = dateAdapter,
        currentUserIdProvider = { "" },
    )
    private val reminderAdapter = MessageReminderInfoAdapter(
        dateAdapter = dateAdapter,
    )
    private val locationAdapter = LocationAdapter(
        dateAdapter = dateAdapter,
    )
    private val messageAdapter = MessageAdapter(
        attachmentAdapter = attachmentAdapter,
        channelInfoAdapter = channelInfoAdapter,
        reactionAdapter = reactionAdapter,
        reactionGroupAdapter = reactionGroupAdapter,
        userAdapter = userAdapter,
        userGroupAdapter = userGroupAdapter,
        moderationDetailsAdapter = moderationDetailsAdapter,
        moderationAdapter = moderationAdapter,
        pollAdapter = pollAdapter,
        reminderAdapter = reminderAdapter,
        locationAdapter = locationAdapter,
        dateAdapter = dateAdapter,
        messageTransformer = NoOpMessageTransformer,
    )

    private val adapter = NewMessageEventAdapter(
        messageAdapter = messageAdapter,
        userAdapter = userAdapter,
    )

    private fun generated(json: String): ChatEvent =
        with(eventMapping) { parser.fromJson(json, ChatEventDto::class.java).toDomain() }

    // region Both paths produce identical events

    @Test
    fun `Both paths - all fields populated produce identical events`() {
        // The fixture transitively pulls in MessageTestData.jsonAllFields, which is the
        // truly-comprehensive Message JSON. Parity here is the meaningful check; a
        // hand-written expected adds maintenance burden without commensurate value.
        val parsed = parser.fromJson(NewMessageEventTestData.jsonAllFields, ChatEventDto::class.java)
        val generatedResult = with(eventMapping) { parsed.toDomain() }
        val directResult = adapter.fromJson(NewMessageEventTestData.jsonAllFields)
        assertEquals(generatedResult, directResult, "Generated path and direct path produced different NewMessageEvents")
    }

    @Test
    fun `Both paths - optional fields missing fall back to identical defaults`() {
        val parsed = parser.fromJson(NewMessageEventTestData.jsonOptionalFieldsMissing, ChatEventDto::class.java)
        val generatedResult = with(eventMapping) { parsed.toDomain() }
        val directResult = adapter.fromJson(NewMessageEventTestData.jsonOptionalFieldsMissing)
        assertEquals(generatedResult, directResult)
        assertEquals(NewMessageEventTestData.expectedOptionalFieldsMissing, generatedResult)
        assertEquals(NewMessageEventTestData.expectedOptionalFieldsMissing, directResult)
    }

    @Test
    fun `Both paths - explicit null counts fall back to zero`() {
        val json = NewMessageEventTestData.jsonOptionalFieldsMissing.replaceFirst(
            "{",
            """{"watcher_count": null, "total_unread_count": null, "unread_channels": null,""",
        )
        assertEquals(NewMessageEventTestData.expectedOptionalFieldsMissing, generated(json))
        assertEquals(NewMessageEventTestData.expectedOptionalFieldsMissing, adapter.fromJson(json))
    }

    @Test
    fun `Both paths - read event user devices leniently and keep undeclared user fields as extra data`() {
        val json = NewMessageEventTestData.jsonUserWithDevicesAndOwnUserFields
        val direct = adapter.fromJson(json)!!

        assertEquals(generated(json), direct)
        assertEquals(
            listOf(
                Device(token = "device-1", pushProvider = PushProvider.FIREBASE, providerName = "Firebase"),
                Device(token = "device-2", pushProvider = PushProvider.fromKey(""), providerName = null),
            ),
            direct.user.devices,
        )
        assertEquals(
            setOf("invisible", "privacy_settings", "total_unread_count"),
            direct.user.extraData.keys,
        )
    }

    @Test
    fun `Both paths - propagate event-level channelInfo to replyTo when neither message has channel`() {
        val parsed = parser.fromJson(NewMessageEventTestData.jsonQuotedMessageNoChannel, ChatEventDto::class.java)
        val generatedResult = with(eventMapping) { parsed.toDomain() }
        val directResult = adapter.fromJson(NewMessageEventTestData.jsonQuotedMessageNoChannel)
        assertEquals(generatedResult, directResult)
        // Guard the specific parity gap this test covers: replyTo.channelInfo must be populated
        // from event-level data when neither the outer message nor quoted_message had `channel`.
        val replyToChannelInfo = checkNotNull(directResult?.message?.replyTo?.channelInfo)
        assertEquals("messaging:general", replyToChannelInfo.cid)
        assertEquals("general", replyToChannelInfo.id)
        assertEquals("messaging", replyToChannelInfo.type)
    }

    // endregion

    // region Error message parity

    @Test
    fun `Generated path - an event without a type parses as an unknown event`() {
        val event = generated(NewMessageEventTestData.jsonMissingType)
        assertTrue(event is UnknownEvent, "Expected an UnknownEvent, got $event")
    }

    @Test
    fun `Direct path - throws on missing type`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingType)
        }
    }

    @Test
    fun `Generated path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingCreatedAt, ChatEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingCreatedAt)
        }
    }

    @Test
    fun `Generated path - throws on missing user`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingUser, ChatEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing user`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingUser)
        }
    }

    @Test
    fun `Generated path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingCid, ChatEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingCid)
        }
    }

    @Test
    fun `Both paths - take the channel type from the cid`() {
        val json = NewMessageEventTestData.jsonMissingChannelType
        assertEquals("messaging", (generated(json) as NewMessageEvent).channelType)
        assertEquals("messaging", adapter.fromJson(json)?.channelType)
    }

    @Test
    fun `Both paths - take the channel id from the cid`() {
        val json = NewMessageEventTestData.jsonMissingChannelId
        assertEquals("general", (generated(json) as NewMessageEvent).channelId)
        assertEquals("general", adapter.fromJson(json)?.channelId)
    }

    @Test
    fun `Generated path - throws on missing message`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMissingMessage, ChatEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing message`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMissingMessage)
        }
    }

    @Test
    fun `Generated path - throws on malformed created_at`() {
        assertThrows<JsonDataException> {
            parser.fromJson(NewMessageEventTestData.jsonMalformedCreatedAt, ChatEventDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on malformed created_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(NewMessageEventTestData.jsonMalformedCreatedAt)
        }
    }

    // endregion
}
