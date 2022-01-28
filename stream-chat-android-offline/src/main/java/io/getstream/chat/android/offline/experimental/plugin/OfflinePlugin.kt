package io.getstream.chat.android.offline.experimental.plugin

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.utils.toLiveDataRetryPolicy
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.global.GlobalMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.extensions.isPermanent
import java.util.Date

/**
 * Implementation of [Plugin] that brings support for the offline feature.
 * The entry point of all offline state ([OfflinePlugin.state]) and behavior ([OfflinePlugin.logic]).
 *
 * @param config Configuration options for this plugin.
 */
@InternalStreamChatApi
@ExperimentalStreamChatApi
public class OfflinePlugin(
    private val config: Config,
) : Plugin,
    QueryChannelsListener,
    QueryChannelListener,
    ThreadQueryListener,
    ChannelMarkReadListener,
    DeleteMessageListener {

    internal constructor() : this(Config())

    // TODO make it val and stateless when remove QueryChannelsMutableState::defaultChannelEventsHandler
    public lateinit var state: StateRegistry
        private set
    internal lateinit var logic: LogicRegistry
        private set

    // TODO: Move to StateRegistry when we remove ChatDomain.
    public val globalState: GlobalState = GlobalMutableState()

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

    /**
     * Method called when an edition in a message starts happens
     *
     * @param message [Message]
     */
    override suspend fun onMessageEditRequest(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        val channelLogic = logic.channel(channelType, channelId)

        val isOnline = ChatDomain.instance().isOnline()

        val messagesToEdit = message.copy(
            updatedLocallyAt = Date(),
            syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED
        ).let(::listOf)

        channelLogic.upsertMessages(messagesToEdit)
        channelLogic.storeMessageLocally(messagesToEdit)
    }

    /**
     * Method called when an edition in a message returns from the API
     *
     * @param result the result of the API call
     */
    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        if (result.isSuccess) {
            val message = result.data()
            val channelLogic = channelLogicForMessage(message)
            val messages = message.copy(syncStatus = SyncStatus.COMPLETED ).let(::listOf)

            updateAndSaveMessages(messages, channelLogic)
        } else {
            val channelLogic = channelLogicForMessage(originalMessage)
            val failedMessage = originalMessage.copy(
                syncStatus = if (result.error().isPermanent()) {
                    SyncStatus.FAILED_PERMANENTLY
                } else {
                    SyncStatus.SYNC_NEEDED
                },
                updatedLocallyAt = Date(),
            ).let(::listOf)

            updateAndSaveMessages(failedMessage, channelLogic)
        }
    }

    override suspend fun onMessageDeleteRequest(message: Message) {

    }

    override suspend fun onMessageDeleteResult(originalMessage: Message, result: kotlin.Result<Message>) {
        TODO("Not yet implemented")
    }

    private suspend fun updateAndSaveMessages(messages: List<Message>, channelLogic: ChannelLogic) {
        channelLogic.upsertMessages(messages)
        channelLogic.storeMessageLocally(messages)
    }

    private fun channelLogicForMessage(message: Message): ChannelLogic {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        return logic.channel(channelType, channelId)
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

    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> =
        logic.channel(channelType, channelId).onChannelMarkReadPrecondition(channelType, channelId)

    internal fun clear() {
        logic.clear()
        state.clear()
    }

    public companion object {
        public const val MODULE_NAME: String = "Offline"
    }
}
