package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.ChatDatabase
import io.getstream.chat.android.livedata.dao.*

class RepositoryHelper(var client: ChatClient, var database: ChatDatabase) {

    private var queryChannelsDao: QueryChannelsDao = database.queryChannelsQDao()
    private var userDao: UserDao = database.userDao()
    private var reactionDao: ReactionDao = database.reactionDao()
    private var messageDao: MessageDao = database.messageDao()
    private var channelDao: ChannelDao = database.channelStateDao()
    private var channelConfigDao: ChannelConfigDao = database.channelConfigDao()

    var users: UserRepository
    var configs: ChannelConfigRepository
    var channels: ChannelRepository
    var queryChannels: QueryChannelsRepository
    var messages: MessageRepository
    var reactions: ReactionRepository

    init {
        users = UserRepository(userDao, 100, client)
        configs = ChannelConfigRepository(channelConfigDao)
        channels = ChannelRepository(channelDao)
        queryChannels = QueryChannelsRepository(queryChannelsDao)
        messages = MessageRepository(messageDao)
        reactions = ReactionRepository(reactionDao)
    }
}