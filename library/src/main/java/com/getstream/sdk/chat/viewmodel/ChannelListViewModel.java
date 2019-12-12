package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.getstream.sdk.chat.LifecycleHandler;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.StreamLifecycleObserver;
import com.getstream.sdk.chat.channels.CreateNewChannel;
import com.getstream.sdk.chat.channels.GetChannels;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.utils.ErrorCallback;
import com.getstream.sdk.chat.utils.ResultCallback;
import com.getstream.sdk.chat.utils.State;
import com.getstream.sdk.chat.utils.SuccessCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;


public class ChannelListViewModel extends AndroidViewModel implements LifecycleHandler {
    private final String TAG = ChannelListViewModel.class.getSimpleName();

    @NotNull
    private LazyQueryChannelLiveData<List<Channel>> channels;
    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> loadingMore;

    private FilterObject filter;
    private QuerySort sort;

    private boolean reachedEndOfPagination;
    private AtomicBoolean initialized;
    private AtomicBoolean isLoading;
    private AtomicBoolean isLoadingMore;
    private boolean queryChannelDone;
    private int pageSize;
    //    private int subscriptionId = 0;
//    private int recoverySubscriptionId = 0;
    private Handler retryLooper;
    //private QueryChannelListCallback queryChannelListCallback;
    //private RetryPolicy retryPolicy;

    public ChannelListViewModel(@NonNull Application application) {
        super(application);

        Log.d(TAG, "instance created");

        isLoading = new AtomicBoolean(false);
        isLoadingMore = new AtomicBoolean(false);
        initialized = new AtomicBoolean(false);

        reachedEndOfPagination = false;
        pageSize = 25;

        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);

        channels = new LazyQueryChannelLiveData<>();
        channels.viewModel = this;
        channels.postValue(new ArrayList<>());
        sort = new QuerySort().desc("last_message_at");

        setupConnectionRecovery();
        setEventHandler(new EventHandler((event, channel) -> false));

        new StreamLifecycleObserver(this);
        retryLooper = new Handler();

        // default retry policy is to retry the request 100 times
//        retryPolicy = new RetryPolicy() {
//            @Override
//            public boolean shouldRetry(Client client, Integer attempt, String errMsg, int errCode) {
//                return attempt < 100;
//            }
//
//            @Override
//            public Integer retryTimeout(Client client, Integer attempt, String errMsg, int errCode) {
//                return Math.min(500 * (attempt * attempt + 1), 30000);
//            }
//        };

        //TODO: Refactoring: add retry policy to client level
    }

//    public RetryPolicy getRetryPolicy() {
//        return retryPolicy;
//    }
//
//    public void setRetryPolicy(RetryPolicy retryPolicy) {
//        this.retryPolicy = retryPolicy;
//    }

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }

    public void loadCh() {
        queryChannels();
    }

    private void setChannels(List<Channel> newChannels) {
        List<Channel> old = channels.getValue();
        old.addAll(newChannels);
        channels.postValue(old);
        //channels.postValue();
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

    public LiveData<State<Channel>> createChannel(String name) {

        MutableLiveData<State<Channel>> result = new MutableLiveData<>();

        setLoading();

        new CreateNewChannel(StreamChat.getChannelsRepository(), StreamChat.client()).create(name).subscribe(data -> {
            setLoadingDone();
            addChannels(Arrays.asList(data.getChannelState()));
            result.postValue(new State<>(data));
        }, throwable -> {
            setLoadingDone();
            result.postValue(new State<>(throwable));
        });

        return result;
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        Log.d(TAG, "onCleared");

//        if (subscriptionId != 0) {
//            client().removeEventHandler(subscriptionId);
//        }
//        if (recoverySubscriptionId != 0) {
//            client().removeEventHandler(recoverySubscriptionId);
//        }
    }

    public boolean setLoading() {
        if (isLoading.compareAndSet(false, true)) {
            loading.postValue(true);
            return true;
        }
        return false;
    }

    public void setLoadingDone() {
        if (isLoading.compareAndSet(true, false))
            loading.postValue(false);
    }

    private boolean setLoadingMore() {
        if (isLoadingMore.compareAndSet(false, true)) {
            loadingMore.postValue(true);
            return true;
        }
        return false;
    }

    private void setLoadingMoreDone() {
        if (isLoadingMore.compareAndSet(true, false))
            loadingMore.postValue(false);
    }

    /**
     * sets the filter used to query the list of channels; if the channel list is already initialized
     * changing the filter will reload the view model using {@link #reload()}
     *
     * @param filter the filter object that will be used to query channels (empty by default)
     */
    public void setChannelFilter(FilterObject filter) {
        this.filter = filter;
        if (initialized.get()) {
            Log.e(TAG, "setChannelFilter on an already initialized channel will reload the view model");
            reload();
        }
    }

    /**
     * hides the channel from queryChannels for the user until a message is added and remove from the current list of channels
     *
     * @param channel  the channel needs to hide
     * @param callback the result callback
     */
    public void hideChannel(@NotNull Channel channel, @Nullable ResultCallback<Void, String> callback) {
        channel.hide(new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                deleteChannel(channel); // remove the channel from the list of not hidden channels
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, errMsg);
                if (callback != null) {
                    callback.onError(errMsg);
                }
            }
        });
    }

    /**
     * removes the hidden status for a channel and remove from the current list of channels
     *
     * @param channel  the channel needs to show
     * @param callback the result callback
     */
    public void showChannel(@NotNull Channel channel, @Nullable ResultCallback<Void, String> callback) {
        channel.show(new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                deleteChannel(channel); // remove the channel from the list of hidden channels
                if (callback != null) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, errMsg);
                if (callback != null) {
                    callback.onError(errMsg);
                }
            }
        });
    }

    /**
     * sets the sorting for the channel list, any channel field can be used to sort in either ASC or
     * DESC direction. if not specified channels are sorted by last_message_at DESC
     *
     * @param sort the sort parameter
     */
    public void setChannelSort(QuerySort sort) {
        this.sort = sort;
    }

    @Override
    public void resume() {
        Log.d(TAG, "resume");
//        if (!initialized.get() || !client().isConnected())
//            setLoading();
    }

    @Override
    public void stopped() {
        Log.d(TAG, "stopped");
    }

    private void setupConnectionRecovery() {
        //TODO: Refactoring: handle broadcast connectivity event
//        recoverySubscriptionId = client().addEventHandler(new ChatEventHandler() {
//            @Override
//            public void onConnectionRecovered(Event event) {
//                Log.i(TAG, "onConnectionRecovered");
//                if (!queryChannelDone) {
//                    queryChannelsInner(0);
//                    return;
//                }
//                setLoadingDone();
//                boolean changed = false;
//                List<Channel> channelCopy = channels.getValue();
//                for (Channel channel : client().getActiveChannels()) {
//                    int idx = -1;
//                    if (channelCopy != null) {
//                        idx = channelCopy.lastIndexOf(channel);
//                    }
//                    if (idx != -1) {
//                        channelCopy.set(idx, channel);
//                        changed = true;
//                    }
//                }
//                if (changed) channels.postValue(channelCopy);
//            }
//        });
    }

    private EventHandler eventHandler;

    public void setEventInterceptor(EventInterceptor interceptor) {
        this.eventHandler = new EventHandler(interceptor);
        initEventHandlers();
    }

    private void setEventHandler(EventHandler eventHandler) {
        this.eventHandler = eventHandler;
        initEventHandlers();
    }

    /*
     * EventInterceptor implementations will receive all events (and channel when applicable) to add
     * custom behavior.
     *
     * shouldDiscard informs the view model what to do next: continue with event handling or
     * ignore the event.
     *
     * This allows the developer to disable some built-in mechanism like automatically add a new
     * channel to the list.
     */
    public interface EventInterceptor {
        boolean shouldDiscard(Event event, @Nullable Channel channel);
    }

    public class EventHandler extends ChatEventHandler {
        private EventInterceptor interceptor;

        public EventHandler(EventInterceptor interceptor) {
            this.interceptor = interceptor;
        }

        @Override
        public void onUserDisconnected() {
            clean();
        }

        @Override
        public void onConnectionChanged(Event event) {
            if (!event.getOnline()) {
                retryLooper.removeCallbacksAndMessages(null);
            }
        }

        @Override
        public void onNotificationMessageNew(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            if (!updateChannel(channel, true)) {
                upsertChannel(channel);
            }
        }

        @Override
        public void onNotificationAddedToChannel(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            if (!updateChannel(channel, true)) {
                upsertChannel(channel);
            }
        }

        @Override
        public void onNotificationRemovedFromChannel(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            deleteChannel(channel);
        }

        @Override
        public void onMessageNew(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, true);
        }

        @Override
        public void onMessageUpdated(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, true);
        }

        @Override
        public void onMessageDeleted(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, false);
        }

        @Override
        public void onChannelDeleted(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            deleteChannel(channel);
        }

        @Override
        public void onChannelUpdated(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, false);
        }

        @Override
        public void onMessageRead(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, false);
        }

        @Override
        public void onMemberAdded(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, false);
        }

        @Override
        public void onMemberUpdated(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, false);
        }

        @Override
        public void onMemberRemoved(Channel channel, Event event) {
            if (interceptor.shouldDiscard(event, channel)) return;
            updateChannel(channel, false);
        }
    }

    synchronized private void initEventHandlers() {
//        if (subscriptionId != 0) {
//            client().removeEventHandler(subscriptionId);
//        }
//        subscriptionId = client().addEventHandler(eventHandler);
    }

    private boolean updateChannel(Channel channel, boolean moveToTop) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }

        int idx = channelCopy.lastIndexOf(channel);

        if (idx != -1) {
            channelCopy.remove(channel);
            channelCopy.add(moveToTop ? 0 : idx, channel);
            channels.postValue(channelCopy);
        }

        return idx != -1;
    }

    private void upsertChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }
        channelCopy.add(0, channel);
        channels.postValue(channelCopy);
    }

    private boolean deleteChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }
        boolean removed = channelCopy.remove(channel);
        channels.postValue(channelCopy);
        return removed;
    }

    public void addChannels(List<ChannelState> newChannelsState) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }

        // - offline loads first
        // - after that we query the API and load more channels
        // - it's possible that the offline results no longer match the query (so we should remove them)

        List<Channel> newChannels = new ArrayList<>();
        for (ChannelState chan : newChannelsState) {
            newChannels.add(chan.getChannel());
        }
        channelCopy.addAll(newChannels);
        channels.postValue(channelCopy);
    }

    public void loadMore() {
        queryChannels();
    }

    private void queryChannels() {

        if(reachedEndOfPagination) return;

        Log.i(TAG, "queryChannels for loading the channels");
        if (!setLoading()) {
            Log.i(TAG, "already loading, skip queryChannels");
            return;
        }
        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withLimit(pageSize)
                .withMessageLimit(20);

        if (channels.getValue() != null)
            request = request.withOffset(channels.getValue().size());

        new GetChannels(StreamChat.getChannelsRepository()).get(request).subscribe(new SuccessCallback<List<Channel>>() {
            @Override
            public void onSuccess(List<Channel> data) {
                queryChannelDone = true;

                if (data.size() < pageSize) {
                    Log.i(TAG, "reached end of pagination");
                    reachedEndOfPagination = true;
                }

                setLoadingDone();

                Log.i(TAG, "onSendMessageSuccess for loading the channels");
                // remove the offline channels before adding the new ones

                //setChannels(data);

                List<ChannelState> states = new ArrayList<>();
                for (Channel d : data) {
                    states.add(d.getChannelState());
                }

                addChannels(states);

//                if (queryChannelListCallback != null) {
//                    queryChannelListCallback.onSuccess(response);
//                }
            }
        }, new ErrorCallback() {
            @Override
            public void error(Throwable throwable) {

                //TODO: Refactoring: show error

//                Log.e(TAG, "onError for loading the channels " + errMsg);
//                Boolean shouldRetry = retryPolicy.shouldRetry(client(), attempt, errMsg, errCode);
//                if (!shouldRetry) {
//                    Log.e(TAG, "tried more than 100 times, give up now");
//                    return;
//                }
//                if (!client().isConnected()) {
//                    return;
//                }
//                int sleep = retryPolicy.retryTimeout(client(), attempt, errMsg, errCode);
//                Log.d(TAG, "retrying in " + sleep);
//                retryLooper.postDelayed(() -> {
//                    queryChannelsInner(attempt + 1);
//                }, sleep);
//
//                if (queryChannelListCallback != null) {
//                    queryChannelListCallback.onError(errMsg, errCode);
//                }
            }
        });
    }

//    public void loadMore() {
//        if (!client().isConnected()) return;
//
//        if (isLoading.get()) {
//            Log.i(TAG, "already loading, skip loading more");
//            return;
//        }
//        if (reachedEndOfPagination) {
//            Log.i(TAG, "already reached end of pagination, skip loading more");
//            return;
//        }
//        if (!setLoadingMore()) {
//            Log.i(TAG, "already loading next page, skip loading more");
//            return;
//        }
//
//        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
//                .withLimit(pageSize)
//                .withMessageLimit(20);
//
//        if (channels.getValue() != null)
//            request = request.withOffset(channels.getValue().size());
//
//        client().queryChannels(request, new QueryChannelListCallback() {
//            @Override
//            public void onSuccess(QueryChannelsResponse response) {
//                Log.i(TAG, "onSendMessageSuccess for loading more channels");
//                setLoadingMoreDone();
//                addChannels(response.getChannelStates());
//                reachedEndOfPagination = response.getChannelStates().size() < pageSize;
//
//                if (reachedEndOfPagination)
//                    Log.i(TAG, "reached end of pagination");
//                callback.onSuccess(response);
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                Log.e(TAG, "onError for loading the channels" + errMsg);
//                setLoadingMoreDone();
//                callback.onError(errMsg, errCode);
//            }
//        });
//    }

    private void clean() {
        retryLooper.removeCallbacksAndMessages(null);
        initialized.set(true);
        channels.postValue(new ArrayList<>());
        setLoadingDone();
        setLoadingMoreDone();
        reachedEndOfPagination = false;
    }

    /**
     * Reloads the state of the view model
     */
    public void reload() {
        clean();
        queryChannels();
    }

//    public QueryChannelListCallback getQueryChannelListCallback() {
//        return queryChannelListCallback;
//    }
//
//    public void setQueryChannelListCallback(QueryChannelListCallback queryChannelListCallback) {
//        this.queryChannelListCallback = queryChannelListCallback;
//    }

    class LazyQueryChannelLiveData<T> extends MutableLiveData<T> {
        protected ChannelListViewModel viewModel;

        @Override
        protected void onActive() {
            super.onActive();
            if (viewModel.initialized.compareAndSet(false, true)) {
                //viewModel.queryChannels();
            }
        }
    }
}