package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.plugin.OfflinePlugin
import io.getstream.chat.android.offline.plugin.state.StateRegistry

internal val ChatClient.state: StateRegistry
    get() = requireNotNull((plugins.firstOrNull { it.name == OfflinePlugin.MODULE_NAME } as? OfflinePlugin)?.state) {
        "Offline plugin must be configured in ChatClient"
    }
