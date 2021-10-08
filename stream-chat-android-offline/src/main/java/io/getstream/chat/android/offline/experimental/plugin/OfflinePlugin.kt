package io.getstream.chat.android.offline.experimental.plugin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

@InternalStreamChatApi
@ExperimentalStreamChatApi
public class OfflinePlugin(private val config: Config) : Plugin {

    internal constructor() : this(Config())

    public val state: StateRegistry = StateRegistry()
    internal val logic: LogicRegistry = LogicRegistry(state)

    override val name: String = MODULE_NAME

    override fun init(appContext: Context, chatClient: ChatClient) {
        ChatDomain.Builder(appContext, chatClient).apply {
            if (config.backgroundSyncEnabled) enableBackgroundSync() else disableBackgroundSync()
            if (config.persistenceEnabled) offlineEnabled() else offlineDisabled()
            if (config.userPresence) userPresenceEnabled() else userPresenceDisabled()
            recoveryEnabled()
        }.build()
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest): Unit =
        logic.queryChannels(request).onQueryChannelsRequest(request)

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest): Unit =
        logic.queryChannels(request).onQueryChannelsResult(result, request)

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Unit = logic.queryChannel().onQueryChannelResult(result, channelType, channelId, request)

    internal fun clear() {
        logic.clear()
        state.clear()
    }

    public companion object {
        public const val MODULE_NAME: String = "Offline"
    }
}
