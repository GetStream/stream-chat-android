package io.getstream.chat.android.offline.experimental.plugin

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.experimental.persistence.OfflinePlugin
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.utils.toLiveDataRetryPolicy
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.plugin.configuration.Config
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

/**
 * Implementation of [Plugin] that brings support for the offline feature.
 * The entry point of all offline state ([OfflinePluginImpl.state]) and behavior ([OfflinePluginImpl.logic]).
 *
 * @param config Configuration options for this plugin.
 */
@InternalStreamChatApi
@ExperimentalStreamChatApi
internal class OfflinePluginImpl(
    private val queryChannelsListener: QueryChannelsListener,
    private val queryChannelListener: QueryChannelListener,
    private val threadQueryListener: ThreadQueryListener,
    private val channelMarkReadListener: ChannelMarkReadListener,
    private val editMessageListener: EditMessageListener,
    private val getMessageListener: GetMessageListener,
    private val hideChannelListener: HideChannelListener,
    private val markAllReadListener: MarkAllReadListener,
    private val config: Config = Config(),
) : OfflinePlugin,
    QueryChannelsListener by queryChannelsListener,
    QueryChannelListener by queryChannelListener,
    ThreadQueryListener by threadQueryListener,
    ChannelMarkReadListener by channelMarkReadListener,
    EditMessageListener by editMessageListener,
    GetMessageListener by getMessageListener,
    HideChannelListener by hideChannelListener,
    MarkAllReadListener by markAllReadListener {

    /**
     * [StateRegistry] which contains all states of this plugin.
     */
    // TODO make it val and stateless when remove QueryChannelsMutableState::defaultChannelEventsHandler
    public lateinit var state: StateRegistry
        private set

    /**
     * [LogicRegistry] which contains all the logic to handle side effects.
     */
    internal lateinit var logic: LogicRegistry
        private set

    override val name: String = MODULE_NAME

    override fun init(appContext: Context, chatClient: ChatClient) {
        ChatDomain.Builder(appContext, chatClient).apply {
            if (config.backgroundSyncEnabled) enableBackgroundSync() else disableBackgroundSync()
            if (config.persistenceEnabled) offlineEnabled() else offlineDisabled()
            if (config.userPresence) userPresenceEnabled() else userPresenceDisabled()
            recoveryEnabled()
            retryPolicy(config.retryPolicy.toLiveDataRetryPolicy())
        }.build()

        initState(io.getstream.chat.android.offline.ChatDomain.instance as ChatDomainImpl, chatClient)
    }

    @VisibleForTesting
    internal fun initState(chatDomainImpl: ChatDomainImpl, chatClient: ChatClient) {
        state = StateRegistry(chatDomainImpl, chatClient)
        logic = LogicRegistry(state)
    }

    
    override fun clear() {
        logic.clear()
        state.clear()
    }

    public companion object {
        /**
         * Name of this plugin module.
         */
        public const val MODULE_NAME: String = "Offline"
    }
}
