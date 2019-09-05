package com.getstream.sdk.chat.storage;

import android.content.Context;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.util.List;

public class Storage {

    private static volatile Storage INSTANCE;
    private Boolean enabled;
    private Context context;
    private ChatDatabase db;
    private MessageDao messageDao;
    private ChannelsDao channelsDao;
    private QueryChannelsQDao queryChannelsQDao;

    public Storage(Context context, Boolean enabled) {
        this.context = context;
        this.enabled = enabled;
        if (enabled) {
            ChatDatabase db = ChatDatabase.getDatabase(getContext());
            messageDao = db.messageDao();
            channelsDao = db.channelsDao();
            queryChannelsQDao = db.queryChannelsQDao();
        }
    }

    public static Storage getStorage(final Context context, final boolean enabled) {
        if (INSTANCE == null) {
            synchronized (Storage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Storage(context, enabled);
                }
            }
        }
        return INSTANCE;
    }

    public MutableLiveData<List<ChannelState>> selectChannelStates(String queryID, Integer limit) {
        if (!enabled) return null;

        LiveData<QueryChannelsQ> queryLiveData = selectQuery(queryID);
        MutableLiveData<List<ChannelState>> channelStates = new MutableLiveData<>();
        // TODO
//        queryLiveData.observe(query -> {
//            List<ChannelState> channels = query.getChannelStates(queryChannelsQDao,100);
//            channelStates.setValue(channels);
//            return;
//        });

        return channelStates;
    }

    public LiveData<QueryChannelsQ> selectQuery(String queryID) {
        if (!enabled) return null;

        return queryChannelsQDao.select(queryID);
    }

    public void insertChannels(List<Channel> channels) {
        if (!enabled) return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                channelsDao.insertChannels(channels);
                return null;
            }
        }.execute();
    }

    public void insertChannel(Channel channel) {
        if (!enabled) return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                channelsDao.insertChannel(channel);
                return null;
            }
        }.execute();
    }

    public void insertMessages(List<Message> messages) {
        if (!enabled) return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                messageDao.insertMessages(messages);
                return null;
            }
        }.execute();

    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

}
