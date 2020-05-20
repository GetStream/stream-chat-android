package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.LifecycleHandler;
import com.getstream.sdk.chat.StreamLifecycleObserver;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.call.Call;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.android.livedata.ChatDomain;
import io.getstream.chat.android.livedata.controller.QueryChannelsController;
import kotlin.Unit;

@Deprecated
public class ChannelListViewModel extends AndroidViewModel implements LifecycleHandler {

    private TaggedLogger logger = ChatLogger.Companion.get("ChannelListViewModel");

    protected LiveData<Boolean> loading;
    protected LiveData<Boolean> loadingMore;

    protected FilterObject filter;
    protected QuerySort sort;

    protected AtomicBoolean initialized;
    protected AtomicBoolean isLoading;
    protected AtomicBoolean isLoadingMore;
    protected boolean queryChannelDone;
    protected int pageSize;
    protected Handler retryLooper;
    protected ChatDomain chatDomain;
    protected QueryChannelsController queryChannelsController;
    private LiveData<List<Channel>> channels;
    private LiveData<Boolean> reachedEndOfPagination;


    public ChannelListViewModel(@NonNull Application application) {
        super(application);

        logger.logI("instance created");

        chatDomain = ChatDomain.instance();


        isLoading = new AtomicBoolean(false);
        isLoadingMore = new AtomicBoolean(false);
        initialized = new AtomicBoolean(false);

        pageSize = 25;

        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);

        sort = new QuerySort().desc("last_message_at");

        new StreamLifecycleObserver(this);
        retryLooper = new Handler();

    }

    public void setQuery(FilterObject filter, QuerySort sort) {
        this.filter = filter;
        this.sort = sort;
        Result<QueryChannelsController> result = chatDomain.getUseCases().getQueryChannels().invoke(filter, sort, 30, 10).execute();
        this.queryChannelsController = result.data();
        if (initialized.get()) {
            logger.logI("setChannelFilter on an already initialized channel will reload the view model");
            reload();
        }

        // connect the livedata objects
        this.channels = queryChannelsController.getChannels();
        this.channels.observeForever((channels) -> {
            Log.i("ChatDomain", "ChatDomain: The channels changed");
        });
        this.loading = queryChannelsController.getLoading();
        this.loadingMore = queryChannelsController.getLoadingMore();
        this.reachedEndOfPagination = queryChannelsController.getEndOfChannels();
        this.queryChannels();
    }

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }


    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoadingMore() {
        return loadingMore;
    }

    public void setChannelsPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        logger.logI("onCleared");
    }




    /**
     * hides the channel from queryChannels for the user until a message is added and remove from the current list of channels
     */
    public Call<Unit> hideChannel(@NonNull String channelType, @NonNull String channelId, boolean clearHistory) {

        return ChatClient.instance().hideChannel(channelType, channelId, clearHistory).map(unit -> {
            //deleteChannel(channelType + ":" + channelId);
            return null;
        });
    }

    /**
     * removes the hidden status for a channel and remove from the current list of channels
     *
     * @param channelId
     * @param channelType
     */
    public Call<Unit> showChannel(@NonNull String channelType, @NonNull String channelId) {
        return ChatClient.instance().showChannel(channelType, channelId).map(unit -> {
            //deleteChannel(channelType + ":" + channelId);
            return null;
        });
    }

    @Override
    public void resume() {
        logger.logI("resume");
    }

    @Override
    public void stopped() {
        logger.logI("stopped");
    }

    /**
     * query channels
     */
    public void queryChannels() {
        chatDomain.getUseCases().getQueryChannelsLoadMore().invoke(filter, sort, pageSize,20);
    }

    /**
     * loads more channels, use this to load a previous page
     */
    public void loadMore() {
        if (!ChatClient.instance().isSocketConnected()) return;

        if (loadingMore.getValue() || loading.getValue()) {
            return;
        }
        if (reachedEndOfPagination.getValue()) {
            return;
        }

        chatDomain.getUseCases().getQueryChannelsLoadMore().invoke(filter, sort, pageSize,20);

    }

    protected void clean() {
        retryLooper.removeCallbacksAndMessages(null);
        initialized.set(true);
    }

    /**
     * Reloads the state of the view model
     */
    public void reload() {
        clean();
        queryChannels();

    }

}