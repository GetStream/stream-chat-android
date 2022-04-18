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
import io.getstream.chat.android.client.channel.manager.ChannelsManagerProvider
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.factory.PluginFactory
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistence.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.persistence.repository.provider.RepositoryProvider
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.errorhandler.factory.internal.OfflineErrorHandlerFactoriesProvider
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerImpl
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerProvider
import io.getstream.chat.android.offline.interceptor.internal.DefaultInterceptor
import io.getstream.chat.android.offline.interceptor.internal.SendMessageInterceptorImpl
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.internal.OfflinePlugin
import io.getstream.chat.android.offline.plugin.listener.internal.ChannelMarkReadListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.CreateChannelListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.MarkAllReadListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelsListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.QueryMembersListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.SendGiphyListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.ThreadQueryListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.TypingEventListenerImpl
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacadeBuilder
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.chat.android.offline.sync.internal.SyncManager
import io.getstream.chat.android.offline.sync.messages.internal.OfflineSyncFirebaseMessagingHandler
import io.getstream.chat.android.offline.utils.internal.ChannelMarkReadHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Implementation of [PluginFactory] that provides [OfflinePlugin].
 *
 * @param config [Config] Configuration of persistance of the SDK.
 * @param appContext [Context]
 */
public class StreamOfflinePluginFactory(
    private val config: Config,
    private val appContext: Context,
) : PluginFactory {

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = createOfflinePlugin(user)

    /**
     * Creates the [OfflinePlugin] and initialized its dependencies. This method must be called after the user is set in the SDK.
     */
    private fun createOfflinePlugin(user: User): OfflinePlugin {
        val chatClient = ChatClient.instance()
        val globalState = GlobalMutableState.getOrCreate().apply {
            clearState()
        }

        val job = SupervisorJob()
        val scope = CoroutineScope(job + DispatcherProvider.IO)

        val repositoryFactory = createRepositoryFactory(scope, appContext, user, config.persistenceEnabled)

        RepositoryProvider.changeRepositoryFactory(repositoryFactory)

        val repos = RepositoryFacadeBuilder {
            context(appContext)
            scope(scope)
            defaultConfig(
                io.getstream.chat.android.client.models.Config(
                    connectEventsEnabled = true,
                    muteEnabled = true
                )
            )
            currentUser(user)
            repositoryFactory(repositoryFactory)
        }.build()

        val userStateFlow = MutableStateFlow(ChatClient.instance().getCurrentUser())
        val stateRegistry = StateRegistry.create(job, scope, userStateFlow, repos, repos.observeLatestUsers())
        val logic = LogicRegistry.create(stateRegistry, globalState, config.userPresence, repos, chatClient)

        ChannelsManagerProvider.setChannelsManager(logic)

        val sendMessageInterceptor = SendMessageInterceptorImpl(
            context = appContext,
            logic = logic,
            globalState = globalState,
            repos = repos,
            scope = scope,
            networkType = config.uploadAttachmentsNetworkType
        )
        val defaultInterceptor = DefaultInterceptor(
            sendMessageInterceptor = sendMessageInterceptor
        )

        val channelMarkReadHelper = ChannelMarkReadHelper(
            chatClient = chatClient,
            logic = logic,
            state = stateRegistry,
            globalState = globalState,
        )

        chatClient.apply {
            addInterceptor(defaultInterceptor)
            addErrorHandlers(
                OfflineErrorHandlerFactoriesProvider.createErrorHandlerFactories()
                    .map { factory -> factory.create() }
            )
        }

        val syncManager = SyncManager(
            chatClient = chatClient,
            globalState = globalState,
            repos = repos,
            logicRegistry = logic,
            stateRegistry = stateRegistry,
            userPresence = config.userPresence,
        ).also { syncManager ->
            syncManager.clearState()
        }

        val eventHandler = EventHandlerImpl(
            recoveryEnabled = true,
            client = chatClient,
            logic = logic,
            state = stateRegistry,
            mutableGlobalState = globalState,
            repos = repos,
            syncManager = syncManager,
        ).also { eventHandler ->
            EventHandlerProvider.eventHandler = eventHandler
            eventHandler.initialize(user, scope)
            eventHandler.startListening(scope)
        }

        InitializationCoordinator.getOrCreate().run {
            addUserDisconnectedListener {
                sendMessageInterceptor.cancelJobs() // Clear all jobs that are observing attachments.
                chatClient.removeAllInterceptors()
                stateRegistry.clear()
                logic.clear()
                globalState.clearState()
                scope.launch { syncManager.storeSyncState() }
                eventHandler.stopListening()
            }
        }

        if (config.backgroundSyncEnabled) {
            chatClient.setPushNotificationReceivedListener { channelType, channelId ->
                OfflineSyncFirebaseMessagingHandler().syncMessages(appContext, "$channelType:$channelId")
            }
        }

        globalState._user.value = user

        ChatClient.OFFLINE_SUPPORT_ENABLED = true

        return OfflinePlugin(
            queryChannelsListener = QueryChannelsListenerImpl(logic),
            queryChannelListener = QueryChannelListenerImpl(logic),
            threadQueryListener = ThreadQueryListenerImpl(logic),
            channelMarkReadListener = ChannelMarkReadListenerImpl(channelMarkReadHelper),
            editMessageListener = EditMessageListenerImpl(logic, globalState),
            hideChannelListener = HideChannelListenerImpl(logic, repos),
            markAllReadListener = MarkAllReadListenerImpl(logic, stateRegistry.scope, channelMarkReadHelper),
            deleteReactionListener = DeleteReactionListenerImpl(logic, globalState, repos),
            sendReactionListener = SendReactionListenerImpl(logic, globalState, repos),
            deleteMessageListener = DeleteMessageListenerImpl(logic, globalState, repos),
            sendMessageListener = SendMessageListenerImpl(logic, repos),
            sendGiphyListener = SendGiphyListenerImpl(logic),
            shuffleGiphyListener = ShuffleGiphyListenerImpl(logic),
            queryMembersListener = QueryMembersListenerImpl(repos),
            typingEventListener = TypingEventListenerImpl(stateRegistry),
            createChannelListener = CreateChannelListenerImpl(globalState, repos),
        )
    }

    private fun createRepositoryFactory(
        scope: CoroutineScope,
        context: Context,
        user: User?,
        offlineEnabled: Boolean,
    ): RepositoryFactory {
        return DatabaseRepositoryFactory(createDatabase(scope, context, user, offlineEnabled), user)
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
}
