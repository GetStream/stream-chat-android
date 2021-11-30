package io.getstream.chat.android.offline.experimental.plugin

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.utils.toLiveDataRetryPolicy
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry

@InternalStreamChatApi
@ExperimentalStreamChatApi
public class OfflinePlugin(private val config: Config) : Plugin {

    internal constructor() : this(Config())

    // TODO make it val and stateless when remove QueryChannelsMutableState::defaultChannelEventsHandler
    public lateinit var state: StateRegistry
        private set
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

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest): Unit =
        logic.queryChannels(request).onQueryChannelsRequest(request)

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest): Unit =
        logic.queryChannels(request).onQueryChannelsResult(result, request)

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> = logic.channel(channelType, channelId).onQueryChannelPrecondition(channelType, channelId, request)

    override suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Unit = logic.channel(channelType, channelId).onQueryChannelRequest(channelType, channelId, request)

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Unit = logic.channel(channelType, channelId).onQueryChannelResult(result, channelType, channelId, request)

    override fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> =
        logic.thread(messageId).onGetRepliesPrecondition(messageId, limit)

    override fun onGetRepliesRequest(messageId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesRequest(messageId, limit)

    override fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesResult(result, messageId, limit)

    override fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> =
        logic.thread(messageId).onGetRepliesMorePrecondition(messageId, firstId, limit)

    override fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesMoreRequest(messageId, firstId, limit)

    override fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ): Unit = logic.thread(messageId).onGetRepliesMoreResult(result, messageId, firstId, limit)

    internal fun clear() {
        logic.clear()
        state.clear()
    }

    public companion object {
        public const val MODULE_NAME: String = "Offline"
    }
}
