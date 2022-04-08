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

    public fun userRepository(): UserRepository

    public fun channelConfigRepository(): ChannelConfigRepository

    public fun channelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository

    public fun queryChannelsRepository(): QueryChannelsRepository

    public fun messageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository

    public fun reactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository

    public fun syncStateRepository(): SyncStateRepository

    public fun attachmentRepository(): AttachmentRepository
}
