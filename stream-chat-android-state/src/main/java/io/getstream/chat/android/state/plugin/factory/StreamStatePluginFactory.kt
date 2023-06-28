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

package io.getstream.chat.android.state.plugin.factory

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.interceptor.message.PrepareMessageLogicFactory
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.errorhandler.factory.internal.OfflineErrorHandlerFactoriesProvider
import io.getstream.chat.android.offline.event.handler.internal.EventHandler
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerImpl
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerSequential
import io.getstream.chat.android.offline.interceptor.internal.SendMessageInterceptorImpl
import io.getstream.chat.android.offline.plugin.listener.internal.ChannelMarkReadListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.FetchCurrentUserListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.MarkAllReadListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelsListenerImpl
import io.getstream.chat.android.offline.plugin.listener.internal.SendGiphyListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.ThreadQueryListenerFull
import io.getstream.chat.android.offline.plugin.listener.internal.TypingEventListenerState
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.sync.internal.SyncHistoryManager
import io.getstream.chat.android.offline.sync.internal.SyncManager
import io.getstream.chat.android.offline.sync.messages.internal.OfflineSyncFirebaseMessagingHandler
import io.getstream.chat.android.offline.utils.internal.ChannelMarkReadHelper
import io.getstream.chat.android.state.plugin.configuration.StatePluginConfig
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.job
import kotlin.reflect.KClass

/**
 * Implementation of [PluginFactory] that provides [StatePlugin].
 *
 * @param config [StatePluginConfig] Configuration of persistence of the SDK.
 * @param appContext [Context]
 */
public class StreamStatePluginFactory(
    private val config: StatePluginConfig,
    private val appContext: Context,
) : PluginFactory {

    @Volatile
    private var cachedStatePluginInstance: StatePlugin? = null

    private val logger = StreamLog.getLogger("Chat:StatePluginFactory")

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = getOrCreateStatePlugin(user)

    /**
     * Tries to get cached [StatePlugin] instance for the user if it exists or
     * creates the new [StatePlugin] and initialized its dependencies.
     *
     * This method must be called after the user is set in the SDK.
     */
    private fun getOrCreateStatePlugin(user: User): StatePlugin {
        val cachedPlugin = cachedStatePluginInstance

        if (cachedPlugin != null && cachedPlugin.activeUser.id == user.id) {
            logger.i { "OfflinePlugin for the user is already initialized. Returning cached instance." }
            return cachedPlugin
        } else {
            clearCachedInstance()
        }
        return createStatePlugin(user).also { offlinePlugin -> cachedStatePluginInstance = offlinePlugin }
    }

    private fun createStatePlugin(user: User): StatePlugin {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            StreamLog.e("StreamStatePlugin", throwable) {
                "[uncaughtCoroutineException] throwable: $throwable, context: $context"
            }
        }
        val scope = ChatClient.instance().inheritScope { parentJob ->
            SupervisorJob(parentJob) + DispatcherProvider.IO + exceptionHandler
        }
        return createStatePlugin(user, scope)
    }

    @InternalStreamChatApi
    @SuppressWarnings("LongMethod")
    public fun createStatePlugin(
        user: User,
        scope: CoroutineScope,
    ): StatePlugin {
        logger.i { "[createStatePlugin] no args" }
        val chatClient = ChatClient.instance()
        val repositoryFacade = chatClient.repositoryFacade
        val clientState = chatClient.clientState.also { clientState ->
            clientState.clearState()
        }
        val globalState = GlobalMutableState.get(chatClient.clientState).apply {
            clearState()
        }

        val stateRegistry = StateRegistry.create(
            scope.coroutineContext.job, scope, clientState.user, repositoryFacade, repositoryFacade.observeLatestUsers()
        )
        val logic = LogicRegistry.create(
            stateRegistry = stateRegistry,
            globalState = globalState,
            userPresence = config.userPresence,
            repos = repositoryFacade,
            client = chatClient,
            clientState = clientState,
            coroutineScope = scope,
        )

        val sendMessageInterceptor = SendMessageInterceptorImpl(
            context = appContext,
            logic = logic,
            clientState = clientState,
            channelRepository = repositoryFacade,
            messageRepository = repositoryFacade,
            attachmentRepository = repositoryFacade,
            scope = scope,
            networkType = config.uploadAttachmentsNetworkType,
            user = user,
            prepareMessageLogic = PrepareMessageLogicFactory().create()
        )

        val channelMarkReadHelper = ChannelMarkReadHelper(
            logic = logic,
            state = stateRegistry,
        )

        chatClient.apply {
            addInterceptor(sendMessageInterceptor)
            addErrorHandlers(
                OfflineErrorHandlerFactoriesProvider.createErrorHandlerFactories(repositoryFacade)
                    .map { factory -> factory.create() }
            )
        }

        val syncManager = SyncManager(
            currentUserId = user.id,
            scope = scope,
            chatClient = chatClient,
            clientState = clientState,
            repos = repositoryFacade,
            logicRegistry = logic,
            stateRegistry = stateRegistry,
            userPresence = config.userPresence,
        )
        syncManager.start()

        val eventHandler: EventHandler = createEventHandler(
            user = user,
            useSequentialEventHandler = config.useSequentialEventHandler,
            scope = scope,
            client = chatClient,
            logicRegistry = logic,
            stateRegistry = stateRegistry,
            mutableGlobalState = globalState,
            repos = repositoryFacade,
            syncedEvents = syncManager.syncedEvents,
            sideEffect = syncManager::awaitSyncing
        )
        eventHandler.startListening()

        InitializationCoordinator.getOrCreate().run {
            addUserDisconnectedListener {
                sendMessageInterceptor.cancelJobs() // Clear all jobs that are observing attachments.
                chatClient.removeAllInterceptors()
                stateRegistry.clear()
                logic.clear()
                clientState.clearState()
                globalState.clearState()
                syncManager.stop()
                eventHandler.stopListening()
                clearCachedInstance()
                scope.cancel()
            }
        }

        if (config.backgroundSyncEnabled) {
            chatClient.setPushNotificationReceivedListener { channelType, channelId ->
                OfflineSyncFirebaseMessagingHandler().syncMessages(appContext, "$channelType:$channelId")
            }
        }

        val getMessageFun: suspend (String) -> Result<Message> = { messageId: String ->
            chatClient.getMessage(messageId).await()
        }

        return StatePlugin(
            activeUser = user,
            queryChannelsListener = QueryChannelsListenerImpl(logic),
            queryChannelListener = QueryChannelListenerImpl(logic),
            threadQueryListener = ThreadQueryListenerFull(logic, repositoryFacade, repositoryFacade, getMessageFun),
            channelMarkReadListener = ChannelMarkReadListenerState(channelMarkReadHelper),
            editMessageListener = EditMessageListenerState(logic, clientState),
            hideChannelListener = HideChannelListenerState(logic),
            markAllReadListener = MarkAllReadListenerState(logic, stateRegistry.scope, channelMarkReadHelper),
            deleteReactionListener = DeleteReactionListenerState(logic, clientState),
            sendReactionListener = SendReactionListenerState(logic, clientState),
            deleteMessageListener = DeleteMessageListenerState(logic, clientState),
            sendMessageListener = SendMessageListenerState(logic),
            sendGiphyListener = SendGiphyListenerState(logic),
            shuffleGiphyListener = ShuffleGiphyListenerState(logic),
            typingEventListener = TypingEventListenerState(stateRegistry),
            fetchCurrentUserListener = FetchCurrentUserListenerState(globalState),
            provideDependency = createDependencyProvider(syncManager, eventHandler)
        )
    }

    private fun createDependencyProvider(
        syncManager: SyncManager,
        eventHandler: EventHandler,
    ): (KClass<*>) -> Any? {
        return { klass ->
            when (klass) {
                SyncHistoryManager::class -> syncManager
                EventHandler::class -> eventHandler
                else -> null
            }
        }
    }

    @Suppress("LongMethod", "LongParameterList")
    private fun createEventHandler(
        user: User,
        useSequentialEventHandler: Boolean,
        scope: CoroutineScope,
        client: ChatClient,
        logicRegistry: LogicRegistry,
        stateRegistry: StateRegistry,
        mutableGlobalState: GlobalMutableState,
        repos: RepositoryFacade,
        sideEffect: suspend () -> Unit,
        syncedEvents: Flow<List<ChatEvent>>,
    ): EventHandler {
        return when (useSequentialEventHandler) {
            true -> EventHandlerSequential(
                scope = scope,
                currentUserId = user.id,
                subscribeForEvents = { listener -> client.subscribe(listener) },
                logicRegistry = logicRegistry,
                stateRegistry = stateRegistry,
                mutableGlobalState = mutableGlobalState,
                repos = repos,
                syncedEvents = syncedEvents,
                sideEffect = sideEffect,
            )
            else -> EventHandlerImpl(
                scope = scope,
                currentUserId = user.id,
                subscribeForEvents = { listener -> client.subscribe(listener) },
                logic = logicRegistry,
                state = stateRegistry,
                mutableGlobalState = mutableGlobalState,
                repos = repos,
                syncedEvents = syncedEvents
            )
        }
    }

    private fun clearCachedInstance() {
        cachedStatePluginInstance = null
    }
}
