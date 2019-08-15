package com.getstream.sdk.chat.utils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.getstream.sdk.chat.adapter.Entity;
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class EntityLiveData extends LiveData<EntityListWrapper> {
    private MutableLiveData<List<Message>> messages;
    private MutableLiveData<List<User>> typing;
    private MutableLiveData<List<ChannelUserRead>> reads;

    private User currentUser;
    private List<Entity> messageEntities;
    private List<Entity> typingEntities;
    private List<ChannelUserRead> listReads;
    private Boolean isLoadingMore;


    public EntityLiveData(User currentUser, MutableLiveData<List<Message>> messages, MutableLiveData<List<User>> typing, MutableLiveData<List<ChannelUserRead>> reads) {
        this.messages = messages;
        this.currentUser = currentUser;
        this.typing = typing;
        this.reads = reads;
        this.messageEntities = new ArrayList<>();
        this.typingEntities = new ArrayList<>();
        this.listReads = new ArrayList<>();
        this.isLoadingMore = false;
    }

    public void setIsLoadingMore(Boolean loading) {
        isLoadingMore = loading;
    }

    private void broadcastValue() {
        List<Entity> merged = new ArrayList<>();
        merged.addAll(messageEntities);

        // TODO replace with more efficient approach
        // this wil become slow with many users and many messages
        for (ChannelUserRead r : listReads) {
            for (int i = merged.size(); i-- > 0; ) {
                Entity e = merged.get(i);
                if (r.getLast_read().getTime() > e.getMessage().getCreatedAt().getTime()) {
                    // set the read state on this entity
                    e.addMessageReadBy(r);
                }
            }
        }

        merged.addAll(typingEntities);
        EntityListWrapper wrapper = new EntityListWrapper(isLoadingMore, merged);
        setValue(wrapper);
        // isLoadingMore is only true once...
        if (isLoadingMore) {
            this.setIsLoadingMore(true);
        }
    }

    private boolean isSameDay(Message a, Message b) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(a.getCreatedAt()).equals(fmt.format(b.getCreatedAt()));
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<? super EntityListWrapper> observer) {
        super.observe(owner, observer);
        this.reads.observe(owner, reads -> {
            listReads = reads;
            broadcastValue();
        });
        this.messages.observe(owner, messages -> {
            // update based on messages
            List<Entity> entities = new ArrayList<Entity>();
            // iterate over messages and stick in the date entities
            Message previousMessage = null;
            int size = messages.size();
            int topIndex = Math.max(0, size -1);
            for (int i = 0; i < size; i++) {
                Message message = messages.get(i);
                Message nextMessage = null;
                if (i +1 <= topIndex){
                    nextMessage = messages.get(i+1);
                }

                // determine if the message is written by the current user
                Boolean mine = message.getUser().equals(currentUser);
                // determine the position (top, middle, bottom)
                User user = message.getUser();
                List<MessageViewHolderFactory.Position> positions = new ArrayList<MessageViewHolderFactory.Position>();
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
                if (previousMessage != null && !isSameDay(previousMessage, message)) {
                    entities.add(new Entity(message.getCreatedAt()));
                }

                Entity entity = new Entity(message,positions, mine);
                entities.add(entity);
                // set the previous message for the next iteration
                previousMessage = message;
            }

            this.messageEntities = entities;
            broadcastValue();
        });
        this.typing.observe(owner, users -> {
            // update
            List<Entity> typingEntities = new ArrayList<Entity>();
            if (users.size() > 0) {
                Entity entity = new Entity(users);
                typingEntities.add(entity);
            }
            this.typingEntities = typingEntities;
            broadcastValue();
        });

    }
}