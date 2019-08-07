package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.response.ChannelState;

import static com.getstream.sdk.chat.utils.Utils.TAG;


/*
* - store the channel data
* - load more data
* -
 */
public class ChannelViewModel2 extends AndroidViewModel {
    private final String TAG = ChannelViewModel2.class.getSimpleName();

    public MutableLiveData<Boolean> loading;
    public MutableLiveData<Boolean> loadingMore;
    public MutableLiveData<Boolean> failed;
    public MutableLiveData<Boolean> online;
    private Channel channel;
    public ChannelState channelState;

    // TODO: Thread
    // TODO: Editing
    //

    public ChannelViewModel2(@NonNull Application application) {
        super(application);
        loading = new MutableLiveData<Boolean>(true);
        loadingMore = new MutableLiveData<Boolean>(false);
        failed = new MutableLiveData<Boolean>(false);
        online = new MutableLiveData<Boolean>(true);
    }


    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
        this.loadChannelState();
    }

    private void loadChannelState() {
        ChannelViewModel2 m = this;
        loading.setValue(true);
        Log.d(TAG, "Channel Connecting...");

        // TODO: figure out why postValue or setValue from the callback don't actually update the UI...

        channel.query(new QueryChannelCallback() {
            @Override
            public void onSuccess(ChannelState response) {
                m.loading.postValue(false);
                m.channelState = response;
                Log.d(TAG, "channelState loaded");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                m.loading.postValue(false);
                Log.d(TAG, "Failed Connect Channel : " + errMsg);
            }
        });
    }
}