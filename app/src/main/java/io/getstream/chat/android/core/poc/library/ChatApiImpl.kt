package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.rest.*

class ChatApiImpl(
    private val apiKey: String,
    private val retrofitApi: RetrofitApi
) {

    var userId: String = ""
    var connectionId: String = ""

    private val callMapper = RetrofitCallMapper()

    fun queryChannels(query: QueryChannelsRequest): ChatCall<QueryChannelsResponse> {
        return callMapper.map(
            retrofitApi.queryChannels(
                apiKey,
                userId,
                connectionId,
                query
            )
        )
    }

    fun stopWatching(
        channelType: String,
        channelId: String
    ): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.stopWatching(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    fun queryChannel(
        query: ChannelQueryRequest,
        channelType: String,
        channelId: String = ""
    ): ChatCall<ChannelState> {

        if (channelId.isEmpty()) {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            )
        } else {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    channelId,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            )
        }
    }

    fun updateChannel(
        channelType: String,
        channelId: String,
        request: UpdateChannelRequest
    ): ChatCall<ChannelResponse> {
        return callMapper.map(
            retrofitApi.updateChannel(
                channelType,
                channelId,
                apiKey,
                connectionId,
                request
            )
        )
    }

    fun showChannel(channelType: String, channelId: String): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.showChannel(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    fun hideChannel(channelType: String, channelId: String, clearHistory:Boolean = false): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.hideChannel(channelType, channelId, apiKey, connectionId, HideChannelRequest(clearHistory))
        ).map {
            Unit
        }
    }

    fun rejectInvite(channelType: String, channelId: String):ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.rejectInvite(
                channelType, channelId, apiKey, connectionId, RejectInviteRequest()
            )
        ).map {
            it.channel
        }
    }

    fun acceptInvite(channelType: String, channelId: String, message:String):ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.acceptInvite(
                channelType, channelId, apiKey, connectionId, AcceptInviteRequest(User(userId), message)
            )
        ).map {
            it.channel
        }
    }

    fun deleteChannel(channelType: String, channelId: String): ChatCall<ChannelResponse> {
        return callMapper.map(
            retrofitApi.deleteChannel(channelType, channelId, apiKey, connectionId)
        )
    }

    fun markAllRead(): ChatCall<EventResponse> {
        return callMapper.map(
            retrofitApi.markAllRead(
                apiKey,
                userId,
                connectionId
            )
        )
    }

}