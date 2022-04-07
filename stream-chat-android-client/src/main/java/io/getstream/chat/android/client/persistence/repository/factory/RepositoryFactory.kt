package io.getstream.chat.android.client.persistence.repository.factory

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.AttachmentRepository
import io.getstream.chat.android.client.persistence.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistence.repository.ChannelRepository
import io.getstream.chat.android.client.persistence.repository.MessageRepository
import io.getstream.chat.android.client.persistence.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistence.repository.ReactionRepository
import io.getstream.chat.android.client.persistence.repository.SyncStateRepository
import io.getstream.chat.android.client.persistence.repository.UserRepository

public interface RepositoryFactory {

    public fun createUserRepository(): UserRepository

    public fun createChannelConfigRepository(): ChannelConfigRepository

    public fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository

    public fun createQueryChannelsRepository(): QueryChannelsRepository

    public fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository

    public fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository

    public fun createSyncStateRepository(): SyncStateRepository

    public fun createAttachmentRepository(): AttachmentRepository
}
