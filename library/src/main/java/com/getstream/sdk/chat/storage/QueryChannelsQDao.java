package com.getstream.sdk.chat.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.getstream.sdk.chat.model.QueryChannelsQ;


@Dao
public interface QueryChannelsQDao {
    /*
    - query channels -> write the query, write many channels
    - notification.new event -> update a single channel
    - offline read flow -> query id based lookup, read a list of channels
     */

    @Insert
    void insert(QueryChannelsQ query);

    @Query("SELECT * FROM stream_queries " +
            "WHERE stream_queries.id=:id")
    LiveData<QueryChannelsQ> select(final String id);



}

