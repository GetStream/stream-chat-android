package io.getstream.chat.android.client

import com.google.gson.Gson
import io.getstream.chat.android.client.api.ChatConfig
import io.getstream.chat.android.client.api.QueryChannelsResponse
import io.getstream.chat.android.client.call.ChatCall
import io.getstream.chat.android.client.gson.JsonParser
import io.getstream.chat.android.client.requests.QueryUsers
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.rest.*
import java.util.*

class ChatApiImpl(
    private val chatConfig: ChatConfig,
    private val retrofitApi: RetrofitApi,
    private val jsonParser: JsonParser,
    private val logger: StreamLogger?
) : ChatApi {

    private var userId: String = ""
    private var connectionId: String = ""
    private val callMapper = RetrofitCallMapper(jsonParser)

    override fun setConnection(userId:String, connectionId:String) {
        this.userId = userId
        this.connectionId = connectionId
    }

    override fun addDevice(request: AddDeviceRequest): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.addDevices(chatConfig.apiKey, userId, connectionId, request)
        ).map {
            Unit
        }
    }

    override fun deleteDevice(deviceId: String): ChatCall<Unit> {
        return callMapper.map(
            retrofitApi.deleteDevice(
                deviceId, chatConfig.apiKey, userId, connectionId
            )
        ).map {
            Unit
        }
    }

    override fun getDevices(): ChatCall<List<Device>> {
        return callMapper.map(
            retrofitApi.getDevices(chatConfig.apiKey, userId, connectionId)
        ).map {
            it.devices
        }
    }

    override fun searchMessages(request: SearchMessagesRequest): ChatCall<List<Message>> {
        return callMapper.map(
            retrofitApi.searchMessages(chatConfig.apiKey, connectionId, request)
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
            retrofitApi.getRepliesMore(messageId, chatConfig.apiKey, userId, connectionId, limit, firstId)
        ).map {
            it.messages
        }
    }

    override fun getReplies(messageId: String, limit: Int): ChatCall<List<Message>> {
        return callMapper.map(
            retrofitApi.getReplies(
                messageId,
                chatConfig.apiKey,
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
                chatConfig.apiKey,
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
                chatConfig.apiKey,
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
                chatConfig.apiKey,
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
                request.messageId, chatConfig.apiKey, userId, connectionId, request
            )
        ).map {
            it.message
        }
    }

    override fun getMessage(messageId: String): ChatCall<Message> {
        return callMapper.map(
            retrofitApi.getMessage(messageId, chatConfig.apiKey, userId, connectionId)
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
                chatConfig.apiKey,
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
                chatConfig.apiKey,
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
                chatConfig.apiKey,
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
            retrofitApi.stopWatching(channelType, channelId, chatConfig.apiKey, connectionId, emptyMap())
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
                    chatConfig.apiKey,
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
                    chatConfig.apiKey,
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
                chatConfig.apiKey,
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
                chatConfig.apiKey,
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
            retrofitApi.showChannel(channelType, channelId, chatConfig.apiKey, connectionId, emptyMap())
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
                chatConfig.apiKey,
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
                channelType, channelId, chatConfig.apiKey, connectionId, RejectInviteRequest()
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
                chatConfig.apiKey,
                connectionId,
                AcceptInviteRequest(User(userId), AcceptInviteRequest.AcceptInviteMessage(message))
            )
        ).map {
            it.channel
        }
    }

    override fun deleteChannel(channelType: String, channelId: String): ChatCall<Channel> {
        return callMapper.map(
            retrofitApi.deleteChannel(channelType, channelId, chatConfig.apiKey, connectionId)
        ).map {
            it.channel
        }
    }

    override fun markAllRead(): ChatCall<EventResponse> {
        return callMapper.map(
            retrofitApi.markAllRead(
                chatConfig.apiKey,
                userId,
                connectionId
            )
        )
    }

    override fun setGuestUser(userId: String, userName: String): ChatCall<TokenResponse> {
        return callMapper.map(
            retrofitApi.setGuestUser(
                chatConfig.apiKey,
                body = GuestUserRequest(
                    id = userId,
                    name = userName
                )
            )
        )
    }

    override fun getUsers(
        queryUser: QueryUsers
    ): ChatCall<QueryUserListResponse> {
        val payload = Gson().toJson(queryUser)
        return callMapper.map(
            retrofitApi.queryUsers(
                apiKey = chatConfig.apiKey,
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
            apiKey = chatConfig.apiKey,
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
            apiKey = chatConfig.apiKey,
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
                apiKey = chatConfig.apiKey,
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
                apiKey = chatConfig.apiKey,
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
                apiKey = chatConfig.apiKey,
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
                apiKey = chatConfig.apiKey,
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
                apiKey = chatConfig.apiKey,
                connectionId = connectionId,
                targetUserId = targetId,
                channelId = channelId,
                channelType = channelType
            )
        )
    }

}