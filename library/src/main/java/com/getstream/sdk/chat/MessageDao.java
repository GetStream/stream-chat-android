package com.getstream.sdk.chat;

import androidx.room.Dao;
import androidx.room.Insert;

import com.getstream.sdk.chat.rest.Message;

import java.util.List;


@Dao
public interface MessageDao {

    @Insert
    void insert(Message word);

    @Insert
    public void insertMessages(List<Message> messages);

    //@Query("DELETE FROM word_table")
    //void deleteAll();

   // @Query("SELECT * from word_table ORDER BY word ASC")
    //List<Word> getAllWords();
}