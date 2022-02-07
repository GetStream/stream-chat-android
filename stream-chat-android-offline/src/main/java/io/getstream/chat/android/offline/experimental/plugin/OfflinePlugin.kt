package io.getstream.chat.android.offline.experimental.plugin

import android.content.Context
import androidx.annotation.VisibleForTesting
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.Plugin
import io.getstream.chat.android.client.experimental.plugin.listeners.ChannelMarkReadListener
import io.getstream.chat.android.client.experimental.plugin.listeners.EditMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.GetMessageListener
import io.getstream.chat.android.client.experimental.plugin.listeners.HideChannelListener
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
    EditMessageListener,
    GetMessageListener,
    HideChannelListener {

    internal constructor() : this(Config())

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

    /**
     * Global state of this plugin.
     */
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
     * Method called when a message edit request happens. This method should be used to update messages locally and
     * update the cache.
     *
     * @param message [Message].
     */
    override suspend fun onMessageEditRequest(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        val channelLogic = logic.channel(channelType, channelId)

        val isOnline = globalState.isOnline()
        val messagesToEdit = message.updateMessageOnlineState(isOnline).let(::listOf)

        updateAndSaveMessages(messagesToEdit, channelLogic)
    }

    /**
     * Method called when an edition in a message returns from the API.
     *
     * @param result the result of the API call.
     */
    override suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>) {
        if (result.isSuccess) {
            val message = result.data()
            val channelLogic = channelLogicForMessage(message)
            val messages = message.copy(syncStatus = SyncStatus.COMPLETED).let(::listOf)

            updateAndSaveMessages(messages, channelLogic)
        } else {
            val channelLogic = channelLogicForMessage(originalMessage)
            val failedMessage = originalMessage.updateFailedMessage(result.error()).let(::listOf)

            updateAndSaveMessages(failedMessage, channelLogic)
        }
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

    override suspend fun onGetMessageResult(
        result: Result<Message>,
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Unit = cid.cidToTypeAndId().let { (channelType, channelId) ->
        logic.channel(channelType, channelId)
            .onGetMessageResult(result, cid, messageId, olderMessagesOffset, newerMessagesOffset)
    }

    override suspend fun onGetMessageError(
        cid: String,
        messageId: String,
        olderMessagesOffset: Int,
        newerMessagesOffset: Int,
    ): Result<Message> = cid.cidToTypeAndId().let { (channelType, channelId) ->
        logic.channel(channelType, channelId)
            .onGetMessageError(cid, messageId, olderMessagesOffset, newerMessagesOffset)
    }

    /**
     * Updates the messages locally and saves it at database.
     *
     * @param messages The list of messages to be updated in the SDK and to be saved in database.
     * @param channelLogic [ChannelLogic].
     */
    private suspend fun updateAndSaveMessages(messages: List<Message>, channelLogic: ChannelLogic) {
        channelLogic.upsertMessages(messages)
        channelLogic.storeMessageLocally(messages)
    }

    /**
     * Gets the channel logic for the channel of a message.
     *
     * @param message [Message].
     */
    private fun channelLogicForMessage(message: Message): ChannelLogic {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        return logic.channel(channelType, channelId)
    }

    /**
     * Update the online state of a message.
     *
     * @param isOnline [Boolean].
     */
    private fun Message.updateMessageOnlineState(isOnline: Boolean): Message {
        return this.copy(
            syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            updatedLocallyAt = Date()
        )
    }

    /**
     * Updates a message that whose request (Edition/Delete/Reaction update...) has failed.
     *
     * @param chatError [ChatError].
     */
    private fun Message.updateFailedMessage(chatError: ChatError): Message {
        return this.copy(
            syncStatus = if (chatError.isPermanent()) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            updatedLocallyAt = Date(),
        )
    }

    override suspend fun onHideChannelPrecondition(
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Result<Unit> =
        logic.channel(channelType, channelId).onHideChannelPrecondition(channelType, channelId, clearHistory)

    override suspend fun onHideChannelRequest(channelType: String, channelId: String, clearHistory: Boolean): Unit =
        logic.channel(channelType, channelId).onHideChannelRequest(channelType, channelId, clearHistory)

    override suspend fun onHideChannelResult(
        result: Result<Unit>,
        channelType: String,
        channelId: String,
        clearHistory: Boolean,
    ): Unit = logic.channel(channelType, channelId).onHideChannelResult(result, channelType, channelId, clearHistory)

    internal fun clear() {
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
