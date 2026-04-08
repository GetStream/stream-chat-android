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
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.AttachmentAdapter
import io.getstream.chat.android.client.parser2.event.ChannelAdapter
import io.getstream.chat.android.client.parser2.event.ChannelInfoAdapter
import io.getstream.chat.android.client.parser2.event.CommandAdapter
import io.getstream.chat.android.client.parser2.event.ConfigAdapter
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.event.LocationAdapter
import io.getstream.chat.android.client.parser2.event.MemberAdapter
import io.getstream.chat.android.client.parser2.event.MessageAdapter
import io.getstream.chat.android.client.parser2.event.MessageModerationDetailsAdapter
import io.getstream.chat.android.client.parser2.event.MessageReminderInfoAdapter
import io.getstream.chat.android.client.parser2.event.ModerationAdapter
import io.getstream.chat.android.client.parser2.event.OptionAdapter
import io.getstream.chat.android.client.parser2.event.PollAdapter
import io.getstream.chat.android.client.parser2.event.PrivacySettingsAdapter
import io.getstream.chat.android.client.parser2.event.ReactionAdapter
import io.getstream.chat.android.client.parser2.event.ReactionGroupAdapter
import io.getstream.chat.android.client.parser2.event.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.ChannelTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class ChannelParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(DateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)

    private val deviceAdapter = DeviceAdapter()
    private val privacySettingsAdapter = PrivacySettingsAdapter()
    private val userAdapter = UserAdapter(
        deviceAdapter = deviceAdapter,
        privacySettingsAdapter = privacySettingsAdapter,
        dateAdapter = dateAdapter,
        userTransformer = NoOpUserTransformer,
    )
    private val memberAdapter = MemberAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
    )
    private val reactionAdapter = ReactionAdapter(
        userAdapter = userAdapter,
        dateAdapter = dateAdapter,
    )
    private val reactionGroupAdapter = ReactionGroupAdapter(
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
        moderationDetailsAdapter = moderationDetailsAdapter,
        moderationAdapter = moderationAdapter,
        pollAdapter = pollAdapter,
        reminderAdapter = reminderAdapter,
        locationAdapter = locationAdapter,
        dateAdapter = dateAdapter,
        messageTransformer = NoOpMessageTransformer,
    )
    private val configAdapter = ConfigAdapter(
        dateAdapter = dateAdapter,
        commandAdapter = CommandAdapter(),
    )

    private val channelAdapter = ChannelAdapter(
        messageAdapter = messageAdapter,
        memberAdapter = memberAdapter,
        userAdapter = userAdapter,
        configAdapter = configAdapter,
        locationAdapter = locationAdapter,
        dateAdapter = dateAdapter,
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
    )

    // region DTO path (JSON → DownstreamChannelDto → Channel)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ChannelTestData.jsonAllFields, DownstreamChannelDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(ChannelTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with nested collections`() {
        val dto = parser.fromJson(ChannelTestData.jsonWithNestedCollections, DownstreamChannelDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(ChannelTestData.expectedWithNestedCollections, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ChannelTestData.jsonOptionalFieldsMissing, DownstreamChannelDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(ChannelTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Channel via ChannelAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = channelAdapter.fromJson(ChannelTestData.jsonAllFields)
        assertEquals(ChannelTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with nested collections`() {
        val domain = channelAdapter.fromJson(ChannelTestData.jsonWithNestedCollections)
        assertEquals(ChannelTestData.expectedWithNestedCollections, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = channelAdapter.fromJson(ChannelTestData.jsonOptionalFieldsMissing)
        assertEquals(ChannelTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelTestData.jsonMissingCid, DownstreamChannelDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing cid`() {
        assertThrows<JsonDataException> {
            channelAdapter.fromJson(ChannelTestData.jsonMissingCid)
        }
    }

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelTestData.jsonMissingId, DownstreamChannelDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            channelAdapter.fromJson(ChannelTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing type`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelTestData.jsonMissingType, DownstreamChannelDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing type`() {
        assertThrows<JsonDataException> {
            channelAdapter.fromJson(ChannelTestData.jsonMissingType)
        }
    }

    @Test
    fun `DTO path - throws on missing frozen`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelTestData.jsonMissingFrozen, DownstreamChannelDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing frozen`() {
        assertThrows<JsonDataException> {
            channelAdapter.fromJson(ChannelTestData.jsonMissingFrozen)
        }
    }

    @Test
    fun `DTO path - throws on missing config`() {
        assertThrows<JsonDataException> {
            parser.fromJson(ChannelTestData.jsonMissingConfig, DownstreamChannelDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing config`() {
        assertThrows<JsonDataException> {
            channelAdapter.fromJson(ChannelTestData.jsonMissingConfig)
        }
    }

    // endregion
}
