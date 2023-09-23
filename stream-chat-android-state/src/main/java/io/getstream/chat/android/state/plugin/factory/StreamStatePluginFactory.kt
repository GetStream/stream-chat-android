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
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.errorhandler.StateErrorHandlerFactory
import io.getstream.chat.android.state.event.handler.internal.EventHandler
import io.getstream.chat.android.state.event.handler.internal.EventHandlerSequential
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.sync.internal.OfflineSyncFirebaseMessagingHandler
import io.getstream.chat.android.state.sync.internal.SyncManager
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.job

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
    private val logger by taggedLogger("Chat:StatePluginFactory")

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = createStatePlugin(user)

    private fun createStatePlugin(user: User): StatePlugin {
        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            StreamLog.e("StreamStatePlugin", throwable) {
                "[uncaughtCoroutineException] throwable: $throwable, context: $context"
            }
        }
        val scope = ChatClient.instance().inheritScope { parentJob ->
            SupervisorJob(parentJob) + DispatcherProvider.IO + exceptionHandler
        }
        return createStatePlugin(user, scope, MutableGlobalState())
    }

    @SuppressWarnings("LongMethod")
    private fun createStatePlugin(
        user: User,
        scope: CoroutineScope,
        mutableGlobalState: MutableGlobalState,
    ): StatePlugin {
        logger.i { "[createStatePlugin] no args" }
        val chatClient = ChatClient.instance()
        val repositoryFacade = chatClient.repositoryFacade
        val clientState = chatClient.clientState

        val stateRegistry = StateRegistry(
            clientState.user,
            repositoryFacade.observeLatestUsers(),
            scope.coroutineContext.job,
            scope,
        )

        val isQueryingFree = MutableStateFlow(true)

        val logic = LogicRegistry(
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            userPresence = config.userPresence,
            repos = repositoryFacade,
            client = chatClient,
            coroutineScope = scope,
            queryingChannelsFree = isQueryingFree,
        )

        chatClient.logicRegistry = logic

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

        val eventHandler: EventHandler = createEventHandler(
            user = user,
            scope = scope,
            client = chatClient,
            logicRegistry = logic,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repositoryFacade,
            syncedEvents = syncManager.syncedEvents,
            sideEffect = syncManager::awaitSyncing,
        )

        if (config.backgroundSyncEnabled) {
            chatClient.setPushNotificationReceivedListener { channelType, channelId ->
                OfflineSyncFirebaseMessagingHandler().syncMessages(appContext, "$channelType:$channelId")
            }
        }

        val stateErrorHandlerFactory = StateErrorHandlerFactory(
            scope = scope,
            logicRegistry = logic,
            clientState = clientState,
            repositoryFacade = repositoryFacade,
        )

        return StatePlugin(
            errorHandlerFactory = stateErrorHandlerFactory,
            logic = logic,
            repositoryFacade = repositoryFacade,
            clientState = clientState,
            stateRegistry = stateRegistry,
            syncManager = syncManager,
            eventHandler = eventHandler,
            globalState = mutableGlobalState,
            queryingChannelsFree = isQueryingFree,
            statePluginConfig = config,
        )
    }

    @Suppress("LongMethod", "LongParameterList")
    private fun createEventHandler(
        user: User,
        scope: CoroutineScope,
        client: ChatClient,
        logicRegistry: LogicRegistry,
        stateRegistry: StateRegistry,
        clientState: ClientState,
        mutableGlobalState: MutableGlobalState,
        repos: RepositoryFacade,
        sideEffect: suspend () -> Unit,
        syncedEvents: Flow<List<ChatEvent>>,
    ): EventHandler {
        return EventHandlerSequential(
            scope = scope,
            currentUserId = user.id,
            subscribeForEvents = { listener -> client.subscribe(listener) },
            logicRegistry = logicRegistry,
            stateRegistry = stateRegistry,
            clientState = clientState,
            mutableGlobalState = mutableGlobalState,
            repos = repos,
            syncedEvents = syncedEvents,
            sideEffect = sideEffect,
        )
    }
}
