package com.getstream.sdk.chat;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;


// TODO: export schema
@Database(entities = {Message.class, User.class, Channel.class}, version = 1, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {

    public abstract MessageDao messageDao();

    private static volatile ChatDatabase INSTANCE;

    public static ChatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ChatDatabase.class, "stream_chat")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}