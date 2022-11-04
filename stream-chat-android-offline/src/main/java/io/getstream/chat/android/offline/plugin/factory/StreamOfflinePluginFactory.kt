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

package io.getstream.chat.android.offline.plugin.factory

import android.content.Context
import androidx.room.Room
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.internal.OfflinePlugin
import io.getstream.chat.android.offline.plugin.listener.internal.CreateChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.QueryMembersListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.ThreadQueryListenerDatabase
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Implementation of [PluginFactory] that provides [OfflinePlugin].
 *
 * @param config [Config] Configuration of persistence of the SDK.
 * @param appContext [Context]
 */
public class StreamOfflinePluginFactory(
    private val config: Config,
    private val appContext: Context,
) : PluginFactory, RepositoryFactory.Provider {

    private val logger = StreamLog.getLogger("Chat:OfflinePluginFactory")
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        StreamLog.e("StreamOfflinePlugin", throwable) {
            "[uncaughtCoroutineException] throwable: $throwable, context: $context"
        }
    }

    @Volatile
    private var _scope: CoroutineScope? = null

    override fun createRepositoryFactory(user: User): RepositoryFactory {
        logger.i { "[createRepositoryFactory] user.id: '${user.id}'" }
        val scope = ensureScope(user)
        return DatabaseRepositoryFactory(
            database = createDatabase(scope, appContext, user, config.persistenceEnabled),
            currentUser = user,
        )
    }

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = createOfflinePlugin(user)

    /**
     * Tries to get cached [OfflinePlugin] instance for the user if it exists or
     * creates the new [OfflinePlugin] and initialized its dependencies.
     *
     * This method must be called after the user is set in the SDK.
     */
    @Suppress("LongMethod")
    private fun createOfflinePlugin(user: User): OfflinePlugin {
        ChatClient.OFFLINE_SUPPORT_ENABLED = true

        val chatClient = ChatClient.instance()
        val clientState = chatClient.clientState
        val repositoryFacade = chatClient.repositoryFacade

        val queryChannelListener = QueryChannelListenerDatabase(repositoryFacade)

        val threadQueryListener = ThreadQueryListenerDatabase(repositoryFacade, repositoryFacade)

        val editMessageListener = EditMessageListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade,
            clientState = clientState
        )

        val hideChannelListener: HideChannelListener = HideChannelListenerDatabase(
            channelRepository = repositoryFacade,
            messageRepository = repositoryFacade
        )

        val deleteReactionListener: DeleteReactionListener = DeleteReactionListenerDatabase(
            clientState = clientState,
            reactionsRepository = repositoryFacade,
            messageRepository = repositoryFacade
        )

        val sendReactionListener = SendReactionListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            reactionsRepository = repositoryFacade,
            userRepository = repositoryFacade
        )

        val deleteMessageListener: DeleteMessageListener = DeleteMessageListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            userRepository = repositoryFacade
        )

        val sendMessageListener: SendMessageListener = SendMessageListenerDatabase(repositoryFacade, repositoryFacade)

        val shuffleGiphyListener: ShuffleGiphyListener = ShuffleGiphyListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade
        )

        val queryMembersListener: QueryMembersListener = QueryMembersListenerDatabase(
            repositoryFacade, repositoryFacade
        )
        val createChannelListener: CreateChannelListener = CreateChannelListenerDatabase(
            clientState = clientState,
            channelRepository = repositoryFacade,
            userRepository = repositoryFacade
        )

        return OfflinePlugin(
            activeUser = user,
            queryChannelListener = queryChannelListener,
            threadQueryListener = threadQueryListener,
            editMessageListener = editMessageListener,
            hideChannelListener = hideChannelListener,
            deleteReactionListener = deleteReactionListener,
            sendReactionListener = sendReactionListener,
            deleteMessageListener = deleteMessageListener,
            sendMessageListener = sendMessageListener,
            shuffleGiphyListener = shuffleGiphyListener,
            queryMembersListener = queryMembersListener,
            createChannelListener = createChannelListener,
        )
    }

    private fun ensureScope(user: User): CoroutineScope {
        val currentScope = _scope
        logger.d {
            "[ensureScope] user.id: '${user.id}', hasScope: ${currentScope != null}, " +
                "isScopeActive: ${currentScope?.isActive}"
        }
        return when (currentScope == null || !currentScope.isActive) {
            true -> ChatClient.instance().inheritScope { parentJob ->
                SupervisorJob(parentJob) + DispatcherProvider.IO + exceptionHandler
            }.also {
                logger.v { "[ensureScope] create new scope: '${user.id}'" }
                _scope = it
            }
            else -> currentScope.also {
                logger.v { "[ensureScope] reuse existing scope: '${user.id}'" }
            }
        }
    }

    private fun createDatabase(
        scope: CoroutineScope,
        context: Context,
        user: User?,
        offlineEnabled: Boolean,
    ): ChatDatabase {
        logger.i { "[createDatabase] user.id: '${user?.id}', offlineEnabled: $offlineEnabled" }
        return if (offlineEnabled && user != null) {
            ChatDatabase.getDatabase(context, user.id)
        } else {
            Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build().also { inMemoryDatabase ->
                scope.launch { inMemoryDatabase.clearAllTables() }
            }
        }
    }
}
