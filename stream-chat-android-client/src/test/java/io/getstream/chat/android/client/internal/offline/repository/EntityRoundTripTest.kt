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

package io.getstream.chat.android.client.internal.offline.repository

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.client.MockChatClientBuilder
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.member.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.member.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.userread.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channel.userread.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.channelconfig.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.channelconfig.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toReplyEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.channelinfo.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.channelinfo.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toDomain
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toReplyEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.push.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.client.internal.offline.repository.domain.queryChannels.internal.QueryChannelsDao
import io.getstream.chat.android.client.internal.offline.repository.domain.queryChannels.internal.QueryChannelsEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.reaction.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.reaction.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.scheduled.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.scheduled.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.syncState.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.syncState.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.threads.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.threads.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.DatabaseUserRepository
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.UserDao
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.UserEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.user.internal.toModel
import io.getstream.chat.android.client.internal.state.plugin.identifier
import io.getstream.chat.android.client.persistence.repository.toEntity
import io.getstream.chat.android.client.persistence.repository.toModel
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.receipts.MessageReceipt
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage
import io.getstream.chat.android.client.sync.SyncState
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelConfig
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.ChatPreferences
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageModerationAction
import io.getstream.chat.android.models.MessageModerationDetails
import io.getstream.chat.android.models.MessageReminderInfo
import io.getstream.chat.android.models.Moderation
import io.getstream.chat.android.models.ModerationAction
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.PendingMessage
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.PushSchedule
import io.getstream.chat.android.models.QuietHoursDay
import io.getstream.chat.android.models.QuietHoursRule
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import io.getstream.chat.android.models.querysort.QuerySortByField
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.io.File
import java.lang.reflect.Modifier
import java.util.Date

/**
 * Maps each stored model to its entity and back, and compares the two field by field.
 *
 * A field that does not come back as it was stored is named in the case's `changed` set, so the test fails both when
 * a field stops being restored and when one starts to be.
 */
internal class EntityRoundTripTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("roundTrips")
    fun `a model comes back from its entity with only the expected fields changed`(case: RoundTrip<*>) = runTest {
        // Mapping a channel reads the current user from the client.
        MockChatClientBuilder().build()

        assertEquals(case.changed, case.changedFields(this))
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("fixtures")
    fun `a fixture sets every field, so one that is not stored cannot go unnoticed`(fixture: Fixture) {
        val blank = fieldsOf(fixture.blank)

        assertEquals(fixture.unset, fieldsOf(fixture.model).filter { (field, value) -> value == blank[field] }.keys)
    }

    internal class RoundTrip<T : Any>(
        private val name: String,
        private val model: T,
        val changed: Set<String> = emptySet(),
        private val restore: suspend TestScope.(T) -> T,
    ) {
        suspend fun changedFields(scope: TestScope): Set<String> {
            val restored = fieldsOf(scope.restore(model))
            return fieldsOf(model).filter { (field, value) -> value != restored[field] }.keys
        }

        override fun toString(): String = name
    }

    /** A [model] whose every field differs from [blank], apart from the [unset] ones. */
    internal class Fixture(val model: Any, val blank: Any, val unset: Set<String> = emptySet()) {
        override fun toString(): String = model.javaClass.simpleName
    }

    companion object {

        @JvmStatic
        fun roundTrips() = userRoundTrips() + channelRoundTrips() + messageRoundTrips() + otherRoundTrips()

        private fun userRoundTrips() = listOf(
            RoundTrip(
                name = "current user",
                model = currentUser,
                // No column yet.
                changed = setOf(
                    "language", "devices", "online", "totalUnreadCount", "unreadChannels", "unreadThreads",
                    "channelMutes", "blockedUserIds", "deactivatedAt",
                ),
            ) { restoredUser(it, current = true) },
            RoundTrip(
                name = "other user",
                model = otherUser,
                changed = setOf("language", "online", "deactivatedAt"),
            ) { restoredUser(it, current = false) },
            RoundTrip(
                name = "user with defaults",
                model = User(),
                // Stored as false.
                changed = setOf("invisible", "banned"),
            ) { restoredUser(it, current = false) },
            RoundTrip(
                name = "mute",
                model = Mute(user = alice, target = bob, createdAt = date(1), updatedAt = date(2), expires = date(3)),
                // Stored as ids, and restored as users with nothing but the id.
                changed = setOf("user", "target"),
            ) { it.toEntity().toModel() },
            RoundTrip(name = "privacy settings", model = privacySettings) { it.toEntity().toModel() },
            RoundTrip(
                name = "push preference",
                model = fullPushPreference,
                // No column yet.
                changed = setOf("chatPreferences"),
            ) { it.toEntity().toModel() },
        )

        private fun channelRoundTrips() = listOf(
            RoundTrip(
                name = "channel",
                model = channel,
                // The unread count is the one of the current user's read. The draft is looked up with the id of the
                // channel, while drafts are stored by cid, so it is never found.
                changed = CHANNEL_FIELDS_NOT_STORED + setOf("unreadCount", "draftMessage"),
            ) { it.restored() },
            RoundTrip(name = "channel with defaults", model = Channel()) { it.restored() },
            RoundTrip(name = "member", model = member) { it.toEntity().toModel(getUser) },
            RoundTrip(
                name = "member with defaults",
                model = Member(user = alice),
                // Stored as false.
                changed = setOf("isInvited"),
            ) { it.toEntity().toModel(getUser) },
            RoundTrip(name = "channel read", model = read) { it.toEntity().toModel(getUser) },
            RoundTrip(name = "channel config", model = ChannelConfig("messaging", config)) { it.toEntity().toModel() },
        )

        private fun messageRoundTrips() = listOf(
            RoundTrip(name = "message", model = message) { it.restored() },
            RoundTrip(
                name = "message with defaults",
                model = Message(),
                // A message without a creation date was never delivered, so it is not read as completed.
                changed = setOf("syncStatus"),
            ) { it.restored() },
            RoundTrip(
                name = "quoted message",
                model = message,
                changed = QUOTED_MESSAGE_FIELDS_NOT_STORED,
            ) { it.toReplyEntity().toModel(getUser, getPoll) },
            RoundTrip(
                name = "attachment",
                model = attachment,
                // Gains the id of its row.
                changed = setOf("extraData"),
            ) { it.toEntity(messageId = "message", index = 0).toModel() },
            RoundTrip(name = "attachment with defaults", model = Attachment(), changed = setOf("extraData")) {
                it.toEntity(messageId = "message", index = 0).toModel()
            },
            RoundTrip(name = "quoted attachment", model = attachment, changed = setOf("extraData")) {
                it.toReplyEntity(messageId = "message", index = 0).toModel()
            },
            RoundTrip(
                name = "attachment waiting to be uploaded",
                model = attachment.copy(uploadState = Attachment.UploadState.Idle),
                // Read as being uploaded.
                changed = setOf("uploadState", "extraData"),
            ) { it.toEntity(messageId = "message", index = 0).toModel() },
            RoundTrip(
                name = "attachment being uploaded",
                model = attachment.copy(uploadState = Attachment.UploadState.InProgress(1, totalBytes = 2)),
                // The progress is not stored.
                changed = setOf("uploadState", "extraData"),
            ) { it.toEntity(messageId = "message", index = 0).toModel() },
            RoundTrip(name = "reaction", model = reaction) { it.toEntity().toModel(getUser) },
            RoundTrip(
                name = "reaction with defaults",
                model = Reaction(),
                // Restored from the user id, which is empty here.
                changed = setOf("user"),
            ) { it.toEntity().toModel(getUser) },
            RoundTrip(name = "reaction group", model = reactionGroup) { it.toEntity().toModel() },
            RoundTrip(
                name = "channel info",
                model = channelInfo.copy(image = "image", members = listOf(summaryMember)),
                // No column.
                changed = setOf("image", "members"),
            ) { it.toEntity().toModel() },
            RoundTrip(name = "channel info with defaults", model = ChannelInfo()) { it.toEntity().toModel() },
            RoundTrip(name = "moderation", model = moderation) { it.toEntity().toDomain() },
            RoundTrip(name = "moderation details", model = moderationDetails) { it.toEntity().toModel() },
            RoundTrip(name = "member info", model = memberInfo) { it.toEntity().toModel() },
            RoundTrip(name = "reminder", model = reminder) { it.toEntity().toModel() },
            RoundTrip(name = "location", model = location) { it.toEntity().toModel() },
            RoundTrip(name = "location with defaults", model = Location()) { it.toEntity().toModel() },
        )

        private fun otherRoundTrips() = listOf(
            RoundTrip(name = "draft message", model = draft) { it.toEntity().toModel(getMessage, getUser) },
            RoundTrip(name = "draft message with defaults", model = DraftMessage()) {
                it.toEntity().toModel(getMessage, getUser)
            },
            RoundTrip(name = "poll", model = poll) { it.toEntity().toModel(getUser) },
            RoundTrip(name = "thread", model = thread) {
                it.toEntity().toModel(
                    getUser = getUser,
                    getMessage = getMessage,
                    getChannel = { cid -> channel.takeIf { it.cid == cid } },
                    getDraftMessage = { messageId -> draft.takeIf { messageId == message.id } },
                )
            },
            RoundTrip(name = "scheduled message", model = scheduledMessage) {
                it.toEntity().toModel(getUser, getMessage)
            },
            RoundTrip(
                name = "message of a scheduled message",
                // Its attachments do not keep the id of their row.
                model = message.copy(attachments = listOf(attachment)),
                changed = SCHEDULED_MESSAGE_FIELDS_NOT_STORED,
            ) { content ->
                scheduledMessage
                    .copy(id = content.id, cid = content.cid, message = content)
                    .copy(createdAt = date(1), updatedAt = date(2))
                    .toEntity()
                    .toModel(getUser, getMessage)
                    .message
            },
            RoundTrip(
                name = "message receipt",
                model = MessageReceipt(messageId = "message", cid = "messaging:general", createdAt = date(1)),
            ) { it.toEntity().toModel() },
            RoundTrip(name = "query channels", model = queryChannels) { restoredQueryChannels(it) },
            RoundTrip(name = "sync state", model = syncState) { it.toEntity().toModel() },
        )

        @JvmStatic
        fun fixtures() = listOf(
            Fixture(currentUser, User()),
            // The fields only the current user has.
            Fixture(
                otherUser,
                User(),
                unset = setOf(
                    "privacySettings", "devices", "totalUnreadCount", "unreadChannels", "unreadThreads", "mutes",
                    "channelMutes", "blockedUserIds", "pushPreference",
                ),
            ),
            Fixture(channel, Channel()),
            Fixture(member, Member(user = User())),
            Fixture(config, Config()),
            Fixture(message, Message()),
            Fixture(attachment, Attachment()),
            Fixture(reaction, Reaction()),
            Fixture(channelInfo.copy(image = "image", members = listOf(summaryMember)), ChannelInfo()),
            Fixture(memberInfo, MemberInfo()),
            Fixture(location, Location()),
            Fixture(draft, DraftMessage()),
            Fixture(read, ChannelUserRead(User(), Date(0), unreadMessages = 0, lastRead = Date(0), null)),
            Fixture(fullPushPreference, PushPreference(level = null, disabledUntil = null)),
            Fixture(moderation, Moderation(ModerationAction.remove, "", emptyList(), emptyList(), null, null, false)),
            Fixture(poll, blankPoll),
            Fixture(thread, blankThread),
            Fixture(queryChannels, QueryChannelsSpec(Filters.neutral(), QuerySortByField())),
            Fixture(privacySettings, PrivacySettings()),
            Fixture(TypingIndicators(enabled = false), TypingIndicators()),
            Fixture(DeliveryReceipts(enabled = false), DeliveryReceipts()),
            Fixture(ReadReceipts(enabled = false), ReadReceipts()),
            Fixture(syncState, SyncState(userId = "")),
        )

        private val CHANNEL_FIELDS_NOT_STORED = setOf(
            "config",
            "pendingMessages",
            "pinnedMessages",
            "cachedLatestMessages",
            "isInsideSearch",
            "activeLiveLocations",
            "pushPreference",
            "scheduledMessages",
        )

        private val QUOTED_MESSAGE_FIELDS_NOT_STORED = setOf(
            "reactionCounts", "reactionScores", "reactionGroups", "latestReactions", "ownReactions", "replyTo",
            "replyMessageId", "skipPushNotification", "skipEnrichUrl", "moderation", "sharedLocation", "deletedForMe",
        )

        // Only the content that can be scheduled is stored.
        private val SCHEDULED_MESSAGE_FIELDS_NOT_STORED = setOf(
            "command", "mentionedUsers", "replyCount", "deletedReplyCount", "reactionCounts", "reactionScores",
            "reactionGroups", "syncStatus", "latestReactions", "ownReactions", "deletedAt", "updatedLocallyAt",
            "createdLocallyAt", "shadowed", "i18n", "channelInfo", "pinnedBy", "threadParticipants",
            "moderationDetails", "moderation", "messageTextUpdatedAt", "poll", "reminder", "channelRole", "member",
            "mentionedChannelMembers", "deletedForMe",
        )

        /** The [user] as the repository reads it back, from the entity the repository wrote for it. */
        @OptIn(ExperimentalCoroutinesApi::class)
        private suspend fun TestScope.restoredUser(user: User, current: Boolean): User {
            val written = mock<UserDao>()
            DatabaseUserRepository(this, written).run { if (current) insertCurrentUser(user) else insertUser(user) }
            advanceUntilIdle()
            val entity = if (current) {
                argumentCaptor<UserEntity>().apply { verify(written).insert(capture()) }.firstValue
            } else {
                argumentCaptor<List<UserEntity>>().apply { verify(written).insertMany(capture()) }.firstValue.single()
            }
            val stored = mock<UserDao> { onBlocking { select(entity.id) } doReturn entity }
            return checkNotNull(DatabaseUserRepository(this, stored).selectUser(entity.id))
        }

        /** The [spec] as the repository reads it back, from the entity the repository wrote for it. */
        private suspend fun restoredQueryChannels(spec: QueryChannelsSpec): QueryChannelsSpec {
            val written = mock<QueryChannelsDao>()
            DatabaseQueryChannelsRepository(written).insertQueryChannels(spec)
            val entity = argumentCaptor<QueryChannelsEntity>().apply { verify(written).insert(capture()) }.firstValue
            val stored = mock<QueryChannelsDao> { onBlocking { select(entity.id) } doReturn entity }
            return checkNotNull(DatabaseQueryChannelsRepository(stored).selectBy(spec.identifier))
        }

        private fun fieldsOf(model: Any): Map<String, Any?> = model.javaClass.declaredFields
            .filterNot { Modifier.isStatic(it.modifiers) || it.isSynthetic }
            .associate { field ->
                field.isAccessible = true
                field.name to field.get(model)
            }
    }
}

private fun date(seconds: Int) = Date(seconds * 1000L)

private val alice = User(id = "alice", name = "Alice", image = "alice.png", role = "user")
private val bob = User(id = "bob", name = "Bob", image = "bob.png", role = "admin")
private val users = listOf(alice, bob).associateBy(User::id)

// A user that is not stored is read as one with nothing but its id.
private val getUser: suspend (String) -> User = { users[it] ?: User(id = it) }

private val privacySettings = PrivacySettings(
    typingIndicators = TypingIndicators(enabled = false),
    deliveryReceipts = DeliveryReceipts(enabled = false),
    readReceipts = ReadReceipts(enabled = false),
)

private val syncState = SyncState("alice", listOf("messaging:general"), date(1), "raw", date(2))

private val pushPreference = PushPreference(
    level = PushPreferenceLevel.all,
    disabledUntil = date(1),
    schedule = PushSchedule(
        timezone = "Europe/Rome",
        quietHours = listOf(QuietHoursRule("22:00", "07:00", days = listOf(QuietHoursDay.monday))),
    ),
)

private val fullPushPreference = pushPreference.copy(chatPreferences = ChatPreferences())

private val otherUser = User(
    id = "carol",
    role = "user",
    name = "Carol",
    image = "carol.png",
    invisible = true,
    language = "it",
    banned = true,
    online = true,
    createdAt = date(1),
    updatedAt = date(2),
    lastActive = date(3),
    teams = listOf("red"),
    teamsRole = mapOf("red" to "admin"),
    avgResponseTime = 42,
    extraData = mapOf("key" to "value"),
    deactivatedAt = date(4),
)

private val currentUser = otherUser.copy(
    privacySettings = privacySettings,
    devices = listOf(Device(token = "token", pushProvider = PushProvider.FIREBASE, providerName = "provider")),
    totalUnreadCount = 1,
    unreadChannels = 2,
    unreadThreads = 3,
    // Mutes keep the ids of their users only, so these have nothing else.
    mutes = listOf(
        Mute(user = User(id = "carol"), target = User(id = "bob"), date(5), date(6), expires = date(7)),
    ),
    channelMutes = listOf(ChannelMute(user = bob, channel = null, date(8), date(9), expires = null)),
    blockedUserIds = listOf("bob"),
    // Without the chat preferences, which have no column yet.
    pushPreference = pushPreference,
)

private val member = Member(
    user = alice,
    createdAt = date(1),
    updatedAt = date(2),
    isInvited = true,
    inviteAcceptedAt = date(3),
    inviteRejectedAt = date(4),
    shadowBanned = true,
    banned = true,
    channelRole = "channel_moderator",
    notificationsMuted = true,
    status = "member",
    banExpires = date(5),
    pinnedAt = date(6),
    archivedAt = date(7),
    extraData = mapOf("key" to "value"),
    deletedAt = date(8),
)

private val read = ChannelUserRead(
    user = alice,
    lastReceivedEventDate = date(1),
    unreadMessages = 2,
    lastRead = date(3),
    lastReadMessageId = "message",
    lastDeliveredAt = date(4),
    lastDeliveredMessageId = "delivered",
)

private val config = Config(
    createdAt = date(1),
    updatedAt = date(2),
    name = "messaging",
    typingEventsEnabled = false,
    readEventsEnabled = false,
    deliveryEventsEnabled = false,
    connectEventsEnabled = false,
    searchEnabled = false,
    isReactionsEnabled = false,
    isThreadEnabled = false,
    muteEnabled = false,
    uploadsEnabled = false,
    urlEnrichmentEnabled = false,
    customEventsEnabled = true,
    pushNotificationsEnabled = false,
    skipLastMsgUpdateForSystemMsgs = true,
    pollsEnabled = true,
    messageRetention = "30",
    maxMessageLength = 100,
    automod = "simple",
    automodBehavior = "flag",
    blocklistBehavior = "block",
    commands = listOf(Command(name = "giphy", description = "Post a gif", args = "[text]", set = "fun")),
    messageRemindersEnabled = true,
    sharedLocationsEnabled = true,
    markMessagesPending = true,
    pushLevel = "all",
    quotesEnabled = false,
    scheduledMessagesMaxPerChannel = 1,
    scheduledMessagesMaxPerUser = 2,
    scheduledMessagesMaxLeadTimeSeconds = 3,
    scheduledMessagesMinLeadTimeSeconds = 4,
)

private val attachment = Attachment(
    authorName = "author",
    authorLink = "author.link",
    titleLink = "title.link",
    thumbUrl = "thumb.url",
    imageUrl = "image.url",
    assetUrl = "asset.url",
    ogUrl = "og.url",
    mimeType = "image/png",
    fileSize = 1,
    title = "title",
    text = "text",
    type = "image",
    image = "image",
    name = "name",
    fallback = "fallback",
    originalHeight = 2,
    originalWidth = 3,
    upload = File("/upload.png"),
    uploadState = Attachment.UploadState.Success,
    extraData = mapOf("key" to "value"),
)

// An attachment gains the id of its row, so the models that hold one hold it with the id.
private val storedAttachment =
    attachment.copy(extraData = attachment.extraData + (AttachmentEntity.EXTRA_DATA_ID_KEY to "attachment"))

private val reaction = Reaction(
    messageId = "message",
    type = "like",
    score = 2,
    user = alice,
    userId = "alice",
    createdAt = date(1),
    createdLocallyAt = date(2),
    updatedAt = date(3),
    deletedAt = date(4),
    syncStatus = SyncStatus.SYNC_NEEDED,
    extraData = mapOf("key" to "value"),
    enforceUnique = true,
    skipPush = true,
    emojiCode = "1f44d",
)

private val reactionGroup = ReactionGroup("like", count = 1, sumScore = 2, date(3), date(4))

// Without the image and the members, which have no column.
private val channelInfo =
    ChannelInfo("messaging:general", id = "general", type = "messaging", memberCount = 2, name = "General")

// A member as the channel summary of a scheduled message stores it.
private val summaryMember = Member(user = User(id = "bob", name = "Bob", image = "bob.png"))

private val moderation = Moderation(
    action = ModerationAction.bounce,
    originalText = "original",
    textHarms = listOf("text"),
    imageHarms = listOf("image"),
    blocklistMatched = "blocklist",
    semanticFilterMatched = "filter",
    platformCircumvented = true,
    blocklistsMatched = listOf("blocklist"),
)

private val moderationDetails = MessageModerationDetails("original", MessageModerationAction.bounce, "error")

private val memberInfo =
    MemberInfo(channelRole = "moderator", notificationsMuted = true, extraData = mapOf("key" to "value"))

private val reminder = MessageReminderInfo(remindAt = date(1), createdAt = date(2), updatedAt = date(3))

private val location = Location(
    cid = "messaging:general",
    messageId = "message",
    userId = "alice",
    endAt = date(1),
    latitude = 1.5,
    longitude = 2.5,
    deviceId = "device",
    createdAt = date(2),
    updatedAt = date(3),
)

private val poll = Poll(
    id = "poll",
    name = "name",
    description = "description",
    options = listOf(Option("option", "text", mapOf("key" to "value"), textI18n = mapOf("it" to "testo"))),
    votingVisibility = VotingVisibility.ANONYMOUS,
    enforceUniqueVote = true,
    maxVotesAllowed = 2,
    allowUserSuggestedOptions = true,
    allowAnswers = true,
    voteCount = 1,
    voteCountsByOption = mapOf("option" to 1),
    votes = listOf(Vote("vote", "poll", "option", date(1), date(2), user = bob)),
    ownVotes = listOf(Vote("own", "poll", "option", date(3), date(4), user = alice)),
    createdAt = date(5),
    updatedAt = date(6),
    closed = true,
    answersCount = 1,
    answers = listOf(Answer("answer", "poll", "text", date(7), date(8), user = bob, mapOf("it" to "testo"))),
    createdBy = alice,
    extraData = mapOf("key" to "value"),
    nameI18n = mapOf("it" to "nome"),
    descriptionI18n = mapOf("it" to "descrizione"),
)

private val blankPoll = Poll(
    id = "",
    name = "",
    description = "",
    options = emptyList(),
    votingVisibility = VotingVisibility.PUBLIC,
    enforceUniqueVote = false,
    maxVotesAllowed = null,
    allowUserSuggestedOptions = false,
    allowAnswers = false,
    voteCount = 0,
    voteCountsByOption = emptyMap(),
    votes = emptyList(),
    ownVotes = emptyList(),
    createdAt = Date(0),
    updatedAt = Date(0),
    closed = false,
    answersCount = 0,
    createdBy = null,
)

private val quoted = Message(id = "quoted", cid = "messaging:general", text = "quoted", user = bob)

private val message = Message(
    id = "message",
    cid = "messaging:general",
    text = "text",
    html = "<p>text</p>",
    parentId = "parent",
    command = "giphy",
    attachments = listOf(storedAttachment),
    mentionedUsersIds = listOf("bob"),
    mentionedUsers = listOf(bob),
    replyCount = 1,
    deletedReplyCount = 2,
    reactionCounts = mapOf("like" to 1),
    reactionScores = mapOf("like" to 2),
    reactionGroups = mapOf("like" to reactionGroup),
    syncStatus = SyncStatus.SYNC_NEEDED,
    type = "regular",
    latestReactions = listOf(reaction),
    ownReactions = listOf(reaction),
    createdAt = date(1),
    updatedAt = date(2),
    deletedAt = date(3),
    updatedLocallyAt = date(4),
    createdLocallyAt = date(5),
    user = alice,
    extraData = mapOf("key" to "value"),
    silent = true,
    shadowed = true,
    i18n = mapOf("it_text" to "testo"),
    showInChannel = true,
    channelInfo = channelInfo,
    replyTo = quoted,
    replyMessageId = quoted.id,
    pinned = true,
    pinnedAt = date(6),
    pinExpires = date(7),
    pinnedBy = bob,
    threadParticipants = listOf(bob),
    skipPushNotification = true,
    skipEnrichUrl = true,
    moderationDetails = moderationDetails,
    moderation = moderation,
    messageTextUpdatedAt = date(8),
    poll = poll,
    restrictedVisibility = listOf("bob"),
    reminder = reminder,
    sharedLocation = location,
    channelRole = memberInfo.channelRole,
    member = memberInfo,
    mentionedChannelMembers = mapOf("bob" to memberInfo),
    deletedForMe = true,
    mentionedHere = true,
    mentionedChannel = true,
    mentionedGroups = listOf(UserGroup(id = "group", name = "Group")),
    mentionedRoles = listOf("admin"),
)

private val messages = listOf(message, quoted).associateBy(Message::id)
private val getMessage: suspend (String) -> Message? = { messages[it] }
private val getPoll: suspend (String) -> Poll? = { id -> poll.takeIf { it.id == id } }

private suspend fun Message.restored(): Message = toEntity().toModel(getUser, getMessage, getPoll)

private val draft = DraftMessage(
    id = "draft",
    cid = "messaging:general",
    text = "text",
    parentId = "parent",
    attachments = listOf(attachment),
    mentionedUsersIds = listOf("bob"),
    extraData = mapOf("key" to "value"),
    silent = true,
    showInChannel = true,
    replyMessage = quoted,
    command = "giphy",
    args = "args",
    createdAt = date(1),
    mentionedUsers = listOf(bob),
)

private val channel = Channel(
    id = "general",
    type = "messaging",
    name = "General",
    image = "general.png",
    watcherCount = 1,
    frozen = true,
    createdAt = date(1),
    deletedAt = date(2),
    updatedAt = date(3),
    syncStatus = SyncStatus.SYNC_NEEDED,
    memberCount = 2,
    // Only the last message is stored with the channel.
    messages = listOf(quoted.copy(createdAt = date(6))),
    members = listOf(member),
    watchers = listOf(bob),
    read = listOf(read),
    config = config,
    createdBy = alice,
    unreadCount = 9,
    team = "red",
    hidden = true,
    hiddenMessagesBefore = date(4),
    cooldown = 3,
    pendingMessages = listOf(PendingMessage(quoted, metadata = mapOf("key" to "value"))),
    pinnedMessages = listOf(quoted),
    ownCapabilities = setOf("send-message"),
    membership = member,
    cachedLatestMessages = listOf(quoted),
    isInsideSearch = true,
    draftMessage = draft,
    activeLiveLocations = listOf(location),
    messageCount = 4,
    pushPreference = pushPreference,
    filterTags = listOf("tag"),
    lastMessageAt = date(7),
    truncatedAt = date(5),
    disabled = true,
    blocked = true,
    truncatedBy = bob,
    autoTranslationEnabled = true,
    autoTranslationLanguages = setOf("it"),
    scheduledMessages = listOf(
        ScheduledMessage(
            id = "scheduled",
            cid = "messaging:general",
            channel = null,
            message = quoted,
            scheduledAt = date(8),
            status = ScheduledMessageStatus.Scheduled,
            failure = null,
            createdAt = date(9),
            updatedAt = date(10),
        ),
    ),
    activeMemberCount = 5,
    extraData = mapOf("key" to "value"),
)

private suspend fun Channel.restored(): Channel = toEntity().toModel(
    getUser = getUser,
    getMessage = { id -> messages.firstOrNull { it.id == id } },
    getDraftMessage = { key -> draftMessage.takeIf { key == cid } },
)

private val thread = Thread(
    activeParticipantCount = 1,
    cid = channel.cid,
    channel = channel,
    parentMessageId = message.id,
    parentMessage = message,
    createdByUserId = alice.id,
    createdBy = alice,
    participantCount = 2,
    threadParticipants = listOf(ThreadParticipant(user = bob, lastThreadMessageAt = date(1))),
    lastMessageAt = date(2),
    createdAt = date(3),
    updatedAt = date(4),
    deletedAt = date(5),
    title = "title",
    latestReplies = listOf(quoted),
    read = listOf(read),
    draft = draft,
    extraData = mapOf("key" to "value"),
)

private val blankThread = Thread(
    activeParticipantCount = 0,
    cid = "",
    channel = null,
    parentMessageId = "",
    parentMessage = Message(),
    createdByUserId = "",
    createdBy = null,
    participantCount = 0,
    threadParticipants = emptyList(),
    lastMessageAt = Date(0),
    createdAt = Date(0),
    updatedAt = Date(0),
    deletedAt = null,
    title = "",
    latestReplies = emptyList(),
    read = emptyList(),
    draft = null,
)

private val queryChannels = QueryChannelsSpec(
    filter = Filters.eq("type", "messaging"),
    querySort = QuerySortByField.descByName("last_updated"),
    cids = setOf("messaging:general"),
    predefinedFilterName = "filter",
    predefinedFilterValues = mapOf("key" to "value"),
    predefinedSortValues = mapOf("key" to "value"),
    groupKey = "group",
)

// The message of a scheduled message is stored with the fields that can be scheduled, and takes the id, the
// channel and the dates of the scheduled message.
private val scheduledMessage = LocalScheduledMessage(
    id = "scheduled",
    cid = "messaging:general",
    channel = channelInfo.copy(memberCount = 0, image = "general.png", members = listOf(summaryMember)),
    message = Message(
        id = "scheduled",
        cid = "messaging:general",
        user = alice,
        text = "text",
        html = "<p>text</p>",
        type = "regular",
        parentId = "parent",
        showInChannel = true,
        replyMessageId = quoted.id,
        replyTo = quoted,
        attachments = listOf(attachment),
        mentionedUsersIds = listOf("bob"),
        mentionedHere = true,
        mentionedChannel = true,
        mentionedGroups = listOf(UserGroup(id = "group", name = "Group")),
        mentionedRoles = listOf("admin"),
        pinned = true,
        pinnedAt = date(1),
        pinExpires = date(2),
        silent = true,
        restrictedVisibility = listOf("bob"),
        sharedLocation = location,
        extraData = mapOf("key" to "value"),
        skipPushNotification = true,
        skipEnrichUrl = true,
        createdAt = date(4),
        updatedAt = date(5),
    ),
    scheduledAt = date(3),
    serverStatus = ScheduledMessageStatus.Failed,
    publishingFailure = ScheduledMessageFailure.ServerFailedPublishing(
        reason = ScheduledMessageFailure.ServerFailedPublishing.Reason.Banned,
    ),
    pendingChange = LocalScheduledMessage.PendingChange.Update,
    hasPendingScheduledAt = true,
    isPendingSendNow = true,
    isDeleted = true,
    isCreateSent = true,
    localFailure = ScheduledMessageFailure.LocalDeviceFailedScheduling(
        reason = ScheduledMessageFailure.LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
        message = "failed",
    ),
    createdAt = date(4),
    updatedAt = date(5),
)
