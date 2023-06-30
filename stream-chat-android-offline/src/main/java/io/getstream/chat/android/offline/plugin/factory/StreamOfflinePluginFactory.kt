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
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.internal.OfflinePlugin
import io.getstream.chat.android.offline.plugin.listener.internal.CreateChannelListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.FetchCurrentUserListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.FetchCurrentUserListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.GetMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.QueryMembersListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerDatabase
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.chat.android.state.plugin.configuration.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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

    @Volatile
    private var cachedOfflinePluginInstance: OfflinePlugin? = null
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        StreamLog.e("StreamOfflinePlugin", throwable) {
            "[uncaughtCoroutineException] throwable: $throwable, context: $context"
        }
    }
    private val statePluginFactory = StreamStatePluginFactory(
        config = StatePluginConfig(
            backgroundSyncEnabled = config.backgroundSyncEnabled,
            userPresence = config.userPresence,
            uploadAttachmentsNetworkType = config.uploadAttachmentsNetworkType,
            useSequentialEventHandler = config.useSequentialEventHandler,
        ),
        appContext = appContext
    )

    @Volatile
    private var _scope: CoroutineScope? = null

    override fun createRepositoryFactory(user: User): RepositoryFactory {
        logger.i { "[createRepositoryFactory] user.id: '${user.id}'" }
        val scope = ensureScope(user)
        return DatabaseRepositoryFactory(
            database = createDatabase(scope, appContext, user, config.persistenceEnabled),
            currentUser = user
        )
    }

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = getOrCreateOfflinePlugin(user)

    /**
     * Tries to get cached [OfflinePlugin] instance for the user if it exists or
     * creates the new [OfflinePlugin] and initialized its dependencies.
     *
     * This method must be called after the user is set in the SDK.
     */
    @Suppress("LongMethod")
    private fun getOrCreateOfflinePlugin(user: User): OfflinePlugin {
        logger.i { "[getOrCreateOfflinePlugin] user.id: '${user.id}'" }
        val cachedPlugin = cachedOfflinePluginInstance

        if (cachedPlugin != null && cachedPlugin.activeUser.id == user.id) {
            logger.i { "OfflinePlugin for the user is already initialized. Returning cached instance." }
            return cachedPlugin
        } else {
            clearCachedInstance()
        }

        ChatClient.OFFLINE_SUPPORT_ENABLED = true

        val scope = ensureScope(user)

        val statePlugin = statePluginFactory.createStatePlugin(user, scope)

        InitializationCoordinator.getOrCreate().addUserDisconnectedListener {
            logger.i { "[onUserDisconnected] user.id: '${it?.id}'" }
            clearCachedInstance()
            _scope?.cancel()
            _scope = null
        }

        val chatClient = ChatClient.instance()
        val clientState = chatClient.clientState
        val repositoryFacade = chatClient.repositoryFacade

        val editMessageListener = getEditMessageListener(clientState, repositoryFacade, statePlugin)
        val hideChannelListener: HideChannelListener = getHideChannelListener(repositoryFacade, statePlugin)
        val deleteReactionListener: DeleteReactionListener = getDeleteReactionListener(
            clientState, repositoryFacade, statePlugin
        )
        val sendReactionListener = getSendReactionListener(clientState, repositoryFacade, statePlugin)
        val deleteMessageListener: DeleteMessageListener = getDeleteMessageListenerDatabase(
            clientState, repositoryFacade, statePlugin
        )
        val sendMessageListener: SendMessageListener = getSendMessageListener(repositoryFacade, statePlugin)
        val shuffleGiphyListener: ShuffleGiphyListener = getShuffleGiphyListener(repositoryFacade, statePlugin)
        val queryMembersListener: QueryMembersListener = QueryMembersListenerDatabase(
            repositoryFacade, repositoryFacade
        )
        val createChannelListener: CreateChannelListener = CreateChannelListenerImpl(
            clientState = clientState,
            channelRepository = repositoryFacade,
            userRepository = repositoryFacade
        )
        val getMessageListener: GetMessageListener = getGetMessageListenerDatabase(repositoryFacade)

        val fetchCurrentUserListener: FetchCurrentUserListener = getFetchCurrentUserListener(
            repositoryFacade, statePlugin
        )

        return OfflinePlugin(
            activeUser = user,
            queryChannelsListener = statePlugin,
            queryChannelListener = statePlugin,
            threadQueryListener = statePlugin,
            channelMarkReadListener = statePlugin,
            editMessageListener = editMessageListener,
            hideChannelListener = hideChannelListener,
            markAllReadListener = statePlugin,
            deleteReactionListener = deleteReactionListener,
            sendReactionListener = sendReactionListener,
            deleteMessageListener = deleteMessageListener,
            sendMessageListener = sendMessageListener,
            sendGiphyListener = statePlugin,
            shuffleGiphyListener = shuffleGiphyListener,
            queryMembersListener = queryMembersListener,
            typingEventListener = statePlugin,
            createChannelListener = createChannelListener,
            getMessageListener = getMessageListener,
            fetchCurrentUserListener = fetchCurrentUserListener,
            childResolver = statePlugin
        ).also { offlinePlugin -> cachedOfflinePluginInstance = offlinePlugin }
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

    private fun getEditMessageListener(
        clientState: ClientState,
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): EditMessageListenerComposite {
        val editMessageListenerDatabase = EditMessageListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade,
            clientState = clientState
        )

        return EditMessageListenerComposite(listOf(statePlugin, editMessageListenerDatabase))
    }

    private fun getHideChannelListener(
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): HideChannelListener {
        val hideChannelListenerDatabase = HideChannelListenerDatabase(
            channelRepository = repositoryFacade,
            messageRepository = repositoryFacade
        )

        return HideChannelListenerComposite(
            listOf(statePlugin, hideChannelListenerDatabase)
        )
    }

    private fun getDeleteReactionListener(
        clientState: ClientState,
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): DeleteReactionListener {
        val deleteReactionListenerDatabase = DeleteReactionListenerDatabase(
            clientState = clientState,
            reactionsRepository = repositoryFacade,
            messageRepository = repositoryFacade
        )

        return DeleteReactionListenerComposite(
            listOf(deleteReactionListenerDatabase, statePlugin)
        )
    }

    private fun getSendReactionListener(
        clientState: ClientState,
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): SendReactionListener {
        val sendReactionListenerDatabase = SendReactionListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            reactionsRepository = repositoryFacade,
            userRepository = repositoryFacade
        )

        return SendReactionListenerComposite(
            listOf(statePlugin, sendReactionListenerDatabase)
        )
    }

    private fun getSendMessageListener(
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): SendMessageListener {

        val sendMessageListenerDatabase = SendMessageListenerDatabase(repositoryFacade, repositoryFacade)
        return SendMessageListenerComposite(listOf(statePlugin, sendMessageListenerDatabase))
    }

    private fun getShuffleGiphyListener(
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): ShuffleGiphyListener {
        val shuffleGiphyListenerDatabase = ShuffleGiphyListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade
        )

        return ShuffleGiphyListenerComposite(
            listOf(shuffleGiphyListenerDatabase, statePlugin)
        )
    }

    private fun getDeleteMessageListenerDatabase(
        clientState: ClientState,
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): DeleteMessageListener {
        val deleteMessageListenerDatabase = DeleteMessageListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            userRepository = repositoryFacade
        )

        return DeleteMessageListenerComposite(
            listOf(statePlugin, deleteMessageListenerDatabase)
        )
    }

    /**
     * Creates an instance of [GetMessageListener].
     *
     * @param repositoryFacade A class that holds a collection of repositories used by the SDK and exposes
     * various repository operations as methods.
     *
     * @return An instance of [getGetMessageListenerDatabase].
     */
    private fun getGetMessageListenerDatabase(repositoryFacade: RepositoryFacade): GetMessageListener =
        GetMessageListenerDatabase(repositoryFacade = repositoryFacade)

    private fun getFetchCurrentUserListener(
        repositoryFacade: RepositoryFacade,
        statePlugin: StatePlugin,
    ): FetchCurrentUserListener {
        val fetchCurrentUserListenerDatabase = FetchCurrentUserListenerDatabase(
            userRepository = repositoryFacade,
        )
        return FetchCurrentUserListenerComposite(listOf(statePlugin, fetchCurrentUserListenerDatabase))
    }

    private fun clearCachedInstance() {
        cachedOfflinePluginInstance = null
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
