package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest

/**
 * [QueryChannelsListener] implementation for [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 * Handles querying the channel offline and managing local state updates.
 *
 * @param logic [LogicRegistry] provided by the [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 */
internal class QueryChannelsListenerImpl(private val logic: LogicRegistry) : QueryChannelsListener {

    private val logger = ChatLogger.get("QueryChannelsLogic")

    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> {
        val loader = logic.queryChannels(request).loadingForCurrentRequest()
        return if (loader.value) {
            logger.logI("Another request to load channels is in progress. Ignoring this request.")
            Result.error(ChatError("Another request to load messages is in progress. Ignoring this request."))
        } else {
            Result.success(Unit)
        }
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        logic.queryChannels(request).run {
            setCurrentQueryChannelsRequest(request)
            queryOffline(request.toPagination())
        }
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logic.queryChannels(request).onQueryChannelsResult(result, request)
    }

    private companion object {
        private fun QueryChannelsRequest.toPagination(): AnyChannelPaginationRequest =
            QueryChannelsPaginationRequest(
                sort = querySort,
                channelLimit = limit,
                channelOffset = offset,
                messageLimit = messageLimit,
                memberLimit = memberLimit
            ).toAnyChannelPaginationRequest()
    }
}
