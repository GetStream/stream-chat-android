package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.plugin.OfflinePlugin

public val ChatClient.offlinePlugin: OfflinePlugin
    get() = requireNotNull(plugins.firstOrNull { it.name == OfflinePlugin.MODULE_NAME } as? OfflinePlugin) {
        "Offline plugin must be configured in ChatClient"
    }
