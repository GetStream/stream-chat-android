package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.os.Handler;

import com.getstream.sdk.chat.LifecycleHandler;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.StreamLifecycleObserver;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.storage.OnQueryListener;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;
import com.getstream.sdk.chat.utils.RetryPolicy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.QueryChannelsRequest;
import io.getstream.chat.android.client.api.models.QuerySort;
import io.getstream.chat.android.client.call.Call;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.ConnectedEvent;
import io.getstream.chat.android.client.events.MessageReadEvent;
import io.getstream.chat.android.client.events.NewMessageEvent;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.FilterObject;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.android.client.utils.observable.Subscription;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static com.getstream.sdk.chat.utils.Utils.removeIf;

public class ChannelListViewModel extends AndroidViewModel implements LifecycleHandler {

    protected final String TAG = ChannelListViewModel.class.getSimpleName();

    protected final MutableLiveData<List<Channel>> channels = new ChannelsLiveData<>();

    protected MutableLiveData<Boolean> loading;
    protected MutableLiveData<Boolean> loadingMore;

    protected FilterObject filter;
    protected QuerySort sort;

    private boolean reachedEndOfPagination;
    protected AtomicBoolean initialized;
    protected AtomicBoolean isLoading;
    protected AtomicBoolean isLoadingMore;
    protected boolean queryChannelDone;
    protected int pageSize;
    protected Handler retryLooper;

    protected QueryChannelListCallback queryChannelListCallback;

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public void setRetryPolicy(RetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy;
    }

    protected RetryPolicy retryPolicy;

    public ChannelListViewModel(@NonNull Application application) {
        super(application);

        StreamChat.getLogger().logD(this, "instance created");

        isLoading = new AtomicBoolean(false);
        isLoadingMore = new AtomicBoolean(false);
        initialized = new AtomicBoolean(false);

        reachedEndOfPagination = false;
        pageSize = 25;

        loading = new MutableLiveData<>(true);
        loadingMore = new MutableLiveData<>(false);

        //channels = new LazyQueryChannelLiveData<>();
        //channels.viewModel = this;
        sort = new QuerySort().desc("last_message_at");

        setupConnectionRecovery();
        initEventHandlers();

        new StreamLifecycleObserver(this);
        retryLooper = new Handler();

        // default retry policy is to retry the request 100 times

        //TODO: llc: put back retry policy

//        retryPolicy = new RetryPolicy() {
//            @Override
//            public boolean shouldRetry(ClientOld client, Integer attempt, String errMsg, int errCode) {
//                return attempt < 100;
//            }
//
//            @Override
//            public Integer retryTimeout(ClientOld client, Integer attempt, String errMsg, int errCode) {
//                return Math.min(500 * (attempt * attempt + 1), 30000);
//            }
//        };
    }

    public LiveData<List<Channel>> getChannels() {
        return channels;
    }

    protected void setChannels(List<Channel> channels) {
        // - offline loads first
        // - after that we query the API and load more channels
        // - it's possible that the offline results no longer match the query (so we should remove them)
        List<Channel> newChannels = new ArrayList<>();
        for (Channel chan : channels) {
            newChannels.add(chan);
        }
        updateChannelsLiveData(newChannels);
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

        StreamChat.getLogger().logD(this, "onCleared");

        if (subscription != null) subscription.unsubscribe();
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

    protected boolean setLoadingMore() {
        if (isLoadingMore.compareAndSet(false, true)) {
            loadingMore.postValue(true);
            return true;
        }
        return false;
    }

    protected void setLoadingMoreDone() {
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
            StreamChat.getLogger().logE(this, "setChannelFilter on an already initialized channel will reload the view model");
            reload();
        }
    }

    /**
     * hides the channel from queryChannels for the user until a message is added and remove from the current list of channels
     */
    public Call<Unit> hideChannel(@NonNull String channelType, @NonNull String channelId, boolean clearHistory) {

        return StreamChat.getInstance().hideChannel(channelType, channelId, clearHistory).map(unit -> {
            deleteChannel(channelType + ":" + channelId);
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

        return StreamChat.getInstance().showChannel(channelType, channelId).map(unit -> {
            deleteChannel(channelType + ":" + channelId);
            return null;
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
        StreamChat.getLogger().logD(this, "resume");
        if (!initialized.get() || !StreamChat.getInstance().isSocketConnected())
            setLoading();
    }

    @Override
    public void stopped() {
        StreamChat.getLogger().logD(this, "stopped");
    }

    protected void setupConnectionRecovery() {
        //TODO: llc check if recovery required
//        recoverySubscriptionId = client().addEventHandler(new ChatEventHandler() {
//            @Override
//            public void onConnectionRecovered(Event event) {
//                StreamChat.getLogger().logI(this, "onConnectionRecovered");
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

    private Subscription subscription;//notification.mark_read //message.read

    protected void initEventHandlers() {
        subscription = StreamChat.getInstance().events().subscribe(event -> {

            if (event instanceof NewMessageEvent) {
                NewMessageEvent e = (NewMessageEvent) event;
                String cid = e.getCid();
                Message message = e.message;

                Channel ch = getChannelByCid(cid);

                if (ch != null) {
                    Channel newChannel = copy(ch);
                    message.setChannel(newChannel);
                    newChannel.setUpdatedAt(message.getCreatedAt());
                    newChannel.getMessages().add(message);
                    newChannel.setLastMessageAt(message.getCreatedAt());

                    updateChannel(ch, newChannel, true);
                }


            } else if (event instanceof MessageReadEvent) {
                MessageReadEvent e = (MessageReadEvent) event;
                String cid = e.getCid();

                Channel ch = getChannelByCid(cid);

                if (ch != null) {
                    Channel newChannel = copy(ch);
                    User user = e.getUser();
                    Date date = e.getReceivedAt();

                    LlcMigrationUtils.updateReadState(newChannel, user, date);

                    updateChannel(ch, newChannel, false);
                }
            }

            return null;
        });
    }

    private Channel getChannelByCid(String cid) {
        List<Channel> list = channels.getValue();
        for (Channel ch : list)
            if (cid.equals(ch.getCid()))
                return ch;

        return null;
    }

    private int lastIndexOf(String cid) {
        List<Channel> list = channels.getValue();
        for (int i = 0; i < list.size(); i++)
            if (cid.equals(list.get(i).getCid()))
                return i;

        return -1;
    }

    private void updateChannel(Channel oldChannel, Channel newChannel, boolean moveToTop) {
        List<Channel> channelsValue = channels.getValue();

        int idx = lastIndexOf(oldChannel.getCid());

        if (idx != -1) {
            if (moveToTop) {
                channelsValue.remove(idx);
                channelsValue.add(0, newChannel);
            } else {
                channelsValue.set(idx, newChannel);
            }
            updateChannelsLiveData(channelsValue);
        }
    }

    private Channel copy(Channel channel) {
        Channel copy = new Channel();
        copy.setCid(channel.getCid());
        copy.getMessages().addAll(channel.getMessages());
        copy.setUpdatedAt(channel.getUpdatedAt());
        copy.getRead().addAll(channel.getRead());
        copy.getExtraData().putAll(channel.getExtraData());
        copy.setLastMessageAt(channel.getLastMessageAt());
        copy.setId(channel.getId());
        copy.setType(channel.getType());
        copy.setCreatedAt(channel.getCreatedAt());
        copy.setDeletedAt(channel.getDeletedAt());
        copy.setMembers(channel.getMembers());
        copy.setWatcherCount(channel.getWatcherCount());
        copy.setCreatedBy(channel.getCreatedBy());

        return copy;
    }

    protected void upsertChannel(Channel channel) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }
        channelCopy.add(0, channel);
        updateChannelsLiveData(channelCopy);
    }

    public boolean deleteChannel(String cid) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }

        boolean removed = removeIf(channelCopy, value -> cid.equals(value.getCid()));

        updateChannelsLiveData(channelCopy);
        return removed;
    }

    public void addChannels(List<Channel> newChannelsState) {
        List<Channel> channelCopy = channels.getValue();
        if (channelCopy == null) {
            channelCopy = new ArrayList<>();
        }

        // - offline loads first
        // - after that we query the API and load more channels
        // - it's possible that the offline results no longer match the query (so we should remove them)

        List<Channel> newChannels = new ArrayList<>();
        for (Channel chan : newChannelsState) {
            newChannels.add(chan);
        }
        channelCopy.addAll(newChannels);
        updateChannelsLiveData(channelCopy);
    }

    private void updateChannelsLiveData(List<Channel> channelCopy) {
        channels.postValue(channelCopy);
    }


    protected void queryChannelsInner(int attempt) {

        QueryChannelsRequest request = new QueryChannelsRequest(filter, 0, pageSize, sort, 20);

//        QueryChannelListCallback queryCallback = new QueryChannelListCallback() {
//            @Override
//            public void onSuccess(QueryChannelsResponse response) {
//                queryChannelDone = true;
//                setLoadingDone();
//
//                StreamChat.getLogger().logI(this, "onSendMessageSuccess for loading the channels");
//                // remove the offline channels before adding the new ones
//                setChannels(response.getChannelStates());
//
//                if (response.getChannelStates().size() < pageSize) {
//                    StreamChat.getLogger().logI(this, "reached end of pagination");
//                    reachedEndOfPagination = true;
//                }
//
//                if (queryChannelListCallback != null) {
//                    queryChannelListCallback.onSuccess(response);
//                }
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                StreamChat.getLogger().logE(this, "onError for loading the channels " + errMsg);
//                Boolean shouldRetry = retryPolicy.shouldRetry(client(), attempt, errMsg, errCode);
//                if (!shouldRetry) {
//                    StreamChat.getLogger().logE(this, "tried more than 100 times, give up now");
//                    return;
//                }
//                if (!StreamChat.getInstance().isSocketConnected()) {
//                    return;
//                }
//                int sleep = retryPolicy.retryTimeout(client(), attempt, errMsg, errCode);
//                StreamChat.getLogger().logD(this, "retrying in " + sleep);
//                retryLooper.postDelayed(() -> {
//                    queryChannelsInner(attempt + 1);
//                }, sleep);
//
//                if (queryChannelListCallback != null) {
//                    queryChannelListCallback.onError(errMsg, errCode);
//                }
//            }
//        };

        StreamChat.getInstance().queryChannels(request).enqueue(result -> {

            if (result.isSuccess()) {
                queryChannelDone = true;
                setLoadingDone();
                List<Channel> data = result.data();
                setChannels(data);

                if (data.size() < pageSize) {
                    reachedEndOfPagination = true;
                }

            } else {
                //TODO: llc add retry
            }

            return null;
        });

        //client().queryChannels(request, queryCallback);
    }

    /**
     * query channels
     *
     * @param callback the result callback
     */
    public void queryChannels(OnQueryListener<List<ChannelState>> callback) {
        StreamChat.getLogger().logI(this, "queryChannels for loading the channels");
//        if (!setLoading()) {
//            StreamChat.getLogger().logI(this, "already loading, skip queryChannels");
//            return;
//        }
//        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
//                .withLimit(pageSize)
//                .withMessageLimit(20);
//        client().getStorage().selectChannelStates(request.query().getId(), 100, new OnQueryListener<List<ChannelState>>() {
//            @Override
//            public void onSuccess(List<ChannelState> channelStates) {
//                if (channels != null && channelStates != null)
//                    addChannels(channelStates);
//                callback.onSuccess(channelStates);
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                StreamChat.getLogger().logW(this, String.format("Failed to read channel list from offline storage, error %s", e.toString()));
//                callback.onFailure(e);
//            }
//        });
        queryChannelsInner(0);
    }

    public void queryChannels() {
        queryChannels(new OnQueryListener<List<ChannelState>>() {
            @Override
            public void onSuccess(List<ChannelState> channelStates) {
                StreamChat.getLogger().logI(this, "Read from local cache...");
            }

            @Override
            public void onFailure(Exception e) {
                StreamChat.getLogger().logE(this, e.getLocalizedMessage());
            }
        });
    }

    /**
     * loads more channels, use this to load a previous page
     */
    public void loadMore() {
        if (!StreamChat.getInstance().isSocketConnected()) return;

        if (isLoading.get()) {
            return;
        }
        if (reachedEndOfPagination) {
            return;
        }
        if (!setLoadingMore()) {
            return;
        }

        QueryChannelsRequest request = new QueryChannelsRequest(filter, 0, pageSize, sort, 20);

        if (channels.getValue() != null)
            request = request.withOffset(channels.getValue().size());

        StreamChat.getInstance().queryChannels(request).enqueue(new Function1<Result<List<Channel>>, Unit>() {
            @Override
            public Unit invoke(Result<List<Channel>> result) {

                setLoadingMoreDone();

                if (result.isSuccess()) {
                    addChannels(result.data());
                    reachedEndOfPagination = result.data().size() < pageSize;
                }

                return null;
            }
        });

//        client().queryChannels(request, new QueryChannelListCallback() {
//            @Override
//            public void onSuccess(QueryChannelsResponse response) {
//                StreamChat.getLogger().logI(this, "onSendMessageSuccess for loading more channels");
//                setLoadingMoreDone();
//                addChannels(response.getChannelStates());
//                reachedEndOfPagination = response.getChannelStates().size() < pageSize;
//
//                if (reachedEndOfPagination)
//                    StreamChat.getLogger().logI(this, "reached end of pagination");
//                callback.onSuccess(response);
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                StreamChat.getLogger().logE(this, "onError for loading the channels" + errMsg);
//                setLoadingMoreDone();
//                callback.onError(errMsg, errCode);
//            }
//        });
    }

    protected void clean() {
        retryLooper.removeCallbacksAndMessages(null);
        initialized.set(true);
        updateChannelsLiveData(new ArrayList<>());
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

    public QueryChannelListCallback getQueryChannelListCallback() {
        return queryChannelListCallback;
    }

    public void setQueryChannelListCallback(QueryChannelListCallback queryChannelListCallback) {
        this.queryChannelListCallback = queryChannelListCallback;
    }

    class ChannelsLiveData<T> extends MutableLiveData<T> {

        private Subscription subscribe;

        @Override
        public void postValue(T value) {
            if (value == null) {

            }
            super.postValue(value);
        }

        @Override
        public void setValue(T value) {
            if (value == null) {

            }
            super.setValue(value);
        }


        @Override
        protected void onActive() {
            ChatClient client = StreamChat.getInstance();
            if (client.isSocketConnected()) {
                queryChannels();
            } else {
                subscribe = client.events().subscribe(new Function1<ChatEvent, Unit>() {
                    @Override
                    public Unit invoke(ChatEvent event) {

                        //TODO: llc ignore next ConnectedEvent

                        if (event instanceof ConnectedEvent) {
                            queryChannels();
                        }

                        return null;
                    }
                });
            }
        }

        @Override
        protected void onInactive() {
            if (subscribe != null) subscribe.unsubscribe();
        }

        //        protected ChannelListViewModel viewModel;
//
//        @Override
//        protected void onActive() {
//            super.onActive();
//            if (viewModel.initialized.compareAndSet(false, true)) {
//                viewModel.queryChannels();
//            }
//        }
    }
}