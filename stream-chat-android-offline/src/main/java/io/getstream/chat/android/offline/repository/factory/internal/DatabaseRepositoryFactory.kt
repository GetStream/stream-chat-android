/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.repository.factory.internal

import io.getstream.chat.android.client.persistance.repository.ChannelConfigRepository
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.QueryChannelsRepository
import io.getstream.chat.android.client.persistance.repository.ReactionRepository
import io.getstream.chat.android.client.persistance.repository.SyncStateRepository
import io.getstream.chat.android.client.persistance.repository.ThreadsRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.domain.channel.internal.DatabaseChannelRepository
import io.getstream.chat.android.offline.repository.domain.channel.internal.RecoverableChannelDao
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.DatabaseChannelConfigRepository
import io.getstream.chat.android.offline.repository.domain.channelconfig.internal.RecoverableChannelConfigDao
import io.getstream.chat.android.offline.repository.domain.message.internal.DatabaseMessageRepository
import io.getstream.chat.android.offline.repository.domain.message.internal.RecoverableMessageDao
import io.getstream.chat.android.offline.repository.domain.message.internal.RecoverablePollDao
import io.getstream.chat.android.offline.repository.domain.message.internal.RecoverableReplyMessageDao
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.DatabaseQueryChannelsRepository
import io.getstream.chat.android.offline.repository.domain.queryChannels.internal.RecoverableQueryChannelsDao
import io.getstream.chat.android.offline.repository.domain.reaction.internal.DatabaseReactionRepository
import io.getstream.chat.android.offline.repository.domain.reaction.internal.RecoverableReactionDao
import io.getstream.chat.android.offline.repository.domain.syncState.internal.DatabaseSyncStateRepository
import io.getstream.chat.android.offline.repository.domain.syncState.internal.RecoverableSyncStateDao
import io.getstream.chat.android.offline.repository.domain.threads.internal.DatabaseThreadsRepository
import io.getstream.chat.android.offline.repository.domain.threads.internal.RecoverableThreadDao
import io.getstream.chat.android.offline.repository.domain.threads.internal.RecoverableThreadOrderDao
import io.getstream.chat.android.offline.repository.domain.user.internal.DatabaseUserRepository
import io.getstream.chat.android.offline.repository.domain.user.internal.RecoverableUserDao
import kotlinx.coroutines.CoroutineScope

private const val DEFAULT_CACHE_SIZE = 1000

internal class DatabaseRepositoryFactory(
    private val database: () -> ChatDatabase,
    private val currentUser: User,
    private val scope: CoroutineScope,
    private val ignoredChannelTypes: Set<String>,
    private val now: () -> Long = { System.currentTimeMillis() },
) : RepositoryFactory {

    private var repositoriesCache: MutableMap<Class<out Any>, Any> = mutableMapOf()

    override fun createUserRepository(): UserRepository {
        val databaseUserRepository = repositoriesCache[UserRepository::class.java] as? DatabaseUserRepository?

        return databaseUserRepository ?: run {
            DatabaseUserRepository(scope, RecoverableUserDao(database), DEFAULT_CACHE_SIZE).also { repository ->
                repositoriesCache[UserRepository::class.java] = repository
            }
        }
    }

    override fun createChannelConfigRepository(): ChannelConfigRepository {
        val databaseChannelConfigRepository =
            repositoriesCache[ChannelConfigRepository::class.java] as? DatabaseChannelConfigRepository?

        return databaseChannelConfigRepository ?: run {
            DatabaseChannelConfigRepository(RecoverableChannelConfigDao(database)).also { repository ->
                repositoriesCache[ChannelConfigRepository::class.java] = repository
            }
        }
    }

    override fun createChannelRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
    ): ChannelRepository {
        val databaseChannelRepository = repositoriesCache[ChannelRepository::class.java] as? DatabaseChannelRepository?
        val messageRepository = createMessageRepository(getUser)

        return databaseChannelRepository ?: run {
            DatabaseChannelRepository(
                scope,
                RecoverableChannelDao(database),
                getUser,
                getMessage,
                messageRepository::selectDraftMessagesByCid,
                now,
            )
                .also { repository ->
                    repositoriesCache[ChannelRepository::class.java] = repository
                }
        }
    }

    override fun createQueryChannelsRepository(): QueryChannelsRepository {
        val databaseQueryChannelsRepository =
            repositoriesCache[QueryChannelsRepository::class.java] as? QueryChannelsRepository?

        return databaseQueryChannelsRepository ?: run {
            DatabaseQueryChannelsRepository(scope, RecoverableQueryChannelsDao(database)).also { repository ->
                repositoriesCache[QueryChannelsRepository::class.java] = repository
            }
        }
    }

    override fun createThreadsRepository(
        getUser: suspend (userId: String) -> User,
        getMessage: suspend (messageId: String) -> Message?,
        getChannel: suspend (cid: String) -> Channel?,
    ): ThreadsRepository {
        val repository = repositoriesCache[ThreadsRepository::class.java] as? ThreadsRepository?
        val messageRepository = createMessageRepository(getUser)

        return repository ?: run {
            DatabaseThreadsRepository(
                scope = scope,
                threadDao = RecoverableThreadDao(database),
                threadOrderDao = RecoverableThreadOrderDao(database),
                getUser = getUser,
                getMessage = getMessage,
                getChannel = getChannel,
                getDraftMessage = messageRepository::selectDraftMessageByParentId,
            ).also {
                repositoriesCache[ThreadsRepository::class.java] = it
            }
        }
    }

    override fun createMessageRepository(
        getUser: suspend (userId: String) -> User,
    ): MessageRepository {
        val databaseMessageRepository = repositoriesCache[MessageRepository::class.java] as? DatabaseMessageRepository?

        return databaseMessageRepository ?: run {
            DatabaseMessageRepository(
                scope,
                RecoverableMessageDao(database),
                RecoverableReplyMessageDao(database),
                RecoverablePollDao(database),
                getUser,
                currentUser,
                ignoredChannelTypes,
                DEFAULT_CACHE_SIZE,
            ).also { repository ->
                repositoriesCache[MessageRepository::class.java] = repository
            }
        }
    }

    override fun createReactionRepository(getUser: suspend (userId: String) -> User): ReactionRepository {
        val databaseReactionRepository =
            repositoriesCache[ReactionRepository::class.java] as? DatabaseReactionRepository?

        return databaseReactionRepository ?: run {
            DatabaseReactionRepository(scope, RecoverableReactionDao(database), getUser).also { repository ->
                repositoriesCache[ReactionRepository::class.java] = repository
            }
        }
    }

    override fun createSyncStateRepository(): SyncStateRepository {
        val databaseSyncStateRepository =
            repositoriesCache[SyncStateRepository::class.java] as? DatabaseSyncStateRepository?

        return databaseSyncStateRepository ?: run {
            DatabaseSyncStateRepository(RecoverableSyncStateDao(database)).also { repository ->
                repositoriesCache[SyncStateRepository::class.java] = repository
            }
        }
    }
}
