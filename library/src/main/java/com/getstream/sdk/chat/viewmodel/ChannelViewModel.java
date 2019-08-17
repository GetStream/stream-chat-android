package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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
import com.getstream.sdk.chat.utils.EntityLiveData;
import com.getstream.sdk.chat.view.MessageInputView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static android.text.format.DateUtils.getRelativeTimeSpanString;
import static java.util.UUID.randomUUID;

/*
 * - store the channel data
 * - load more data
 * -
 */
public class ChannelViewModel extends AndroidViewModel implements MessageInputView.SendMessageListener {
    private final String TAG = ChannelViewModel.class.getSimpleName();

    private Channel channel;
    private Looper looper;
    private Map<String, Event> typingState;

    // TODO: channelState should be removed!
    public ChannelState channelState;
    private int channelSubscriptionId = 0;

    // constants
    private long TYPING_TIMEOUT = 10000;

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
    private MutableLiveData<Boolean> online;
    private MutableLiveData<String> channelName;
    private MutableLiveData<List<Message>> messages;
    private MutableLiveData<Boolean> anyOtherUsersOnline;
    private MutableLiveData<Number> watcherCount;
    private MutableLiveData<String> lastActiveString;
    private MutableLiveData<Boolean> hasNewMessages;
    private MutableLiveData<List<User>> typingUsers;
    private MutableLiveData<List<ChannelUserRead>> reads;
    private MutableLiveData<Boolean> endOfPagination;
    private MutableLiveData<InputType> inputType;
    private EntityLiveData entities;

    public Channel getChannel() {
        return channel;
    }

    public Client client(){
        return StreamChat.getInstance(getApplication());
    }

    public ChannelViewModel(Application application, Channel channel) {
        super(application);
        this.channel = channel;
        this.channelState = channel.getChannelState();

        loading = new MutableLiveData<>(false);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        online = new MutableLiveData<>(true);
        inputType = new MutableLiveData<>(InputType.DEFAULT);
        endOfPagination = new MutableLiveData<>(false);
        hasNewMessages = new MutableLiveData<>(false);
        anyOtherUsersOnline = new MutableLiveData<>();
        channelName = new MutableLiveData<>();

        messages = new MutableLiveData<>(channelState.getMessages());
        typingUsers = new MutableLiveData<>(new ArrayList<>());
        reads = new MutableLiveData<>(channelState.getReads());

        entities = new EntityLiveData(client().getUser(), messages, typingUsers, reads);
        watcherCount = new MutableLiveData<>();

        lastActiveString = new MutableLiveData<>();
        typingState = new HashMap<>();

        Callable<Void> markRead = () -> {
            channel.markRead();
            return null;
        };
        looper = new Looper(markRead);
        looper.start();

        updateChannelHead();
        this.queryChannel();
    }

    // updates live data about channel status (useful to render header and similar UI)
    private void updateChannelHead() {
        Date lastActive = channelState.getLastActive();
        String humanizedDate = getRelativeTimeSpanString(lastActive.getTime()).toString();
        lastActiveString.postValue(humanizedDate);
        anyOtherUsersOnline.postValue(channelState.anyOtherUsersOnline());
        channelName.postValue(channelState.getChannelNameOrMembers());
    }

    // region Getter
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

    public LiveData<Boolean> getEndOfPagination() {
        return endOfPagination;
    }

    public LiveData<String> getChannelName() {
        return channelName;
    }

    public LiveData<Boolean> getOnline() {
        return online;
    }

    public LiveData<Boolean> getFailed() {
        return failed;
    }

    public LiveData<Boolean> getAnyOtherUsersOnline() {
        return anyOtherUsersOnline;
    }

    public LiveData<String> getLastActiveString() {
        return lastActiveString;
    }

    public LiveData<InputType> getInputType() {
        return inputType;
    }

    public LiveData<List<User>> getTypingUsers() {
        return typingUsers;
    }

    // endregion
    public void setInputType(InputType inputType) {
        this.inputType.postValue(inputType);
    }


    public void markRead() {
        looper.markRead();
    }

    private void initEventHandlers() {
        channelSubscriptionId = channel.addEventHandler(new ChatChannelEventHandler() {
            @Override
            public void onAnyEvent(Event event) {
                Number watcherCount = event.getWatcherCount();
                if (watcherCount != null) {
                    ChannelViewModel.this.watcherCount.postValue(watcherCount);
                    updateChannelHead();
                }
                Log.d(TAG, "New Event: " + event.getType());
            }

            @Override
            public void onMessageNew(Event event) {
                Log.i(TAG, "onMessageNew for channelviewmodel" + event.getMessage().getText());
                upsertMessage(event.getMessage());
                if (!client().fromCurrentUser(event.getMessage())){
                    markRead();
                }
                updateChannelHead();
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

    public void removeEventHandler() {
        if (channelSubscriptionId == 0) return;
        channel.removeEventHandler(channelSubscriptionId);
        channelSubscriptionId = 0;
    }

    public boolean upsertMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(message);
        Boolean updated = index != -1;
        if (updated) {
            messagesCopy.set(index, message);
        } else {
            messagesCopy.add(message);
        }

        messages.postValue(messagesCopy);
        return updated;
    }

    public boolean updateMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(message);
        Boolean updated = index != -1;
        if (updated) {
            messagesCopy.set(index, message);
            messages.postValue(messagesCopy);
        }
        return updated;
    }

    public boolean deleteMessage(Message message) {
        List<Message> messagesCopy = messages.getValue();
        Boolean removed = messagesCopy.remove(message);
        return removed;
    }

    public void addMessage(Message message) {
        List<Message> newMessages = new ArrayList<Message>();
        newMessages.add(message);
        addMessages(newMessages);
    }

    public void addMessages(List<Message> newMessages) {
        List<Message> messagesCopy = messages.getValue();
        if (messagesCopy == null) {
            messagesCopy = new ArrayList<>();
        }
        messagesCopy.addAll(newMessages);
        messages.postValue(messagesCopy);
    }

    private void queryChannel() {
        loading.postValue(true);
        int limit = 10; // Constant.DEFAULT_LIMIT

        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(limit);

        channel.query(
            request,
            new QueryChannelCallback() {
                @Override
                public void onSuccess(ChannelState response) {
                    loading.postValue(false);
                    Log.i(TAG, response.getMessages().size() + " more messages loaded");
                    if (response.getMessages().size() < limit) {
                        endOfPagination.postValue(true);
                    }
                    addMessages(response.getMessages());
                    initEventHandlers();
                    markRead();
                    updateChannelHead();
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    loading.postValue(false);
                }
            }
        );
    }

    public void loadMore() {
        // don't load more if the channel state is empty or if we are already loading more
        if (loadingMore.getValue() || channelState.getOldestMessageId() == null) {
            return;
        }
        loadingMore.setValue(true);

        Log.i(TAG, String.format("Loading %d more messages, oldest message is %s", Constant.DEFAULT_LIMIT,  channelState.getOldestMessageId()));

        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(Pagination.LESS_THAN, channelState.getOldestMessageId(), Constant.DEFAULT_LIMIT);

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
                            endOfPagination.setValue(true);
                        loadingMore.postValue(false);
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        loadingMore.postValue(false);
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
        String clientSideID = this.client().getUserId() + "-" + randomUUID().toString();
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


    public EntityLiveData getEntities() {
        return entities;
    }

    private List<User> getCleanedTypingUsers() {
        List<User> users = new ArrayList<>();
        long now = new Date().getTime();
        for (Event event: typingState.values()){
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
}
