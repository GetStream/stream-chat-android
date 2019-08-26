package com.getstream.sdk.chat.adapter;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageListItem {
    private MessageListItemAdapter.EntityType type;
    private Message message;
    private List<ChannelUserRead> messageReadBy;
    private List<MessageViewHolderFactory.Position> positions;
    private Date date;
    private MessageViewHolderFactory.Position messagePosition;
    private Boolean messageMine;
    private List<User> users;

    public MessageListItem(Date date) {
        this.type = MessageListItemAdapter.EntityType.DATE_SEPARATOR;
        this.date = date;
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(Message message, List<MessageViewHolderFactory.Position> positions, Boolean messageMine) {
        this.type = MessageListItemAdapter.EntityType.MESSAGE;
        this.message = message;
        this.positions = positions;
        this.messageMine = messageMine;
        this.messageReadBy = new ArrayList<>();
    }

    public MessageListItem(List<User> users) {
        this.type = MessageListItemAdapter.EntityType.TYPING;
        this.users = users;
        this.messageMine = false;
        this.messageReadBy = new ArrayList<>();
    }

    public boolean isMine() {
        return this.messageMine;
    }

    public boolean isTheirs() {
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

    public MessageListItemAdapter.EntityType getType() {
        return type;
    }

    public List<MessageViewHolderFactory.Position> getPositions() {
        return positions;
    }

    public List<ChannelUserRead> getMessageReadBy() {
        return messageReadBy;
    }

    public void removeMessageReadBy() {
        this.messageReadBy = new ArrayList<>();
    }

    public void addMessageReadBy(ChannelUserRead r) {
        this.messageReadBy.add(r);
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (obj == null) return false;
        if (this != obj) return false;

        MessageListItem other = (MessageListItem) obj;

        Message newMessage = this.getMessage();
        Message oldMessage = other.getMessage();

        if (oldMessage == null || newMessage == null) {
            return false;
        }


        if (!oldMessage.equals(newMessage)) return false;

        if (oldMessage.getUpdatedAt() != null && oldMessage.getUpdatedAt().getTime() < newMessage.getUpdatedAt().getTime()) {
            return false;
        }

        if (oldMessage.getText() == null && newMessage.getText() != null){
            return false;
        }

        if (!TextUtils.equals(oldMessage.getText(), newMessage.getText())){
            return false;
        }


        if (oldMessage.getAttachments() == null && newMessage.getAttachments() != null){
            return false;
        }

        if (!oldMessage.getAttachments().equals(newMessage.getAttachments())){
            return false;
        }

        if (oldMessage.getReactionCounts() != null && oldMessage.getReactionCounts() == null && newMessage.getReactionCounts() != null){
            return false;
        }

        if (oldMessage.getReactionCounts() != null && !oldMessage.getReactionCounts().equals(newMessage.getReactionCounts())){
            return false;
        }

        return super.equals(obj);
    }
}
