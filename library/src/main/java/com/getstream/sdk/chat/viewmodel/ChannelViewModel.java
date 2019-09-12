package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.getstream.sdk.chat.LifecycleHandler;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.StreamLifecycleObserver;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.enums.MessageStatus;
import com.getstream.sdk.chat.enums.Pagination;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.storage.Storage;
import com.getstream.sdk.chat.storage.Sync;
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
public class ChannelViewModel extends AndroidViewModel implements MessageInputView.SendMessageListener, LifecycleHandler {
    private static final String TAG = ChannelViewModel.class.getSimpleName();

    private Channel channel;
    private Looper looper;
    private Map<String, Event> typingState;

    private int channelSubscriptionId = 0;
    private int threadParentPosition = 0;
    private AtomicBoolean initialized;
    private AtomicBoolean isLoading;
    private AtomicBoolean isLoadingMore;
    private boolean reachedEndOfPagination;
    private boolean reachedEndOfPaginationThread;
    private Date lastMarkRead;

    private Date lastKeystrokeAt;

    private MutableLiveData<Boolean> loading;
    private MutableLiveData<Boolean> messageListScrollUp;
    private MutableLiveData<Boolean> loadingMore;
    private MutableLiveData<Boolean> failed;
    private MutableLiveData<Message> editMessage;
    private MutableLiveData<Message> threadParentMessage;
    private MutableLiveData<ChannelState> channelState;
    private LazyQueryChannelLiveData<List<Message>> messages;
    private LazyQueryChannelLiveData<List<Message>> threadMessages;
    private LiveData<Boolean> anyOtherUsersOnline;
    private LiveData<Number> watcherCount;
    private MutableLiveData<Boolean> hasNewMessages;
    private LazyQueryChannelLiveData<List<User>> typingUsers;
    private LazyQueryChannelLiveData<Map<String, ChannelUserRead>> reads;
    private MutableLiveData<InputType> inputType;
    private MessageListItemLiveData entities;

    public ChannelViewModel(Application application, Channel channel) {
        super(application);
        this.channel = channel;

        initialized = new AtomicBoolean(false);
        isLoading = new AtomicBoolean(false);
        isLoadingMore = new AtomicBoolean(false);
        reachedEndOfPagination = false;

        loading = new MutableLiveData<>(false);
        threadParentMessage = new MutableLiveData<>(null);
        messageListScrollUp = new MutableLiveData<>(false);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        inputType = new MutableLiveData<>(InputType.DEFAULT);
        hasNewMessages = new MutableLiveData<>(false);

        messages = new LazyQueryChannelLiveData<>();
        messages.viewModel = this;
        messages.setValue(channel.getChannelState().getMessages());

        threadMessages = new LazyQueryChannelLiveData<>();
        threadMessages.viewModel = this;
        threadMessages.setValue(null);

        // fetch offline messages
        client().storage().selectChannelState(channel.getCid(), new Storage.OnQueryListener<ChannelState>() {
            @Override
            public void onSuccess(ChannelState channelState) {
                Log.i(TAG, "Read messages from local cache...");
                if (channelState != null) {
                    messages.setValue(channelState.getMessages());
                }
            }

            @Override
            public void onFailure(Exception e) {
                // TODO
            }
        });

        typingUsers = new LazyQueryChannelLiveData<>();
        typingUsers.viewModel = this;
        typingUsers.setValue(new ArrayList<>());

        reads = new LazyQueryChannelLiveData<>();
        reads.viewModel = this;
        reads.setValue(channel.getChannelState().getReadsByUser());

        entities = new MessageListItemLiveData(client().getUser(), messages, threadMessages, typingUsers, reads);
        reads.setValue(channel.getChannelState().getReadsByUser());

        typingState = new HashMap<>();
        editMessage = new MutableLiveData<>();

        channelState = new MutableLiveData<>(channel.getChannelState());
        watcherCount = Transformations.map(channelState, ChannelState::getWatcherCount);
        anyOtherUsersOnline = Transformations.map(watcherCount, count -> count != null && count.intValue() > 1);

        Callable<Void> markRead = () -> {
            channel.markRead();
            return null;
        };
        looper = new Looper(markRead);
        looper.start();

        new StreamLifecycleObserver(this);
        initEventHandlers();
        setupConnectionRecovery();
    }

    public Channel getChannel() {
        return channel;
    }

    public Client client() {
        return StreamChat.getInstance(getApplication());
    }

    // region Getter

    public LiveData<ChannelState> getChannelState() {
        return channelState;
    }

    public LiveData<List<Message>> getMessages() {
        return isThread() ? threadMessages : messages;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Boolean> getLoadingMore() {
        return loadingMore;
    }

    public LiveData<Map<String, ChannelUserRead>> getReads() {
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

    public void setInputType(InputType inputType) {
        switch (inputType) {
            case SELECT:
                if (this.inputType.getValue() == InputType.DEFAULT)
                    this.inputType.postValue(inputType);
                break;
            default:
                this.inputType.postValue(inputType);
                break;
        }
    }

    public LiveData<List<User>> getTypingUsers() {
        return typingUsers;
    }

    public LiveData<Message> getEditMessage() {
        return editMessage;
    }

    public void setEditMessage(Message editMessage) {
        setInputType(InputType.EDIT);
        this.editMessage.postValue(editMessage);
    }

    public LiveData<Boolean> getMessageListScrollUp() {
        return messageListScrollUp;
    }

    public void setMessageListScrollUp(Boolean messageListScrollUp) {
        this.messageListScrollUp.postValue(messageListScrollUp);
    }

    // region Thread
    public LiveData<Message> getThreadParentMessage() {
        return threadParentMessage;
    }

    public void setThreadParentMessage(Message threadParentMessage_) {
        configThread(threadParentMessage_);
        threadParentMessage.postValue(threadParentMessage_);
    }

    public int getThreadParentPosition() {
        return threadParentPosition;
    }

    public void setThreadParentPosition(int threadParentPosition) {
        if (isThread()) return;
        this.threadParentPosition = threadParentPosition;
    }

    public boolean isThread() {
        return threadParentMessage.getValue() != null;
    }

    private void configThread(Message threadParentMessage_) {
        if (threadParentMessage_.getReplyCount() == 0) {
            reachedEndOfPaginationThread = true;
            threadMessages.postValue(new ArrayList<Message>() {
                {
                    add(threadParentMessage_);
                }
            });
        } else {
            channel.getReplies(threadParentMessage_.getId(), String.valueOf(10), null, new GetRepliesCallback() {
                @Override
                public void onSuccess(GetRepliesResponse response) {
                    List<Message> newMessages = new ArrayList<>(response.getMessages());
                    newMessages.add(0, threadParentMessage_);
                    reachedEndOfPaginationThread = newMessages.size() < 10;
                    threadMessages.postValue(newMessages);
                }

                @Override
                public void onError(String errMsg, int errCode) {

                }
            });
        }
    }

    public void initThread() {
        threadParentMessage.postValue(null);
        threadMessages.postValue(null);
        messages.postValue(channel.getChannelState().getMessages());
        reachedEndOfPaginationThread = false;
    }
    // endregion

    // endregion

    private String getThreadOldestMessageId() {
        List<Message> messages = threadMessages.getValue();
        if (messages != null && messages.size() > 1)
            return threadMessages.getValue().get(1).getId();
        return null;
    }

    private boolean setLoading() {
        if (isLoading.compareAndSet(false, true)) {
            loading.postValue(true);
            return true;
        }
        return false;
    }

    private void setLoadingDone() {
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
                channelState.postValue(channel.getChannelState());
            }

            @Override
            public void onUserWatchingStop(Event event) {
                channelState.postValue(channel.getChannelState());
            }

            @Override
            public void onChannelUpdated(Event event) {
                channelState.postValue(channel.getChannelState());
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
                reads.postValue(channel.getChannelState().getReadsByUser());
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

    private void replaceMessage(Message oldMessage, Message newMessage) {
        List<Message> messagesCopy = getMessages().getValue();
        for (int i = 0; i < messagesCopy.size(); i++) {
            if (oldMessage.getId().equals(messagesCopy.get(i).getId())) {
                newMessage.setStatus(MessageStatus.RECEIVED);
                if (oldMessage.getStatus() == MessageStatus.FAILED) {
                    messagesCopy.remove(oldMessage);
                    messagesCopy.add(newMessage);
                } else {
                    messagesCopy.set(i, newMessage);
                }
                if (isThread())
                    threadMessages.postValue(messagesCopy);
                else
                    messages.postValue(messagesCopy);

                break;
            }
        }
    }

    private void upsertMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        Log.d(TAG, "messages Count:" + messages.getValue().size());
        List<Message> messagesCopy = getMessages().getValue();

        Log.d(TAG, "New messages Count:" + messagesCopy.size());

        if (message.getType().equals(ModelType.message_reply)) {
            if (!isThread()
                    || !message.getParentId().equals(threadParentMessage.getValue().getId()))
                return;

            for (int i = 0; i < threadMessages.getValue().size(); i++) {
                if (message.getId().equals(threadMessages.getValue().get(i).getId())) {
                    messagesCopy.set(i, message);
                    threadMessages.postValue(messagesCopy);
                    return;
                }
            }

            messagesCopy.add(message);
            threadMessages.postValue(messagesCopy);
        } else {
            int index = messagesCopy.indexOf(message);
            if (index != -1) {
                messagesCopy.set(index, message);
            } else {
                messagesCopy.add(message);
            }
            messages.postValue(messagesCopy);
        }

//        Log.d(TAG,"New messages Count:" + messages.getValue().size());

    }

    private boolean updateMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = getMessages().getValue();
        boolean updated = false;
        if (message.getType().equals(ModelType.message_reply)) {
            if (!isThread()
                    || !message.getParentId().equals(threadParentMessage.getValue().getId()))
                return updated;

            for (int i = 0; i < threadMessages.getValue().size(); i++) {
                if (message.getId().equals(threadMessages.getValue().get(i).getId())) {
                    messagesCopy.set(i, message);
                    threadMessages.postValue(messagesCopy);
                    updated = true;
                    break;
                }
            }
        } else {
            int index = messagesCopy.indexOf(message);
            updated = index != -1;
            if (updated) {
                messagesCopy.set(index, message);
                messages.postValue(messagesCopy);
            }

            if (isThread() && threadParentMessage.getValue().getId().equals(message.getId())){
                messagesCopy.set(0, message);
                threadMessages.postValue(messagesCopy);
            }
            Log.d(TAG, "updateMessage:" + updated);
        }
        return updated;
    }

    private void updateFailedMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(message);
        boolean updated = index != -1;
        if (updated) {
            String clientSideID = client().getUserId() + "-" + randomUUID().toString();
            message.setId(clientSideID);
            messagesCopy.set(index, message);
            messages.postValue(messagesCopy);
        }
    }

    private void shuffleGiphy(Message oldMessage, Message message) {
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(oldMessage);
        if (index != -1) {
            messagesCopy.set(index, message);
            messages.postValue(messagesCopy);
        }
    }

    private void cancelGiphy(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = messages.getValue();
        int index = messagesCopy.indexOf(message);
        if (index != -1) {
            messagesCopy.remove(message);
            messages.postValue(messagesCopy);
        }
    }

    private boolean deleteMessage(Message message) {
        List<Message> messagesCopy = getMessages().getValue();
        for (int i = 0; i < messagesCopy.size(); i++) {
            if (message.getId().equals(messagesCopy.get(i).getId())) {
                messagesCopy.remove(i);
                if (isThread()){
                    if (i == 0)
                        initThread();
                    else
                        threadMessages.postValue(messagesCopy);
                }
                else
                    messages.postValue(messagesCopy);

                return true;
            }
        }
        return false;
    }

    private void addMessage(Message message) {
        List<Message> messagesCopy = getMessages().getValue();
        messagesCopy.add(message);
        if (isThread())
            threadMessages.postValue(messagesCopy);
        else
            messages.postValue(messagesCopy);

    }


    private void addMessages(List<Message> newMessages) {
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

    private void channelLoadingDone() {
        initialized.set(true);
        setLoadingDone();
        channelState.postValue(channel.getChannelState());
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
                        if (response.getMessages().size() < limit) {
                            reachedEndOfPagination = true;
                        }
                        addMessages(response.getMessages());
                        channelLoadingDone();
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        channelLoadingDone();

                    }
                }
        );
    }

    public void loadMore() {
        if (!client().isConnected()) {
            return;
        }

        if (isLoading.get()) {
            Log.i(TAG, "already loading, skip loading more");
            return;
        }

        if (!setLoadingMore()) {
            Log.i(TAG, "already loading next page, skip loading more");
            return;
        }

        Log.i(TAG, String.format("Loading %d more messages, oldest message is %s", Constant.DEFAULT_LIMIT, channel.getChannelState().getOldestMessageId()));
        if (isThread()) {
            if (reachedEndOfPaginationThread) {
                Log.i(TAG, "already reached end of pagination, skip loading more");
                setLoadingMoreDone();
                return;
            }

            channel.getReplies(threadParentMessage.getValue().getId(), String.valueOf(Constant.DEFAULT_LIMIT), getThreadOldestMessageId(), new GetRepliesCallback() {
                @Override
                public void onSuccess(GetRepliesResponse response) {
                    entities.setIsLoadingMore(true);
                    List<Message> newMessages = new ArrayList<>(response.getMessages());
                    List<Message> messagesCopy = threadMessages.getValue();
                    for (int i = newMessages.size() - 1; i > -1; i--)
                        messagesCopy.add(1, newMessages.get(i));

                    threadMessages.postValue(messagesCopy);
                    reachedEndOfPaginationThread = newMessages.size() < Constant.DEFAULT_LIMIT;
                    setLoadingMoreDone();
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    setLoadingMoreDone();
                }
            });
        } else {

            if (reachedEndOfPagination) {
                Log.i(TAG, "already reached end of pagination, skip loading more");
                setLoadingMoreDone();
                return;
            }
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
                        }

                        @Override
                        public void onError(String errMsg, int errCode) {
                            setLoadingMoreDone();
                        }
                    }
            );
        }
    }

    @Override
    public void onSendMessage(final Message message, @Nullable final MessageCallback callback) {
        // send typing.stop immediately
        stopTyping();

        if (message.getStatus() == null) {
            message.setUser(client().getUser());
            message.setCreatedAt(new Date());
            message.setType("regular");
            if (isThread())
                message.setParentId(threadParentMessage.getValue().getId());
            message.setStatus(client().isConnected() ? MessageStatus.SENDING : MessageStatus.FAILED);
            String clientSideID = client().getUserId() + "-" + randomUUID().toString();
            message.setId(clientSideID);
            message.preStorage();
            message.setSyncStatus(Sync.LOCAL_ONLY);
            client().storage().insertMessageForChannel(channel, message);
            addMessage(message);
        }

        if (!client().isConnected()) {
            if (callback != null)
                callback.onError("no internet", -1);
            return;
        }

        // afterwards send the request
        channel.sendMessage(message,
                new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        replaceMessage(message, response.getMessage());
                        if (callback != null)
                            callback.onSuccess(response);
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        Message clone = message.copy();
                        clone.setStatus(MessageStatus.FAILED);
                        updateFailedMessage(clone);
                        if (callback != null)
                            callback.onError(errMsg, errCode);
                    }
                });
    }

    public void sendGiphy(Message message, GiphyAction action) {
        Map<String, String> map = new HashMap<>();
        switch (action) {
            case SEND:
                map.put("image_action", ModelType.action_send);
                break;
            case SHUFFLE:
                map.put("image_action", ModelType.action_shuffle);
                break;
            case CANCEL:
                cancelGiphy(message);
                return;
        }

        SendActionRequest request = new SendActionRequest(getChannel().getId(),
                message.getId(),
                ModelType.channel_messaging,
                map);
        channel.sendAction(message.getId(), request, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                if (action == GiphyAction.SHUFFLE)
                    shuffleGiphy(message, response.getMessage());
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.d(TAG, errMsg);
            }
        });
    }

    public MessageListItemLiveData getEntities() {
        return entities;
    }

    private List<User> getCleanedTypingUsers() {
        List<User> users = new ArrayList<>();
        long now = new Date().getTime();
        for (Event event : typingState.values()) {
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

    @Override
    public void resume() {
        setLoading();
    }

    @Override
    public void stopped() {

    }

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

    private void setupConnectionRecovery() {
        client().addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionRecovered(Event event) {
                addMessages(channel.getChannelState().getMessages());
                channelLoadingDone();
            }
        });
    }

    public synchronized void keystroke() {
        if (lastKeystrokeAt == null || (new Date().getTime() - lastKeystrokeAt.getTime() > 3000)) {
            lastKeystrokeAt = new Date();
            channel.sendEvent(EventType.TYPING_START, new EventCallback() {
                @Override
                public void onSuccess(EventResponse response) {
                }

                @Override
                public void onError(String errMsg, int errCode) {
                }
            });
        }
    }

    public synchronized void stopTyping() {
        if (lastKeystrokeAt == null) return;
        lastKeystrokeAt = null;
        channel.sendEvent(EventType.TYPING_STOP, new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {
            }

            @Override
            public void onError(String errMsg, int errCode) {
            }
        });
    }

    /**
     * Service thread to keep state neat and clean. Ticks twice per second
     */
    class Looper extends Thread {
        private Callable<Void> markReadFn;
        private AtomicInteger pendingMarkReadRequests;

        Looper(Callable<Void> markReadFn) {
            this.markReadFn = markReadFn;
            pendingMarkReadRequests = new AtomicInteger(0);
        }

        void markRead() {
            pendingMarkReadRequests.incrementAndGet();
        }

        private void sendStoppedTyping() {

            // typing did not start quit
            if (lastKeystrokeAt == null) {
                return;
            }

            long timeSinceLastKeystroke = new Date().getTime() - lastKeystrokeAt.getTime();

            if (timeSinceLastKeystroke > 5000) {
                stopTyping();
            }
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
                sendStoppedTyping();

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
                if (channel.getChannelState().getLastMessage() == null) {
                    viewModel.queryChannel();
                } else {
                    channelLoadingDone();
                }
            }
        }
    }
}