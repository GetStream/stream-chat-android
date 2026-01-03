/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.errorhandler.factory.ErrorHandlerFactory
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.listeners.BlockUserListener
import io.getstream.chat.android.client.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.plugin.listeners.DeleteChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageForMeListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.DraftMessageListener
import io.getstream.chat.android.client.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.plugin.listeners.FetchCurrentUserListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.LiveLocationListener
import io.getstream.chat.android.client.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.plugin.listeners.PushPreferencesListener
import io.getstream.chat.android.client.plugin.listeners.QueryBlockedUsersListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.QueryThreadsListener
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.client.plugin.listeners.UnblockUserListener
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.internal.EventHandler
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.listener.internal.BlockUserListenerState
import io.getstream.chat.android.state.plugin.listener.internal.ChannelMarkReadListenerState
import io.getstream.chat.android.state.plugin.listener.internal.DeleteChannelListenerState
import io.getstream.chat.android.state.plugin.listener.internal.DeleteMessageForMeListenerState
import io.getstream.chat.android.state.plugin.listener.internal.DeleteMessageListenerState
import io.getstream.chat.android.state.plugin.listener.internal.DeleteReactionListenerState
import io.getstream.chat.android.state.plugin.listener.internal.DraftMessageListenerState
import io.getstream.chat.android.state.plugin.listener.internal.EditMessageListenerState
import io.getstream.chat.android.state.plugin.listener.internal.FetchCurrentUserListenerState
import io.getstream.chat.android.state.plugin.listener.internal.HideChannelListenerState
import io.getstream.chat.android.state.plugin.listener.internal.LiveLocationListenerState
import io.getstream.chat.android.state.plugin.listener.internal.MarkAllReadListenerState
import io.getstream.chat.android.state.plugin.listener.internal.PushPreferencesListenerState
import io.getstream.chat.android.state.plugin.listener.internal.QueryBlockedUsersListenerState
import io.getstream.chat.android.state.plugin.listener.internal.QueryChannelListenerState
import io.getstream.chat.android.state.plugin.listener.internal.QueryChannelsListenerState
import io.getstream.chat.android.state.plugin.listener.internal.QueryMembersListenerState
import io.getstream.chat.android.state.plugin.listener.internal.QueryThreadsListenerState
import io.getstream.chat.android.state.plugin.listener.internal.SendAttachmentListenerState
import io.getstream.chat.android.state.plugin.listener.internal.SendGiphyListenerState
import io.getstream.chat.android.state.plugin.listener.internal.SendMessageListenerState
import io.getstream.chat.android.state.plugin.listener.internal.SendReactionListenerState
import io.getstream.chat.android.state.plugin.listener.internal.ShuffleGiphyListenerState
import io.getstream.chat.android.state.plugin.listener.internal.ThreadQueryListenerState
import io.getstream.chat.android.state.plugin.listener.internal.TypingEventListenerState
import io.getstream.chat.android.state.plugin.listener.internal.UnblockUserListenerState
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.sync.internal.SyncHistoryManager
import io.getstream.chat.android.state.sync.internal.SyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

/**
 * Implementation of [Plugin] that brings support for the offline feature. This class work as a delegator of calls for one
 * of its dependencies, so avoid to add logic here.
 *
 * @param logic [LogicRegistry]
 * @param repositoryFacade [RepositoryFacade]
 * @param clientState [ClientState]
 * @param stateRegistry [StateRegistry]
 * @param syncManager [SyncManager]
 * @param eventHandler [EventHandler]
 * @param mutableGlobalState [GlobalState]
 */
@InternalStreamChatApi
@Suppress("LongParameterList")
public class StatePlugin internal constructor(
    private val errorHandlerFactory: ErrorHandlerFactory,
    private val logic: LogicRegistry,
    private val repositoryFacade: RepositoryFacade,
    private val clientState: ClientState,
    private val stateRegistry: StateRegistry,
    private val syncManager: SyncManager,
    private val eventHandler: EventHandler,
    private val mutableGlobalState: MutableGlobalState,
    private val queryingChannelsFree: MutableStateFlow<Boolean>,
    private val statePluginConfig: StatePluginConfig,
) : Plugin,
    QueryMembersListener by QueryMembersListenerState(logic),
    QueryChannelsListener by QueryChannelsListenerState(logic, queryingChannelsFree),
    QueryChannelListener by QueryChannelListenerState(logic),
    ThreadQueryListener by ThreadQueryListenerState(logic, repositoryFacade),
    ChannelMarkReadListener by ChannelMarkReadListenerState(logic),
    EditMessageListener by EditMessageListenerState(logic, clientState),
    HideChannelListener by HideChannelListenerState(logic),
    MarkAllReadListener by MarkAllReadListenerState(logic),
    DeleteReactionListener by DeleteReactionListenerState(logic, clientState),
    DeleteChannelListener by DeleteChannelListenerState(logic, clientState),
    SendReactionListener by SendReactionListenerState(logic, clientState),
    DeleteMessageListener by DeleteMessageListenerState(logic, clientState, mutableGlobalState),
    DeleteMessageForMeListener by DeleteMessageForMeListenerState(logic, clientState),
    SendGiphyListener by SendGiphyListenerState(logic),
    ShuffleGiphyListener by ShuffleGiphyListenerState(logic),
    SendMessageListener by SendMessageListenerState(logic),
    TypingEventListener by TypingEventListenerState(logic),
    SendAttachmentListener by SendAttachmentListenerState(logic),
    FetchCurrentUserListener by FetchCurrentUserListenerState(clientState, mutableGlobalState),
    QueryThreadsListener by QueryThreadsListenerState(logic),
    BlockUserListener by BlockUserListenerState(mutableGlobalState),
    DraftMessageListener by DraftMessageListenerState(mutableGlobalState),
    UnblockUserListener by UnblockUserListenerState(mutableGlobalState),
    LiveLocationListener by LiveLocationListenerState(mutableGlobalState),
    QueryBlockedUsersListener by QueryBlockedUsersListenerState(mutableGlobalState),
    PushPreferencesListener by PushPreferencesListenerState(logic) {

    private val lazyErrorHandler: ErrorHandler by lazy { errorHandlerFactory.create() }
    override fun getErrorHandler(): ErrorHandler = lazyErrorHandler

    override fun onUserSet(user: User) {
        syncManager.start()
        eventHandler.startListening()
    }

    override fun onUserDisconnected() {
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
        LogicRegistry::class -> logic as T
        StateRegistry::class -> stateRegistry as T
        GlobalState::class -> mutableGlobalState as T
        StatePluginConfig::class -> statePluginConfig as T
        else -> null
    }
}
