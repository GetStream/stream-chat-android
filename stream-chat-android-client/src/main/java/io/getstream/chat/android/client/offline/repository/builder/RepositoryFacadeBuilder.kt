package io.getstream.chat.android.client.offline.repository.builder

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import androidx.room.RoomDatabase
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.offline.repository.RepositoryFacade
import io.getstream.chat.android.client.offline.repository.database.ChatDatabase
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@InternalStreamChatApi
public class RepositoryFacadeBuilder {
    public companion object {
        /**
         * Only test oriented property
         */
        @SuppressLint("StaticFieldLeak")
        @VisibleForTesting
        public var instance: RepositoryFacadeBuilder? = null

        public operator fun invoke(builderAction: RepositoryFacadeBuilder.() -> Unit): RepositoryFacadeBuilder {
            return instance ?: RepositoryFacadeBuilder().apply(builderAction)
        }
    }

    private var context: Context? = null
    private var currentUser: User? = null
    private var isOfflineEnabled: Boolean = false
    private var databaseBuilder: (RoomDatabase.Builder<*>) -> (RoomDatabase.Builder<*>) = { it }
    private var coroutineScope: CoroutineScope? = null
    private var defaultConfig: Config? = null

    public fun context(context: Context): RepositoryFacadeBuilder = apply { this.context = context }
    public fun currentUser(user: User): RepositoryFacadeBuilder = apply { this.currentUser = user }
    public fun setOfflineEnabled(isOfflineEnabled: Boolean): RepositoryFacadeBuilder =
        apply { this.isOfflineEnabled = isOfflineEnabled }

    public fun databaseBuilder(databaseBuilder: (RoomDatabase.Builder<*>) -> (RoomDatabase.Builder<*>)): RepositoryFacadeBuilder =
        apply { this.databaseBuilder = databaseBuilder }

    public fun scope(scope: CoroutineScope): RepositoryFacadeBuilder = apply { this.coroutineScope = scope }
    public fun defaultConfig(config: Config): RepositoryFacadeBuilder = apply { this.defaultConfig = config }

    @Suppress("UNCHECKED_CAST")
    private fun createDatabase(
        scope: CoroutineScope,
        context: Context,
        user: User?,
        offlineEnabled: Boolean,
    ): ChatDatabase {
        return if (offlineEnabled && user != null) {
            ChatDatabase.getDatabase(context, user.id)
        } else {
            Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).let { databaseBuilder(it) as RoomDatabase.Builder<ChatDatabase> }.build()
                .also { inMemoryDatabase ->
                    scope.launch { inMemoryDatabase.clearAllTables() }
                }
        }
    }

    public fun build(): RepositoryFacade {
        val config = requireNotNull(defaultConfig)
        val scope = requireNotNull(coroutineScope)

        val factory = RepositoryFactory(
            createDatabase(scope, requireNotNull(context), currentUser, isOfflineEnabled),
            currentUser
        )

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
            attachmentRepository = factory.createAttachmentRepository(),
            scope = scope,
            defaultConfig = config,
        )
    }
}
