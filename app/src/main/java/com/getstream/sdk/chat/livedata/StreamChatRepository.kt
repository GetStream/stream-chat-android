package com.getstream.sdk.chat.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.livedata.dao.ChannelQueryDao
import com.getstream.sdk.chat.livedata.dao.MessageDao
import com.getstream.sdk.chat.livedata.dao.ReactionDao
import com.getstream.sdk.chat.livedata.dao.UserDao
import com.getstream.sdk.chat.livedata.entity.ChannelQuery
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import com.getstream.sdk.chat.livedata.entity.UserEntity
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class StreamChatRepository(
    private val channelQueryDao: ChannelQueryDao,
    private val userDao: UserDao,
    private val reactionDao: ReactionDao,
    private val messageDao: MessageDao
) {
    var online = false
    var roomOfflineStorageEnabled = true

    fun channel(channelType: String, channelID: String): StreamChatChannelRepository {
        return StreamChatChannelRepository()
    }

    fun setOffline() {
        online = false
    }
    fun setOnline() {
        online = true
    }

    fun insertUser(user: User) {
        GlobalScope.launch {
            userDao.insert(UserEntity(user))
        }

    }


    fun insertReaction(reaction: Reaction) {
        GlobalScope.launch {
            reactionDao.insert(ReactionEntity(reaction))
        }
    }

    fun insertQuery(query: ChannelQuery) {
        GlobalScope.launch {
            channelQueryDao.insert(query)
        }
    }

    fun insertMessage(message: Message) {
        // TODO: Assign a message id here somewhere...
        GlobalScope.launch {
            messageDao.insert(MessageEntity(message))
        }
    }

    fun connectionRecovered() {
        // update the results for queries that are actively being shown right now
        // TODO: how do we know this?

        // update the data for all channels that are being show right now...
    }


    /**
     * queryChannels
     * - first read the current results from Room
     * - if we are online make the API call to update results
     */
    fun queryChannels(
        query: ChannelQuery,
        request: QueryChannelsRequest
    ): LiveData<List<String>> {
        // return a livedata object with the channels

        val channels = MutableLiveData<List<String>>()
        GlobalScope.launch {
            val newchannels = channelQueryDao.select(query.id)
            channels.value = listOf("123", "345")
        }

        return channels


    }
}