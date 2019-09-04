package com.getstream.sdk.chat;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.getstream.sdk.chat.model.Channel;

import java.util.List;

@Dao
public interface ChannelsDao {
    /*
    - query channels -> write the query, write many channels
    - notification.new event -> update a single channel
    - offline read flow -> query id based lookup, read a list of channels
     */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertChannel(Channel channel);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertChannels(List<Channel> channels);

}