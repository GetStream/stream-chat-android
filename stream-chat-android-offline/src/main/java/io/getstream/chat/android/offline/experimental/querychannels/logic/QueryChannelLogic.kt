package io.getstream.chat.android.offline.experimental.querychannels.logic

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl

@ExperimentalStreamChatApi
internal class QueryChannelLogic(private val chatDomainImpl: ChatDomainImpl) : QueryChannelListener {
    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        result.onSuccessSuspend {
            chatDomainImpl.repos.insertChannel(it)
        }
    }
}
