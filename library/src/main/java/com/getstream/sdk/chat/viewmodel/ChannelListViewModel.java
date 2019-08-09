package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;


public class ChannelListViewModel extends AndroidViewModel {
    private final String TAG = ChannelListViewModel.class.getSimpleName();

    public MutableLiveData<Boolean> loading;
    public MutableLiveData<Boolean> loadingMore;
    public MutableLiveData<Boolean> failed;
    public MutableLiveData<Boolean> endOfPagination;
    public MutableLiveData<Boolean> online;
    private FilterObject filter;
    private QuerySort sort;


    public ChannelListViewModel(@NonNull Application application) {
        super(application);
        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        online = new MutableLiveData<>(true);
        endOfPagination = new MutableLiveData<>(false);
        Client c = StreamChat.getInstance();
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
                .withLimit(20)
                .withMessageLimit(20);

        Client c = StreamChat.getInstance();
        c.queryChannels(request, new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSucces for loading the channels");
                loading.postValue(false);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);
                loading.postValue(false);
            }
        });
    }

    public void loadMore() {
        // TODO: Implement loadmore
    }
}
