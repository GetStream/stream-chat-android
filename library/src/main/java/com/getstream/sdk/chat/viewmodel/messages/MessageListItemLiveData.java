package com.getstream.sdk.chat.viewmodel.messages;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper;

import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.TaggedLogger;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static kotlin.collections.CollectionsKt.filter;
import static kotlin.collections.CollectionsKt.map;

public class MessageListItemLiveData extends LiveData<MessageListItemWrapper> {

    private TaggedLogger logger = ChatLogger.Companion.get("MessageListItemLiveData");

    private LiveData<List<Message>> messages;
    private LiveData<List<Message>> threadMessages;
    private LiveData<List<User>> typing;
    private LiveData<List<ChannelUserRead>> reads;

    private User currentUser;
    private List<MessageListItem> messageEntities;
    private List<MessageListItem.TypingItem> typingEntities;
    private Boolean isLoadingMore;
    private String lastMessageID;
    private LifecycleOwner lifecycleOwner;
    private List<User> previousTypingUsers;

    private Observer<List<Message>> threadMessagesObserver = this::onThreadMessagesChanged;
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);

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
        previousTypingUsers = new LinkedList<>();
    }

    private void setIsLoadingMore(Boolean loading) {
        isLoadingMore = loading;
    }

    private synchronized void broadcastValue(
            final boolean hasNewMessages,
            final boolean isLoadingMore,
            final List<MessageListItem.TypingItem> typingEntities,
            final List<MessageListItem> messageEntities
    ) {
        List<MessageListItem> merged = new ArrayList<>();

        for (MessageListItem i : messageEntities) {
            merged.add(i.deepCopy());
        }

        // TODO no need to do this whole thing for typing changes!
        // TODO replace with more efficient approach
        // remove the old read state
        for (MessageListItem i : merged) {
            if (i.getMessageReadBy().size() != 0) {
                i.getMessageReadBy().clear();
            }
        }

        // set the new read state
        // this wil become slow with many users and many messages
        if (reads != null && reads.getValue() != null) {
            for (ChannelUserRead userRead : reads.getValue()) {
                // we don't show read state for the current user
                if (userRead.getUser().getId().equals(currentUser.getId())) {
                    continue;
                }
                // skip things that aren't messages
                final List<MessageListItem> filteredItems =
                        filter(merged, listItem -> listItem instanceof MessageListItem.MessageItem);

                final List<MessageListItem.MessageItem> messageItems =
                        map(filteredItems, item -> (MessageListItem.MessageItem) item);

                for (int i = messageItems.size(); i-- > 0; ) {
                    final MessageListItem.MessageItem messageListItem = messageItems.get(i);
                    if (userRead.getUserId().equals(messageListItem.getMessage().getUser().getId())) {
                        // last read message is added by this user, so break
                        break;
                    }
                    if (userRead.getLastRead() != null &&
                            messageListItem.getMessage().getCreatedAt() != null &&
                            !userRead.getLastRead().before(messageListItem.getMessage().getCreatedAt())) {
                        // set the read state on this entity
                        messageListItem.getMessageReadBy().add(userRead);
                        // we only show it for the last message, so break
                        break;
                    }
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
        Date aDate = (a.getCreatedAt() != null) ? a.getCreatedAt() : a.getCreatedLocallyAt();
        Date bDate = (b.getCreatedAt() != null) ? b.getCreatedAt() : b.getCreatedLocallyAt();
        return aDate != null &&
                bDate != null &&
                SIMPLE_DATE_FORMAT.format(aDate).equals(SIMPLE_DATE_FORMAT.format(bDate));
    }

    private boolean isThread() {
        return !(threadMessages.getValue() == null || threadMessages.getValue().isEmpty());
    }

    private void onMessagesChanged(List<Message> messages) {
        if (!isThread()) {
            progressMessages(messages);
        }
    }

    private void onThreadMessagesChanged(List<Message> messages) {
        if (isThread()) {
            progressMessages(messages);
        }
    }

    private void onReadsChanged(List<ChannelUserRead> channelUserRead) {
        logger.logI("broadcast because reads changed");
        broadcastValue(false, isLoadingMore, typingEntities, messageEntities);
    }

    private void onTypingChanged(List<User> users) {
        boolean onlyUserTyping = onlyUserTyping(users);

        previousTypingUsers.clear();
        previousTypingUsers.addAll(users);

        if (isThread() || onlyUserTyping) {
            return;
        }

        // update
        List<MessageListItem.TypingItem> typingEntities = new ArrayList<>();

        if (users.size() > 0) {
            MessageListItem.TypingItem messageListItem = new MessageListItem.TypingItem(users);
            typingEntities.add(messageListItem);
        }

        this.typingEntities = typingEntities;

        logger.logI("broadcast because typing changed");
        broadcastValue(false, isLoadingMore, typingEntities, messageEntities);
    }

    private boolean onlyUserTyping(List<User> users) {
        boolean isTheirs = hasOtherUser(users);
        boolean wasTheirs = hasOtherUser(previousTypingUsers);
        return !isTheirs && !wasTheirs;
    }

    private boolean hasOtherUser(List<User> users) {
        for (User user : users) {
            if (!isCurrentUser(user.getId())) {
                return true;
            }
        }

        return false;
    }

    private boolean isCurrentUser(String id) {
        return id.equals(currentUser.getId());
    }

    private void progressMessages(List<Message> messages) {
        if (messages == null || messages.size() == 0) return;
        // update based on messages
        boolean hasNewMessages = false;
        String newlastMessageID = messages.get(messages.size() - 1).getId();
        if (!newlastMessageID.equals(lastMessageID)) {
            hasNewMessages = true;
        }
        lastMessageID = newlastMessageID;
        List<MessageListItem> entities = new ArrayList<>();
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
            boolean mine = isCurrentUser(message.getUser().getId());
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
            final Date date = message.getCreatedAt();
            if (previousMessage != null && !isSameDay(previousMessage, message) && date != null) {
                entities.add(new MessageListItem.DateSeparatorItem(date));
            }

            MessageListItem messageListItem = new MessageListItem.MessageItem(message, positions, mine);
            entities.add(messageListItem);

            // Insert Thread Separator
            if (isThread() && i == 0) {
                entities.add(new MessageListItem.ThreadSeparatorItem());
                previousMessage = null;
            } else {
                // set the previous message for the next iteration
                previousMessage = message;
            }
        }
        this.messageEntities.clear();
        this.messageEntities.addAll(entities);
        logger.logI("broadcast because messages changed");
        broadcastValue(hasNewMessages, isLoadingMore, typingEntities, entities);
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner,
                        @NonNull Observer<? super MessageListItemWrapper> observer) {
        super.observe(owner, observer);
        this.lifecycleOwner = owner;

        this.reads.observe(owner, this::onReadsChanged);
        this.messages.observe(owner, this::onMessagesChanged);
        this.threadMessages.observe(owner, threadMessagesObserver);
        this.typing.observe(owner, this::onTypingChanged);
    }

    @Override
    public void observeForever(@NonNull Observer<? super MessageListItemWrapper> observer) {
        super.observeForever(observer);

        this.reads.observeForever(this::onReadsChanged);
        this.messages.observeForever(this::onMessagesChanged);
        this.threadMessages.observeForever(threadMessagesObserver);
        this.typing.observeForever(this::onTypingChanged);
    }

    public void setThreadMessages(@NotNull LiveData<List<Message>> threadMessages) {
        // remove the old observer
        this.threadMessages.removeObserver(threadMessagesObserver);

        // setup the new observer
        this.threadMessages = threadMessages;
        if (lifecycleOwner == null) {
            threadMessages.observeForever(threadMessagesObserver);
        } else {
            threadMessages.observe(lifecycleOwner, threadMessagesObserver);
        }

        // trigger an update
        onThreadMessagesChanged(threadMessages.getValue());
    }

    public void resetThread() {
        threadMessages = new MutableLiveData<>();
        onMessagesChanged(this.messages.getValue());
    }
}
