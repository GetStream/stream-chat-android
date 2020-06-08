package com.getstream.sdk.chat.viewmodel.messages;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;

import static com.getstream.sdk.chat.adapter.MessageViewHolderFactory.MESSAGEITEM_MESSAGE;
import static com.getstream.sdk.chat.adapter.MessageViewHolderFactory.MESSAGEITEM_THREAD_SEPARATOR;

public class MessageListItemLiveData extends LiveData<MessageListItemWrapper> {

    private TaggedLogger logger = ChatLogger.Companion.get("MessageListItemLiveData");

    private LiveData<List<Message>> messages;
    private LiveData<List<Message>> threadMessages;
    private LiveData<List<User>> typing;
    private LiveData<List<ChannelUserRead>> reads;

    private User currentUser;
    private List<MessageListItem> messageEntities;
    private List<MessageListItem> typingEntities;
    private Boolean isLoadingMore;
    private Boolean hasNewMessages;
    private String lastMessageID;
    private LifecycleOwner lifecycleOwner;


    public MessageListItemLiveData(User currentUser,
                                   LiveData<List<Message>> messages,
                                   LiveData<List<Message>> threadMessages,
                                   LiveData<List<User>> typing,
                                   LiveData<List<ChannelUserRead>> reads) {
        this.messages = messages;
        this.threadMessages = threadMessages;
        this.currentUser = currentUser;
        this.typing = typing;
        this.reads = reads;
        this.messageEntities = new ArrayList<>();
        this.typingEntities = new ArrayList<>();
        this.isLoadingMore = false;
        // scroll behaviour is only triggered for new messages
        this.lastMessageID = "";
        this.hasNewMessages = false;
    }

    private void setIsLoadingMore(Boolean loading) {
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
        for (ChannelUserRead userRead : reads.getValue()) {
            // we don't show read state for the current user
            if (userRead.getUser().getId().equals(currentUser.getId())) {
                continue;
            }
            for (int i = merged.size(); i-- > 0; ) {
                MessageListItem e = merged.get(i);
                // skip things that aren't messages
                if (e.getType() != MESSAGEITEM_MESSAGE) {
                    continue;
                }
                // skip message owner as reader
                if (userRead.getUserId().equals(e.getMessage().getUser().getId())) {
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

        final boolean isTyping = !typingEntities.isEmpty();
        MessageListItemWrapper wrapper =
                new MessageListItemWrapper(
                        isLoadingMore,
                        hasNewMessages,
                        merged,
                        isTyping,
                        isThread()
                );

        postValue(wrapper);
        if (isLoadingMore) {
            this.setIsLoadingMore(false);
        }
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
        this.lifecycleOwner = owner;

        this.reads.observe(owner, reads -> {
            hasNewMessages = false;
            logger.logI("broadcast because reads changed");
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
            logger.logI("broadcast because typing changed");
            broadcastValue();
        });
    }

    @Override
    public void observeForever(@NonNull Observer<? super MessageListItemWrapper> observer) {
        super.observeForever(observer);
        this.reads.observeForever(reads -> {
            hasNewMessages = false;
            logger.logI("broadcast because reads changed");
            broadcastValue();
        });

        messages.observeForever(messages -> {
            if (threadMessages.getValue() != null) return;
            progressMessages(messages);
        });

        threadMessages.observeForever(this::progressMessages);

        this.typing.observeForever(users -> {
            if (isThread()) return;
            // update
            hasNewMessages = false;
            List<MessageListItem> typingEntities = new ArrayList<>();
            if (users.size() > 0) {
                MessageListItem messageListItem = new MessageListItem(users);
                typingEntities.add(messageListItem);
            }
            this.typingEntities = typingEntities;
            logger.logI("broadcast because typing changed");
            broadcastValue();
        });
    }

    private void progressMessages(List<Message> messages) {
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
            } else {
                // set the previous message for the next iteration
                previousMessage = message;
            }
        }
        this.messageEntities.clear();
        this.messageEntities.addAll(entities);
        logger.logI("broadcast because messages changed");
        broadcastValue();
    }

    public Boolean getHasNewMessages() {
        return hasNewMessages;
    }

    public void setThreadMessages(@NotNull LiveData<List<Message>> threadMessages) {
        // remove the old observer
        this.threadMessages.removeObserver(this::progressMessages);

        // setup the new observer
        this.threadMessages = threadMessages;
        if (lifecycleOwner == null) {
            threadMessages.observeForever(this::progressMessages);
        } else {
            threadMessages.observe(lifecycleOwner, this::progressMessages);
        }

        // trigger an update
        progressMessages(threadMessages.getValue());
    }
}