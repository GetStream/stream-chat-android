package com.getstream.sdk.chat.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.enums.Pagination;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ChatChannelEventHandler;
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

    public LiveData<List<User>> getTypingUsers() {
        return typingUsers;
    }

    public MutableLiveData<List<User>> typingUsers;
    private MutableLiveData<List<ChannelUserRead>> reads;
    private EntityLiveData entities;
    public MutableLiveData<Boolean> endOfPagination;


    public Channel getChannel() {
        return channel;
    }

    public MutableLiveData<List<Message>> getMessages() {
        return messages;
    }

    public MutableLiveData<Boolean> getLoading() {
        return loading;
    }

    public MutableLiveData<Boolean> getLoadingMore() {
        return loadingMore;
    }

    public ChannelViewModel(Application application, Channel channel) {
        super(application);
        this.channel = channel;
        this.channelState = channel.getChannelState();

        loading = new MutableLiveData<>(false);
        loadingMore = new MutableLiveData<>(false);
        failed = new MutableLiveData<>(false);
        online = new MutableLiveData<>(true);
        endOfPagination = new MutableLiveData<>(false);
        hasNewMessages = new MutableLiveData<>(false);
        // TODO: actually listen to the events and verify if anybody is online
        anyOtherUsersOnline = new MutableLiveData<>(channelState.anyOtherUsersOnline());
        // TODO: change this if the list of channel members changes or the channel is updated
        channelName = new MutableLiveData<>(channelState.getChannelNameOrMembers());

        messages = new MutableLiveData<>(channelState.getMessages());
        typingUsers = new MutableLiveData<>(new ArrayList<User>());
        reads = new MutableLiveData<>(channelState.getReads());

        entities = new EntityLiveData(this.channel.getClient().getUser(), messages, typingUsers);
        watcherCount = new MutableLiveData<>();

        // humanized time diff
        Date lastActive = channelState.getLastActive();
        String humanizedDate = getRelativeTimeSpanString(lastActive.getTime()).toString();
        lastActiveString = new MutableLiveData<>(humanizedDate);
        typingState = new HashMap<>();

        Callable<Void> markRead = () -> {
            channel.markRead();
            return null;
        };
        looper = new Looper(markRead);
        looper.start();

        this.queryChannel();
    }

    public MutableLiveData<List<ChannelUserRead>> getReads() {
        return reads;
    }

    public MutableLiveData<Boolean> getEndOfPagination() {
        return endOfPagination;
    }

    // endregion

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
                }
                Log.d(TAG, "New Event: " + event.getType());
            }

            @Override
            public void onMessageNew(Event event) {
                Log.i(TAG, "onMessageNew for channelviewmodel" + event.getMessage().getText());
                List<Message> messageList = messages.getValue();
                if (messageList == null) {
                    messageList = new ArrayList<>();
                }
                messageList.add(event.getMessage());
                messages.postValue(messageList);

                if (!TextUtils.equals(event.getMessage().getUser().getId(), channel.getClient().getUserId())) {
                    markRead();
                }
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
                User user = event.getUser();
                typingState.put(user.getId(), event);
                typingUsers.postValue(getCleanedTypingUsers());
            }

            @Override
            public void onTypingStop(Event event) {
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
        channel.query(
            new ChannelQueryRequest().withMessages(limit),
            new QueryChannelCallback() {
                @Override
                public void onSuccess(ChannelState response) {
                    loading.postValue(false);
                    Log.i(TAG, "messages loaded");
                    channelState = response;
                    if (channelState.getMessages().size() < limit) {
                        endOfPagination.postValue(true);
                    }
                    addMessages(channelState.getMessages());
                    initEventHandlers();
                    markRead();
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
        entities.setIsLoadingMore(true);

        Log.d(TAG, "ViewModel loadMore...");

        ChannelQueryRequest request = new ChannelQueryRequest().withMessages(Pagination.LESS_THAN, channelState.getOldestMessageId(), Constant.DEFAULT_LIMIT);

        channel.query(
                request,
                new QueryChannelCallback() {
                    @Override
                    public void onSuccess(ChannelState response) {
                        loadingMore.postValue(false);
                        List<Message> newMessages = new ArrayList<>(response.getMessages());
                        // TODO: messages added via load more should be added at the bottom
                        addMessages(newMessages);

                        if (newMessages.size() < Constant.DEFAULT_LIMIT)
                            endOfPagination.setValue(true);
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        loadingMore.setValue(false);
                    }
                }
        );

        // TODO: Handle thread...

        //if (isThreadMode()) {
//            binding.setShowMainProgressbar(true);
//            client.getReplies(thread_parentMessage.getId(),
//                    String.valueOf(Constant.THREAD_MESSAGE_LIMIT),
//                    threadMessages.get(0).getId(), new GetRepliesCallback() {
//                        @Override
//                        public void onSuccess(GetRepliesResponse response) {
//                            binding.setShowMainProgressbar(false);
//                            List<Message> newMessages = new ArrayList<>(response.getMessages());
//                            if (newMessages.size() < Constant.THREAD_MESSAGE_LIMIT)
//                                noHistoryThread = true;
//
//                            Message.setStartDay(newMessages, null);
//                            // Add new to current Message List
//                            for (int i = newMessages.size() - 1; i > -1; i--) {
//                                threadMessages.add(0, newMessages.get(i));
//                            }
//                            int scrollPosition = ((LinearLayoutManager) recyclerView().getLayoutManager()).findLastCompletelyVisibleItemPosition() + response.getMessages().size();
//                            mThreadAdapter.notifyDataSetChanged();
//                            recyclerView().scrollToPosition(scrollPosition);
//                            isCalling = false;
//                        }
//
//                        @Override
//                        public void onError(String errMsg, int errCode) {
//                            Utils.showMessage(getContext(), errMsg);
//                            isCalling = false;
//                            binding.setShowMainProgressbar(false);
//                        }
//                    }
//            );
        // } else {


        // }
    }


//    private void newMessageEvent(Message message) {
//        Message.setStartDay(Arrays.asList(message), getLastMessage());
//
//        switch (message.getType()) {
//            case ModelType.message_regular:
//                if (!message.isIncoming())
//                    message.setDelivered(true);
//
//                messages().remove(ephemeralMessage);
//                if (message.isIncoming() && !isShowLastMessage) {
//                    scrollPosition = -1;
////                    binding.tvNewMessage.setVisibility(View.VISIBLE);
//                } else {
//                    scrollPosition = 0;
//                }
//                mViewModel.setChannelMessages(channelMessages);
//                messageMarkRead();
//                break;
//            case ModelType.message_ephemeral:
//            case ModelType.message_error:
//                boolean isContain = false;
//                for (int i = messages().size() - 1; i >= 0; i--) {
//                    Message message1 = messages().get(i);
//                    if (message1.getId().equals(message.getId())) {
//                        messages().remove(message1);
//                        isContain = true;
//                        break;
//                    }
//                }
//                if (!isContain) messages().add(message);
//                scrollPosition = 0;
//                if (isThreadMode()) {
//                    mThreadAdapter.notifyDataSetChanged();
//                    threadBinding.rvThread.scrollToPosition(threadMessages.size() - 1);
//                } else {
//                    mViewModel.setChannelMessages(messages());
//                }
//                break;
//            case ModelType.message_reply:
//                if (isThreadMode() && message.getParentId().equals(thread_parentMessage.getId())) {
//                    messages().remove(ephemeralMessage);
//                    threadMessages.add(message);
//                    mThreadAdapter.notifyDataSetChanged();
//                    threadBinding.rvThread.scrollToPosition(threadMessages.size() - 1);
//                }
//                break;
//            case ModelType.message_system:
//                break;
//            default:
//                break;
//        }
//    }

//    public void sendNewMessage(Message message) {
//        if (offline) {
//            //sendOfflineMessage();
//            return;
//        }
//        if (resendMessageId == null) {
//            ephemeralMessage = createEphemeralMessage(false);
//            handleAction(ephemeralMessage);
//        }
//        binding.messageInput.setEnabled(false);
//        channel.sendMessage(text,
//                attachments,
//                isThreadMode() ? thread_parentMessage.getId() : null,
//                new MessageCallback() {
//                    @Override
//                    public void onSuccess(MessageResponse response) {
//                        binding.messageInput.setEnabled(true);
//                        progressSendMessage(response.getMessage(), resendMessageId);
//                    }
//
//                    @Override
//                    public void onError(String errMsg, int errCode) {
//                        binding.messageInput.setEnabled(true);
//                        Utils.showMessage(getContext(), errMsg);
//                    }
//                });
//    }


    @Override
    public void onSendMessage(Message message) {
        Log.i(TAG, "onSendMessage handler called at viewmodel level");
        channel.sendMessage(message,
                new MessageCallback() {
                    @Override
                    public void onSuccess(MessageResponse response) {
                        Message responseMessage = response.getMessage();
                        Log.i(TAG, "onSuccess event for sending the message");
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        //binding.messageInput.setEnabled(true);
                    }
                });

    }

    public MutableLiveData<String> getChannelName() {
        return channelName;
    }

    public MutableLiveData<Boolean> getOnline() {
        return online;
    }

    public MutableLiveData<Boolean> getFailed() {
        return failed;
    }

    public MutableLiveData<Boolean> getAnyOtherUsersOnline() {
        return anyOtherUsersOnline;
    }

    public MutableLiveData<String> getLastActiveString() {
        return lastActiveString;
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
