package com.getstream.sdk.chat.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

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


    @Query("SELECT * FROM channel " +
            "WHERE channel.cid IN (:ids)")
    List<Channel> getChannels(final List<String> ids);

    @Query("SELECT * FROM channel " +
            "WHERE channel.cid IN (:cid)")
    Channel getChannel(final String cid);

}