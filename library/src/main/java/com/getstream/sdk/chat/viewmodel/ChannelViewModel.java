package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import com.getstream.sdk.chat.LifecycleHandler;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.StreamLifecycleObserver;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.enums.InputType;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;
import com.getstream.sdk.chat.utils.MessageListItemLiveData;
import com.getstream.sdk.chat.utils.ResultCallback;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import io.getstream.chat.android.client.api.models.ChannelQueryRequest;
import io.getstream.chat.android.client.api.models.ChannelWatchRequest;
import io.getstream.chat.android.client.api.models.Pagination;
import io.getstream.chat.android.client.api.models.SendActionRequest;
import io.getstream.chat.android.client.call.Call;
import io.getstream.chat.android.client.events.*;
import io.getstream.chat.android.client.models.*;
import io.getstream.chat.android.client.utils.Result;
import io.getstream.chat.android.client.utils.observable.Subscription;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

import static java.util.UUID.randomUUID;

/*
 * - store the channel data
 * - load more data
 * -
 */
public class ChannelViewModel extends AndroidViewModel implements LifecycleHandler {

    protected static final String TAG = ChannelViewModel.class.getSimpleName();

    /**
     * The A livedata object for the list of messages
     */
    protected MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    /**
     * The numbers of users currently watching this channel
     */
    protected LiveData<Number> watcherCount;
    /**
     * The list of users currently typing
     */
    protected MutableLiveData<List<User>> typingUsers = new MutableLiveData<>();
    /**
     * Mutable live data object for the current messageInputText
     */
    protected MutableLiveData<String> messageInputText = new MutableLiveData<>("");

    private Date lastKeystrokeAt;
    private Date lastStartTypingEvent;

    protected String channelId;
    protected String channelType;

    protected Looper looper;
    protected Map<String, ChatEvent> typingState;

    protected int channelSubscriptionId = 0;
    protected int threadParentPosition = 0;

    protected InitViewModelLiveData initialized = new InitViewModelLiveData(this);
    protected AtomicBoolean isLoading = new AtomicBoolean(false);
    protected AtomicBoolean isLoadingMore = new AtomicBoolean(false);
    protected boolean reachedEndOfPagination;
    protected boolean reachedEndOfPaginationThread;
    protected Date lastMarkRead;
    protected MutableLiveData<Number> currentUserUnreadMessageCount = new MutableLiveData<>();
    protected Integer lastCurrentUserUnreadMessageCount = 0;
    protected MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    protected MutableLiveData<Boolean> messageListScrollUp = new MutableLiveData<>(false);
    protected MutableLiveData<Boolean> loadingMore = new MutableLiveData<>(false);
    protected MutableLiveData<Boolean> failed = new MutableLiveData<>(false);
    protected MutableLiveData<Message> editMessage;
    protected MutableLiveData<Message> threadParentMessage = new MutableLiveData<>(null);
    protected MutableLiveData<Channel> channelState = new MutableLiveData<>();

    protected MutableLiveData<List<Message>> threadMessages = new MutableLiveData<>();
    protected LiveData<Boolean> anyOtherUsersOnline;

    protected MutableLiveData<Boolean> hasNewMessages = new MutableLiveData<>(false);

    protected MutableLiveData<Map<String, ChannelUserRead>> reads = new MutableLiveData<>();
    protected MutableLiveData<InputType> inputType = new MutableLiveData<>(InputType.DEFAULT);
    protected MessageListItemLiveData entities;
    protected boolean enableMarkRead; // Used to prevent automatic mark reading messages.

    private List<Subscription> subscriptions = new ArrayList<>();


    public ChannelViewModel(Application application, String channelType, String channelId) {
        super(application);

        this.channelType = channelType;
        this.channelId = channelId;

        StreamChat.getLogger().logI(this, "instance created");

        threadMessages.setValue(null);
        typingUsers.setValue(new ArrayList<>());

        User currentUser = StreamChat.getInstance().getCurrentUser();

        entities = new MessageListItemLiveData(currentUser, messages, threadMessages, typingUsers, reads);

        typingState = new HashMap<>();
        editMessage = new MutableLiveData<>();

        enableMarkRead = true;

        Callable<Void> markRead = () -> {

            //TODO: llc unsub from all enqueue
            StreamChat.getInstance().markMessageRead(channelType, channelId, "").enqueue(new Function1<Result<Unit>, Unit>() {
                @Override
                public Unit invoke(Result<Unit> result) {

                    if (result.isSuccess()) {
                        StreamChat.getLogger().logI(this, "Marked read message");
                    } else {
                        StreamChat.getLogger().logE(this, result.error().getMessage());
                    }

                    return null;
                }
            });
            return null;
        };
        looper = new Looper(markRead);
        looper.start();

        new StreamLifecycleObserver(this);

        setupConnectionRecovery();
    }

    // region Getter

    public Map<String, ChannelUserRead> getReadsByUser(Channel channel) {
        Map<String, ChannelUserRead> readsByUser = new HashMap<>();
        for (ChannelUserRead r : channel.getRead()) {
            readsByUser.put(r.getUserId(), r);
        }
        return readsByUser;
    }

    public MutableLiveData<Number> getCurrentUserUnreadMessageCount() {
        return currentUserUnreadMessageCount;
    }

    public Channel getChannel() {
        return channelState.getValue();
    }

    public LiveData<Boolean> getInitializedState() {
        return initialized;
    }

    public LiveData<Channel> getChannelState() {
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
        if (inputType == InputType.SELECT && isEditing()) {
            this.inputType.postValue(InputType.EDIT);
            return;
        }
        this.inputType.postValue(inputType);
    }

    public LiveData<List<User>> getTypingUsers() {
        return typingUsers;
    }

    public LiveData<Message> getEditMessage() {
        return editMessage;
    }

    public void setEditMessage(Message editMessage) {
        this.editMessage.postValue(editMessage);
    }

    public LiveData<Boolean> getMessageListScrollUp() {
        return messageListScrollUp;
    }

    public void setMessageListScrollUp(Boolean messageListScrollUp) {
        this.messageListScrollUp.postValue(messageListScrollUp);
    }

    public boolean isEditing() {
        return getEditMessage().getValue() != null;
    }

    // region Thread
    public LiveData<Message> getThreadParentMessage() {
        return threadParentMessage;
    }

    public void setThreadParentMessage(Message threadParentMessage_) {
        threadParentMessage.postValue(threadParentMessage_);
        configThread(threadParentMessage_);
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

    protected void configThread(Message message) {

        if (message.getReplyCount() == 0) {
            reachedEndOfPaginationThread = true;
            threadMessages.postValue(new ArrayList<Message>() {
                {
                    add(message);
                }
            });
        } else {

            StreamChat.getInstance().getReplies(message.getId(), 30).enqueue(new Function1<Result<List<Message>>, Unit>() {
                @Override
                public Unit invoke(Result<List<Message>> result) {

                    if (result.isSuccess()) {
                        List<Message> newMessages = new ArrayList<>(result.data());
                        newMessages.add(0, message);
                        reachedEndOfPaginationThread = newMessages.size() < 30 + 1;
                        threadMessages.postValue(newMessages);
                    }

                    return null;
                }
            });
        }
    }

    public void initThread() {
        threadParentMessage.postValue(null);
        threadMessages.postValue(null);
        Channel channel = channelState.getValue();
        updateMessageLiveData(new ArrayList<>(channel.getMessages()));
        reachedEndOfPaginationThread = false;
    }
    // endregion

    // endregion

    protected String getThreadOldestMessageId() {
        List<Message> messages = threadMessages.getValue();
        if (messages != null && messages.size() > 1)
            return threadMessages.getValue().get(1).getId();
        return "";
    }

    protected boolean setLoading() {
        if (isLoading.compareAndSet(false, true)) {
            loading.postValue(true);
            return true;
        }
        return false;
    }

    protected void setLoadingDone() {
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

    public void markLastMessageRead() {
        // this prevents infinite loops with mark read commands
        Channel channel = channelState.getValue();
        Message message = LlcMigrationUtils.computeLastMessage(channel);
        User currentUser = StreamChat.getInstance().getCurrentUser();
        if (message == null || !isEnableMarkRead() || message.getUserId().equals(currentUser.getUserId())) {
            return;
        }
        if (lastMarkRead == null || message.getCreatedAt().getTime() > lastMarkRead.getTime()) {
            looper.markRead();
            lastMarkRead = message.getCreatedAt();
        }
    }

    protected boolean isEnableMarkRead() {
        return enableMarkRead;
    }

    public void setEnableMarkRead(boolean enableMarkRead) {
        this.enableMarkRead = enableMarkRead;
    }

    /**
     * bans a user from this channel
     *
     * @param targetUserId the ID of the user to ban
     * @param reason       the reason the ban was created.
     * @param timeout      the timeout in minutes until the ban is automatically expired
     */
    public Call<Unit> banUser(@NotNull String targetUserId, @Nullable String reason,
                              @Nullable Integer timeout) {

        return StreamChat.getInstance().banUser(targetUserId, channelType, channelId, reason, timeout);
    }

    /**
     * removes the ban for a user on this channel
     *
     * @param targetUserId the ID of the user to remove the ban
     * @param callback     the result callback
     */
    public Call<Unit> unBanUser(@NotNull String targetUserId, @Nullable ResultCallback<Void, String> callback) {

        return StreamChat.getInstance().unBanUser(targetUserId, channelType, channelId);
    }

    protected void initEventHandlers() {

        subscriptions.add(StreamChat.getInstance().events().subscribe(new Function1<ChatEvent, Unit>() {
            @Override
            public Unit invoke(ChatEvent event) {

                Channel channel = event.getChannel();

                if (event instanceof NewMessageEvent) {
                    upsertMessage(event.getMessage());
                } else if (event instanceof UserStartWatchingEvent) {

                } else if (event instanceof UserStopWatchingEvent) {

                } else if (event instanceof ChannelUpdatedEvent) {

                } else if (event instanceof MessageUpdatedEvent) {
                    updateMessage(event.message);
                } else if (event instanceof MessageDeletedEvent) {
                    deleteMessage(event.message);
                } else if (event instanceof MessageReadEvent) {


                    if (channel != null) {
                        reads.postValue(LlcMigrationUtils.getReadsByUser(channel));

                        User currentUser = StreamChat.getInstance().getCurrentUser();
                        String currentUserId = currentUser.getId();

                        int unreadMessageCount = LlcMigrationUtils.getUnreadMessageCount(currentUserId, channel);

                        if (unreadMessageCount != lastCurrentUserUnreadMessageCount) {
                            lastCurrentUserUnreadMessageCount = unreadMessageCount;
                            currentUserUnreadMessageCount.postValue(lastCurrentUserUnreadMessageCount);
                        }
                    }


                } else if (event instanceof ReactionNewEvent) {
                    updateMessage(event.message);
                } else if (event instanceof ReactionDeletedEvent) {
                    updateMessage(event.message);
                } else if (event instanceof TypingStartEvent) {
                    if (!LlcMigrationUtils.isFromCurrentUser(event)) {
                        User user = event.getUser();
                        typingState.put(user.getId(), event);
                        typingUsers.postValue(getCleanedTypingUsers());
                    }
                } else if (event instanceof TypingStopEvent) {
                    if (!LlcMigrationUtils.isFromCurrentUser(event)) {
                        User user = event.getUser();
                        typingState.remove(user.getId());
                        typingUsers.postValue(getCleanedTypingUsers());
                    }
                } else if (event instanceof MemberAddedEvent) {

                } else if (event instanceof MemberRemovedEvent) {

                } else if (event instanceof MemberUpdatedEvent) {

                }

                User currentUser = StreamChat.getInstance().getCurrentUser();
                String currentUserId = currentUser.getId();

                if (channel != null) {
                    channelState.postValue(channel);

                    int unreadMessageCount = LlcMigrationUtils.getUnreadMessageCount(currentUserId, channel);

                    if (unreadMessageCount != lastCurrentUserUnreadMessageCount) {
                        lastCurrentUserUnreadMessageCount = unreadMessageCount;
                        currentUserUnreadMessageCount.postValue(lastCurrentUserUnreadMessageCount);
                    }
                }

                return null;
            }
        }));

    }

    protected void replaceMessage(Message oldMessage, Message newMessage) {
        List<Message> messagesCopy = getMessages().getValue();
        String oldMessageId = oldMessage.getId();
        for (int i = messagesCopy.size() - 1; i >= 0; i--) {
            String messageId = messagesCopy.get(i).getId();
            if (oldMessageId.equals(messageId)) {
                //TODO: llc test offline case
//                if (oldMessage.getSyncStatus() == Sync.LOCAL_FAILED) {
//                    messagesCopy.remove(oldMessage);
//                } else {
//                    messagesCopy.set(i, newMessage);
//                }

                messagesCopy.set(i, newMessage);

                if (isThread())
                    threadMessages.postValue(messagesCopy);
                else
                    updateMessageLiveData(messagesCopy);

                break;
            }
        }
    }

    protected void upsertMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change

        if (message.getType().equals(ModelType.message_reply)
                || !TextUtils.isEmpty(message.getParentId())) {
            if (!isThread()
                    || !message.getParentId().equals(threadParentMessage.getValue().getId()))
                return;

            List<Message> messagesCopy = threadMessages.getValue();
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
            List<Message> messagesCopy = messages.getValue();
            boolean updated = false;
            for (int i = 0; i < messagesCopy.size(); i++) {
                Message m = messagesCopy.get(i);
                if (m.getId().equals(message.getId())) {
                    updated = true;
                    messagesCopy.set(i, message);
                    break;
                }
            }

            if (!updated) {
                messagesCopy.add(message);
            }

            updateMessageLiveData(messagesCopy);
            markLastMessageRead();
        }
    }

    private void updateMessageLiveData(List<Message> messagesCopy) {
        messages.postValue(messagesCopy);
    }

    protected boolean updateMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = getMessages().getValue();
        boolean updated = false;
        if (message.getType().equals(ModelType.message_reply)
                || !TextUtils.isEmpty(message.getParentId())) {
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
            int index = LlcMigrationUtils.indexOf(messagesCopy, message);
            updated = index != -1;
            if (updated) {
                messagesCopy.set(index, message);
                updateMessageLiveData(messagesCopy);
            }
            // Check if message is Thread Parent Message
            if (isThread() && threadParentMessage.getValue().getId().equals(message.getId())) {
                List<Message> messagesCopy_ = threadMessages.getValue();
                messagesCopy_.set(0, message);
                threadMessages.postValue(messagesCopy_);
                updated = true;
            }
            StreamChat.getLogger().logI(this, "updateMessage:" + updated);
        }
        return updated;
    }

    protected void updateFailedMessage(Message message) {
        // doesn't touch the message order, since message.created_at can't change
        List<Message> messagesCopy = messages.getValue();
        int index = LlcMigrationUtils.indexOf(messagesCopy, message);
        boolean updated = index != -1;
        if (updated) {
            User currentUser = StreamChat.getInstance().getCurrentUser();
            String clientSideID = currentUser.getUserId() + "-" + randomUUID().toString();
            message.setId(clientSideID);
            messagesCopy.set(index, message);
            updateMessageLiveData(messagesCopy);
        }
    }

    protected void shuffleGiphy(Message oldMessage, Message message) {
        List<Message> messagesCopy = getMessages().getValue();
        int index = LlcMigrationUtils.indexOf(messagesCopy, oldMessage);
        if (index != -1) {
            messagesCopy.set(index, message);
            if (isThread())
                threadMessages.postValue(messagesCopy);
            else
                updateMessageLiveData(messagesCopy);
        }
    }


    protected boolean deleteMessage(Message message) {
        List<Message> messagesCopy = getMessages().getValue();
        for (int i = 0; i < messagesCopy.size(); i++) {
            if (message.getId().equals(messagesCopy.get(i).getId())) {
                messagesCopy.set(i, message);
                if (isThread()) {
                    if (i == 0)
                        initThread();
                    else
                        threadMessages.postValue(messagesCopy);
                } else
                    updateMessageLiveData(messagesCopy);

                return true;
            }
        }
        return false;
    }

    protected void checkErrorOrPendingMessage() {
        boolean hasErrorOrPendingMessage = false;
        List<Message> messagesCopy = getMessages().getValue();
        for (int i = 0; i < messagesCopy.size(); i++) {
            Message message = getMessages().getValue().get(i);
            if (message.getType().equals(ModelType.message_error)) {
                messagesCopy.remove(i);
                hasErrorOrPendingMessage = true;
            }
        }
        if (!hasErrorOrPendingMessage) return;

        if (isThread()) {
            threadMessages.postValue(messagesCopy);
        } else
            updateMessageLiveData(messagesCopy);
    }

    protected void checkFailedMessage(Message message) {

        List<Message> messagesCopy = getMessages().getValue();
        for (int i = 0; i < messagesCopy.size(); i++) {
            if (message.getId().equals(messagesCopy.get(i).getId())) {
                messagesCopy.remove(message);
                if (isThread())
                    threadMessages.postValue(messagesCopy);
                else
                    updateMessageLiveData(messagesCopy);
                break;
            }
        }
    }

    protected void addMessage(Message message) {
        List<Message> messagesCopy = getMessages().getValue();
        messagesCopy.add(message);
        if (isThread())
            threadMessages.postValue(messagesCopy);
        else
            updateMessageLiveData(messagesCopy);

    }


    protected void addMessages(List<Message> newMessages) {
        List<Message> messagesCopy = messages.getValue();
        if (messagesCopy == null) {
            messagesCopy = new ArrayList<>();
        }

        // iterate in reverse-order since newMessages is assumed to be ordered by created_at DESC
        for (int i = newMessages.size() - 1; i >= 0; i--) {
            Message message = newMessages.get(i);

            int index = LlcMigrationUtils.indexOf(messagesCopy, message);

            if (index == -1) {
                messagesCopy.add(0, message);
            } else {
                messagesCopy.set(index, message);
            }
        }
        updateMessageLiveData(messagesCopy);
    }

    protected void onChannelLoaded(Channel channel) {

        reachedEndOfPagination = channel.getMessages().size() < 10;

        //Log.d(TAG, "Setting channel. Client:" + client() + ", storage:" + client().getStorage());
        // fetch offline messages
//        client().getStorage().selectChannelState(channel.getCid(), new OnQueryListener<ChannelState>() {
//            @Override
//            public void onSuccess(ChannelState channelState) {
//                StreamChat.getLogger().logI(this,"Read messages from local cache...");
//                if (channelState != null) {
//                    messages.setValue(channelState.getMessages());
//                }
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                StreamChat.getLogger().logW(this, String.format("Failed to read channel state from offline storage, error %s", e.toString()));
//            }
//        });

        User currentUser = StreamChat.getInstance().getCurrentUser();

        watcherCount = Transformations.map(channelState, Channel::getWatcherCount);
        anyOtherUsersOnline = Transformations.map(watcherCount, count -> count != null && count.intValue() > 1);
        lastCurrentUserUnreadMessageCount = LlcMigrationUtils.getUnreadMessageCount(currentUser.getUserId(), channel);
        currentUserUnreadMessageCount.postValue(lastCurrentUserUnreadMessageCount);


        channelState.postValue(channel);
        reads.setValue(getReadsByUser(channel));
        messages.setValue(channel.getMessages());
        initEventHandlers();
        setLoadingDone();
        initialized.postValue(true);
    }

    public MessageListItemLiveData getEntities() {
        return entities;
    }

    protected List<User> getCleanedTypingUsers() {
        List<User> users = new ArrayList<>();
        long now = new Date().getTime();
        for (ChatEvent event : typingState.values()) {
            // constants
            long TYPING_TIMEOUT = 10000;
            if (now - event.getReceivedAt().getTime() < TYPING_TIMEOUT) {
                users.add(event.getUser());
            }
        }
        return users;
    }

    public MutableLiveData<Boolean> getHasNewMessages() {
        return hasNewMessages;
    }

    public void setHasNewMessages(Boolean hasNewMessages) {
        this.hasNewMessages.postValue(hasNewMessages);
    }

    @Override
    public void resume() {
        StreamChat.getLogger().logI(this, "resume");
    }

    @Override
    public void stopped() {
        StreamChat.getLogger().logI(this, "stopped");
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        StreamChat.getLogger().logI(this, "onCleared");

        for (Subscription sub : subscriptions) sub.unsubscribe();
        subscriptions.clear();


        if (looper != null) {
            looper.interrupt();
        }

        if (channelSubscriptionId != 0) {
            //TODO: llc unsubscribe
            //channel.removeEventHandler(channelSubscriptionId);
        }
    }

    protected void setupConnectionRecovery() {

        //TODO: llc unsubscrube
        subscriptions.add(StreamChat.getInstance().events().subscribe(chatEvent -> {
            if (chatEvent instanceof ConnectedEvent) {

                //TODO: llc refresh on connected

//                    String type = channel.getType();
//                    String id = channel.getId();
//                    StreamChat.getInstance().queryChannel(type, id, new ChannelQueryRequest().withMessages()).enqueue(
//                            new Function1<Result<Channel>, Unit>() {
//                                @Override
//                                public Unit invoke(Result<Channel> channelResult) {
//                                    addMessages(channel.getMessages());
//                                    channelLoadingDone();
//                                    return null;
//                                }
//                            }
//                    );
            }
            return null;
        }));
    }

    /**
     * watches channel
     */
    public void watchChannel() {
        int limit = 10; // Constant.DEFAULT_LIMIT
        if (!setLoading()) return;

        ChannelWatchRequest request = new ChannelWatchRequest().withMessages(limit);

        StreamChat.getInstance().queryChannel(channelType, channelId, request).enqueue(new Function1<Result<Channel>, Unit>() {
            @Override
            public Unit invoke(Result<Channel> channelResult) {

                Channel channel = channelResult.data();

                if (channelResult.isSuccess()) {
                    onChannelLoaded(channel);
                } else {
                    //TODO: show error message
                }


                return null;
            }
        });
    }

    /**
     * loads more messages, use this to load a previous page
     */
    public void loadMore() {
        if (!StreamChat.getInstance().isSocketConnected()) {
            StreamChat.getLogger().logI(this, "connection failed.");
            return;
        }

        if (isLoading.get()) {
            StreamChat.getLogger().logI(this, "already loading, skip loading more");
            return;
        }

        if (!setLoadingMore()) {
            StreamChat.getLogger().logI(this, "already loading next page, skip loading more");
            return;
        }

        if (isThread()) {
            if (reachedEndOfPaginationThread) {
                setLoadingMoreDone();
                StreamChat.getLogger().logI(this, "already reached end of pagination, skip loading more");
                return;
            }

            if (threadParentMessage.getValue() == null) {
                setLoadingMoreDone();
                StreamChat.getLogger().logI(this, "Can't find thread parent message.");
                return;
            }

            String id = threadParentMessage.getValue().getId();
            String oldestMessageId = getThreadOldestMessageId();

            if (oldestMessageId.isEmpty()) {
                StreamChat.getInstance().getReplies(id, Constant.DEFAULT_LIMIT).enqueue(result -> {
                    onReactionsLoaded(result);
                    return null;
                });
            } else {
                StreamChat.getInstance().getRepliesMore(id, oldestMessageId, Constant.DEFAULT_LIMIT).enqueue(result -> {
                    onReactionsLoaded(result);
                    return null;
                });
            }
        } else {

            if (reachedEndOfPagination) {
                setLoadingMoreDone();
                StreamChat.getLogger().logI(this, "already reached end of pagination, skip loading more");
                return;
            }

            Message oldestMessage = messages.getValue().get(0);
            String oldestMessageId = oldestMessage.getId();

            ChannelQueryRequest request = new ChannelQueryRequest().
                    withMessages(Pagination.LESS_THAN,
                            oldestMessageId,
                            Constant.DEFAULT_LIMIT);

            StreamChat.getInstance().queryChannel(channelType, channelId, request).enqueue(result -> {

                if (result.isSuccess()) {

                    Channel channel = result.data();

                    reachedEndOfPagination = channel.getMessages().isEmpty();
                    List<Message> newMessages = new ArrayList<>(channel.getMessages());
                    // used to modify the scroll behaviour...
                    entities.setIsLoadingMore(true);
                    addMessages(newMessages);
                    setLoadingMoreDone();
                } else {
                    setLoadingMoreDone();
                }

                return null;
            });
        }
    }

    private void onReactionsLoaded(Result<List<Message>> result) {
        if (result.isSuccess()) {

            List<Message> messages = result.data();

            entities.setIsLoadingMore(true);
            List<Message> newMessages = new ArrayList<>(messages);
            List<Message> messagesCopy = threadMessages.getValue();
            for (int i = newMessages.size() - 1; i > -1; i--)
                messagesCopy.add(1, newMessages.get(i));

            threadMessages.postValue(messagesCopy);
            reachedEndOfPaginationThread = newMessages.size() < Constant.DEFAULT_LIMIT;
            setLoadingMoreDone();
        } else {
            setLoadingMoreDone();
        }
    }

    /**
     * sends message
     *
     * @param message the Message sent
     */
    public Call<Message> sendMessage(Message message) {
        // set the current user
        // message.setUser(client().getUser());
        // set the thread id if we are viewing a thread

        if (isThread()) {
            String parentMessageId = getThreadParentMessage().getValue().getId();
            message.setParentId(parentMessageId);
        }

        //if (message.getSyncStatus() == LOCAL_ONLY) return;

        checkErrorOrPendingMessage();
        checkFailedMessage(message);
        stopTyping();

        //TODO: llc check in memory offline

        // Check uploading file
//        if (message.getSyncStatus() == Sync.LOCAL_UPDATE_PENDING) {
//            addMessage(message);
//            return;
//        }
//
//        if (message.getSyncStatus() == Sync.IN_MEMORY) {
//            // insert the message into local storage
//            client().getStorage().insertMessageForChannel(channel, message);
//
//            // add the message here
//            addMessage(message);
//        }

        message.setCreatedAt(new Date());
        message.setUser(StreamChat.getInstance().getCurrentUser());

        addMessage(message);

        return StreamChat.getInstance()
                .sendMessage(channelType, channelId, message)
                .onSuccess(m -> {
                    replaceMessage(message, m);
                    return Unit.INSTANCE;
                }).onError(chatError -> {
                    updateFailedMessage(message);
                    return Unit.INSTANCE;
                });
    }


    /**
     * Edit message
     *
     * @param message the Message sent
     */
    public Call<Message> editMessage(Message message) {
//        if (message.getSyncStatus() == Sync.LOCAL_UPDATE_PENDING) {
//            replaceMessage(message, message);
//            return;
//        }

        // Check Error or Pending Messages
        checkErrorOrPendingMessage();

        return StreamChat.getInstance().updateMessage(message);
    }

    /**
     * sendGiphy - Send giphy with shuffle and cancel.
     *
     * @param message the message that has giphy attachment
     * @param action  the giphy send, shuffle and cancel
     */
    public void sendGiphy(Message message, GiphyAction action) {
        Map<Object, Object> map = new HashMap<>();
        switch (action) {
            case SEND:
                map.put("image_action", ModelType.action_send);
                break;
            case SHUFFLE:
                map.put("image_action", ModelType.action_shuffle);
                break;
            case CANCEL:
                List<Message> messagesCopy = getMessages().getValue();
                int index = LlcMigrationUtils.indexOf(messagesCopy, message);
                if (index != -1) {
                    messagesCopy.remove(message);
                    if (isThread())
                        threadMessages.postValue(messagesCopy);
                    else
                        updateMessageLiveData(messagesCopy);
                }
                return;
        }

        SendActionRequest request = new SendActionRequest(
                channelId,
                message.getId(),
                ModelType.channel_messaging,
                map);

        StreamChat.getInstance().sendAction(request).enqueue(result -> {

            if (result.isSuccess())
                if (action == GiphyAction.SHUFFLE) shuffleGiphy(message, result.data());

            return null;
        });
    }

    /**
     * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke
     */
    public synchronized void keystroke() {
        if (isThread()) return;

        Date now = new Date();
        lastKeystrokeAt = now;

        if (lastStartTypingEvent == null || (now.getTime() - lastStartTypingEvent.getTime() > 3000)) {
            lastStartTypingEvent = now;

            StreamChat.getInstance()
                    .sendEvent(EventType.INSTANCE.getTYPING_START(), channelType, channelId, new HashMap<>())
                    .enqueue(
                            chatEventResult -> null
                    );
        }
    }

    /**
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */

    public void stopTyping() {
        if (isThread()) return;

        StreamChat.getInstance()
                .sendEvent(EventType.INSTANCE.getTYPING_STOP(), channelType, channelId, new HashMap<>())
                .enqueue(
                        chatEventResult -> null
                );
    }

    /**
     * Cleans up the typing state by removing typing users that did not send
     * typing.stop event for long time
     */
    protected void cleanupTypingUsers() {
        List<User> prev = typingUsers.getValue();
        List<User> cleaned = getCleanedTypingUsers();
        if (prev != null && cleaned != null && prev.size() != cleaned.size()) {
            typingUsers.postValue(getCleanedTypingUsers());
        }
    }

    public MutableLiveData<String> getMessageInputText() {
        return messageInputText;
    }

    public void setMessageInputText(MutableLiveData<String> messageInputText) {
        this.messageInputText = messageInputText;
    }

    /**
     * Service thread to keep state neat and clean. Ticks twice per second
     */
    class Looper extends Thread {
        protected Callable<Void> markReadFn;
        protected AtomicInteger pendingMarkReadRequests;

        Looper(Callable<Void> markReadFn) {
            this.markReadFn = markReadFn;
            pendingMarkReadRequests = new AtomicInteger(0);
        }

        void markRead() {
            pendingMarkReadRequests.incrementAndGet();
        }

        protected void sendStoppedTyping() {

            // typing did not start, quit
            if (lastStartTypingEvent == null) {
                return;
            }

            // if we didn't press a key for more than 5 seconds send the stopTyping event

            long timeSinceLastKeystroke = new Date().getTime() - lastKeystrokeAt.getTime();

            // TODO: this should be a config value on the client or channel object...
            if (timeSinceLastKeystroke > 5000) {
                stopTyping();
            }
        }

        protected void throttledMarkRead() {
            int pendingCalls = pendingMarkReadRequests.get();
            if (pendingCalls == 0) {
                return;
            }
            try {
                markReadFn.call();
            } catch (Exception e) {
                StreamChat.getLogger().logE(this, e.getLocalizedMessage());
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

    static class InitViewModelLiveData extends MutableLiveData<Boolean> {

        protected ChannelViewModel viewModel;

        public InitViewModelLiveData(ChannelViewModel viewModel) {
            //super(value);
            this.viewModel = viewModel;
        }

        @Override
        protected void onActive() {
            super.onActive();
            Boolean value = getValue();

            if (value == null || value == false) {
                viewModel.watchChannel();
            }
        }
    }
}