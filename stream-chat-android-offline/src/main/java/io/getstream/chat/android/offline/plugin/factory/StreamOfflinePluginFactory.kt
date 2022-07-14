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
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryProvider
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.internal.OfflinePlugin
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.chat.android.state.plugin.configuration.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
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
) : PluginFactory {

    private var cachedOfflinePluginInstance: OfflinePlugin? = null

    private val logger = StreamLog.getLogger("Chat:StreamOfflinePluginFactory")

    /**
     * Creates a [Plugin]
     *
     * @return The [Plugin] instance.
     */
    override fun get(user: User): Plugin = getOrCreateOfflinePlugin(user)

    private var repositoryFactory: RepositoryFactory? = null

    private val statePluginFactory = StreamStatePluginFactory(
        config = StatePluginConfig(
            backgroundSyncEnabled = config.backgroundSyncEnabled,
            userPresence = config.userPresence,
            uploadAttachmentsNetworkType = config.uploadAttachmentsNetworkType
        ),
        appContext = appContext
    )

    /**
     * Sets a custom repository factory. Use this to change the persistence layer of the SDK.
     */
    public fun setRepositoryFactory(repositoryFactory: RepositoryFactory) {
        this.repositoryFactory = repositoryFactory
    }

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

        val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
            StreamLog.e("StreamOfflinePlugin", throwable) {
                "[uncaughtCoroutineException] throwable: $throwable, context: $context"
            }
        }
        val job = SupervisorJob()
        val scope = CoroutineScope(job + DispatcherProvider.IO + exceptionHandler)

        val repositoryFactory = repositoryFactory
            ?: createRepositoryFactory(scope, appContext, user, config.persistenceEnabled)

        RepositoryProvider.changeRepositoryFactory(repositoryFactory)

        ChatClient.OFFLINE_SUPPORT_ENABLED = true

        val statePlugin = statePluginFactory.createStatePlugin(user, scope, repositoryFactory)

        InitializationCoordinator.getOrCreate().addUserDisconnectedListener {
            clearCachedInstance()
        }

        return OfflinePlugin(
            queryChannelsListener = statePlugin,
            queryChannelListener = statePlugin,
            threadQueryListener = statePlugin,
            channelMarkReadListener = statePlugin,
            editMessageListener = statePlugin,
            hideChannelListener = statePlugin,
            markAllReadListener = statePlugin,
            deleteReactionListener = statePlugin,
            sendReactionListener = statePlugin,
            deleteMessageListener = statePlugin,
            sendMessageListener = statePlugin,
            sendGiphyListener = statePlugin,
            shuffleGiphyListener = statePlugin,
            queryMembersListener = statePlugin,
            typingEventListener = statePlugin,
            createChannelListener = statePlugin,
            activeUser = user
        ).also { offlinePlugin -> cachedOfflinePluginInstance = offlinePlugin }
    }

    private fun clearCachedInstance() {
        cachedOfflinePluginInstance = null
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
