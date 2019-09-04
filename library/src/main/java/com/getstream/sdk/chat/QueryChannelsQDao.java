package com.getstream.sdk.chat;

import androidx.room.Dao;
import androidx.room.Insert;


@Dao
public interface QueryChannelsQDao {

    @Insert
    void insert(QueryChannelsQ query);


}

