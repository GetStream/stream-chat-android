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

import com.squareup.moshi.JsonAdapter
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.parser2.adapters.GENERATED_CHANNEL_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_FULL_USER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_MEMBER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_MESSAGE_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_OWN_USER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.GENERATED_USER_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.LEGACY_CHANNEL_EXTRA_DATA_KEYS
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChatPreferences
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageTransformer
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.Action
import io.getstream.chat.android.network.models.ChannelConfigOverrides
import io.getstream.chat.android.network.models.ChannelGetOrCreateRequest
import io.getstream.chat.android.network.models.ChannelInput
import io.getstream.chat.android.network.models.ChannelInputRequest
import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.ChatPreferencesInput
import io.getstream.chat.android.network.models.ConfigOverridesRequest
import io.getstream.chat.android.network.models.DeliveryReceiptsResponse
import io.getstream.chat.android.network.models.Field
import io.getstream.chat.android.network.models.Images
import io.getstream.chat.android.network.models.MessagePaginationParams
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.PaginationParams
import io.getstream.chat.android.network.models.PrivacySettingsResponse
import io.getstream.chat.android.network.models.ReactionRequest
import io.getstream.chat.android.network.models.ReadReceiptsResponse
import io.getstream.chat.android.network.models.SharedLocation
import io.getstream.chat.android.network.models.TypingIndicatorsResponse
import io.getstream.chat.android.network.models.UserRequest
import java.util.Date
import io.getstream.chat.android.network.models.Attachment as AttachmentRequest

@Suppress("TooManyFunctions")
internal class DtoMapping(
    private val messageTransformer: MessageTransformer,
    private val userTransformer: UserTransformer,
) {

    private val supportedUpstreamMessageTypes = setOf(MessageType.REGULAR, MessageType.SYSTEM)

    private val dateFormatter = StreamDateFormatter()

    /**
     * Maps the domain [Attachment] to the generated network [AttachmentRequest] model.
     *
     * The spec does not declare `file_size`, `image`, `mime_type` or `name`, so they go through `custom`: the v1
     * adapter flattens it to the root and v2 sends it nested, and the backend stores them as custom data either way.
     * `file_size` stays an Int here: parsing yields a Double for an undeclared number, and writing that would change
     * `1` to `1.0`.
     *
     * An extra data key named like a typed field fills that field when it is unset, and is never sent as custom data,
     * which the backend rejects for the multi-word field names.
     */
    internal fun Attachment.toAttachmentRequest(): AttachmentRequest {
        val custom = extraData.toMutableMap()
        image?.let { custom["image"] = it }
        name?.let { custom["name"] = it }
        mimeType?.let { custom["mime_type"] = it }
        custom["file_size"] = fileSize
        return AttachmentRequest(
            authorIcon = custom.takeString("author_icon"),
            color = custom.takeString("color"),
            footer = custom.takeString("footer"),
            footerIcon = custom.takeString("footer_icon"),
            pretext = custom.takeString("pretext"),
            actions = custom.takeList("actions", actionAdapter),
            fields = custom.takeList("fields", fieldAdapter),
            giphy = custom.takeTyped("giphy") { imagesAdapter.fromJsonValue(it) },
            assetUrl = custom.takeString("asset_url").let { assetUrl ?: it },
            authorName = custom.takeString("author_name").let { authorName ?: it },
            fallback = custom.takeString("fallback").let { fallback ?: it },
            imageUrl = custom.takeString("image_url").let { imageUrl ?: it },
            ogScrapeUrl = custom.takeString("og_scrape_url").let { ogUrl ?: it },
            text = custom.takeString("text").let { text ?: it },
            thumbUrl = custom.takeString("thumb_url").let { thumbUrl ?: it },
            title = custom.takeString("title").let { title ?: it },
            titleLink = custom.takeString("title_link").let { titleLink ?: it },
            authorLink = custom.takeString("author_link").let { authorLink ?: it },
            type = custom.takeString("type").let { type ?: it },
            originalHeight = custom.takeInt("original_height").let { originalHeight ?: it },
            originalWidth = custom.takeInt("original_width").let { originalWidth ?: it },
            custom = custom,
        )
    }

    private val actionAdapter by lazy { Serializer.moshi.adapter(Action::class.java) }
    private val channelConfigOverridesAdapter by lazy { Serializer.moshi.adapter(ChannelConfigOverrides::class.java) }
    private val configOverridesRequestAdapter by lazy { Serializer.moshi.adapter(ConfigOverridesRequest::class.java) }
    private val userRequestAdapter by lazy { Serializer.moshi.adapter(UserRequest::class.java) }
    private val channelMemberRequestAdapter by lazy { Serializer.moshi.adapter(ChannelMemberRequest::class.java) }
    private val fieldAdapter by lazy { Serializer.moshi.adapter(Field::class.java) }
    private val imagesAdapter by lazy { Serializer.moshi.adapter(Images::class.java) }

    /**
     * Removes [key] and returns its value read as a typed field, so it is sent where v2 reads it (v2 ignores these
     * keys under `custom`, and v1 writes typed fields at the root like flattened custom data). A value that doesn't
     * read as the type stays in custom data.
     */
    private fun <T> MutableMap<String, Any>.takeTyped(key: String, read: (Any) -> T?): T? {
        val value = this[key] ?: return null
        return runCatching { read(value) }.getOrNull()?.also { remove(key) }
    }

    private fun MutableMap<String, Any>.takeString(key: String): String? = takeTyped(key) { it as? String }

    private fun MutableMap<String, Any>.takeInt(key: String): Int? = takeTyped(key) { (it as? Number)?.toInt() }

    private fun MutableMap<String, Any>.takeStrings(key: String): List<String>? =
        takeTyped(key) { list -> (list as List<*>).map { it as String } }

    private fun <T> MutableMap<String, Any>.takeList(key: String, adapter: JsonAdapter<T>): List<T>? =
        takeTyped(key) { list -> (list as List<*>).map { adapter.fromJsonValue(it)!! } }

    /**
     * Maps the domain [MemberData] to the generated network [ChannelMemberRequest] model.
     *
     * A string `channel_role` in the extra data sets the member's role, as it does for the members in channel data,
     * instead of being stored as custom data.
     */
    internal fun MemberData.toChannelMemberRequest(): ChannelMemberRequest {
        val custom = (extraData - RESPONSE_ONLY_MEMBER_KEYS).toMutableMap()
        return ChannelMemberRequest(
            userId = userId,
            channelRole = custom.takeString("channel_role"),
            user = null,
            custom = custom,
        )
    }

    /**
     * Maps the domain [Member] to the generated network [ChannelMemberRequest] model.
     *
     * The query endpoint hashes the user ids to resolve a distinct channel and reads nothing else, but the role
     * and custom data are carried anyway since the domain member has them. `user` stays absent: the member is
     * identified by its user id.
     */
    internal fun Member.toChannelMemberRequest(): ChannelMemberRequest = ChannelMemberRequest(
        userId = getUserId(),
        channelRole = channelRole,
        user = null,
        custom = extraData - RESPONSE_ONLY_MEMBER_KEYS,
    )

    /**
     * Maps the public [QueryChannelRequest] to the generated network [ChannelGetOrCreateRequest] model.
     *
     * The `data` map goes through [toChannelInput]. Pagination keys the generated models do not declare are not sent.
     */
    internal fun QueryChannelRequest.toChannelGetOrCreateRequest(): ChannelGetOrCreateRequest =
        ChannelGetOrCreateRequest(
            state = state,
            watch = watch,
            presence = presence,
            includeScheduledMessages = includeScheduledMessages.takeIf { it },
            // Always present, even when empty: the backend decodes them into pointers.
            messages = messages.toMessagePaginationParams(),
            members = members.toPaginationParams(),
            watchers = watchers.toPaginationParams(),
            data = data.toChannelInput(),
        )

    /**
     * Splits a channel `data` map into the declared [ChannelInput] fields and custom data. v1 flattened the whole map
     * into `data`, where the backend matched declared keys first; v2 reads custom data only under `custom`, so the
     * declared keys go to their typed fields. A value that doesn't read as its type stays in custom data.
     */
    internal fun Map<String, Any>.toChannelInput(): ChannelInput {
        val custom = toMutableMap()
        custom.keys.removeAll(RESPONSE_ONLY_CHANNEL_KEYS)
        val shared = SharedChannelData(custom)
        return ChannelInput(
            autoTranslationEnabled = shared.autoTranslationEnabled,
            autoTranslationLanguage = shared.autoTranslationLanguage,
            disabled = shared.disabled,
            frozen = shared.frozen,
            team = shared.team,
            invites = shared.invites,
            members = shared.members,
            createdBy = shared.createdBy,
            createdById = custom.takeString("created_by_id"),
            truncatedById = custom.takeString("truncated_by_id"),
            filterTags = custom.takeStrings("filter_tags"),
            configOverrides = custom.takeTyped("config_overrides") { channelConfigOverridesAdapter.fromJsonValue(it) },
            custom = custom,
        )
    }

    /** The update counterpart of [toChannelInput]. */
    internal fun Map<String, Any>.toChannelInputRequest(): ChannelInputRequest {
        val custom = toMutableMap()
        custom.keys.removeAll(RESPONSE_ONLY_CHANNEL_KEYS)
        val shared = SharedChannelData(custom)
        return ChannelInputRequest(
            autoTranslationEnabled = shared.autoTranslationEnabled,
            autoTranslationLanguage = shared.autoTranslationLanguage,
            disabled = shared.disabled,
            frozen = shared.frozen,
            team = shared.team,
            invites = shared.invites ?: emptyList(),
            members = shared.members ?: emptyList(),
            createdBy = shared.createdBy,
            configOverrides = custom.takeTyped("config_overrides") { value ->
                val overrides = value as Map<*, *>
                val read = configOverridesRequestAdapter.fromJsonValue(overrides)!!
                // Its defaults would also send "commands":[] and "grants":{}, overriding what the caller left alone.
                read.copy(
                    commands = read.commands.takeIf { "commands" in overrides },
                    grants = read.grants.takeIf { "grants" in overrides },
                )
            },
            custom = custom,
        )
    }

    /** The declared `data` keys [ChannelInput] and [ChannelInputRequest] share, taken out of [custom]. */
    private inner class SharedChannelData(custom: MutableMap<String, Any>) {
        val autoTranslationEnabled = custom.takeTyped("auto_translation_enabled") { it as? Boolean }
        val autoTranslationLanguage = custom.takeString("auto_translation_language")
        val disabled = custom.takeTyped("disabled") { it as? Boolean }
        val frozen = custom.takeTyped("frozen") { it as? Boolean }
        val team = custom.takeString("team")
        val invites = custom.takeMembers("invites")
        val members = custom.takeMembers("members")
        val createdBy = custom.takeTyped("created_by") { user ->
            userRequestAdapter.fromJsonValue(user.nestingCustomData(USER_REQUEST_KEYS))
        }
    }

    /** Members as typed requests, user ids or JSON-shaped maps; null entries are skipped. */
    private fun MutableMap<String, Any>.takeMembers(key: String): List<ChannelMemberRequest>? =
        takeTyped(key) { list ->
            (list as List<*>).filterNotNull().map { member ->
                when (member) {
                    is ChannelMemberRequest -> member
                    is String -> ChannelMemberRequest(userId = member)
                    else -> channelMemberRequestAdapter.fromJsonValue(member.nestingCustomData(MEMBER_REQUEST_KEYS))!!
                }
            }
        }

    /**
     * A JSON-shaped map with its undeclared keys moved under `custom`, the way v1 callers write custom data flat.
     * Reading it as is would drop them.
     */
    private fun Any.nestingCustomData(declared: Set<String>): Map<String, Any?> {
        val map = this as Map<*, *>
        val flat = map.filterKeys { it !in declared }.mapKeys { it.key.toString() }
        val nested = (map["custom"] as? Map<*, *>).orEmpty().mapKeys { it.key.toString() }
        val declaredPart = map.filterKeys { it in declared && it != "custom" }.mapKeys { it.key.toString() }
        val custom = nested + flat
        return if (custom.isEmpty()) declaredPart else declaredPart + ("custom" to custom)
    }

    private fun Map<String, Any>.toMessagePaginationParams() = MessagePaginationParams(
        limit = int("limit"),
        idGt = this[Pagination.GREATER_THAN.toString()] as? String,
        idGte = this[Pagination.GREATER_THAN_OR_EQUAL.toString()] as? String,
        idLt = this[Pagination.LESS_THAN.toString()] as? String,
        idLte = this[Pagination.LESS_THAN_OR_EQUAL.toString()] as? String,
        idAround = this[Pagination.AROUND_ID.toString()] as? String,
        createdAtAfter = date("created_at_after"),
        createdAtAfterOrEqual = date("created_at_after_or_equal"),
        createdAtBefore = date("created_at_before"),
        createdAtBeforeOrEqual = date("created_at_before_or_equal"),
        createdAtAround = date("created_at_around"),
    )

    private fun Map<String, Any>.toPaginationParams() = PaginationParams(
        limit = int("limit"),
        offset = int("offset"),
        idGt = int("id_gt"),
        idGte = int("id_gte"),
        idLt = int("id_lt"),
        idLte = int("id_lte"),
    )

    private fun Map<String, Any>.int(key: String): Int? = (this[key] as? Number)?.toInt()

    // The maps are public, so a date can arrive as the ISO string the backend also accepts.
    private fun Map<String, Any>.date(key: String): Date? = when (val value = this[key]) {
        is Date -> value
        is String -> dateFormatter.parse(value)
        else -> null
    }

    /**
     * Maps the domain [Location] to the generated network [SharedLocation] model.
     */
    internal fun Location.toSharedLocation(): SharedLocation = SharedLocation(
        latitude = latitude,
        longitude = longitude,
        createdByDeviceId = deviceId,
        endAt = endAt,
    )

    /**
     * Transforms the domain [Message] to the generated network [MessageRequest] model.
     */
    internal fun Message.toMessageRequest(): MessageRequest =
        messageTransformer.transform(this)
            .run {
                val upstreamType = if (type in supportedUpstreamMessageTypes) type else ""
                // Responses also copy these declared fields into extraData (see DomainMapping.withDeclared), so a
                // message sent back carries them. They go back to their typed fields, taking precedence as they did
                // when v1 flattened extraData over the root. The response-only ones are dropped, since the backend
                // would store them as custom data on either version. So are the group ids: mentionedGroups holds the
                // current ones, and the copy would undo an edit of the mentions.
                val custom = extraData.toMutableMap()
                custom.keys.removeAll(RESPONSE_ONLY_MESSAGE_KEYS)
                MessageRequest(
                    id = id,
                    text = text,
                    type = MessageRequest.Type.fromString(upstreamType),
                    attachments = attachments.map { it.toAttachmentRequest() },
                    mentionedUsers = mentionedUsersIds,
                    mentionedHere = mentionedHere,
                    mentionedChannel = mentionedChannel,
                    mentionedGroupIds = mentionedGroups.map(UserGroup::id),
                    mentionedRoles = mentionedRoles,
                    mml = custom.takeString(MML_KEY),
                    pollId = custom.takeString(POLL_ID_KEY),
                    parentId = parentId,
                    pinExpires = pinExpires,
                    pinned = pinned,
                    pinnedAt = pinnedAt,
                    quotedMessageId = replyMessageId,
                    showInChannel = showInChannel,
                    silent = silent,
                    restrictedVisibility = custom.takeStrings(RESTRICTED_VISIBILITY_KEY) ?: restrictedVisibility,
                    sharedLocation = sharedLocation?.toSharedLocation(),
                    custom = custom,
                )
            }

    /**
     * Maps the domain [DraftMessage] to the generated network [MessageRequest] model. The draft endpoint
     * takes the same request body as sending a message.
     */
    internal fun DraftMessage.toMessageRequest(): MessageRequest = MessageRequest(
        id = id,
        text = text,
        type = MessageRequest.Type.Regular,
        attachments = attachments.map { it.toAttachmentRequest() },
        mentionedUsers = mentionedUsersIds,
        parentId = parentId,
        quotedMessageId = replyMessage?.id,
        showInChannel = showInChannel,
        silent = silent,
        custom = extraData +
            listOfNotNull(
                command?.let { DRAFT_COMMAND_KEY to it },
                args?.let { DRAFT_ARGS_KEY to it },
            ),
    )

    /**
     * Maps the domain [Reaction] model to a network [ReactionRequest].
     */
    internal fun Reaction.toDto(): ReactionRequest = ReactionRequest(
        type = type,
        createdAt = createdAt,
        score = score,
        updatedAt = updatedAt,
        custom = if (emojiCode != null) extraData + (EMOJI_CODE_KEY to emojiCode) else extraData,
    )

    /**
     * Maps the domain [PrivacySettings] model to the network model, which the spec shares between the
     * request and the response.
     */
    internal fun PrivacySettings.toDto(): PrivacySettingsResponse = PrivacySettingsResponse(
        typingIndicators = typingIndicators?.let { TypingIndicatorsResponse(enabled = it.enabled) },
        readReceipts = readReceipts?.let { ReadReceiptsResponse(enabled = it.enabled) },
        deliveryReceipts = deliveryReceipts?.let { DeliveryReceiptsResponse(enabled = it.enabled) },
    )

    /**
     * Maps the domain [User] model to a network [UserRequest] model.
     *
     * Applies [UserTransformer] first, then maps only the fields a client is allowed to set. The
     * backend marks role/teams/teams_role as ignore_if_client_side, so they are dropped from
     * client requests server-side regardless; the generated model omits them accordingly.
     */
    internal fun User.toUserRequest(): UserRequest =
        userTransformer.transform(this)
            .run {
                UserRequest(
                    id = id,
                    name = name,
                    image = image,
                    invisible = isInvisible,
                    language = language,
                    privacySettings = privacySettings?.toDto(),
                    custom = extraData - RESPONSE_ONLY_USER_KEYS - USER_DEVICES_CUSTOM_KEY,
                )
            }

    internal fun ChatPreferences.toChatPreferencesInput(): ChatPreferencesInput = ChatPreferencesInput(
        directMentions = directMentions?.value?.let(ChatPreferencesInput.DirectMentions::fromString),
        roleMentions = roleMentions?.value?.let(ChatPreferencesInput.RoleMentions::fromString),
        groupMentions = groupMentions?.value?.let(ChatPreferencesInput.GroupMentions::fromString),
        hereMentions = hereMentions?.value?.let(ChatPreferencesInput.HereMentions::fromString),
        channelMentions = channelMentions?.value?.let(ChatPreferencesInput.ChannelMentions::fromString),
        threadReplies = threadReplies?.value?.let(ChatPreferencesInput.ThreadReplies::fromString),
        defaultPreference = defaultPreference?.value?.let(ChatPreferencesInput.DefaultPreference::fromString),
    )
}

private const val MML_KEY = "mml"
private const val POLL_ID_KEY = "poll_id"
private const val RESTRICTED_VISIBILITY_KEY = "restricted_visibility"

/** The declared message fields responses copy into extraData that a message request sends back as typed fields. */
internal val SENT_BACK_MESSAGE_KEYS = setOf(MML_KEY, POLL_ID_KEY, RESTRICTED_VISIBILITY_KEY)

/**
 * Declared message fields that responses also copy into extraData but a message request doesn't take from there. Most
 * aren't carried by a request: sent back, the backend stores them as custom data, hidden from v1 readers, where the
 * name clashes with the field, and shown to v2 readers under `custom`. The group ids are carried, but set from
 * `mentionedGroups`. Derived, so a field added to the copied ones is dropped until a request takes it.
 */
private val RESPONSE_ONLY_MESSAGE_KEYS = GENERATED_MESSAGE_EXTRA_DATA_KEYS - SENT_BACK_MESSAGE_KEYS

/** The user counterpart of [RESPONSE_ONLY_MESSAGE_KEYS]: every declared user field the user adapters keep. */
private val RESPONSE_ONLY_USER_KEYS =
    GENERATED_USER_EXTRA_DATA_KEYS + GENERATED_FULL_USER_EXTRA_DATA_KEYS + GENERATED_OWN_USER_EXTRA_DATA_KEYS

/**
 * The channel counterpart of [RESPONSE_ONLY_MESSAGE_KEYS]: the declared channel fields the channel adapters keep,
 * except those a channel request also declares. v1 rejects `muted` and `mute_expires_at` as reserved, while v2 would
 * store them.
 */
private val RESPONSE_ONLY_CHANNEL_KEYS = GENERATED_CHANNEL_EXTRA_DATA_KEYS + LEGACY_CHANNEL_EXTRA_DATA_KEYS -
    setOf("auto_translation_enabled", "auto_translation_language", "disabled")

/** The member counterpart of [RESPONSE_ONLY_MESSAGE_KEYS]. */
private val RESPONSE_ONLY_MEMBER_KEYS = GENERATED_MEMBER_EXTRA_DATA_KEYS

/** The JSON names [ChannelMemberRequest] declares. */
private val MEMBER_REQUEST_KEYS = setOf("channel_role", "user_id", "custom", "user")

/** The JSON names [UserRequest] declares. */
private val USER_REQUEST_KEYS = setOf("id", "image", "invisible", "language", "name", "custom", "privacy_settings")
