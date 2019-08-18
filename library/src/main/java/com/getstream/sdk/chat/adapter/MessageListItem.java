package com.getstream.sdk.chat.adapter;

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

    public void addMessageReadBy(ChannelUserRead r) {
        this.messageReadBy.add(r);
    }
}
