package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    private MutableLiveData<List<Channel>> channels;
    public MutableLiveData<Boolean> loading;
    public MutableLiveData<Boolean> loadingMore;
    public MutableLiveData<Boolean> failed;
    public MutableLiveData<Boolean> endOfPagination;
    public MutableLiveData<Boolean> online;

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }

    private FilterObject filter;
    private QuerySort sort;

    public Client client(){
        return StreamChat.getInstance(getApplication());
    }

    public ChannelListViewModel(@NonNull Application application) {
        super(application);
        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        online = new MutableLiveData<>(true);
        endOfPagination = new MutableLiveData<>(false);
        channels = new MutableLiveData<>();

        Client c = StreamChat.getInstance(application);
        c.addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionChanged(Event event) {
                online.postValue(event.getOnline());
            }
        });
        sort = new QuerySort().desc("last_message_at");
    }

    public void setChannelFilter(FilterObject filter) {
        this.filter = filter;
        this.queryChannels();
    }

    public void initEventHandlers(){
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
        });
    }

    public boolean updateChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        if (removed ) {
            channelCopy.add(0, channel);
            channels.postValue(channelCopy);
        }
        Boolean updated = removed;
        return updated;
    }

    public void upsertChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        channelCopy.add(0, channel);
        channels.postValue(channelCopy);
    }

    public boolean deleteChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        Boolean removed = channelCopy.remove(channel);
        channels.postValue(channelCopy);
        return removed;
    }

    public void addChannels(List<ChannelState> newChannelsState) {
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

    public void queryChannels() {
        Log.i(TAG, "queryChannels for loading the channels");

        int limit = 30;
        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(limit)
                .withMessageLimit(20);

        client().queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSuccess for loading the channels");
                loading.postValue(false);
                addChannels(response.getChannels());
                if (response.getChannels().size() < limit) {
                    endOfPagination.postValue(true);
                }
                // TODO move this
                initEventHandlers();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                loading.postValue(false);
            }
        });
    }

    public void loadMore() {
        int limit = 30;
        if (loadingMore.getValue()) {
            return;
        }
        loadingMore.setValue(true);
        int offset = channels.getValue().size();
        Log.i(TAG, "Offset is " + offset);
        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(limit)
                .withOffset(offset)
                .withMessageLimit(20);

        Client c = StreamChat.getInstance(getApplication());
        c.queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSuccess for loading more channels");
                loadingMore.postValue(false);
                addChannels(response.getChannels());
                if (response.getChannels().size() < limit) {
                    endOfPagination.postValue(true);
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                loadingMore.postValue(false);
            }
        });
    }
}
