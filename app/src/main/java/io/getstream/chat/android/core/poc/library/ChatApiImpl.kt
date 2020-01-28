package io.getstream.chat.android.core.poc.library

import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.requests.QueryUsers
import io.getstream.chat.android.core.poc.library.rest.*
import java.util.*

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
        channelType: String,
        channelId: String = "",
        query: ChannelQueryRequest
    ): ChatCall<Channel> {

        if (channelId.isEmpty()) {
            return callMapper.map(
                retrofitApi.queryChannel(
                    channelType,
                    apiKey,
                    userId,
                    connectionId,
                    query
                )
            ).map {
                it.channel
            }
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
            ).map {
                it.channel
            }
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

    fun markRead(channelType: String, channelId: String, messageId: String): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.markRead(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                MarkReadRequest(messageId)
            )
        ).map {
            Unit
        }
    }

    fun showChannel(channelType: String, channelId: String): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.showChannel(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean = false
    ): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.hideChannel(
                channelType,
                channelId,
                apiKey,
                connectionId,
                HideChannelRequest(clearHistory)
            )
        ).map {
            Unit
        }
    }

    fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.rejectInvite(
                channelType, channelId, apiKey, connectionId, RejectInviteRequest()
            )
        ).map {
            it.channel
        }
    }

    fun acceptInvite(channelType: String, channelId: String, message: String): ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.acceptInvite(
                channelType,
                channelId,
                apiKey,
                connectionId,
                AcceptInviteRequest(User(userId), message)
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

    fun setGuestUser(apiKey: String, userId: String, userName: String?) = callMapper.map(
        retrofitApi.setGuestUser(
            apiKey = apiKey,
            body = GuestUserRequest(
                id = userId,
                name = userName
            )
        )
    )

    fun getUsers(
        apiKey: String,
        connectionId: String,
        queryUser: QueryUsers
    ): ChatCall<QueryUserListResponse> {
        val payload = Gson().toJson(queryUser)
        return callMapper.map(
            retrofitApi.queryUsers(
                apiKey = apiKey,
                connectionId = connectionId,
                payload = payload
            )
        )
    }

    fun addMembers(
        apiKey: String,
        connectionId: String,
        channelType: String,
        channelId: String,
        members: List<String>
    ) = callMapper.map(
        retrofitApi.addMembers(
            apiKey = apiKey,
            connectionId = connectionId,
            channelType = channelType,
            channelId = channelId,
            body = AddMembersRequest(
                members = members
            )
        )
    )

    fun removeMembers(
        apiKey: String,
        connectionId: String,
        channelType: String,
        channelId: String,
        members: List<String>
    ) = callMapper.map(
        retrofitApi.removeMembers(
            apiKey = apiKey,
            connectionId = connectionId,
            channelType = channelType,
            channelId = channelId,
            body = RemoveMembersRequest(
                members = members
            )
        )
    )

    fun muteUser(
        apiKey: String,
        connectionId: String,
        userId: String,
        targetId: String
    ): ChatCall<MuteUserResponse> {
        val body: MutableMap<String, String> = HashMap()
        body["target_id"] = targetId
        body["user_id"] = userId

        return callMapper.map(
            retrofitApi.muteUser(
                apiKey = apiKey,
                connectionId = connectionId,
                userId = userId,
                body = body
            )
        )
    }

    fun unMuteUser(
        apiKey: String,
        connectionId: String,
        userId: String,
        targetId: String
    ): ChatCall<MuteUserResponse> {
        val body: MutableMap<String, String> = HashMap()
        body["target_id"] = targetId
        body["user_id"] = userId

        return callMapper.map(
            retrofitApi.unMuteUser(
                apiKey = apiKey,
                connectionId = connectionId,
                userId = userId,
                body = body
            )
        )
    }

    fun flag(
        apiKey: String,
        connectionId: String,
        userId: String,
        targetId: String
    ): ChatCall<FlagResponse> {
        val body: MutableMap<String, String> = HashMap()
        body["target_user_id"] = targetId

        return callMapper.map(
            retrofitApi.flag(
                apiKey = apiKey,
                connectionId = connectionId,
                userId = userId,
                body = body
            )
        )
    }

    fun banUser(
        apiKey: String,
        connectionId: String,
        targetId: String,
        timeout: Int?,
        reason: String?,
        channelType: String?,
        channelId: String?
    ): ChatCall<CompletableResponse> {

        return callMapper.map(
            retrofitApi.banUser(
                apiKey = apiKey,
                connectionId = connectionId,
                body = BanUserRequest(
                    targetUserId = targetId,
                    timeout = timeout,
                    reason = reason,
                    channelType = channelType,
                    channelId = channelId
                )
            )
        )
    }

    fun unBanUser(
        apiKey: String,
        connectionId: String,
        targetId: String,
        channelType: String,
        channelId: String
    ): ChatCall<CompletableResponse> {

        return callMapper.map(
            retrofitApi.unBanUser(
                apiKey = apiKey,
                connectionId = connectionId,
                targetUserId = targetId,
                channelId = channelId,
                channelType = channelType
            )
        )
    }

}