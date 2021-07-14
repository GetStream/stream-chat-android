package io.getstream.chat.android.client

import com.flextrade.jfixture.JFixture
import com.flextrade.kfixture.KFixture
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.NeutralFilterObject
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
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

internal object Mother {
    private val fixture
        get() = JFixture().apply {
            customise().sameInstance(Mute::class.java, mock())
            customise().circularDependencyBehaviour().omitSpecimen()
            customise().repeatCount(0)
        }

    fun randomString() = io.getstream.chat.android.test.randomString()

    fun randomAttachment(attachmentBuilder: Attachment.() -> Unit = { }): Attachment {
        return KFixture(fixture) {
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Attachment>().apply(attachmentBuilder)
    }

    fun randomChannel(channelBuilder: Channel.() -> Unit = { }): Channel {
        return KFixture(fixture) {
            sameInstance(Mute::class.java, mock())
            sameInstance(Message::class.java, randomMessage())
            sameInstance(Attachment.UploadState::class.java, Attachment.UploadState.Success)
        } <Channel>().apply(channelBuilder)
    }

    fun randomUser(userBuilder: User.() -> Unit = { }): User {
        return KFixture(fixture) {
            propertyOf(User::class.java, User::mutes.name, emptyList<Mute>())
        } <User>().apply(userBuilder)
    }

    internal fun randomUserEntity(
        id: String = randomString(),
        originalId: String = randomString(),
        name: String = randomString(),
        role: String = randomString(),
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
        id: String = randomString(),
        cid: String = randomCID(),
        userId: String = randomString(),
        text: String = randomString(),
        attachments: List<AttachmentEntity> = emptyList(),
        type: String = randomString(),
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
        parentId: String? = randomString(),
        command: String? = randomString(),
        shadowed: Boolean = randomBoolean(),
        extraData: Map<String, Any> = emptyMap(),
        replyToId: String? = randomString(),
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

    internal fun randomChannelConfig(
        type: String = randomString(),
        config: Config = randomConfig(),
    ): ChannelConfig =
        ChannelConfig(type = type, config = config)

    internal fun randomQueryChannelsSpec(
        filter: FilterObject = NeutralFilterObject,
        cids: List<String> = emptyList(),
    ): QueryChannelsSpec = QueryChannelsSpec(filter, cids)

    internal fun randomConfig(
        createdAt: Date? = randomDate(),
        updatedAt: Date? = randomDate(),
        name: String = randomString(),
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
        messageRetention: String = randomString(),
        maxMessageLength: Int = randomInt(),
        automod: String = randomString(),
        automodBehavior: String = randomString(),
        blocklistBehavior: String = randomString(),
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

    fun randomMessage(messageBuilder: Message.() -> Unit = { }): Message {
        return KFixture(fixture) {
            propertyOf(Message::class.java, Message::id.name, randomString())
            propertyOf(Message::class.java, Message::attachments.name, mutableListOf<Attachment>())
            propertyOf(Message::class.java, Message::user.name, randomUser())
            propertyOf(Message::class.java, Message::latestReactions.name, mutableListOf<Reaction>())
            propertyOf(Message::class.java, Message::ownReactions.name, mutableListOf<Reaction>())
        } <Message>().apply(messageBuilder)
    }

    fun randomReaction(reactionBuilder: Reaction.() -> Unit = { }): Reaction {
        return KFixture(fixture) {
            propertyOf(Reaction::class.java, Reaction::user.name, randomUser())
        } <Reaction>().apply(reactionBuilder)
    }

    fun randomMember(memberBuilder: Member.() -> Unit = { }) = KFixture(fixture)<Member>().apply(memberBuilder)
}
