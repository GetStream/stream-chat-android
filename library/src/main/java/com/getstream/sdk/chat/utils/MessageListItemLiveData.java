package com.getstream.sdk.chat.utils;

import android.os.Handler;
import android.text.TextUtils;
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

    List<MessageListItem> merged = new ArrayList<>();

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
        merged.clear();
        merged.addAll(messageEntities);

        // TODO replace with more efficient approach
        // remove the old read state
        for (MessageListItem i : merged) {
            if (!i.getMessageReadBy().isEmpty()) {
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
        wrapper.setTyping(typingEntities.size() > 0);
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
        // message observe
        this.messages.observe(owner, messages -> progressNewMessages(messages, false));
        // upsertMessage observe
        this.upsertMessage.observe(owner, message -> {
            if (message == null || message.getId() == null) return;
            hasNewMessages = false;

            int index = this.messages.getValue().indexOf(message);
            if (index != -1) {
                MessageListItem messageListItem = getMessageItemFromMessage(message);
                if (messageListItem != null) {
                    MessageListItem messageListItem_ = new MessageListItem(message, messageListItem.getPositions(), messageListItem.isMine());
                    messages.getValue().set(index, message);
                    // refactor previous Message's Position
                    Message previousMessage, nextMessage;
                    previousMessage = (index > 0) ? this.messages.getValue().get(index - 1) : null;
                    nextMessage = (index < this.messages.getValue().size() - 1) ? this.messages.getValue().get(index + 1) : null;
                    messageListItem_.setPositions(setPositions(previousMessage, message, nextMessage));
                    setPreviousMessagePosition(previousMessage, nextMessage, message.getUserId());
                    messageEntities.set(this.messageEntities.indexOf(messageListItem), messageListItem_);
                    broadcastValue();
                } else {
                    progressNewMessages(Arrays.asList(message), false);
                }
            } else {
                progressNewMessages(Arrays.asList(message), false);
            }
        });
        // reads observe
        this.reads.observe(owner, reads -> {
            hasNewMessages = false;
            if (reads == null) {
                reads = new ArrayList<>();
            }
            listReads = reads;
            // Check Current User read state
            if (!listReads.isEmpty() && listReads.get(listReads.size() -1).getUserId().equals(currentUser.getId()))
                return;

            // Update last two Items
            if (messages.getValue() == null || messages.getValue().isEmpty()) return;
            Message lastMessage = messages.getValue().get(messages.getValue().size() - 1);
            MessageListItem lastItem = getMessageItemFromMessage(lastMessage);

            Message previousMessage;
            MessageListItem previousItem = null;

            try {
                previousMessage = messages.getValue().get(messages.getValue().size() - 2);
                previousItem = getMessageItemFromMessage(previousMessage);
            }catch (Exception e){}

            if (lastItem != null) {
                MessageListItem lastItem_ = new MessageListItem(lastItem.getMessage(), lastItem.getPositions(), lastItem.isMine());
                messageEntities.set(this.messageEntities.indexOf(lastItem), lastItem_);
            }
            if (previousItem != null) {
                MessageListItem previousItem_ = new MessageListItem(previousItem.getMessage(), previousItem.getPositions(), previousItem.isMine());
                messageEntities.set(this.messageEntities.indexOf(previousItem), previousItem_);
            }
            // Delay
            new Handler().postDelayed(this::broadcastValue, 200);

        });
        // loadMore observe
        this.loadMoreMessages.observe(owner, messages ->
            progressNewMessages(messages, true)
        );
        // typing observe
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

        if (messages == null || messages.isEmpty()) return;
        // update based on messages
        hasNewMessages = false;
        String newlastMessageID = messages.get(messages.size() - 1).getId();
        if (!newlastMessageID.equals(lastMessageID)) {
            hasNewMessages = true;
        }
        lastMessageID = newlastMessageID;

        Message previousMessage = null;
        int size = messages.size();
        int topIndex = Math.max(0, size - 1);

        if (messages.size() == 1) {
            int index = this.messages.getValue().indexOf(messages.get(0));
            if (index != -1) {
                try {
                    previousMessage = this.messages.getValue().get(index - 1);
                }catch (Exception e){
                }

            }
        }

        isLoadingMore = isLoadMoreMessages;
        if (isLoadMoreMessages) {
            hasNewMessages = true;
        }

        for (int i = 0; i < size; i++) {
            Message message = messages.get(i);
            Message nextMessage = null;
            if (i + 1 <= topIndex) {
                nextMessage = messages.get(i + 1);
            }
            if (isLoadMoreMessages) {
                previousMessage = null;
                if (i + 1 <= messages.size() - 1)
                    previousMessage = messages.get(i + 1);


                if (i == 0) {
                    nextMessage = this.messages.getValue().get(0);
                    setNextMessagePosition(message.getUserId(), nextMessage);
                } else
                    nextMessage = messages.get(i - 1);
            }
            // determine if the message is written by the current user
            boolean mine = message.getUser().equals(currentUser);
            // set Delivered
            if (mine && nextMessage != null)
                message.setDelivered(true);
            // determine the position (top, middle, bottom)
            setPositions(previousMessage, message, nextMessage);
            setPreviousMessagePosition(previousMessage, nextMessage, message.getUserId());


            MessageListItem messageListItem = new MessageListItem(message,
                    setPositions(previousMessage, message, nextMessage),
                    mine);

            if (isLoadMoreMessages) {
                this.messages.getValue().add(0, message);
                this.messageEntities.add(0, messageListItem);
                // date separator
                if (previousMessage != null && !isSameDay(previousMessage, message))
                    this.messageEntities.add(0, new MessageListItem(message.getCreatedAt()));

            } else {
                // date separator
                if (previousMessage != null && !isSameDay(previousMessage, message))
                    this.messageEntities.add(new MessageListItem(message.getCreatedAt()));

                this.messageEntities.add(messageListItem);
            }
            // set the previous message for the next iteration
            previousMessage = message;
        }
        broadcastValue();
    }

    private List<MessageViewHolderFactory.Position> setPositions(Message previousMessage, Message message, Message nextMessage) {
        List<MessageViewHolderFactory.Position> positions = new ArrayList<>();
        if (previousMessage == null || !previousMessage.getUser().equals(message.getUser())) {
            positions.add(MessageViewHolderFactory.Position.TOP);
        }

        if (nextMessage == null || !nextMessage.getUser().equals(message.getUser())) {
            positions.add(MessageViewHolderFactory.Position.BOTTOM);
        }

        if (previousMessage != null && nextMessage != null) {
            if (previousMessage.getUser().equals(message.getUser()) && nextMessage.getUser().equals(message.getUser())) {
                positions.add(MessageViewHolderFactory.Position.MIDDLE);
            }
        }
        return positions;
    }

    private void setPreviousMessagePosition(Message previousMessage, Message nextMessage, String useId) {
        if (previousMessage != null && nextMessage == null) {
            if (TextUtils.isEmpty(useId)) return;
            if (previousMessage.getUserId() == null) return;

            if (!previousMessage.getUserId().equals(useId)) return;
            int index = this.messages.getValue().indexOf(previousMessage);
            if (index == -1) return;

            MessageListItem messageListItem = getMessageItemFromMessage(previousMessage);
            if (messageListItem == null) return;
            messageListItem.getPositions().remove(MessageViewHolderFactory.Position.BOTTOM);
            messageListItem.getPositions().add(MessageViewHolderFactory.Position.MIDDLE);
            MessageListItem messageListItem_ = new MessageListItem(previousMessage, messageListItem.getPositions(), messageListItem.isMine());
            messageEntities.set(messageEntities.indexOf(messageListItem), messageListItem_);
        }
    }

    private void setNextMessagePosition(String useId, Message nextMessage) {
        if (nextMessage == null) return;
        if (nextMessage.getUserId() == null) return;
        if (TextUtils.isEmpty(useId)) return;
        if (!nextMessage.getUserId().equals(useId)) return;

        int index = this.messages.getValue().indexOf(nextMessage);
        if (index == -1) return;
        MessageListItem messageListItem = getMessageItemFromMessage(nextMessage);
        if (messageListItem == null) return;
        messageListItem.getPositions().remove(MessageViewHolderFactory.Position.TOP);
        MessageListItem messageListItem_ = new MessageListItem(nextMessage, messageListItem.getPositions(), messageListItem.isMine());
        messageEntities.set(messageEntities.indexOf(messageListItem), messageListItem_);
    }

    private MessageListItem getMessageItemFromMessage(Message message) {
        for (MessageListItem item : this.messageEntities) {
            if (item.getMessage() == null || TextUtils.isEmpty(item.getMessage().getId())) continue;
            if (item.getMessage().getId().equals(message.getId()))
                return item;
        }
        return null;
    }

    public Boolean getHasNewMessages() {
        return hasNewMessages;
    }
}