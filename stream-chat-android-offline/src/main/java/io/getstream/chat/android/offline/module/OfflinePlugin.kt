package io.getstream.chat.android.offline.module

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.plugin.Plugin
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.module.state.StateRegistry

public class OfflinePlugin(private val config: Config) : Plugin {

    public val state: StateRegistry = StateRegistry()

    override val name: String = MODULE_NAME

    override fun init(appContext: Context, chatClient: ChatClient) {
        ChatDomain.Builder(appContext, chatClient).apply {
            if (config.backgroundSyncEnabled) enableBackgroundSync() else disableBackgroundSync()
            if (config.persistenceEnabled) offlineEnabled() else offlineDisabled()
            if (config.userPresence) userPresenceEnabled() else userPresenceDisabled()
            recoveryEnabled()
        }.build()
    }

    public companion object {
        public const val MODULE_NAME: String = "Offline"
    }
}
