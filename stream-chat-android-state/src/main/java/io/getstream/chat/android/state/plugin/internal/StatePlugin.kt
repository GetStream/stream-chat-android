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

package io.getstream.chat.android.state.plugin.internal

import io.getstream.chat.android.client.errorhandler.ErrorHandler
import io.getstream.chat.android.client.interceptor.MessageInterceptor
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.DependencyResolver
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.errorhandler.factory.internal.OfflineErrorHandlerFactoriesProvider
import io.getstream.chat.android.offline.event.handler.internal.EventHandler
import io.getstream.chat.android.offline.interceptor.internal.SendMessageInterceptor
import io.getstream.chat.android.offline.plugin.listener.internal.ChannelMarkReadListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.MarkAllReadListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelsListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.SendAttachmentListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.SendGiphyListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.ThreadQueryListenerState
import io.getstream.chat.android.offline.plugin.listener.internal.TypingEventListenerState
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.sync.internal.SyncHistoryManager
import io.getstream.chat.android.offline.sync.internal.SyncManager
import kotlin.reflect.KClass

/**
 * Implementation of [Plugin] that brings support for the offline feature. This class work as a delegator of calls for one
 * of its dependencies, so avoid to add logic here.
 *
 * @param sendMessageInterceptor [SendMessageInterceptor]
 * @param logic [LogicRegistry]
 * @param repositoryFacade [RepositoryFacade]
 * @param clientState [ClientState]
 * @param stateRegistry [StateRegistry]
 * @param syncManager [SyncManager]
 * @param eventHandler [EventHandler]
 */
@InternalStreamChatApi
@Suppress("LongParameterList")
public class StatePlugin internal constructor(
    private val sendMessageInterceptor: SendMessageInterceptor,
    private val logic: LogicRegistry,
    private val repositoryFacade: RepositoryFacade,
    private val clientState: ClientState,
    private val stateRegistry: StateRegistry,
    private val syncManager: SyncManager,
    private val eventHandler: EventHandler,
) : StateAwarePlugin,
    DependencyResolver,
    QueryChannelsListener by QueryChannelsListenerState(logic),
    QueryChannelListener by QueryChannelListenerState(logic),
    ThreadQueryListener by ThreadQueryListenerState(logic, repositoryFacade),
    ChannelMarkReadListener by ChannelMarkReadListenerState(stateRegistry),
    EditMessageListener by EditMessageListenerState(logic, clientState),
    HideChannelListener by HideChannelListenerState(logic),
    MarkAllReadListener by MarkAllReadListenerState(logic, stateRegistry),
    DeleteReactionListener by DeleteReactionListenerState(logic, clientState),
    SendReactionListener by SendReactionListenerState(logic, clientState),
    DeleteMessageListener by DeleteMessageListenerState(logic, clientState),
    SendGiphyListener by SendGiphyListenerState(logic),
    ShuffleGiphyListener by ShuffleGiphyListenerState(logic),
    SendMessageListener by SendMessageListenerState(logic),
    TypingEventListener by TypingEventListenerState(stateRegistry),
    SendAttachmentListener by SendAttachmentListenerState(logic) {

    override val errorHandlers: List<ErrorHandler> = OfflineErrorHandlerFactoriesProvider
        .createErrorHandlerFactories(repositoryFacade)
        .map { factory -> factory.create() }

    override fun onUserSet(user: User) {
        syncManager.start()
        eventHandler.startListening()
    }

    override val interceptors: List<MessageInterceptor> = listOf(sendMessageInterceptor)

    override fun onUserDisconnected() {
        sendMessageInterceptor.cancelJobs()
        stateRegistry.clear()
        logic.clear()
        syncManager.stop()
        eventHandler.stopListening()
    }

    @Suppress("UNCHECKED_CAST")
    @InternalStreamChatApi
    public override fun <T : Any> resolveDependency(klass: KClass<T>): T? = when (klass) {
        SyncHistoryManager::class -> syncManager as T
        EventHandler::class -> eventHandler as T
        else -> null
    }
}
