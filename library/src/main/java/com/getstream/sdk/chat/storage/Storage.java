package com.getstream.sdk.chat.storage;

import android.content.Context;
import android.os.AsyncTask;

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

    public List<Channel> selectChannels(String queryID, Integer limit) {
        if (!enabled) return null;

        QueryChannelsQ query = selectQuery(queryID);
        List<Channel> channels = query.getChannels(queryChannelsQDao,100);
        return channels;
    }
    public List<ChannelState> selectChannelStates(String queryID, Integer limit) {
        if (!enabled) return null;

        QueryChannelsQ query = selectQuery(queryID);
        List<ChannelState> channels = query.getChannelStates(queryChannelsQDao,100);
        return channels;
    }

    public QueryChannelsQ selectQuery(String queryID) {
        if (!enabled) return null;

        return queryChannelsQDao.select();
    }

    public void insertChannels(List<Channel> channels) {
        if (!enabled) return;

        // TODO: wrap in task
        channelsDao.insertChannels(channels);
    }

    public void insertChannel(Channel channel) {
        if (!enabled) return;

        // TODO: wrap in task
        channelsDao.insertChannel(channel);
    }

    public void insertMessages(List<Message> messages) {
        if (!enabled) return;

        InsertMessageAsyncTask task = new InsertMessageAsyncTask(messageDao);
        task.execute(messages);
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

    private static class InsertMessageAsyncTask extends AsyncTask<Message, Void, Void> {

        private MessageDao mAsyncTaskDao;

        public void insertAsyncTask(MessageDao dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected Void doInBackground(final Message... params) {
            mAsyncTaskDao.insert(params[0]);
            return null;
        }
    }

    private static class InsertQueryAsyncTask extends AsyncTask<QueryChannelsQ, Void, Void> {

        private QueryChannelsQDao mDao;

        public void insertAsyncTask(QueryChannelsQDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(final QueryChannelsQ... params) {
            mDao.insert(params[0]);
            return null;
        }
    }


    private static class InsertChannelAsyncTask extends AsyncTask<Channel, Void, Void> {

        private ChannelsDao mDao;

        public void insertAsyncTask(ChannelsDao dao) {
            mDao = dao;
        }

        @Override
        protected Void doInBackground(final Channel... params) {
            mDao.insertChannels(params);
            return null;
        }
    }
}
