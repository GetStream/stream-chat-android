package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
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
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
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

    public void queryChannels() {
        Log.i(TAG, "queryChannels for loading the channels");

        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(30)
                .withMessageLimit(20);

        Client c = StreamChat.getInstance(getApplication());
        c.queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSuccess for loading the channels");
                loading.postValue(false);
                List<Channel> channelList = channels.getValue();
                if (channelList == null) {
                    channelList = new ArrayList<>();
                }
                for (ChannelState chan: response.getChannels()) {
                    // TODO: super duper ugly trick to get the right object noooooo
                    channelList.add(client().getChannelByCid(chan.getChannel().getCid()));
                }
                channels.postValue(channelList);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                loading.postValue(false);
            }
        });
    }

    public void loadMore() {
        // different loader, offset, perhaps callback...
        // TODO: Make this more DRY

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
                List<Channel> channelList = channels.getValue();
                if (channelList == null) {
                    channelList = new ArrayList<>();
                }
                if (response.getChannels().size() < limit) {
                    endOfPagination.postValue(true);
                }
                for (ChannelState chan: response.getChannels()) {
                    // TODO: super duper ugly trick to get the right object noooooo
                    channelList.add(client().getChannelByCid(chan.getChannel().getCid()));
                }
                channels.postValue(channelList);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                loadingMore.postValue(false);
            }
        });
    }
}
