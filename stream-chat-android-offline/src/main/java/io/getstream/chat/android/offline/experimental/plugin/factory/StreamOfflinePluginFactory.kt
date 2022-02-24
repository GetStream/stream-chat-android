package io.getstream.chat.android.offline.experimental.plugin.factory

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.factory.PluginFactory
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.InitializationCoordinator
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.interceptor.DefaultInterceptor
import io.getstream.chat.android.offline.experimental.interceptor.SendMessageInterceptorImpl
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.configuration.Config
import io.getstream.chat.android.offline.experimental.plugin.listener.ChannelMarkReadListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.DeleteMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.DeleteReactionListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.EditMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.GetMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.HideChannelListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.MarkAllReadListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.QueryChannelListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.QueryChannelsListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.SendMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.SendReactionListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.ThreadQueryListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory
import io.getstream.chat.android.offline.repository.creation.builder.RepositoryFacadeBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalStreamChatApi
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
        val globalState = GlobalMutableState.getOrCreate()
        globalState.clearState()

        if (!ChatDomain.isInitialized) {
            ChatDomain.Builder(appContext, chatClient).apply {
                if (config.backgroundSyncEnabled) enableBackgroundSync() else disableBackgroundSync()
                if (config.userPresence) userPresenceEnabled() else userPresenceDisabled()
                recoveryEnabled()
            }.build()
        }

        val chatDomainImpl = (io.getstream.chat.android.offline.ChatDomain.instance as ChatDomainImpl)
        chatDomainImpl.setUser(user)
        chatDomainImpl.userConnected(user)

        val scope = CoroutineScope(DispatcherProvider.IO)

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
            setOfflineEnabled(config.persistenceEnabled)
        }.build()

        chatDomainImpl.repos = repos

        val userStateFlow = MutableStateFlow(ChatClient.instance().getCurrentUser())
        val stateRegistry = StateRegistry.getOrCreate(scope, userStateFlow, repos, repos.observeLatestUsers())
        val logic = LogicRegistry.getOrCreate(stateRegistry)

        val defaultInterceptor = DefaultInterceptor(
            sendMessageInterceptor = SendMessageInterceptorImpl(
                context = appContext,
                logic = logic,
                globalState = globalState,
                scope = scope,
                repos = repos,
                messageSendingService = MessageSendingServiceFactory
            )
        )

        chatClient.addInterceptor(defaultInterceptor)

        InitializationCoordinator.getOrCreate().run {
            addUserConnectedListener(chatDomainImpl::userConnected)

            addUserDisconnectedListener {
                stateRegistry.clear()
                logic.clear()
                globalState.clearState()
            }
        }

        return OfflinePlugin(
            queryChannelsListener = QueryChannelsListenerImpl(logic),
            queryChannelListener = QueryChannelListenerImpl(logic),
            threadQueryListener = ThreadQueryListenerImpl(logic),
            channelMarkReadListener = ChannelMarkReadListenerImpl(logic),
            editMessageListener = EditMessageListenerImpl(logic, globalState),
            getMessageListener = GetMessageListenerImpl(logic),
            hideChannelListener = HideChannelListenerImpl(logic),
            markAllReadListener = MarkAllReadListenerImpl(logic),
            deleteReactionListener = DeleteReactionListenerImpl(logic, globalState, repos),
            sendReactionListener = SendReactionListenerImpl(logic, globalState, repos),
            deleteMessageListener = DeleteMessageListenerImpl(logic, globalState, repos),
            sendMessageListener = SendMessageListenerImpl(logic, repos),
        )
    }
}
