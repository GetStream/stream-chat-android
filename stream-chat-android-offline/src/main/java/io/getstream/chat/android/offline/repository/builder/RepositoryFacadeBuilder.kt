package io.getstream.chat.android.offline.repository.builder

import android.content.Context
import androidx.room.Room
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.repository.database.ChatDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

internal class RepositoryFacadeBuilder {
    companion object {
        operator fun invoke(builderAction: RepositoryFacadeBuilder.() -> Unit): RepositoryFacadeBuilder {
            return RepositoryFacadeBuilder().apply(builderAction)
        }
    }

    private var context: Context? = null
    private var currentUser: User? = null
    private var isOfflineEnabled: Boolean = false
    private var database: ChatDatabase? = null
    private var coroutineScope: CoroutineScope? = null
    private var defaultConfig: Config? = null

    fun context(context: Context): RepositoryFacadeBuilder = apply { this.context = context }
    fun currentUser(user: User): RepositoryFacadeBuilder = apply { this.currentUser = user }
    fun setOfflineEnabled(isOfflineEnabled: Boolean): RepositoryFacadeBuilder = apply { this.isOfflineEnabled = isOfflineEnabled }
    fun database(database: ChatDatabase?): RepositoryFacadeBuilder = apply { this.database = database }
    fun scope(scope: CoroutineScope): RepositoryFacadeBuilder = apply { this.coroutineScope = scope }
    fun defaultConfig(config: Config): RepositoryFacadeBuilder = apply { this.defaultConfig = config }

    private fun createDatabase(scope: CoroutineScope, context: Context, user: User?, offlineEnabled: Boolean) = if (offlineEnabled && user != null) {
        ChatDatabase.getDatabase(context, user.id)
    } else {
        Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build().also { inMemoryDatabase ->
            scope.launch { inMemoryDatabase.clearAllTables() }
        }
    }

    private fun getChatDatabase(scope: CoroutineScope): ChatDatabase {
        return database ?: createDatabase(scope, requireNotNull(context), currentUser, isOfflineEnabled)
    }

    fun build(): RepositoryFacade {
        val config = requireNotNull(defaultConfig)
        val scope = requireNotNull(coroutineScope)

        val factory = RepositoryFactory(getChatDatabase(scope), currentUser)

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
