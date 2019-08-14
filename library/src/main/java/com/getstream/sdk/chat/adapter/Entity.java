package com.getstream.sdk.chat.adapter;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;

import java.util.Date;
import java.util.List;

public class Entity {
    private MessageListItemAdapter.EntityType type;
    private Message message;
    private List<MessageViewHolderFactory.Position> positions;
    private Date date;
    private MessageViewHolderFactory.Position messagePosition;
    private Boolean messageMine;
    private List<User> users;

    public Entity(Date date) {
        this.type = MessageListItemAdapter.EntityType.DATE_SEPARATOR;
        this.date = date;
    }

    public Entity(Message message, List<MessageViewHolderFactory.Position> positions, Boolean messageMine) {
        this.type = MessageListItemAdapter.EntityType.MESSAGE;
        this.message = message;
        this.positions = positions;
        this.messageMine = messageMine;
    }

    public Entity(List<User> users) {
        this.type = MessageListItemAdapter.EntityType.TYPING;
        this.users = users;
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
}
