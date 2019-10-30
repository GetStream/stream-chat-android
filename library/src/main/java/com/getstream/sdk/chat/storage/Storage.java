package com.getstream.sdk.chat.storage;

import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Storage {

    MutableLiveData<ChannelState> selectChannelState(String cid, OnQueryListener l);

    MutableLiveData<List<ChannelState>> selectChannelStates(String queryID, Integer limit, OnQueryListener l);

    QueryChannelsQ selectQuery(String queryID);

    void insertChannels(List<Channel> channels);

    void insertQueryWithChannels(QueryChannelsQ query, List<Channel> channels);

    void insertChannel(Channel channel);

    /**
     * delete channel from database
     *
     * @param channel the channel needs to delete
     */
    void deleteChannel(@NotNull Channel channel);

    void insertQuery(QueryChannelsQ query);

    void deleteMessage(String id);

    void insertMessageForChannel(Channel channel, Message message);

    void insertMessagesForChannel(Channel channel, List<Message> messages);

    List<ChannelState> getChannelStates(QueryChannelsQ query, Integer limit);

    List<Channel> getChannels(QueryChannelsQ query, Integer limit);

}
