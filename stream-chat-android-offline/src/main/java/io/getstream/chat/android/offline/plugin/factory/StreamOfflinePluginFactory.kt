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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.persistance.repository.factory.RepositoryFactory
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.plugin.listeners.CreateChannelListener
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.plugin.listeners.QueryMembersListener
import io.getstream.chat.android.client.plugin.listeners.SendAttachmentListener
import io.getstream.chat.android.client.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.models.User
import io.getstream.chat.android.offline.plugin.internal.OfflinePlugin
import io.getstream.chat.android.offline.plugin.listener.internal.CreateChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.DeleteReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.EditMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.FetchCurrentUserListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.GetMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.HideChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.QueryChannelListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.QueryMembersListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendAttachmentsListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendMessageListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.SendReactionListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.ShuffleGiphyListenerDatabase
import io.getstream.chat.android.offline.plugin.listener.internal.ThreadQueryListenerDatabase
import io.getstream.chat.android.offline.repository.database.internal.ChatDatabase
import io.getstream.chat.android.offline.repository.factory.internal.DatabaseRepositoryFactory
import io.getstream.log.taggedLogger

/**
 * Implementation of [PluginFactory] that provides [OfflinePlugin].
 *
 * @param appContext [Context]
 */
public class StreamOfflinePluginFactory(private val appContext: Context) : PluginFactory, RepositoryFactory.Provider {

    private val logger by taggedLogger("Chat:OfflinePluginFactory")

    override fun createRepositoryFactory(user: User): RepositoryFactory {
        logger.i { "[createRepositoryFactory] user.id: '${user.id}'" }
        return DatabaseRepositoryFactory(
            database = createDatabase(appContext, user),
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
            clientState = clientState,
        )

        val hideChannelListener: HideChannelListener = HideChannelListenerDatabase(
            channelRepository = repositoryFacade,
            messageRepository = repositoryFacade,
        )

        val deleteReactionListener: DeleteReactionListener = DeleteReactionListenerDatabase(
            clientState = clientState,
            reactionsRepository = repositoryFacade,
            messageRepository = repositoryFacade,
        )

        val sendReactionListener = SendReactionListenerDatabase(
            clientState = clientState,
            messageRepository = repositoryFacade,
            reactionsRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val deleteMessageListener: DeleteMessageListener = DeleteMessageListenerDatabase(
            clientState = clientState,
            currentUserId = chatClient.getCurrentUser()?.id,
            messageRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val sendMessageListener: SendMessageListener = SendMessageListenerDatabase(
            repositoryFacade,
            repositoryFacade,
        )

        val sendAttachmentListener: SendAttachmentListener = SendAttachmentsListenerDatabase(
            repositoryFacade,
            repositoryFacade,
        )

        val shuffleGiphyListener: ShuffleGiphyListener = ShuffleGiphyListenerDatabase(
            userRepository = repositoryFacade,
            messageRepository = repositoryFacade,
        )

        val queryMembersListener: QueryMembersListener = QueryMembersListenerDatabase(
            repositoryFacade,
            repositoryFacade,
        )

        val createChannelListener: CreateChannelListener = CreateChannelListenerDatabase(
            clientState = clientState,
            channelRepository = repositoryFacade,
            userRepository = repositoryFacade,
        )

        val getMessageListener: GetMessageListener = GetMessageListenerDatabase(
            repositoryFacade = repositoryFacade,
        )

        val fetchCurrentUserListener = FetchCurrentUserListenerDatabase(
            userRepository = repositoryFacade,
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
            sendAttachmentListener = sendAttachmentListener,
            shuffleGiphyListener = shuffleGiphyListener,
            queryMembersListener = queryMembersListener,
            createChannelListener = createChannelListener,
            getMessageListener = getMessageListener,
            fetchCurrentUserListener = fetchCurrentUserListener,
        )
    }

    private fun createDatabase(
        context: Context,
        user: User,
    ): ChatDatabase {
        return ChatDatabase.getDatabase(context, user.id)
    }
}
