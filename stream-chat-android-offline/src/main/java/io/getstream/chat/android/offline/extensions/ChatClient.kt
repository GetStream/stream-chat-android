package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.plugin.OfflinePlugin

public val ChatClient.offlinePlugin: OfflinePlugin
    get() {
        return requireNotNull(plugins.firstOrNull { it.name == OfflinePlugin.MODULE_NAME } as? OfflinePlugin) {
            "The offline plugin has not been set to ChatClient!!!"
        }
    }
