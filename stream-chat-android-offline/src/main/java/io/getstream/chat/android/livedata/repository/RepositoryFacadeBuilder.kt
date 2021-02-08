package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.CoroutineScope

internal class RepositoryFacadeBuilder {
    companion object {
        operator fun invoke(builderAction: RepositoryFacadeBuilder.() -> Unit): RepositoryFacadeBuilder {
            return RepositoryFacadeBuilder().apply(builderAction)
        }
    }

    var factory: RepositoryFactory? = null
    var coroutineScope: CoroutineScope? = null
    var defaultConfig: Config? = null

    fun build(): RepositoryFacade {
        val config = requireNotNull(defaultConfig)
        val factory = requireNotNull(factory)
        val scope = requireNotNull(coroutineScope)

        val userRepository = factory.createUserRepository()
        val getUser: suspend (userId: String) -> User = { userId ->
            requireNotNull(userRepository.selectUser(userId)) { "User with the userId: `$userId` has not been found" }
        }

        val messageRepository = factory.createMessageRepository(getUser)
        val getMessage: suspend (messageId: String) -> Message? = messageRepository::selectMessage

        return RepositoryFacade(
            userRepository = factory.createUserRepository(),
            configsRepository = factory.createChannelConfigRepository(),
            channelsRepository = factory.createChannelRepository(getUser, getMessage),
            queryChannelsRepository = factory.createQueryChannelsRepository(),
            messageRepository = messageRepository,
            reactionsRepository = factory.createReactionRepository(getUser),
            syncStateRepository = factory.createSyncStateRepository(),
            scope = scope,
            defaultConfig = config,
        )
    }
}
