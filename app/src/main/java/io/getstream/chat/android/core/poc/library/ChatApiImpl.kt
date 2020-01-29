package io.getstream.chat.android.core.poc.library

import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.api.QueryChannelsResponse
import io.getstream.chat.android.core.poc.library.call.ChatCall
import io.getstream.chat.android.core.poc.library.requests.QueryUsers
import io.getstream.chat.android.core.poc.library.gson.JsonParser
import io.getstream.chat.android.core.poc.library.rest.*
import io.getstream.chat.android.core.poc.library.socket.ConnectionData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class ChatApiImpl(
    private val apiKey: String,
    private val retrofitApi: RetrofitApi,
    private val jsonParser: JsonParser
) : ChatApi {

    private var userId: String = ""
    private var connectionId: String = ""
    private val callMapper = RetrofitCallMapper(jsonParser)

    override fun setConnection(connection: ConnectionData) {
        userId = connection.user.id
        connectionId = connection.connectionId
    }

    override fun addDevice(request: AddDeviceRequest): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.addDevices(apiKey, userId, connectionId, request)
        ).map {
            Unit
        }
    }

    override fun deleteDevice(deviceId: String): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.deleteDevice(
                deviceId, apiKey, userId, connectionId
            )
        ).map {
            Unit
        }
    }

    override fun getDevices(): ChatCall<List<Device>> {
        return callMapper.map(
            retrofitApi.getDevices(apiKey, userId, connectionId)
        ).map {
            it.devices
        }
    }

    override fun searchMessages(request: SearchMessagesRequest): ChatCall<List<Message>> {
        return callMapper.map(
            retrofitApi.searchMessages(apiKey, connectionId, request)
        ).map {
            it.results.map { resp ->
                resp.message
            }
        }
    }

    override fun getRepliesMore(
        messageId: String,
        firstId: String,
        limit: Int
    ): ChatCall<List<Message>> {
        return callMapper.map(
            retrofitApi.getRepliesMore(messageId, apiKey, userId, connectionId, limit, firstId)
        ).map {
            it.messages
        }
    }

    override fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>> {
        return callMapper.map(
            retrofitApi.getReplies(
                messageId,
                apiKey,
                userId,
                connectionId,
                limit
            )
        ).map {
            it.messages
        }
    }

    override fun getReactions(
        messageId: String,
        offset: Int,
        limit: Int
    ): ChatCall<List<Reaction>> {
        return callMapper.map(
            retrofitApi.getReactions(
                messageId,
                apiKey,
                connectionId,
                offset,
                limit
            )
        ).map {
            it.reactions
        }
    }

    override fun deleteReaction(messageId: String, reactionType: String): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.deleteReaction(
                messageId,
                reactionType,
                apiKey,
                userId,
                connectionId
            )
        ).map {
            it.message
        }
    }

    override fun deleteMessage(messageId: String): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.deleteMessage(
                messageId,
                apiKey,
                userId,
                connectionId
            )
        ).map {
            it.message
        }
    }

    override fun sendAction(request: SendActionRequest): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.sendAction(
                request.messageId, apiKey, userId, connectionId, request
            )
        ).map {
            it.message
        }
    }

    override fun getMessage(messageId: String): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.getMessage(messageId, apiKey, userId, connectionId)
        ).map {
            it.message
        }
    }

    override fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.sendMessage(
                channelType,
                channelId,
                apiKey,
                userId,
                connectionId,
                MessageRequest(message)
            )
        ).map {
            it.message
        }
    }

    override fun updateMessage(
        message: Message
    ): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.updateMessage(
                message.id,
                apiKey,
                userId,
                connectionId,
                MessageRequest(message)
            )
        ).map {
            it.message
        }
    }


    override fun queryChannels(query: QueryChannelsRequest): ChatCall<QueryChannelsResponse> {
        return callMapper.map(
            retrofitApi.queryChannels(
                apiKey,
                userId,
                connectionId,
                query
            )
        )
    }

    override fun stopWatching(
        channelType: String,
        channelId: String
    ): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.stopWatching(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    override fun queryChannel(
        channelType: String,
        channelId: String,
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

    override fun updateChannel(
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

    override fun markRead(
        channelType: String,
        channelId: String,
        messageId: String
    ): ChatCall<Unit> {
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

    override fun showChannel(channelType: String, channelId: String): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.showChannel(channelType, channelId, apiKey, connectionId, emptyMap())
        ).map {
            Unit
        }
    }

    override fun hideChannel(
        channelType: String,
        channelId: String,
        clearHistory: Boolean
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

    override fun rejectInvite(channelType: String, channelId: String): ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.rejectInvite(
                channelType, channelId, apiKey, connectionId, RejectInviteRequest()
            )
        ).map {
            it.channel
        }
    }

    override fun acceptInvite(
        channelType: String,
        channelId: String,
        message: String
    ): ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.acceptInvite(
                channelType,
                channelId,
                apiKey,
                connectionId,
                AcceptInviteRequest(User(userId), AcceptInviteRequest.AcceptInviteMessage(message))
            )
        ).map {
            it.channel
        }
    }

    override fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.deleteChannel(channelType, channelId, apiKey, connectionId)
        ).map {
            it.channel
        }
    }

    override fun markAllRead(): ChatCall<EventResponse> {
        return callMapper.map(
            retrofitApi.markAllRead(
                apiKey,
                userId,
                connectionId
            )
        )
    }

    override fun setGuestUser(userId: String, userName: String?): ChatCall<TokenResponse> {
        return callMapper.map(retrofitApi.setGuestUser(
            apiKey = apiKey,
            body = GuestUserRequest(
                id = userId,
                name = userName
            )
        ))
    }

    override fun getUsers(
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

    override fun addMembers(
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

    override fun removeMembers(
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

    override fun muteUser(
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

    override fun unMuteUser(
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

    override fun flag(
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

    override fun banUser(
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

    override fun unBanUser(
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