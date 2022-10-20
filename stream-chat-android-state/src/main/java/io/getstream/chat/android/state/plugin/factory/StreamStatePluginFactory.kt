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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.event.handler.internal.EventHandler
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerSequential
import io.getstream.chat.android.offline.interceptor.internal.SendMessageInterceptor
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.offline.sync.internal.SyncManager
import io.getstream.chat.android.offline.sync.messages.internal.OfflineSyncFirebaseMessagingHandler
import io.getstream.chat.android.state.plugin.configuration.StatePluginConfig
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.logging.StreamLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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
    private val logger = StreamLog.getLogger("Chat:StatePluginFactory")

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
        return createStatePlugin(user, scope)
    }

    @SuppressWarnings("LongMethod")
    public fun createStatePlugin(
        user: User,
        scope: CoroutineScope,
    ): StatePlugin {
        logger.i { "[createStatePlugin] no args" }
        val chatClient = ChatClient.instance()
        val repositoryFacade = chatClient.repositoryFacade
        val clientState = chatClient.clientState
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

        val sendMessageInterceptor = SendMessageInterceptor(
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
            mutableGlobalState = globalState,
            repos = repositoryFacade,
            syncedEvents = syncManager.syncedEvents,
            sideEffect = syncManager::awaitSyncing
        )

        if (config.backgroundSyncEnabled) {
            chatClient.setPushNotificationReceivedListener { channelType, channelId ->
                OfflineSyncFirebaseMessagingHandler().syncMessages(appContext, "$channelType:$channelId")
            }
        }

        return StatePlugin(
            sendMessageInterceptor = sendMessageInterceptor,
            logic = logic,
            repositoryFacade = repositoryFacade,
            clientState = clientState,
            stateRegistry = stateRegistry,
            syncManager = syncManager,
            eventHandler = eventHandler,
        )
    }

    @Suppress("LongMethod", "LongParameterList")
    private fun createEventHandler(
        user: User,
        scope: CoroutineScope,
        client: ChatClient,
        logicRegistry: LogicRegistry,
        stateRegistry: StateRegistry,
        mutableGlobalState: GlobalMutableState,
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
            mutableGlobalState = mutableGlobalState,
            repos = repos,
            syncedEvents = syncedEvents,
            sideEffect = sideEffect,
        )
    }
}
