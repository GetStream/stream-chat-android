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
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.getstream.sdk.chat.storage.Sync.LOCAL_ONLY;

public class StreamStorage implements Storage {
    private static volatile StreamStorage INSTANCE;
    final String TAG = StreamStorage.class.getSimpleName();
    private Boolean enabled;
    private Client client;
    private Context context;
    private ChatDatabase db;
    private MessageDao messageDao;
    private UsersDao usersDao;

    private ChannelsDao channelsDao;
    private QueryChannelsQDao queryChannelsQDao;

    public StreamStorage(Client client, Context context, Boolean enabled) {
        this.client = client;
        this.context = context;
        this.enabled = enabled;
        if (enabled) {
            ChatDatabase db = ChatDatabase.getDatabase(context);
            messageDao = db.messageDao();
            channelsDao = db.channelsDao();
            queryChannelsQDao = db.queryChannelsQDao();
            usersDao = db.usersDao();
        }
    }

    public static StreamStorage getStorage(Client client, final Context context, final boolean enabled) {
        if (INSTANCE == null) {
            synchronized (StreamStorage.class) {
                if (INSTANCE == null) {
                    INSTANCE = new StreamStorage(client, context, enabled);
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public MutableLiveData<ChannelState> selectChannelState(String cid, OnQueryListener l) {
        if (!enabled) return null;

        MutableLiveData<ChannelState> channelStates = new MutableLiveData<>();

        new ChannelQuery(l).execute(cid);

        return channelStates;
    }

    @Override
    public MutableLiveData<List<ChannelState>> selectChannelStates(String queryID, Integer limit, OnQueryListener l) {
        if (!enabled) return null;

        MutableLiveData<List<ChannelState>> channelStates = new MutableLiveData<>();

        // based on https://gist.github.com/cesarferreira/ef70baa8d64f9753b4da
        new QueryTask(l).execute(queryID);

        return channelStates;
    }

    @Override
    public QueryChannelsQ selectQuery(String queryID) {
        if (!enabled) return null;

        return queryChannelsQDao.select(queryID);
    }

    @Override
    public void insertChannels(List<Channel> channels) {
        if (!enabled) return;

        for (Channel c : channels) {
            c.preStorage();
            c.getLastState().preStorage();
        }

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                StreamChat.getLogger().logI(this, String.format("Inserted %d channels into offline storage", channels.size()));
                channelsDao.insertChannels(channels);
                return null;
            }
        }.execute();
    }

    @Override
    public void insertQueryWithChannels(QueryChannelsQ query, List<Channel> channels) {
        if (!enabled) return;

        List<String> channelIDs = new ArrayList<>();
        for (Channel c : channels) {
            channelIDs.add(c.getCid());
        }
        query.setChannelCIDs(channelIDs);

        List<User> users = new ArrayList<>();
        List<Message> messages = new ArrayList<>();


        for (Channel c : channels) {
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
            for (Message m : c.getLastState().getMessages()) {
                users.add(m.getUser());
                for (Reaction r : m.getOwnReactions()) {
                    users.add(r.getUser());
                }
                for (Reaction r : m.getLatestReactions()) {
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

    @Override
    public void insertChannel(Channel channel) {
        if (!enabled) return;

        List<Channel> channels = new ArrayList<>();
        channels.add(channel);

        insertChannels(channels);
    }

    /**
     * delete channel from database
     *
     * @param channel the channel needs to delete
     */
    @Override
    public void deleteChannel(@NotNull Channel channel) {
        if (!enabled) return;

        new DeleteChannelAsyncTask(channelsDao).execute(channel.getCid());
    }

    @Override
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

    @Override
    public void deleteMessage(String id) {
        if (!enabled) return;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                messageDao.deleteMessage(id);
                return null;
            }
        }.execute();

    }

    @Override
    public void insertMessageForChannel(Channel channel, Message message) {
        // set the ids etc
        if (message.getId() == null) {
            String clientSideID = getClient().generateMessageID();
            message.setId(clientSideID);
        }
        if (message.getCreatedAt() == null) {
            message.setCreatedAt(new Date());
        }
        if (message.getUser() == null)
            message.setUser(client.getUser());

        message.setSyncStatus(LOCAL_ONLY);
        message.preStorage();

        if (!enabled) return;

        List<Message> messages = new ArrayList<>();
        messages.add(message);

        insertMessagesForChannel(channel, messages);
    }

    @Override
    public void insertMessagesForChannel(Channel channel, List<Message> messages) {
        if (!enabled) return;

        List<User> users = new ArrayList<>();

        for (Message m : messages) {
            m.setCid(channel.getCid());
            m.preStorage();

            users.add(m.getUser());
            List<Reaction> reactions = new ArrayList<>();
            if (m.getOwnReactions() != null) {
                reactions.addAll(m.getOwnReactions());
            }
            if (m.getLatestReactions() != null) {
                reactions.addAll(m.getLatestReactions());
            }
            for (Reaction r : reactions) {
                users.add(r.getUser());
            }
        }


        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                insertMessagesAndUsers(users, messages);

                return null;
            }
        }.execute();
    }

    @Override
    public List<ChannelState> getChannelStates(QueryChannelsQ query, Integer limit) {
        List<Channel> channels = getChannels(query, limit);
        List<ChannelState> channelStates = new ArrayList<>();
        for (Channel c : channels) {
            ChannelState state = c.getLastState();
            channelStates.add(state);
        }
        return channelStates;
    }

    @Override
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
            for (String cid : selectedChannelIDs) {
                Channel channel = channelMap.get(cid);
                channel.setChannelState(channel.getLastState());
                channel.setClient(StreamChat.getInstance(context));
                if (channel == null) {
                    StreamChat.getLogger().logW(this, "Missing channel for cid " + cid);
                } else {
                    ChannelState state = channel.getLastState();
                    state.setChannel(channel);
                    selectedChannels.add(channel);
                }

            }
        }

        return selectedChannels;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Transaction
    private void insertMessagesAndUsers(List<User> users, List<Message> messages) {
        usersDao.insertUsers(users);
        messageDao.insertMessages(messages);
        StreamChat.getLogger().logI(this, String.format("Inserted %d messages and %d users into offline storage", messages.size(), users.size()));
    }

    private void enrichUsers(Channel channel) {
        List<String> userIDs = new ArrayList<>();

        // the person who created the channel
        userIDs.add(channel.getCreatedByUserID());
        // iterate over messages and write the users
        for (Message m : channel.getChannelState().getMessages()) {
            userIDs.add(m.getUserID());
            for (Reaction r : m.getLatestReactions()) {
                userIDs.add(r.getUserID());
            }
            for (Reaction r : m.getOwnReactions()) {
                userIDs.add(r.getUserID());
            }
        }

        // query those users as a map
        List<User> users = usersDao.getUsers(userIDs);
        HashMap<String, User> userMap = new HashMap<String, User>();
        for (User u : users) {
            userMap.put(u.getId(), u);
        }

        //
        for (Message m : channel.getChannelState().getMessages()) {
            // add the user objects
            User u = userMap.get(m.getUserID());
            m.setUser(u);
            for (Reaction r : m.getLatestReactions()) {
                r.setUser(userMap.get(r.getUserID()));
            }
            for (Reaction r : m.getOwnReactions()) {
                r.setUser(userMap.get(r.getUserID()));
            }
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Transaction
    private void insertChannelStateInTransaction(List<User> users, QueryChannelsQ query, List<Channel> channels, List<Message> messages) {
        insertUsersUnique(users);
        StreamChat.getLogger().logI(this, String.format("Inserted %d channels, %d messages into offline storage for query with id %s", channels.size(), messages.size(), query.getId()));
        channelsDao.insertChannels(channels);
        queryChannelsQDao.insertQuery(query);
        for (Message m : messages) {
            m.preStorage();
        }
        messageDao.insertMessages(messages);
    }

    private void insertUsersUnique(List<User> users) {
        HashMap<String, User> userMap = new HashMap<>();
        for (User u : users) {
            if (u == null) continue;
            userMap.put(u.getId(), u);
        }

        StreamChat.getLogger().logI(this, String.format("Inserted %d users into offline storage", userMap.values().size()));

        usersDao.insertUsers(new ArrayList(userMap.values()));
    }

    private void enrichUsers(List<Channel> channels) {
        List<String> userIDs = new ArrayList<>();
        // gather all the user ids

        for (Channel c : channels) {

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
                userIDs.add(lastMessage.getUser().getId());
            }
        }

        // query those users as a map
        List<User> users = usersDao.getUsers(userIDs);
        HashMap<String, User> userMap = new HashMap<String, User>();
        for (User u : users) {
            userMap.put(u.getId(), u);
        }

        // add the object, fun fun
        for (Channel c : channels) {

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
                lastMessage.setUser(userMap.get(lastMessage.getUserID()));
            }
        }

    }

    private class ChannelQuery extends AsyncTask<String, Void, ChannelState> {
        public Exception mException;
        private OnQueryListener<ChannelState> mCallBack;

        public ChannelQuery(OnQueryListener callback) {
            mCallBack = callback;
        }

        @Override
        protected ChannelState doInBackground(String... params) {
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
                channel.setChannelState(state);
                enrichUsers(channel);
                return state;


            } catch (Exception e) {
                throw e;

                //mException = e;
            }
        }

        @Override
        protected void onPostExecute(ChannelState result) {
            if (mCallBack != null) {
                if (mException == null) {
                    mCallBack.onSuccess(result);
                } else {
                    mCallBack.onFailure(mException);
                }
            }
        }
    }


    private class QueryTask extends AsyncTask<String, Void, List<ChannelState>> {

        public Exception mException;
        private OnQueryListener<List<ChannelState>> mCallBack;


        public QueryTask(OnQueryListener callback) {
            mCallBack = callback;
        }

        @Override
        protected List<ChannelState> doInBackground(String... params) {
            try {
                String queryID = params[0];
                QueryChannelsQ query = selectQuery(queryID);
                if (query != null) {
                    List<Channel> channels = getChannels(query, 100);
                    enrichUsers(channels);
                    List<ChannelState> channelStates = new ArrayList<>();
                    for (Channel c : channels) {
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
            StreamChat.getLogger().logI(this, "QT Post");
            if (mCallBack != null) {
                if (mException == null) {
                    mCallBack.onSuccess(result);
                } else {
                    mCallBack.onFailure(mException);
                }
            }
        }
    }

    private static class DeleteChannelAsyncTask extends AsyncTask<String, Void, Void> {
        private WeakReference<ChannelsDao> channelsDao;

        private DeleteChannelAsyncTask(ChannelsDao channelsDao) {
            this.channelsDao = new WeakReference<>(channelsDao);
        }

        @Override
        protected Void doInBackground(String... params) {
            if (channelsDao.get() != null) {
                channelsDao.get().deleteChannel(params[0]); // params[0] - channel cid
            }
            return null;
        }
    }

}
