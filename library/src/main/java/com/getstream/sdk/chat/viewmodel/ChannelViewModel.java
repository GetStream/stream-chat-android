package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.Pagination;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.MessageListItemLiveData;
import com.getstream.sdk.chat.view.MessageInputView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.UUID.randomUUID;

/*
 * - store the channel data
 * - load more data
 * -
 */
public class ChannelViewModel extends AndroidViewModel implements MessageInputView.SendMessageListener {
    private static final String TAG = ChannelViewModel.class.getSimpleName();

    private Channel channel;
    private Looper looper;
    private Map<String, Event> typingState;

    private int channelSubscriptionId = 0;
    private AtomicBoolean initialized;
    private AtomicBoolean isLoading;
    private AtomicBoolean isLoadingMore;
    private boolean reachedEndOfPagination;
    private Date lastMarkRead;

    @Override
    protected void onCleared() {
        super.onCleared();

        if (looper != null) {
            looper.interrupt();
        }

        if (channelSubscriptionId != 0) {
            channel.removeEventHandler(channelSubscriptionId);
        }
    }

    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> loadingMore;
    private MutableLiveData<Boolean> failed;
    private MutableLiveData<ChannelState> channelState;
    private LazyQueryChannelLiveData<List<Message>> messages;
    private LiveData<Boolean> anyOtherUsersOnline;
    private LiveData<Number> watcherCount;
    private MutableLiveData<Boolean> hasNewMessages;
    private LazyQueryChannelLiveData<List<User>> typingUsers;
    private LazyQueryChannelLiveData<List<ChannelUserRead>> reads;
    private MutableLiveData<InputType> inputType;
    private MessageListItemLiveData entities;

    public Channel getChannel() {
        return channel;
    }

    public Client client(){
        return StreamChat.getInstance(getApplication());
    }

    public ChannelViewModel(Application application, Channel channel) {
        super(application);
        this.channel = channel;

        initialized = new AtomicBoolean(false);
        isLoading = new AtomicBoolean(false);
        isLoadingMore = new AtomicBoolean(false);
        reachedEndOfPagination = false;

        loading = new MutableLiveData<>(false);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        inputType = new MutableLiveData<>(InputType.DEFAULT);
        hasNewMessages = new MutableLiveData<>(false);


        messages = new LazyQueryChannelLiveData<>();
        messages.viewModel = this;
        messages.setValue(channel.getChannelState().getMessages());

        typingUsers = new LazyQueryChannelLiveData<>();
        typingUsers.viewModel = this;
        typingUsers.setValue(new ArrayList<>());

        reads = new LazyQueryChannelLiveData<>();
        reads.viewModel = this;
        reads.setValue(channel.getChannelState().getReads());

        entities = new MessageListItemLiveData(client().getUser(), messages, typingUsers, reads);
        reads.setValue(channel.getChannelState().getReads());

        typingState = new HashMap<>();

        channelState = new MutableLiveData<>(channel.getChannelState());
        watcherCount = Transformations.map(channelState, ChannelState::getWatcherCount);
        anyOtherUsersOnline = Transformations.map(watcherCount, count -> count != null && count.intValue() > 1);

        Callable<Void> markRead = () -> {
            channel.markRead();
            return null;
        };
        looper = new Looper(markRead);
        looper.start();

        initEventHandlers();
    }

    // region Getter

    public LiveData<ChannelState> getChannelState() {
        return channelState;
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoadingMore() {
        return loadingMore;
    }

    public LiveData<List<ChannelUserRead>> getReads() {
        return reads;
    }

    public LiveData<Number> getWatcherCount() {
        return watcherCount;
    }

    public LiveData<Boolean> getFailed() {
        return failed;
    }

    public LiveData<Boolean> getAnyOtherUsersOnline() {
        return anyOtherUsersOnline;
    }

    public LiveData<InputType> getInputType() {
        return inputType;
    }

    public LiveData<List<User>> getTypingUsers() {
        return typingUsers;
    }

    // endregion

    private boolean setLoading(){
        if (isLoading.compareAndSet(false, true)) {
            loading.postValue(true);
            return true;
        }
        return false;
    }

    private void setLoadingDone(){
        if (isLoading.compareAndSet(true, false))
            loading.postValue(false);
    }

    private boolean setLoadingMore(){
        if (isLoadingMore.compareAndSet(false, true)) {
            loadingMore.postValue(true);
            return true;
        }
        return false;
    }

    private void setLoadingMoreDone(){
        if (isLoadingMore.compareAndSet(true, false))
            loadingMore.postValue(false);
    }

    public void setInputType(InputType inputType) {
        this.inputType.postValue(inputType);
    }

    public void markLastMessageRead() {
        // this prevents infinite loops with mark read commands
        Message message = this.channel.getChannelState().getLastMessage();
        if (lastMarkRead == null || message.getCreatedAt().getTime() > lastMarkRead.getTime()) {
            looper.markRead();
            lastMarkRead = message.getCreatedAt();
        }
    }

    private void initEventHandlers() {
        channelSubscriptionId = channel.addEventHandler(new ChatChannelEventHandler() {
            @Override
            public void onMessageNew(Event event) {
                upsertMessage(event.getMessage());
                channelState.postValue(channel.getChannelState());
            }

            @Override
            public void onUserWatchingStart(Event event) {
                channelState.postValue(event.getChannel().getChannelState());
            }

            @Override
            public void onUserWatchingStop(Event event) {
                channelState.postValue(event.getChannel().getChannelState());
            }

            @Override
            public void onChannelUpdated(Event event) {
                channelState.postValue(event.getChannel().getChannelState());
            }

            @Override
            public void onMessageUpdated(Event event) {
                updateMessage(event.getMessage());
            }

            @Override
            public void onMessageDeleted(Event event) {
                deleteMessage(event.getMessage());
            }

            @Override
            public void onMessageRead(Event event) {
                Log.i(TAG, "Message read by " + event.getUser().getId());
                List<ChannelUserRead> readsCopy = new ArrayList<>();
                for (ChannelUserRead r : reads.getValue()) {
                    if (!r.getUser().equals(event.getUser())) {
                        readsCopy.add(r);
                    }
                }
                ChannelUserRead newRead = new ChannelUserRead();
                newRead.setUser(event.getUser());
                newRead.setLastRead(event.getCreatedAt());
                readsCopy.add(newRead);
                reads.postValue(readsCopy);
            }

            @Override
            public void onReactionNew(Event event) {
                updateMessage(event.getMessage());
            }

            @Override
            public void onReactionDeleted(Event event) {
                updateMessage(event.getMessage());
            }

            @Override
            public void onTypingStart(Event event) {
                if (client().fromCurrentUser(event)) return;
                User user = event.getUser();
                typingState.put(user.getId(), event);
                typingUsers.postValue(getCleanedTypingUsers());
            }

            @Override
            public void onTypingStop(Event event) {
                if (client().fromCurrentUser(event)) return;
                User user = event.getUser();
                typingState.remove(user.getId());
                typingUsers.postValue(getCleanedTypingUsers());
            }
        });
    }

    private boolean upsertMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        Log.d(TAG,"messages Count:" + messages.getValue().size());
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(message);
        Boolean updated = index != -1;
        if (updated) {
            messagesCopy.set(index, message);
        } else {
            messagesCopy.add(message);
        }
        Log.d(TAG,"New messages Count:" + messagesCopy.size());
        messages.postValue(messagesCopy);
        Log.d(TAG,"New messages Count:" + messages.getValue().size());
        return updated;
    }

    private boolean updateMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(message);
        boolean updated = index != -1;
        if (updated) {
            messagesCopy.set(index, message);
            messages.postValue(messagesCopy);
        }
        return updated;
    }

    private boolean deleteMessage(Message message) {
        List<Message> messagesCopy = messages.getValue();
        boolean removed = messagesCopy.remove(message);
        messages.postValue(messagesCopy);
        return removed;
    }

    private void addMessage(Message message) {
        Log.d(TAG,"Add Message");
        List<Message> messagesCopy = messages.getValue();
        messagesCopy.add(message);
        messages.postValue(messagesCopy);
    }

    private void addMessages(List<Message> newMessages) {
        Log.d(TAG,"Add Messages");
        List<Message> messagesCopy = messages.getValue();
        if (messagesCopy == null) {
            messagesCopy = new ArrayList<>();
        }

        // iterate in reverse-order since newMessages is assumed to be ordered by created_at DESC
        for (int i = newMessages.size() - 1; i >= 0; i--) {
            Message message = newMessages.get(i);
            int index = messagesCopy.indexOf(message);
            if (index == -1) {
                messagesCopy.add(0, message);
            } else {
                messagesCopy.set(index, message);
            }
        }
        messages.postValue(messagesCopy);
    }

    private void queryChannel() {
        int limit = 10; // Constant.DEFAULT_LIMIT
        if (!setLoading()) return;

        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(limit);

        channel.query(
                request,
                new QueryChannelCallback() {
                    @Override
                    public void onSuccess(ChannelState response) {
                        initialized.set(true);
                        setLoadingDone();
                        if (response.getMessages().size() < limit) {
                            reachedEndOfPagination = true;
                        }
                        addMessages(response.getMessages());
                        channelState.postValue(channel.getChannelState());
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        initialized.set(true);
                        setLoadingDone();
                    }
                }
        );
    }

    public void loadMore() {
        if (isLoading.get()) {
            Log.i(TAG, "already loading, skip loading more");
            return;
        }
        if (reachedEndOfPagination) {
            Log.i(TAG, "already reached end of pagination, skip loading more");
            return;
        }
        if (!setLoadingMore()) {
            Log.i(TAG, "already loading next page, skip loading more");
            return;
        }

        loadingMore.setValue(true);
        Log.i(TAG, String.format("Loading %d more messages, oldest message is %s", Constant.DEFAULT_LIMIT,  channel.getChannelState().getOldestMessageId()));

        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(Pagination.LESS_THAN, channel.getChannelState().getOldestMessageId(), Constant.DEFAULT_LIMIT);

        channel.query(
                request,
                new QueryChannelCallback() {
                    @Override
                    public void onSuccess(ChannelState response) {
                        List<Message> newMessages = new ArrayList<>(response.getMessages());
                        // used to modify the scroll behaviour...
                        entities.setIsLoadingMore(true);
                        addMessages(newMessages);
                        if (newMessages.size() < Constant.DEFAULT_LIMIT)
                            reachedEndOfPagination = true;
                        setLoadingMoreDone();
                        loadingMore.setValue(false);
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        loadingMore.setValue(false);
                        setLoadingMoreDone();
                    }
                }
        );
    }

    @Override
    public void onSendMessage(Message message, MessageCallback callback) {
        Log.i(TAG, "onSendMessage handler called at viewmodel level");
        // immediately add the message
        message.setUser(client().getUser());
        message.setCreatedAt(new Date());
        message.setType("regular");
        String clientSideID = client().getUserId() + "-" + randomUUID().toString();
        message.setId(clientSideID);
        addMessage(message);

        // afterwards send the request
        channel.sendMessage(message,
                new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        callback.onSuccess(response);
                        Message responseMessage = response.getMessage();
                        Log.i(TAG, "onSuccess event for sending the message");
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        callback.onError(errMsg, errCode);
                        //binding.messageInput.setEnabled(true);
                    }
                });
    }


    public MessageListItemLiveData getEntities() {
        return entities;
    }

    private List<User> getCleanedTypingUsers() {
        List<User> users = new ArrayList<>();
        long now = new Date().getTime();
        for (Event event: typingState.values()){
            // constants
            long TYPING_TIMEOUT = 10000;
            if (now - event.getCreatedAt().getTime() < TYPING_TIMEOUT) {
                users.add(event.getUser());
            }
        }
        return users;
    }

    /**
     * Cleans up the typing state by removing typing users that did not send
     * typing.stop event for long time
     */
    private void cleanupTypingUsers() {
        List<User> prev = typingUsers.getValue();
        List<User> cleaned = getCleanedTypingUsers();
        if (prev != null && cleaned != null && prev.size() != cleaned.size()) {
            typingUsers.postValue(getCleanedTypingUsers());
        }
    }

    public MutableLiveData<Boolean> getHasNewMessages() {
        return hasNewMessages;
    }

    public void setHasNewMessages(Boolean hasNewMessages) {
        this.hasNewMessages.postValue(hasNewMessages);
    }

    /**
     * Service thread to keep state neat and clean. Ticks twice per second
     */
    class Looper extends Thread {
        private Callable<Void> markReadFn;
        private AtomicInteger pendingMarkReadRequests;

        public Looper(Callable<Void> markReadFn) {
            this.markReadFn = markReadFn;
            pendingMarkReadRequests = new AtomicInteger(0);
        }

        public void markRead(){
            pendingMarkReadRequests.incrementAndGet();
        }

        private void throttledMarkRead() {
            int pendingCalls = pendingMarkReadRequests.get();
            if (pendingCalls == 0) {
                return;
            }
            try {
                markReadFn.call();
            } catch (Exception e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            pendingMarkReadRequests.compareAndSet(pendingCalls, 0);
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                cleanupTypingUsers();
                throttledMarkRead();

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }

    class LazyQueryChannelLiveData<T> extends MutableLiveData<T> {

        protected ChannelViewModel viewModel;

        @Override
        protected void onActive() {
            super.onActive();
            if (viewModel.initialized.compareAndSet(false, true)) {
                viewModel.queryChannel();
            }
        }
    }
}