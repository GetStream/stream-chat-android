package com.getstream.sdk.chat.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.model.Channel;

import java.util.List;


@Dao
public interface QueryChannelsQDao {
    /*
    - query channels -> write the query, write many channels
    - notification.new event -> update a single channel
    - offline read flow -> query id based lookup, read a list of channels
     */

    @Insert
    void insert(QueryChannelsQ query);

    //@Insert(onConflict = OnConflictStrategy.REPLACE)
    //public void insertChannels(User... users);

    @Query("SELECT * FROM stream_queries " +
            "WHERE stream_queries.id=:id")
    QueryChannelsQ select(final String id);

    @Query("SELECT * FROM channel " +
            "WHERE channel.id IN (:ids)")
    List<Channel> getChannels(final List<String> ids);


}

