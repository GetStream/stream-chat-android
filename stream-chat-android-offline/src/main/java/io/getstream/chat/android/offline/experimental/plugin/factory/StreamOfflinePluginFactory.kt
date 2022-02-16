package io.getstream.chat.android.offline.experimental.plugin.factory

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.factory.PluginFactory
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.offline.experimental.plugin.configuration.Config
import io.getstream.chat.android.offline.experimental.plugin.listener.ChannelMarkReadListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.DeleteReactionListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.EditMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.GetMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.HideChannelListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.MarkAllReadListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.QueryChannelListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.QueryChannelsListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.SendMessageListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.listener.ThreadQueryListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
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

    private var instance: OfflinePlugin? = null

    override fun getOrCreate(): Plugin {
        return instance ?: createOfflinePlugin().also {
            instance = it
        }
    }

    private fun createOfflinePlugin(): OfflinePlugin {
        // This can only be called after ChatClient was instantiated!!
        val chatClient = ChatClient.instance()
        ChatDomain.Builder(appContext, chatClient).apply {
            if (config.backgroundSyncEnabled) enableBackgroundSync() else disableBackgroundSync()
            if (config.persistenceEnabled) offlineEnabled() else offlineDisabled()
            if (config.userPresence) userPresenceEnabled() else userPresenceDisabled()
            recoveryEnabled()
        }.build()

        val userStateFlow = MutableStateFlow<User?>(null)
        chatClient.preSetUserListeners.add { user -> userStateFlow.value = user }

        val chatDomainImpl = io.getstream.chat.android.offline.ChatDomain.instance as ChatDomainImpl
        val stateRegistry = chatDomainImpl.run {
            StateRegistry.getOrCreate(scope, userStateFlow, repos, repos.observeLatestUsers())
        }
        val logic = LogicRegistry.getOrCreate(stateRegistry)
        val globalState = GlobalMutableState.getOrCreate()

        return OfflinePlugin(
            queryChannelsListener = QueryChannelsListenerImpl(logic),
            queryChannelListener = QueryChannelListenerImpl(logic),
            threadQueryListener = ThreadQueryListenerImpl(logic),
            channelMarkReadListener = ChannelMarkReadListenerImpl(logic),
            editMessageListener = EditMessageListenerImpl(logic, globalState),
            getMessageListener = GetMessageListenerImpl(logic),
            hideChannelListener = HideChannelListenerImpl(logic),
            markAllReadListener = MarkAllReadListenerImpl(logic),
            deleteReactionListener = DeleteReactionListenerImpl(
                logic = logic,
                globalState = globalState,
                repos = chatDomainImpl.repos,
            ),
            sendMessageListener = SendMessageListenerImpl(
                context = appContext,
                logic = logic,
                globalState = globalState,
                scope = chatDomainImpl.scope,
                repos = chatDomainImpl.repos
            )
        )
    }
}
