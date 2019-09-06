package com.getstream.sdk.chat.adapter;


import androidx.annotation.Nullable;

import com.getstream.sdk.chat.enums.EntityType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MessageListItem {

    private static final String TAG = MessageListItem.class.getSimpleName();

    private EntityType type;
    private Message message;
    private List<ChannelUserRead> messageReadBy;
    private List<MessageViewHolderFactory.Position> positions;
    private Date date;
    private Boolean messageMine;
    private List<User> users;

    public MessageListItem(Date date) {
        this.type = EntityType.DATE_SEPARATOR;
        this.date = date;
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(Message message, List<MessageViewHolderFactory.Position> positions, Boolean messageMine) {
        this.type = EntityType.MESSAGE;
        this.message = message;
        this.positions = positions;
        this.messageMine = messageMine;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(List<User> users) {
        this.type = EntityType.TYPING;
        this.users = users;
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(EntityType entityType) {
        this.type = entityType;
        this.date = new Date();
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem copy() {
        MessageListItem clone = new MessageListItem(message, positions, messageMine);
        clone.date = date;
        clone.type = type;
        clone.users = users;
        clone.messageReadBy.addAll(messageReadBy);
        return clone;
    }

    // TODO: make this a little bit more compact (ie. ensure lists are not null higher up in the code)
    boolean samePositions(List<MessageViewHolderFactory.Position> a, List<MessageViewHolderFactory.Position> b) {
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        }
        if (a == null) {
            return true;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }

    // TODO: make this a little bit more compact (ie. ensure lists are not null higher up in the code)
    boolean sameReads(List<ChannelUserRead>a, List<ChannelUserRead> b){
        if ((a == null && b != null) || (a != null && b == null)) {
            return false;
        }
        if (a == null) {
            return true;
        }
        if (a.size() == 0 && b.size() == 0) {
            return true;
        }
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!a.get(i).equals(b.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        MessageListItem other = (MessageListItem) obj;

        if (other.type != type) {
            return false;
        }

        switch (type) {
            case TYPING:
                return false;
            case THREAD_SEPARATOR:
                return false;
            case MESSAGE:
                boolean sameReads = sameReads(other.messageReadBy, messageReadBy);
                boolean samePositions = samePositions(other.positions, positions);
                boolean sameMessage = Objects.equals(other.message, message);
                return sameMessage && samePositions && sameReads;
            case DATE_SEPARATOR:
                return Objects.equals(other.date, date);
        }

        return false;
    }

    long getStableID(){
        Checksum checksum = new CRC32();
        String plaintext = type.toString() + ":";
        switch (type) {
            case TYPING:
                plaintext += "typing";
                break;
            case THREAD_SEPARATOR:
                plaintext += "Start of a new thread";
                break;
            case MESSAGE:
                plaintext += message.getId();
                break;
            case DATE_SEPARATOR:
                plaintext += date.toString();
                break;
        }
        checksum.update(plaintext.getBytes(), 0, plaintext.getBytes().length);
        return checksum.getValue();
    }

    public boolean isMine() {
        return this.messageMine;
    }

    boolean isTheirs() {
        return !this.messageMine;
    }

    public Message getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public List<User> getUsers() {
        return users;
    }

    public EntityType getType() {
        return type;
    }

    public List<MessageViewHolderFactory.Position> getPositions() {
        return positions;
    }

    public void setPositions(List<MessageViewHolderFactory.Position> positions) {
        this.positions = positions;
    }

    public List<ChannelUserRead> getMessageReadBy() {
        return messageReadBy;
    }

    public void removeMessageReadBy() {
        messageReadBy = new ArrayList<>();
    }

    public void addMessageReadBy(ChannelUserRead r) {
        messageReadBy.add(r);
    }

}
