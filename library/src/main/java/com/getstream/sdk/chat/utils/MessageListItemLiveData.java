package com.getstream.sdk.chat.utils;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageListItemAdapter;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class MessageListItemLiveData extends LiveData<MessageListItemWrapper> {

    private final String TAG = MessageListItemLiveData.class.getSimpleName();

    private MutableLiveData<List<Message>> messages;
    private MutableLiveData<List<Message>> loadMoreMessages;
    private MutableLiveData<Message> upsertMessage;
    private MutableLiveData<List<User>> typing;
    private MutableLiveData<List<ChannelUserRead>> reads;

    private User currentUser;
    private List<MessageListItem> messageEntities;
    private List<MessageListItem> typingEntities;
    private List<ChannelUserRead> listReads;
    private Boolean isLoadingMore;
    private Boolean hasNewMessages;
    private String lastMessageID;


    public MessageListItemLiveData(User currentUser,
                                   MutableLiveData<List<Message>> messages,
                                   MutableLiveData<List<Message>> loadMoreMessages,
                                   MutableLiveData<Message> upsertMessage,
                                   MutableLiveData<List<User>> typing,
                                   MutableLiveData<List<ChannelUserRead>> reads) {
        this.messages = messages;
        this.loadMoreMessages = loadMoreMessages;
        this.upsertMessage = upsertMessage;
        this.currentUser = currentUser;
        this.typing = typing;
        this.reads = reads;
        this.messageEntities = new ArrayList<>();
        this.typingEntities = new ArrayList<>();
        this.listReads = new ArrayList<>();
        this.isLoadingMore = false;
        // scroll behaviour is only triggered for new messages
        this.lastMessageID = "";
        this.hasNewMessages = false;
    }

    public void setIsLoadingMore(Boolean loading) {
        isLoadingMore = loading;
    }

    private void broadcastValue() {
        List<MessageListItem> merged = new ArrayList<>();
        merged.addAll(messageEntities);

        // TODO replace with more efficient approach
        // remove the old read state
        for (MessageListItem i : merged) {
            if (i.getMessageReadBy().size() != 0) {
                i.removeMessageReadBy();
            }
        }

        // set the new read state
        // this wil become slow with many users and many messages
        for (ChannelUserRead r : listReads) {
            // we don't show read state for the current user
            if (r.getUser().getId().equals(currentUser.getId())) {
                continue;
            }
//            Log.i(TAG, "Setting read state for user: " + r.getUser().getId());
            for (int i = merged.size(); i-- > 0; ) {
                MessageListItem e = merged.get(i);
                // skip things that aren't messages
                if (e.getType() != MessageListItemAdapter.EntityType.MESSAGE) {
                    continue;
                }
                if (r.getLastRead().getTime() > e.getMessage().getCreatedAt().getTime()) {
                    // set the read state on this entity
                    e.addMessageReadBy(r);
                    // we only show it for the last message, so break
                    break;
                }

            }
        }

        merged.addAll(typingEntities);
        MessageListItemWrapper wrapper = new MessageListItemWrapper(isLoadingMore, hasNewMessages, merged);
        wrapper.setTyping(typingEntities.size()>0);
        setValue(wrapper);
        // isLoadingMore is only true once...
        if (isLoadingMore) {
            this.setIsLoadingMore(false);
        }
    }

    private boolean isSameDay(Message a, Message b) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(a.getCreatedAt()).equals(fmt.format(b.getCreatedAt()));
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner,
                        @NonNull Observer<? super MessageListItemWrapper> observer) {
        super.observe(owner, observer);
        this.reads.observe(owner, reads -> {
            hasNewMessages = false;
            if (reads == null) {
                reads = new ArrayList<>();
            }
            listReads = reads;
            broadcastValue();
        });

        this.messages.observe(owner, messages -> {
            Log.i(TAG, "observe messages");
            progressNewMessages(messages, false);
        });

        this.loadMoreMessages.observe(owner, messages -> {
            progressNewMessages(messages, true);
        });
        this.upsertMessage.observe(owner, message -> {
            if (message == null || message.getId() == null) return;
            Log.i(TAG, "observe upsertMessage");
            Log.i(TAG, "messages1 size: " + this.messages.getValue().size());
            hasNewMessages = false;

            int index = this.messages.getValue().indexOf(message);
            if (index != -1) {
                try {
                    MessageListItem messageListItem = this.messageEntities.get(index);
                    MessageListItem messageListItem_ = new MessageListItem(message, messageListItem.getPositions(), messageListItem.isMine());
                    messages.getValue().set(index, message);
                    messageEntities.set(index, messageListItem_);
                    broadcastValue();
                } catch (Exception e) {
                    progressNewMessages(Arrays.asList(message), false);
                }
            } else {
                progressNewMessages(Arrays.asList(message), false);
            }
        });

        this.typing.observe(owner, users -> {
            // update
            hasNewMessages = false;
            List<MessageListItem> typingEntities = new ArrayList<>();
            if (users.size() > 0) {
                MessageListItem messageListItem = new MessageListItem(users);
                typingEntities.add(messageListItem);
            }
            this.typingEntities = typingEntities;
            broadcastValue();
        });
    }

    public void progressNewMessages(List<Message> messages, boolean isLoadMoreMessages) {

        if (messages == null || messages.size() == 0) return;
        // update based on messages
        hasNewMessages = false;
        String newlastMessageID = messages.get(messages.size() - 1).getId();
        if (!newlastMessageID.equals(lastMessageID)) {
            hasNewMessages = true;
        }
        lastMessageID = newlastMessageID;
//        List<MessageListItem> entities = new ArrayList<>();
        // iterate over messages and stick in the date entities
        Message previousMessage = null;
        int size = messages.size();
        int topIndex = Math.max(0, size - 1);

        if (messages.size() == 1) {
            int index = this.messages.getValue().indexOf(messages.get(0));
            if (index != -1) {
                previousMessage = this.messages.getValue().get(index - 1);
            }
        }

        isLoadingMore = isLoadMoreMessages;
        if (isLoadMoreMessages){
            hasNewMessages = true;
        }

        if (previousMessage != null)
            Log.i(TAG, "previousMessage: " + previousMessage.getText());

        for (int i = 0; i < size; i++) {
            Message message = messages.get(i);
            Message nextMessage = null;
            if (i + 1 <= topIndex) {
                nextMessage = messages.get(i + 1);
            }

            // determine if the message is written by the current user
            boolean mine = message.getUser().equals(currentUser);
            // determine the position (top, middle, bottom)
            User user = message.getUser();
            List<MessageViewHolderFactory.Position> positions = new ArrayList<>();
            if (previousMessage == null || !previousMessage.getUser().equals(user)) {
                positions.add(MessageViewHolderFactory.Position.TOP);
            }

            if (nextMessage == null || !nextMessage.getUser().equals(user)) {
                positions.add(MessageViewHolderFactory.Position.BOTTOM);
            }

            if (previousMessage != null && nextMessage != null) {
                if (previousMessage.getUser().equals(user) && nextMessage.getUser().equals(user)) {
                    positions.add(MessageViewHolderFactory.Position.MIDDLE);
                }
            }

            if (previousMessage != null && nextMessage == null) {
                if (previousMessage.getUser().equals(user)) {
                    int index = this.messages.getValue().indexOf(previousMessage);
                    if (index != -1) {
                        MessageListItem messageListItem = this.messageEntities.get(index);
                        List<MessageViewHolderFactory.Position> positionList = Arrays.asList(MessageViewHolderFactory.Position.MIDDLE);
                        MessageListItem messageListItem_ = new MessageListItem(previousMessage, positionList, messageListItem.isMine());
                        messageEntities.set(index, messageListItem_);
                    }
                }
            }

            // date separator
            if (previousMessage != null && !isSameDay(previousMessage, message)) {
                this.messageEntities.add(new MessageListItem(message.getCreatedAt()));
            }


            MessageListItem messageListItem = new MessageListItem(message, positions, mine);
            if (isLoadMoreMessages) {
                this.messages.getValue().add(0, message);
                this.messageEntities.add(0, messageListItem);
            } else {
                this.messageEntities.add(messageListItem);
            }

            // set the previous message for the next iteration
            previousMessage = message;
        }
        broadcastValue();
    }

    public Boolean getHasNewMessages() {
        return hasNewMessages;
    }
}