package com.getstream.sdk.chat.storage;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import io.getstream.chat.android.client.models.Channel;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import com.getstream.sdk.chat.rest.response.ChannelState;


// TODO: export schema
@Database(entities = {Message.class, User.class, Channel.class, QueryChannelsQ.class, ChannelState.class}, version = 16, exportSchema = false)
public abstract class ChatDatabase extends RoomDatabase {

    private static volatile ChatDatabase INSTANCE;

    public static ChatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (ChatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            ChatDatabase.class, "stream_chat")
                            // ensure we don't crash if migrations are missing
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract MessageDao messageDao();

    public abstract QueryChannelsQDao queryChannelsQDao();

    public abstract ChannelsDao channelsDao();

    public abstract UsersDao usersDao();
}