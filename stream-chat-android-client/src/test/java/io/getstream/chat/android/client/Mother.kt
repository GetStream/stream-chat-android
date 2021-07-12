package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.offline.model.ChannelConfig
import io.getstream.chat.android.client.offline.model.QueryChannelsSpec
import io.getstream.chat.android.client.offline.repository.domain.message.MessageEntity
import io.getstream.chat.android.client.offline.repository.domain.message.MessageInnerEntity
import io.getstream.chat.android.client.offline.repository.domain.message.attachment.AttachmentEntity
import io.getstream.chat.android.client.offline.repository.domain.reaction.ReactionEntity
import io.getstream.chat.android.client.offline.repository.domain.user.UserEntity
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.randomBoolean
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomDate
import io.getstream.chat.android.test.randomInt
import java.util.Date
import java.util.UUID

internal object Mother {
    private val fixture = JFixture()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomChannel(channelBuilder: Channel.() -> Unit = { }): Channel {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
            sameInstance(Message::class.java, mock())
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Channel>().apply(channelBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
        } <User>().apply(userBuilder)
    }

    fun randomString(): String = UUID.randomUUID().toString()

    internal fun randomUserEntity(
        id: String = io.getstream.chat.android.test.randomString(),
        originalId: String = io.getstream.chat.android.test.randomString(),
        name: String = io.getstream.chat.android.test.randomString(),
        role: String = io.getstream.chat.android.test.randomString(),
        createdAt: Date? = null,
        updatedAt: Date? = null,
        lastActive: Date? = null,
        invisible: Boolean = randomBoolean(),
        banned: Boolean = randomBoolean(),
        mutes: List<String> = emptyList(),
        extraData: Map<String, Any> = emptyMap(),
    ): UserEntity =
        UserEntity(id, originalId, name, role, createdAt, updatedAt, lastActive, invisible, banned, mutes, extraData)

    internal fun randomMessageEntity(
        id: String = io.getstream.chat.android.test.randomString(),
        cid: String = randomCID(),
        userId: String = io.getstream.chat.android.test.randomString(),
        text: String = io.getstream.chat.android.test.randomString(),
        attachments: List<AttachmentEntity> = emptyList(),
        type: String = io.getstream.chat.android.test.randomString(),
        syncStatus: SyncStatus = SyncStatus.COMPLETED,
        replyCount: Int = randomInt(),
        createdAt: Date? = randomDate(),
        createdLocallyAt: Date? = randomDate(),
        updatedAt: Date? = randomDate(),
        updatedLocallyAt: Date? = randomDate(),
        deletedAt: Date? = randomDate(),
        latestReactions: List<ReactionEntity> = emptyList(),
        ownReactions: List<ReactionEntity> = emptyList(),
        mentionedUsersId: List<String> = emptyList(),
        reactionCounts: Map<String, Int> = emptyMap(),
        reactionScores: Map<String, Int> = emptyMap(),
        parentId: String? = io.getstream.chat.android.test.randomString(),
        command: String? = io.getstream.chat.android.test.randomString(),
        shadowed: Boolean = randomBoolean(),
        extraData: Map<String, Any> = emptyMap(),
        replyToId: String? = io.getstream.chat.android.test.randomString(),
        threadParticipantsIds: List<String> = emptyList(),
    ) = MessageEntity(
        messageInnerEntity = MessageInnerEntity(
            id = id,
            cid = cid,
            userId = userId,
            text = text,
            type = type,
            syncStatus = syncStatus,
            replyCount = replyCount,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            updatedAt = updatedAt,
            updatedLocallyAt = updatedLocallyAt,
            deletedAt = deletedAt,
            mentionedUsersId = mentionedUsersId,
            reactionCounts = reactionCounts,
            reactionScores = reactionScores,
            parentId = parentId,
            command = command,
            shadowed = shadowed,
            extraData = extraData,
            replyToId = replyToId,
            threadParticipantsIds = threadParticipantsIds,
        ),
        attachments = attachments,
        latestReactions = latestReactions,
        ownReactions = ownReactions,
    )

    internal fun randomChannelConfig(type: String = io.getstream.chat.android.test.randomString(), config: Config = randomConfig()): ChannelConfig =
        ChannelConfig(type = type, config = config)

    internal fun randomQueryChannelsSpec(
        filter: FilterObject = NeutralFilterObject,
        sort: QuerySort<Channel> = QuerySort.Companion.asc(Channel::lastMessageAt),
        cids: List<String> = emptyList(),
    ): QueryChannelsSpec = QueryChannelsSpec(filter, sort, cids)

    internal fun randomConfig(
        createdAt: Date? = randomDate(),
        updatedAt: Date? = randomDate(),
        name: String = io.getstream.chat.android.test.randomString(),
        isTypingEvents: Boolean = randomBoolean(),
        isReadEvents: Boolean = randomBoolean(),
        isConnectEvents: Boolean = randomBoolean(),
        isSearch: Boolean = randomBoolean(),
        isReactionsEnabled: Boolean = randomBoolean(),
        isRepliesEnabled: Boolean = randomBoolean(),
        isMutes: Boolean = randomBoolean(),
        uploadsEnabled: Boolean = randomBoolean(),
        urlEnrichmentEnabled: Boolean = randomBoolean(),
        customEventsEnabled: Boolean = randomBoolean(),
        pushNotificationsEnabled: Boolean = randomBoolean(),
        messageRetention: String = io.getstream.chat.android.test.randomString(),
        maxMessageLength: Int = randomInt(),
        automod: String = io.getstream.chat.android.test.randomString(),
        automodBehavior: String = io.getstream.chat.android.test.randomString(),
        blocklistBehavior: String = io.getstream.chat.android.test.randomString(),
        commands: List<Command> = emptyList(),
    ) = Config(
        created_at = createdAt,
        updated_at = updatedAt,
        name = name,
        isTypingEvents = isTypingEvents,
        isReadEvents = isReadEvents,
        isConnectEvents = isConnectEvents,
        isSearch = isSearch,
        isReactionsEnabled = isReactionsEnabled,
        isRepliesEnabled = isRepliesEnabled,
        isMutes = isMutes,
        uploadsEnabled = uploadsEnabled,
        urlEnrichmentEnabled = urlEnrichmentEnabled,
        customEventsEnabled = customEventsEnabled,
        pushNotificationsEnabled = pushNotificationsEnabled,
        messageRetention = messageRetention,
        maxMessageLength = maxMessageLength,
        automod = automod,
        automodBehavior = automodBehavior,
        blocklistBehavior = blocklistBehavior,
        commands = commands,
    )
}
