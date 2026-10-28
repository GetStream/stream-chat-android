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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.parser2.adapters.GENERATED_CHANNEL_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_FULL_USER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_MEMBER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_MESSAGE_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_OWN_USER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_USER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.LEGACY_CHANNEL_EXTRA_DATA_KEYS
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.Action
import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.PrivacySettingsResponse
import io.getstream.chat.android.network.models.ReactionRequest
import io.getstream.chat.android.network.models.ReadReceiptsResponse
import io.getstream.chat.android.network.models.TypingIndicatorsResponse
import io.getstream.chat.android.network.models.UserRequest
import io.getstream.chat.android.randomAttachment
import io.getstream.chat.android.randomDraftMessage
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMemberData
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.randomUserGroup
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

internal class DtoMappingTest {

    @Test
    fun `DraftMessage is correctly mapped to the generated request model`() {
        // Pinned rather than random: an empty extraData or a random `silent` would let a mapper
        // that drops either of them pass.
        val message = randomDraftMessage(
            silent = true,
            extraData = mapOf("draftKey" to "draftValue"),
            attachments = listOf(randomAttachment()),
            mentionedUsers = listOf(randomUser()),
            command = "giphy",
            args = "cat",
        )
        val mapping = Fixture().get()

        val request = with(mapping) { message.toMessageRequest() }

        val expected = MessageRequest(
            id = message.id,
            text = message.text,
            type = MessageRequest.Type.Regular,
            attachments = message.attachments.map { with(mapping) { it.toAttachmentRequest() } },
            mentionedUsers = message.mentionedUsersIds,
            parentId = message.parentId,
            quotedMessageId = message.replyMessage?.id,
            showInChannel = message.showInChannel,
            silent = message.silent,
            custom = message.extraData + mapOf("command" to "giphy", "args" to "cat"),
        )
        request shouldBeEqualTo expected
    }

    @Test
    fun `DraftMessage without an active command carries no command custom data`() {
        val message = randomDraftMessage(extraData = mapOf("draftKey" to "draftValue"))
        val mapping = Fixture().get()

        val request = with(mapping) { message.toMessageRequest() }

        request.custom shouldBeEqualTo mapOf("draftKey" to "draftValue")
    }

    @Test
    fun `Attachment is correctly mapped to the generated request model`() {
        val attachment = randomAttachment()
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.assetUrl shouldBeEqualTo attachment.assetUrl
        request.authorName shouldBeEqualTo attachment.authorName
        request.authorLink shouldBeEqualTo attachment.authorLink
        request.fallback shouldBeEqualTo attachment.fallback
        request.imageUrl shouldBeEqualTo attachment.imageUrl
        request.ogScrapeUrl shouldBeEqualTo attachment.ogUrl
        request.text shouldBeEqualTo attachment.text
        request.thumbUrl shouldBeEqualTo attachment.thumbUrl
        request.title shouldBeEqualTo attachment.title
        request.titleLink shouldBeEqualTo attachment.titleLink
        request.type shouldBeEqualTo attachment.type
        request.originalHeight shouldBeEqualTo attachment.originalHeight
        request.originalWidth shouldBeEqualTo attachment.originalWidth
    }

    @Test
    fun `Attachment folds the fields the spec omits into custom`() {
        // The generated model declares none of these, so they have to travel in `custom` for the
        // flattening adapter to write them back at the JSON root.
        val attachment = randomAttachment().copy(
            image = "https://example.com/i.png",
            name = "i.png",
            mimeType = "image/png",
            fileSize = 2048,
            extraData = mapOf("sentinel" to "keep-me"),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.custom shouldBeEqualTo mapOf(
            "sentinel" to "keep-me",
            "image" to "https://example.com/i.png",
            "name" to "i.png",
            "mime_type" to "image/png",
            "file_size" to 2048,
        )
    }

    @Test
    fun `Attachment sends the extraData keys the model declares as typed fields`() {
        val url = "https://giphy.com/h.gif"
        val image = mapOf("frames" to "1", "height" to "200", "size" to "100", "url" to url, "width" to "320")
        val giphy = listOf(
            "fixed_height",
            "fixed_height_downsampled",
            "fixed_height_still",
            "fixed_width",
            "fixed_width_downsampled",
            "fixed_width_still",
            "original",
        ).associateWith { image }
        val attachment = randomAttachment().copy(
            image = null,
            name = null,
            mimeType = null,
            extraData = mapOf(
                "color" to "red",
                "actions" to listOf(mapOf("name" to "image_action", "text" to "Send", "type" to "button")),
                "giphy" to giphy,
                "sentinel" to "keep-me",
            ),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.color shouldBeEqualTo "red"
        request.actions shouldBeEqualTo listOf(Action(name = "image_action", text = "Send", type = "button"))
        request.giphy?.fixedHeight?.url shouldBeEqualTo url
        request.fields shouldBeEqualTo null
        request.custom shouldBeEqualTo mapOf("sentinel" to "keep-me", "file_size" to attachment.fileSize)
    }

    @Test
    fun `Attachment fills an unset typed field from an extraData key named like it, and never sends the key as custom`() {
        val attachment = randomAttachment().copy(
            thumbUrl = null,
            titleLink = "https://example.com/field",
            originalWidth = null,
            image = null,
            name = null,
            mimeType = null,
            extraData = mapOf(
                "thumb_url" to "https://example.com/extra.png",
                "title_link" to "https://example.com/extra",
                "original_width" to 320.0,
                "sentinel" to "keep-me",
            ),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.thumbUrl shouldBeEqualTo "https://example.com/extra.png"
        request.titleLink shouldBeEqualTo "https://example.com/field"
        request.originalWidth shouldBeEqualTo 320
        request.custom shouldBeEqualTo mapOf("sentinel" to "keep-me", "file_size" to attachment.fileSize)
    }

    @Test
    fun `Attachment keeps an extraData key in custom data when it does not read as the typed field`() {
        val partialGiphy = mapOf("original" to mapOf("url" to "https://giphy.com/o.gif"))
        val attachment = randomAttachment().copy(
            image = null,
            name = null,
            mimeType = null,
            extraData = mapOf("giphy" to partialGiphy),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { attachment.toAttachmentRequest() }

        request.giphy shouldBeEqualTo null
        request.custom["giphy"] shouldBeEqualTo partialGiphy
    }

    @Test
    fun `Channel data sends its declared keys as typed fields and keeps the rest as custom data`() {
        val data = mapOf(
            "name" to "Channel",
            "members" to listOf(mapOf("user_id" to "amit", "channel_role" to "channel_moderator")),
            "filter_tags" to listOf("vip"),
            "config_overrides" to mapOf("replies" to false),
            "frozen" to "not a boolean",
        )
        val mapping = Fixture().get()

        val input = with(mapping) { data.toChannelInput() }

        input.members shouldBeEqualTo listOf(ChannelMemberRequest(userId = "amit", channelRole = "channel_moderator"))
        input.filterTags shouldBeEqualTo listOf("vip")
        input.configOverrides?.replies shouldBeEqualTo false
        input.frozen shouldBeEqualTo null
        // A value that doesn't read as its declared type stays custom data, as v1 sent it.
        input.custom shouldBeEqualTo mapOf("name" to "Channel", "frozen" to "not a boolean")
    }

    @Test
    fun `Channel data sends back none of the declared fields the channel adapters keep in extraData`() {
        val kept = GENERATED_CHANNEL_EXTRA_DATA_KEYS + LEGACY_CHANNEL_EXTRA_DATA_KEYS
        val typed = mapOf("auto_translation_enabled" to true, "auto_translation_language" to "it", "disabled" to true)
        val data = kept.associateWith { "from a response" } + typed + ("color" to "teal")
        val mapping = Fixture().get()

        with(mapping) {
            data.toChannelInput().run {
                custom shouldBeEqualTo mapOf("color" to "teal")
                autoTranslationLanguage shouldBeEqualTo "it"
                disabled shouldBeEqualTo true
            }
            data.toChannelInputRequest().run {
                custom shouldBeEqualTo mapOf("color" to "teal")
                autoTranslationEnabled shouldBeEqualTo true
            }
        }
    }

    @Test
    fun `Members send back none of the declared fields the member adapter keeps in extraData`() {
        val extraData = GENERATED_MEMBER_EXTRA_DATA_KEYS.associateWith { "from a response" } + ("color" to "teal")
        val mapping = Fixture().get()

        with(mapping) {
            MemberData("amit", extraData).toChannelMemberRequest().custom shouldBeEqualTo mapOf("color" to "teal")
            randomMember().copy(extraData = extraData).toChannelMemberRequest().custom shouldBeEqualTo
                mapOf("color" to "teal")
        }
    }

    @Test
    fun `Channel data keeps flat custom data of members and the creator, and skips null members`() {
        val data = mapOf(
            "members" to listOf(mapOf("user_id" to "amit", "is_vip" to true), null, "jaewoong"),
            "created_by" to mapOf("id" to "filip", "nickname" to "Chewie"),
        )
        val mapping = Fixture().get()

        val input = with(mapping) { data.toChannelInput() }

        input.members shouldBeEqualTo listOf(
            ChannelMemberRequest(userId = "amit", custom = mapOf("is_vip" to true)),
            ChannelMemberRequest(userId = "jaewoong"),
        )
        input.createdBy?.custom shouldBeEqualTo mapOf("nickname" to "Chewie")
        input.custom shouldBeEqualTo emptyMap()
    }

    @Test
    fun `Channel update data sends only the config overrides it carries`() {
        val data = mapOf("config_overrides" to mapOf("replies" to false))
        val mapping = Fixture().get()

        val input = with(mapping) { data.toChannelInputRequest() }

        input.configOverrides?.replies shouldBeEqualTo false
        input.configOverrides?.commands shouldBeEqualTo null
        input.configOverrides?.grants shouldBeEqualTo null
    }

    @Test
    fun `MemberData is correctly mapped to the generated request model`() {
        val memberData = randomMemberData(extraData = mapOf("sentinel" to "keep-me"))
        val mapping = Fixture().get()

        val request = with(mapping) { memberData.toChannelMemberRequest() }

        request.userId shouldBeEqualTo memberData.userId
        request.custom shouldBeEqualTo memberData.extraData
        // The endpoint takes the id; sending a whole user is neither needed nor serializable.
        request.user shouldBeEqualTo null
        request.channelRole shouldBeEqualTo null
    }

    @Test
    fun `MemberData sends a channel_role in its extra data as the member's role`() {
        val memberData = MemberData("amit", mapOf("channel_role" to "channel_moderator", "color" to "teal"))
        val mapping = Fixture().get()

        val request = with(mapping) { memberData.toChannelMemberRequest() }

        request.channelRole shouldBeEqualTo "channel_moderator"
        request.custom shouldBeEqualTo mapOf("color" to "teal")
    }

    @Test
    fun `MemberData keeps a channel_role that is not a string as custom data`() {
        val memberData = MemberData("amit", mapOf("channel_role" to 1))
        val mapping = Fixture().get()

        val request = with(mapping) { memberData.toChannelMemberRequest() }

        request.channelRole shouldBeEqualTo null
        request.custom shouldBeEqualTo mapOf("channel_role" to 1)
    }

    @Test
    fun `Member is correctly mapped to the generated request model`() {
        val member = randomMember(channelRole = "channel_moderator")
            .copy(user = randomUser(id = "leandro"), extraData = mapOf("sentinel" to "keep-me"))
        val mapping = Fixture().get()

        val request = with(mapping) { member.toChannelMemberRequest() }

        request.userId shouldBeEqualTo "leandro"
        request.channelRole shouldBeEqualTo "channel_moderator"
        request.custom shouldBeEqualTo mapOf("sentinel" to "keep-me")
        // The endpoint takes the id; sending a whole user is neither needed nor serializable.
        request.user shouldBeEqualTo null
    }

    @Test
    fun `Message is correctly mapped to the generated request model`() {
        // Pinned rather than random: the helper leaves attachments empty, which would let a mapper
        // that drops them pass.
        // Every collection is populated: the helper leaves them empty, so a mapper that drops one
        // maps to the same empty list the assertion expects.
        val message = randomMessage(
            type = MessageType.REGULAR,
            extraData = mutableMapOf("messageKey" to "messageValue"),
            attachments = listOf(randomAttachment()),
            mentionedUsers = listOf(randomUser()),
            mentionedGroups = listOf(randomUserGroup()),
            mentionedRoles = listOf(randomString()),
            restrictedVisibility = listOf(randomString()),
        )
        val messageTransformer = spy(NoOpMessageTransformer)
        val mapping = Fixture().withMessageTransformer(messageTransformer).get()

        val request = with(mapping) { message.toMessageRequest() }

        request.id shouldBeEqualTo message.id
        request.text shouldBeEqualTo message.text
        request.type shouldBeEqualTo MessageRequest.Type.fromString(MessageType.REGULAR)
        request.attachments shouldBeEqualTo message.attachments.map { with(mapping) { it.toAttachmentRequest() } }
        request.mentionedUsers shouldBeEqualTo message.mentionedUsersIds
        request.mentionedHere shouldBeEqualTo message.mentionedHere
        request.mentionedChannel shouldBeEqualTo message.mentionedChannel
        request.mentionedGroupIds shouldBeEqualTo message.mentionedGroups.map(UserGroup::id)
        request.mentionedRoles shouldBeEqualTo message.mentionedRoles
        request.parentId shouldBeEqualTo message.parentId
        request.pinExpires shouldBeEqualTo message.pinExpires
        request.pinned shouldBeEqualTo message.pinned
        request.pinnedAt shouldBeEqualTo message.pinnedAt
        request.quotedMessageId shouldBeEqualTo message.replyMessageId
        request.showInChannel shouldBeEqualTo message.showInChannel
        request.silent shouldBeEqualTo message.silent
        request.restrictedVisibility shouldBeEqualTo message.restrictedVisibility
        request.sharedLocation shouldBeEqualTo with(mapping) { message.sharedLocation?.toSharedLocation() }
        request.custom shouldBeEqualTo message.extraData
        verify(messageTransformer, times(1)).transform(message)
    }

    @Test
    fun `Message sends the declared fields it carries in extraData as typed fields`() {
        // Responses keep these in extraData, and sendPoll passes the poll id there.
        val message = randomMessage(
            type = MessageType.REGULAR,
            extraData = mutableMapOf(
                "poll_id" to "poll",
                "mml" to "<mml/>",
                "restricted_visibility" to listOf("amit"),
                "draft" to mapOf("channel_cid" to "messaging:a"),
                "image_labels" to mapOf("i" to listOf("cat")),
                "messageKey" to "messageValue",
            ),
            restrictedVisibility = emptyList(),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { message.toMessageRequest() }

        request.pollId shouldBeEqualTo "poll"
        request.mml shouldBeEqualTo "<mml/>"
        request.restrictedVisibility shouldBeEqualTo listOf("amit")
        // The response-only ones are not sent, so they are not stored as custom data.
        request.custom shouldBeEqualTo mapOf("messageKey" to "messageValue")
    }

    @Test
    fun `Message toMessageRequest sends the current group mentions, not the ids kept in extraData`() {
        val newGroup = randomUserGroup()
        val message = randomMessage(
            extraData = mutableMapOf("messageKey" to "messageValue", MENTIONED_GROUP_IDS_KEY to listOf("oldGroup")),
            mentionedGroups = listOf(newGroup),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { message.toMessageRequest() }

        request.mentionedGroupIds shouldBeEqualTo listOf(newGroup.id)
        request.custom shouldBeEqualTo mapOf("messageKey" to "messageValue")
    }

    @Test
    fun `Message request body clears the group mentions when an edit removes them all`() {
        val message = randomMessage(
            extraData = mutableMapOf(MENTIONED_GROUP_IDS_KEY to listOf("oldGroup")),
            mentionedGroups = emptyList(),
        )
        val mapping = Fixture().get()

        val json = Serializer.moshi.adapter(MessageRequest::class.java).toJson(with(mapping) { message.toMessageRequest() })

        Serializer.moshi.adapter(Map::class.java).fromJson(json)!!["mentioned_group_ids"] shouldBeEqualTo emptyList<String>()
    }

    @ParameterizedTest
    @MethodSource("messageTypeCoercionInput")
    fun `Message toMessageRequest coerces type to allowed upstream values`(
        inputType: String,
        expectedType: String,
    ) {
        val message = randomMessage(type = inputType)
        val mapping = Fixture().get()

        val request = with(mapping) { message.toMessageRequest() }

        request.type shouldBeEqualTo MessageRequest.Type.fromString(expectedType)
    }

    @Test
    fun `Reaction is correctly mapped to Dto`() {
        val reaction = randomReaction(emojiCode = "smile", extraData = mutableMapOf("k" to "v"))
        val mapping = Fixture().get()
        val dto = with(mapping) { reaction.toDto() }
        val expected = ReactionRequest(
            type = reaction.type,
            createdAt = reaction.createdAt,
            score = reaction.score,
            updatedAt = reaction.updatedAt,
            custom = mapOf("k" to "v", "emoji_code" to "smile"),
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `Reaction without an emojiCode omits emoji_code from custom`() {
        val reaction = randomReaction(emojiCode = null, extraData = mutableMapOf("k" to "v"))
        val mapping = Fixture().get()
        val dto = with(mapping) { reaction.toDto() }
        val expected = ReactionRequest(
            type = reaction.type,
            createdAt = reaction.createdAt,
            score = reaction.score,
            updatedAt = reaction.updatedAt,
            custom = mapOf("k" to "v"),
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `PrivacySettings is correctly mapped to Dto`() {
        val privacySettings = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = true),
            readReceipts = ReadReceipts(enabled = false),
            deliveryReceipts = DeliveryReceipts(enabled = false),
        )
        val mapping = Fixture().get()
        val dto = with(mapping) { privacySettings.toDto() }
        val expected = PrivacySettingsResponse(
            typingIndicators = TypingIndicatorsResponse(enabled = true),
            readReceipts = ReadReceiptsResponse(enabled = false),
            deliveryReceipts = DeliveryReceiptsResponse(enabled = false),
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `PrivacySettings with null deliveryReceipts is correctly mapped to Dto`() {
        val privacySettings = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = true),
            readReceipts = ReadReceipts(enabled = false),
            deliveryReceipts = null,
        )
        val mapping = Fixture().get()
        val dto = with(mapping) { privacySettings.toDto() }
        val expected = PrivacySettingsResponse(
            typingIndicators = TypingIndicatorsResponse(enabled = true),
            readReceipts = ReadReceiptsResponse(enabled = false),
            deliveryReceipts = null,
        )
        dto shouldBeEqualTo expected
    }

    @Test
    fun `User with privacy settings is correctly mapped to UserRequest`() {
        val userTransformer = spy(NoOpUserTransformer)
        val user = randomUser(
            privacySettings = PrivacySettings(
                typingIndicators = TypingIndicators(enabled = true),
                readReceipts = ReadReceipts(enabled = false),
                deliveryReceipts = DeliveryReceipts(enabled = true),
            ),
        )
        val mapping = Fixture()
            .withUserTransformer(userTransformer)
            .get()

        val request = with(mapping) { user.toUserRequest() }

        val expected = UserRequest(
            id = user.id,
            name = user.name,
            image = user.image,
            invisible = user.isInvisible,
            language = user.language,
            privacySettings = PrivacySettingsResponse(
                typingIndicators = TypingIndicatorsResponse(enabled = true),
                readReceipts = ReadReceiptsResponse(enabled = false),
                deliveryReceipts = DeliveryReceiptsResponse(enabled = true),
            ),
            custom = user.extraData,
        )
        request shouldBeEqualTo expected
        // Verify the transformer is called
        verify(userTransformer, times(1)).transform(user)
    }

    @Test
    fun `User sends back none of the declared fields the user adapters keep in extraData`() {
        val kept = GENERATED_USER_EXTRA_DATA_KEYS + GENERATED_FULL_USER_EXTRA_DATA_KEYS +
            GENERATED_OWN_USER_EXTRA_DATA_KEYS
        val user = randomUser(extraData = (kept.associateWith { "from a response" } + ("color" to "teal")).toMutableMap())
        val mapping = Fixture().get()

        val request = with(mapping) { user.toUserRequest() }

        request.custom shouldBeEqualTo mapOf("color" to "teal")
    }

    @Test
    fun `User with empty privacy settings maps each sub-setting to null`() {
        val user = randomUser(
            privacySettings = PrivacySettings(
                typingIndicators = null,
                readReceipts = null,
                deliveryReceipts = null,
            ),
        )
        val mapping = Fixture().get()

        val request = with(mapping) { user.toUserRequest() }

        request.privacySettings shouldBeEqualTo PrivacySettingsResponse(
            typingIndicators = null,
            readReceipts = null,
            deliveryReceipts = null,
        )
    }

    @Test
    fun `User without privacy settings is mapped to UserRequest with null privacy settings`() {
        val user = randomUser(privacySettings = null)
        val mapping = Fixture().get()

        val request = with(mapping) { user.toUserRequest() }

        val expected = UserRequest(
            id = user.id,
            name = user.name,
            image = user.image,
            invisible = user.isInvisible,
            language = user.language,
            privacySettings = null,
            custom = user.extraData,
        )
        request shouldBeEqualTo expected
    }

    @Test
    fun `User devices copy in extraData is not sent back in the UserRequest`() {
        val user = randomUser(extraData = mutableMapOf("devices" to listOf(mapOf("id" to "token")), "flair" to "gold"))
        val mapping = Fixture().get()

        val request = with(mapping) { user.toUserRequest() }

        request.custom shouldBeEqualTo mapOf("flair" to "gold")
    }

    companion object {
        @JvmStatic
        fun messageTypeCoercionInput(): List<Arguments> = listOf(
            Arguments.of(MessageType.REGULAR, MessageType.REGULAR),
            Arguments.of(MessageType.SYSTEM, MessageType.SYSTEM),
            Arguments.of(MessageType.REPLY, ""),
            Arguments.of(MessageType.EPHEMERAL, ""),
            Arguments.of(MessageType.ERROR, ""),
            Arguments.of(MessageType.FAILED, ""),
            Arguments.of("some-unknown-type", ""),
            Arguments.of("", ""),
        )
    }

    internal class Fixture {

        private var messageTransformer: MessageTransformer = NoOpMessageTransformer
        private var userTransformer: UserTransformer = NoOpUserTransformer

        fun withMessageTransformer(messageTransformer: MessageTransformer) = apply {
            this.messageTransformer = messageTransformer
        }

        fun withUserTransformer(userTransformer: UserTransformer) = apply {
            this.userTransformer = userTransformer
        }

        fun get(): DtoMapping = DtoMapping(
            messageTransformer = messageTransformer,
            userTransformer = userTransformer,
        )
    }

    @Test
    fun `The fields a message request sends back typed are among the declared fields responses copy to extraData`() {
        (SENT_BACK_MESSAGE_KEYS - GENERATED_MESSAGE_EXTRA_DATA_KEYS) shouldBeEqualTo emptySet()
    }
}
