package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class ChannelListViewModel extends AndroidViewModel {
    private final String TAG = ChannelListViewModel.class.getSimpleName();

    public MutableLiveData<Boolean> loading;
    public MutableLiveData<Boolean> loadingMore;
    public MutableLiveData<Boolean> failed;
    public MutableLiveData<Boolean> endOfPagination;
    public MutableLiveData<Boolean> online;
    private FilterObject filter;


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

    }

    public void setChannelFilter(FilterObject filter) {
        this.filter = filter;
        this.queryChannels();
    }

    public void queryChannels() {
        Log.i(TAG, "queryChannels for loading the channels");
        Map<String, Object> payload = new HashMap<>();
        payload.put("filter_conditions", filter.getData());
        payload.put("presence", true);
        payload.put("state", true);
        payload.put("watch", true);
        Map<String, Object> sort = new HashMap<>();
        sort.put("field", "last_message_at");
        sort.put("direction", -1);

        payload.put("sort", Collections.singletonList(sort));
        payload.put("message_limit", 20);
        payload.put("limit", 20);
        payload.put("offset", 0);

        Client c = StreamChat.getInstance();
        c.queryChannels(new JSONObject(payload), new QueryChannelListCallback() {
            @Override
            public void onSuccess(QueryChannelsResponse response) {
                Log.i(TAG, "onSucces for loading the channels");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "onError for loading the channels" + errMsg);

            }
        });
    }

    public void loadMore() {
        // TODO: Implement loadmore
    }
}
