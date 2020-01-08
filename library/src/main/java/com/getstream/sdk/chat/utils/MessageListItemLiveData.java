package com.getstream.sdk.chat.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.getstream.sdk.chat.adapter.MessageViewHolderFactory.MESSAGEITEM_MESSAGE;
import static com.getstream.sdk.chat.adapter.MessageViewHolderFactory.MESSAGEITEM_THREAD_SEPARATOR;


public class MessageListItemLiveData extends LiveData<MessageListItemWrapper> {

    private static final String TAG = MessageListItemLiveData.class.getSimpleName();

    private MutableLiveData<List<Message>> messages;
    private MutableLiveData<List<Message>> threadMessages;
    private MutableLiveData<List<User>> typing;
    private MutableLiveData<Map<String, ChannelUserRead>> reads;

    private User currentUser;
    private List<MessageListItem> messageEntities;
    private List<MessageListItem> typingEntities;
    private Map<String, ChannelUserRead> readsByUser;
    private Boolean isLoadingMore;
    private Boolean hasNewMessages;
    private String lastMessageID;


    public MessageListItemLiveData(User currentUser,
                                   MutableLiveData<List<Message>> messages,
                                   MutableLiveData<List<Message>> threadMessages,
                                   MutableLiveData<List<User>> typing,
                                   MutableLiveData<Map<String, ChannelUserRead>> reads) {
        this.messages = messages;
        this.threadMessages = threadMessages;
        this.currentUser = currentUser;
        this.typing = typing;
        this.reads = reads;
        this.messageEntities = new ArrayList<>();
        this.typingEntities = new ArrayList<>();
        this.readsByUser = new HashMap<>();
        this.isLoadingMore = false;
        // scroll behaviour is only triggered for new messages
        this.lastMessageID = "";
        this.hasNewMessages = false;
    }

    public void setIsLoadingMore(Boolean loading) {
        isLoadingMore = loading;
    }

    private synchronized void broadcastValue() {
        List<MessageListItem> merged = new ArrayList<>();

        for (MessageListItem i : messageEntities) {
            merged.add(i.copy());
        }

        // TODO no need to do this whole thing for typing changes!
        // TODO replace with more efficient approach
        // remove the old read state
        for (MessageListItem i : merged) {
            if (i.getMessageReadBy().size() != 0) {
                i.removeMessageReadBy();
            }
        }

        // set the new read state
        // this wil become slow with many users and many messages
        for (Map.Entry<String, ChannelUserRead> entry : readsByUser.entrySet()) {
            // we don't show read state for the current user
            if (entry.getValue().getUser().getId().equals(currentUser.getId())) {
                continue;
            }
            for (int i = merged.size(); i-- > 0; ) {
                MessageListItem e = merged.get(i);
                ChannelUserRead userRead = entry.getValue();
                // skip things that aren't messages
                if (e.getType() != MESSAGEITEM_MESSAGE) {
                    continue;
                }
                // skip message owner as reader
                if (userRead.getUserId().equals(e.getMessage().getUserId())) {
                    continue;
                }
                if (userRead.getLastRead().after(e.getMessage().getCreatedAt())) {
                    // set the read state on this entity
                    e.addMessageReadBy(userRead);
                    // we only show it for the last message, so break
                    break;
                }
            }
        }

        merged.addAll(typingEntities);

        MessageListItemWrapper wrapper = new MessageListItemWrapper(isLoadingMore, hasNewMessages, merged);
        // Typing
        wrapper.setTyping(!typingEntities.isEmpty());
        // Thread
        wrapper.setThread(isThread());

        // run setValue on main thread now that the whole computation is done
        new Handler(Looper.getMainLooper()).post(() -> {
            setValue(wrapper);
            if (isLoadingMore) {
                this.setIsLoadingMore(false);
            }
        });
    }

    private boolean isSameDay(Message a, Message b) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(a.getCreatedAt()).equals(fmt.format(b.getCreatedAt()));
    }

    private boolean isThread() {
        return !(threadMessages.getValue() == null || threadMessages.getValue().isEmpty());
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner,
                        @NonNull Observer<? super MessageListItemWrapper> observer) {
        super.observe(owner, observer);

        this.reads.observe(owner, reads -> {
            hasNewMessages = false;
            if (reads == null) {
                reads = new HashMap<>();
            }
            readsByUser = reads;
            StreamChat.getLogger().logI(this,"broadcast because reads changed");
            broadcastValue();
        });

        messages.observe(owner, messages -> {
            if (threadMessages.getValue() != null) return;
            progressMessages(messages);
        });

        threadMessages.observe(owner, this::progressMessages);

        this.typing.observe(owner, users -> {
            if (isThread()) return;
            // update
            hasNewMessages = false;
            List<MessageListItem> typingEntities = new ArrayList<>();
            if (users.size() > 0) {
                MessageListItem messageListItem = new MessageListItem(users);
                typingEntities.add(messageListItem);
            }
            this.typingEntities = typingEntities;
            StreamChat.getLogger().logI(this,"broadcast because typing changed");
            broadcastValue();
        });
    }

    public void progressMessages(List<Message> messages) {
        if (messages == null || messages.size() == 0) return;
        // update based on messages
        hasNewMessages = false;
        String newlastMessageID = messages.get(messages.size() - 1).getId();
        if (!newlastMessageID.equals(lastMessageID)) {
            hasNewMessages = true;
        }
        lastMessageID = newlastMessageID;
        List<MessageListItem> entities = new ArrayList<MessageListItem>();
        // iterate over messages and stick in the date entities
        Message previousMessage = null;
        int size = messages.size();
        int topIndex = Math.max(0, size - 1);
        for (int i = 0; i < size; i++) {
            Message message = messages.get(i);
            Message nextMessage = null;
            if (i + 1 <= topIndex) {
                nextMessage = messages.get(i + 1);
            }

            // Thread
            if (isThread() && i == 0)
                nextMessage = null;

            // determine if the message is written by the current user
            Boolean mine = message.getUser().equals(currentUser);
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
            // date separator
            if (previousMessage != null && !isSameDay(previousMessage, message))
                entities.add(new MessageListItem(message.getCreatedAt()));

            MessageListItem messageListItem = new MessageListItem(message, positions, mine);
            entities.add(messageListItem);

            // Insert Thread Separator
            if (isThread() && i == 0) {
                entities.add(new MessageListItem(MESSAGEITEM_THREAD_SEPARATOR));
                previousMessage = null;
            }else{
                // set the previous message for the next iteration
                previousMessage = message;
            }
        }
        this.messageEntities.clear();
        this.messageEntities.addAll(entities);
        StreamChat.getLogger().logI(this,"broadcast because messages changed");
        broadcastValue();
    }

    public Boolean getHasNewMessages() {
        return hasNewMessages;
    }
}