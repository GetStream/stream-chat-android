package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;

import java.util.ArrayList;
import java.util.List;


public class ChannelListViewModel extends AndroidViewModel {

    private final String TAG = ChannelListViewModel.class.getSimpleName();

    private LazyQueryChannelLiveData<List<Channel>> channels;
    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> loadingMore;

    private FilterObject filter;
    private QuerySort sort;

    private boolean reachedEndOfPagination;
    private boolean initialized;
    private boolean isLoading;
    private boolean isLoadingMore;
    private int pageSize;

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<Boolean> getLoadingMore() {
        return loadingMore;
    }

    public void setChannelsPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    private void setLoading(){
        isLoading = true;
        loading.postValue(true);
    }

    private void setLoadingDone(){
        isLoading = false;
        loading.postValue(false);
    }

    private void setLoadingMore(){
        setLoading();
        isLoadingMore = true;
        loadingMore.postValue(true);
    }

    private void setLoadingMoreDone(){
        setLoadingDone();
        isLoadingMore = false;
        loadingMore.postValue(false);
    }

    public void setChannelFilter(FilterObject filter) {
        if (initialized) {
            Log.e(TAG, "setChannelFilter on an already initialized channel list is a no-op, make sure to set filters *before* consuming channels or create a new ChannelListViewModel if you need a different query");
            return;
        }
        this.filter = filter;
    }

    public Client client(){
        return StreamChat.getInstance(getApplication());
    }

    public ChannelListViewModel(@NonNull Application application) {
        super(application);

        isLoading = true;
        isLoadingMore = false;
        reachedEndOfPagination = false;
        pageSize = 25;

        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);

        channels = new LazyQueryChannelLiveData<>();
        channels.viewModel = this;
        sort = new QuerySort().desc("last_message_at");

        setupConnectionRecovery();
        initEventHandlers();
    }

    private void setupConnectionRecovery(){
        client().addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionRecovered(Event event) {
                Log.w(TAG, "onConnectionRecovered");
                boolean changed = false;
                List<Channel> channelCopy = channels.getValue();
                for (Channel channel: client().getActiveChannels()) {
                    int idx = -1;
                    if (channelCopy != null) {
                        idx = channelCopy.lastIndexOf(channel);
                    }
                    Log.w(TAG, "new channel size: " + channel.getChannelState().getMessages().size());
                    Log.w(TAG, "new channel last message: " + channel.getChannelState().getLastMessage().getText());
                    Log.w(TAG, "index of channel " + idx);
                    if (idx != -1) {
                        channelCopy.set(idx, channel);
                        changed = true;
                        Log.w(TAG, "current channel size: " + channelCopy.get(idx).getChannelState().getMessages().size());
                    }
                }
                if (changed) channels.postValue(channelCopy);
            }
        });
    }

    private void initEventHandlers() {
        client().addEventHandler(new ChatEventHandler() {
            @Override
            public void onNotificationMessageNew(Event event) {
                Message lastMessage = event.getChannel().getChannelState().getLastMessage();
                Log.i(TAG, "onMessageNew Event: Received a new message with text: " + event.getMessage().getText());
                Log.i(TAG, "onMessageNew State: Last message is: " + lastMessage.getText());
                Log.i(TAG, "onMessageNew Unread Count " + event.getChannel().getChannelState().getCurrentUserUnreadMessageCount());
                upsertChannel(event.getChannel());
            }

            @Override
            public void onMessageNew(Event event) {
                Message lastMessage = event.getChannel().getChannelState().getLastMessage();
                Log.i(TAG, "onMessageNew Event: Received a new message with text: " + event.getMessage().getText());
                Log.i(TAG, "onMessageNew State: Last message is: " + lastMessage.getText());
                Log.i(TAG, "onMessageNew Unread Count " + event.getChannel().getChannelState().getCurrentUserUnreadMessageCount());
                updateChannel(event.getChannel());
            }

            @Override
            public void onChannelDeleted(Event event) {
                deleteChannel(event.getChannel());
            }

            @Override
            public void onChannelUpdated(Event event) {
                updateChannel(event.getChannel());
            }

            @Override
            public void onMessageRead(Event event) {
                Log.i(TAG, "Event: Message read by user " + event.getUser().getName());
                List<ChannelUserRead> reads = event.getChannel().getChannelState().getLastMessageReads();
                if (reads.size() > 0) {
                    Log.i(TAG, "State: Message read by user " + reads.get(0).getUser().getName());
                }

                updateChannel(event.getChannel());
            }
            @Override
            public void onUserWatchingStart(Event event){
//                Channel channel = client().getChannelByCid(event.getCid());
            }
        });
    }

    private boolean updateChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        if (removed) {
            channelCopy.add(0, channel);
            channels.postValue(channelCopy);
        }
        Boolean updated = removed;
        return updated;
    }

    private void upsertChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        channelCopy.add(0, channel);
        channels.postValue(channelCopy);
    }

    private boolean deleteChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        channels.postValue(channelCopy);
        return removed;
    }

    private void addChannels(List<ChannelState> newChannelsState) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }
        List<Channel> newChannels = new ArrayList<>();
        for (ChannelState chan: newChannelsState) {
            newChannels.add(chan.getChannel());
        }
        channelCopy.addAll(newChannels);
        channels.postValue(channelCopy);
    }

    private void queryChannels() {
        Log.i(TAG, "queryChannels for loading the channels");

        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(pageSize)
                .withMessageLimit(20);

        client().queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                initialized = true;
                setLoadingDone();

                Log.i(TAG, "onSuccess for loading the channels");
                addChannels(response.getChannels());
                if (response.getChannels().size() < pageSize) {
                    Log.i(TAG, "reached end of pagination");
                    reachedEndOfPagination = true;
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                initialized = true;
                setLoadingDone();
                Log.e(TAG, "onError for loading the channels" + errMsg);
            }
        });
    }

    public void loadMore() {
        if (isLoading) {
            Log.i(TAG, "already loading, skip loading more");
            return;
        }
        if (reachedEndOfPagination) {
            Log.i(TAG, "already reached end of pagination, skip loading more");
            return;
        }
        if (isLoadingMore) {
            Log.i(TAG, "already loading next page, skip loading more");
            return;
        }
        setLoadingMore();

        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(pageSize)
                .withMessageLimit(20);

        if (channels.getValue() != null) {
            request = request.withLimit(channels.getValue().size());
        }

        client().queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSuccess for loading more channels");
                setLoadingMoreDone();
                addChannels(response.getChannels());
                if (response.getChannels().size() < pageSize) {
                    Log.i(TAG, "reached end of pagination");
                    reachedEndOfPagination = true;
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                setLoadingMoreDone();
            }
        });
    }

    class LazyQueryChannelLiveData<T> extends MutableLiveData<T> {

        protected ChannelListViewModel viewModel;

        @Override
        public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super T> observer) {
            super.observe(owner, observer);
        }

        @Override
        protected void onActive() {
            super.onActive();
            if (!viewModel.initialized) {
                viewModel.queryChannels();
            }
        }
    }

}
