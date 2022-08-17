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
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.internal.OfflinePlugin
import io.getstream.chat.android.offline.plugin.listener.internal.CreateChannelListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.QueryMembersListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerComposite
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerDatabase
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

    private var cachedOfflinePluginInstance: OfflinePlugin? = null
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        StreamLog.e("StreamOfflinePlugin", throwable) {
            "[uncaughtCoroutineException] throwable: $throwable, context: $context"
        }
    }
    private val scope = CoroutineScope(SupervisorJob() + DispatcherProvider.IO + exceptionHandler)
    private val logger = StreamLog.getLogger("Chat:StreamOfflinePluginFactory")

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = getOrCreateOfflinePlugin(user)

    private val statePluginFactory = StreamStatePluginFactory(
        config = StatePluginConfig(
            backgroundSyncEnabled = config.backgroundSyncEnabled,
            userPresence = config.userPresence,
            uploadAttachmentsNetworkType = config.uploadAttachmentsNetworkType
        ),
        appContext = appContext
    )

    /**
     * Tries to get cached [OfflinePlugin] instance for the user if it exists or
     * creates the new [OfflinePlugin] and initialized its dependencies.
     *
     * This method must be called after the user is set in the SDK.
     */
    private fun getOrCreateOfflinePlugin(user: User): OfflinePlugin {
        val cachedPlugin = cachedOfflinePluginInstance

        if (cachedPlugin != null && cachedPlugin.activeUser.id == user.id) {
            logger.i { "OfflinePlugin for the user is already initialized. Returning cached instance." }
            return cachedPlugin
        } else {
            clearCachedInstance()
        }

        ChatClient.OFFLINE_SUPPORT_ENABLED = true

        val statePlugin = statePluginFactory.createStatePlugin(user, scope)

        InitializationCoordinator.getOrCreate().addUserDisconnectedListener {
            clearCachedInstance()
        }

        val chatClient = ChatClient.instance()
        val repositoryFactory = chatClient.repositoryFacade

        val hideChannelListener: HideChannelListener = getHideChannelListener(chatClient, statePlugin)
        val deleteReactionListener: DeleteReactionListener = getDeleteReactionListener(chatClient, statePlugin)
        val shuffleGiphyListener: ShuffleGiphyListener = getShuffleGiphyListener(chatClient, statePlugin)
        val sendReactionListener = getSendReactionListener(chatClient, statePlugin)
        val deleteMessageListener: DeleteMessageListener = getDeleteMessageListenerDatabase(chatClient, statePlugin)
        val createChannelListener: CreateChannelListener = CreateChannelListenerImpl(
            clientState = chatClient.clientState,
            channelRepository = repositoryFactory,
            userRepository = repositoryFactory
        )
        val editMessageListener = getEditMessageListener(chatClient, statePlugin)
        val queryMembersListener: QueryMembersListener =
            QueryMembersListenerDatabase(chatClient.repositoryFacade, chatClient.repositoryFacade)

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
            sendMessageListener = statePlugin,
            sendGiphyListener = statePlugin,
            shuffleGiphyListener = shuffleGiphyListener,
            queryMembersListener = queryMembersListener,
            typingEventListener = statePlugin,
            createChannelListener = createChannelListener,
            childResolver = statePlugin
        ).also { offlinePlugin -> cachedOfflinePluginInstance = offlinePlugin }
    }

    private fun getHideChannelListener(chatClient: ChatClient, statePlugin: StatePlugin): HideChannelListener {
        val hideChannelListenerDatabase = HideChannelListenerDatabase(
            channelRepository = chatClient.repositoryFacade,
            messageRepository = chatClient.repositoryFacade
        )

        return HideChannelListenerComposite(
            listOf(statePlugin, hideChannelListenerDatabase)
        )
    }

    private fun getDeleteReactionListener(chatClient: ChatClient, statePlugin: StatePlugin): DeleteReactionListener {
        val deleteReactionListenerDatabase = DeleteReactionListenerDatabase(
            clientState = chatClient.clientState,
            reactionsRepository = chatClient.repositoryFacade,
            messageRepository = chatClient.repositoryFacade
        )

        return DeleteReactionListenerComposite(
            listOf(deleteReactionListenerDatabase, statePlugin)
        )
    }

    private fun getShuffleGiphyListener(chatClient: ChatClient, statePlugin: StatePlugin): ShuffleGiphyListener {
        val shuffleGiphyListenerDatabase = ShuffleGiphyListenerDatabase(
            userRepository = chatClient.repositoryFacade,
            messageRepository = chatClient.repositoryFacade
        )

        return ShuffleGiphyListenerComposite(
            listOf(shuffleGiphyListenerDatabase, statePlugin)
        )
    }

    private fun getSendReactionListener(chatClient: ChatClient, statePlugin: StatePlugin): SendReactionListener {
        val sendReactionListenerDatabase = SendReactionListenerDatabase(
            clientState = chatClient.clientState,
            messageRepository = chatClient.repositoryFacade,
            reactionsRepository = chatClient.repositoryFacade,
            userRepository = chatClient.repositoryFacade
        )

        return SendReactionListenerComposite(
            listOf(statePlugin, sendReactionListenerDatabase)
        )
    }

    private fun getEditMessageListener(chatClient: ChatClient, statePlugin: StatePlugin): EditMessageListenerComposite {
        val editMessageListenerDatabase = EditMessageListenerDatabase(
            userRepository = chatClient.repositoryFacade,
            messageRepository = chatClient.repositoryFacade,
            clientState = chatClient.clientState
        )

        return EditMessageListenerComposite(listOf(statePlugin, editMessageListenerDatabase))
    }

    private fun getDeleteMessageListenerDatabase(
        chatClient: ChatClient,
        statePlugin: StatePlugin,
    ): DeleteMessageListener {
        val deleteMessageListenerDatabase = DeleteMessageListenerDatabase(
            clientState = chatClient.clientState,
            messageRepository = chatClient.repositoryFacade,
            userRepository = chatClient.repositoryFacade
        )

        return DeleteMessageListenerComposite(
            listOf(statePlugin, deleteMessageListenerDatabase)
        )
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
        return if (offlineEnabled && user != null) {
            ChatDatabase.getDatabase(context, user.id)
        } else {
            Room.inMemoryDatabaseBuilder(context, ChatDatabase::class.java).build().also { inMemoryDatabase ->
                scope.launch { inMemoryDatabase.clearAllTables() }
            }
        }
    }

    override fun createRepositoryFactory(user: User): RepositoryFactory {
        return DatabaseRepositoryFactory(createDatabase(scope, appContext, user, config.persistenceEnabled), user)
    }
}
