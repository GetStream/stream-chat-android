package com.getstream.sdk.chat.storage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.room.Transaction;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {
    final String TAG = Storage.class.getSimpleName();

    private static volatile Storage INSTANCE;
    private Boolean enabled;
    private Context context;
    private ChatDatabase db;
    private MessageDao messageDao;
    private UsersDao usersDao;

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
            usersDao = db.usersDao();
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

    public MutableLiveData<List<ChannelState>> selectChannelStates(String queryID, Integer limit, OnQueryListener l) {
        if (!enabled) return null;

        MutableLiveData<List<ChannelState>> channelStates = new MutableLiveData<>();

        // based on https://gist.github.com/cesarferreira/ef70baa8d64f9753b4da
        new QueryTask(l).execute(queryID);

        return channelStates;
    }



    public QueryChannelsQ selectQuery(String queryID) {
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

    @Transaction
    private void insertQueryWithChannelsTransaction(QueryChannelsQ query, List<Channel> channels) {

        channelsDao.insertChannels(channels);
        queryChannelsQDao.insertQuery(query);
    }

    private void insertUsersUnique(List<User> users) {
        HashMap<String, User> userMap = new HashMap<>();
        for (User u: users) {
            userMap.put(u.getId(), u);
        }

        usersDao.insertUsers(new ArrayList(userMap.values()));
    }

    public void insertQueryWithChannels(QueryChannelsQ query, List<Channel> channels) {
        if (!enabled) return;

        List<User> users = new ArrayList<>();


        Log.i(TAG, "writing channels to storage");
        for (Channel c: channels) {
            c.preStorage();
            c.getLastState().preStorage();
            // gather the users from members, read, last message and created by
            users.add(c.getCreatedByUser());
            for (Member m : c.getLastState().getMembers()) {
                users.add(m.getUser());
            }
            // TODO: what if there are >1000 user reads on a channel...
            for (ChannelUserRead r : c.getLastState().getReads()) {
                users.add(r.getUser());
            }
            Message lastMessage = c.getLastState().getLastMessage();
            if (lastMessage != null) {
                users.add(lastMessage.getUser());
            }
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                insertUsersUnique(users);
                insertQueryWithChannelsTransaction(query, channels);
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



    public void insertQuery(QueryChannelsQ query) {
        if (!enabled) return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                queryChannelsQDao.insertQuery(query);
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

    public interface OnQueryListener<T> {
        public void onSuccess(T object);
        public void onFailure(Exception e);
    }

    public class QueryTask extends AsyncTask<String, Void, List<ChannelState>> {

        private OnQueryListener<List<ChannelState>> mCallBack;
        public Exception mException;


        public QueryTask(OnQueryListener callback) {
            mCallBack = callback;
        }

        @Override
        protected List<ChannelState> doInBackground(String... params) {
            try {
                String queryID = params[0];
                QueryChannelsQ query = selectQuery(queryID);
                if (query != null) {
                    List<ChannelState> channels = query.getChannelStates(channelsDao,100);
                    return channels;
                } else {
                    return null;
                }


            } catch (Exception e) {
                throw e;

                //mException = e;
            }

            //return null;
        }

        @Override
        protected void onPostExecute(List<ChannelState> result) {
            Log.i(TAG, "QT Post");
            if (mCallBack != null) {
                if (mException == null) {
                    mCallBack.onSuccess(result);
                } else {
                    mCallBack.onFailure(mException);
                }
            }
        }
    }

}
