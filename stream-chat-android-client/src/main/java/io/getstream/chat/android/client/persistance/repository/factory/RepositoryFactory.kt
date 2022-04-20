package io.getstream.chat.android.client.persistance.repository.factory

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository

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
