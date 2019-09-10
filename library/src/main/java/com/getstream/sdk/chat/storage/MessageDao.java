package com.getstream.sdk.chat.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;

import com.getstream.sdk.chat.rest.Message;

import java.util.List;


@Dao
public interface MessageDao {

    @Insert
    void insert(Message word);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertMessages(List<Message> messages);

}