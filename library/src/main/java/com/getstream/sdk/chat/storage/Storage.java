package com.getstream.sdk.chat.storage;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.room.Transaction;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                Log.i(TAG, String.format("Inserted %d channels into offline storage", channels.size()));
                channelsDao.insertChannels(channels);
                return null;
            }
        }.execute();
    }

    @Transaction
    private void insertChannelStateInTransaction(List<User> users, QueryChannelsQ query, List<Channel> channels, List<Message> messages) {
        insertUsersUnique(users);
        Log.i(TAG, String.format("Inserted %d channels, %d messages into offline storage for query with id %s", channels.size(), messages.size(), query.getId()));
        channelsDao.insertChannels(channels);
        queryChannelsQDao.insertQuery(query);
        messageDao.insertMessages(messages);
    }

    private void insertUsersUnique(List<User> users) {
        HashMap<String, User> userMap = new HashMap<>();
        for (User u: users) {
            userMap.put(u.getId(), u);
        }

        Log.i(TAG, String.format("Inserted %d users into offline storage", userMap.values().size()));

        usersDao.insertUsers(new ArrayList(userMap.values()));
    }

    private void enrichUsers(List<Channel> channels) {
        List<String> userIDs = new ArrayList<>();
        // gather all the user ids

        for (Channel c: channels) {

            // gather the users from members, read, last message and created by
            userIDs.add(c.getCreatedByUserID());
            for (Member m : c.getLastState().getMembers()) {
                userIDs.add(m.getUserId());
            }
            for (ChannelUserRead r : c.getLastState().getReads()) {
                userIDs.add(r.getUserId());
            }
            Message lastMessage = c.getLastState().computeLastMessage();
            if (lastMessage != null) {
                userIDs.add(lastMessage.getUserId());
            }
        }

        // query those users as a map
        List<User> users = usersDao.getUsers(userIDs);
        HashMap<String, User> userMap = new HashMap<String, User>();
        for (User u: users) {
            userMap.put(u.getId(), u);
        }

        // add the object, fun fun
        for (Channel c: channels) {

            // gather the users from members, read, last message and created by
            c.setCreatedByUser(userMap.get(c.getCreatedByUserID()));
            for (Member m : c.getLastState().getMembers()) {
                m.setUser(userMap.get(m.getUserId()));
            }
            for (ChannelUserRead r : c.getLastState().getReads()) {
                User u = userMap.get(r.getUserId());
                if (u == null) continue;
                r.setUser(u);
            }
            Message lastMessage = c.getLastState().computeLastMessage();
            if (lastMessage != null) {
                lastMessage.setUser(userMap.get(lastMessage.getUserId()));
            }
        }

    }

    public void insertQueryWithChannels(QueryChannelsQ query, List<Channel> channels) {
        if (!enabled) return;

        List<User> users = new ArrayList<>();
        List<Message> messages = new ArrayList<>();


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
            Message lastMessage = c.getLastState().computeLastMessage();
            if (lastMessage != null) {
                users.add(lastMessage.getUser());
            }
            messages.addAll(c.getLastState().getMessages());
            for (Message m: c.getLastState().getMessages()) {
                users.add(m.getUser());
                for (Reaction r: m.getOwnReactions()) {
                    users.add(r.getUser());
                }
                for (Reaction r: m.getLatestReactions()) {
                    users.add(r.getUser());
                }
            }
        }


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                insertChannelStateInTransaction(users, query, channels, messages);
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
                Log.i(TAG, String.format("Inserted %d messages into offline storage", messages.size()));
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


    public List<ChannelState> getChannelStates(QueryChannelsQ query, Integer limit) {
        List<Channel> channels = getChannels(query, limit);
        List<ChannelState> channelStates = new ArrayList<>();
        for (Channel c: channels) {
            ChannelState state = c.getLastState();
            channelStates.add(state);
        }
        return channelStates;
    }

    public List<Channel> getChannels(QueryChannelsQ query, Integer limit) {
        List<String> selectedChannelIDs = query.getChannelCIDs();
        List<Channel> selectedChannels = new ArrayList<>();

        if (selectedChannelIDs != null) {
            Integer max = (limit > selectedChannelIDs.size()) ? selectedChannelIDs.size() : limit;
            selectedChannelIDs = selectedChannelIDs.subList(0, max);
            List<Channel> channels = channelsDao.getChannels(selectedChannelIDs);
            Map<String, Channel> channelMap = new HashMap<String, Channel>();
            for (Channel c : channels) {
                channelMap.put(c.getCid(), c);
            }
            // restore the original sort
            for (String cid: selectedChannelIDs) {
                Channel channel = channelMap.get(cid);
                channel.setChannelState(channel.getLastState());
                channel.setClient(StreamChat.getInstance(context));
                if (channel==null) {
                    Log.w(TAG, "Missing channel for cid " + cid);
                } else {
                    ChannelState state = channel.getLastState();
                    state.setChannel(channel);
                    selectedChannels.add(channel);
                }

            }
        }

        return selectedChannels;

    }

    public class ChannelQuery extends AsyncTask<String, Void, List<ChannelState>> {
        private OnQueryListener<List<ChannelState>> mCallBack;
        public Exception mException;

        public ChannelQuery(OnQueryListener callback) {
            mCallBack = callback;
        }

        @Override
        protected List<ChannelState> doInBackground(String... params) {
            try {
                String channelID = params[0];
                // get the channel
                Channel channel = channelsDao.getChannel(channelID);
                if (channel == null) {
                    return null;
                }

                // fetch the message
                List<Message> messages = messageDao.selectMessagesForChannel(channel.getCid(), 100);
                ChannelState state = channel.getLastState();
                state.setMessages(messages);
                state.setChannel(channel);



            } catch (Exception e) {
                throw e;

                //mException = e;
            }

            return null;
        }

        @Override
        protected void onPostExecute(List<ChannelState> result) {
            if (mCallBack != null) {
                if (mException == null) {
                    mCallBack.onSuccess(result);
                } else {
                    mCallBack.onFailure(mException);
                }
            }
        }
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
                    List<Channel> channels = getChannels(query,100);
                    enrichUsers(channels);
                    List<ChannelState> channelStates = new ArrayList<>();
                    for (Channel c: channels) {
                        ChannelState state = c.getLastState();
                        channelStates.add(state);
                    }

                    return channelStates;
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
