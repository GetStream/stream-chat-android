package com.getstream.sdk.chat.adapter;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MessageListItem {

    private static final String TAG = MessageListItem.class.getSimpleName();

    private MessageListItemAdapter.EntityType type;
    private Message message;
    private List<ChannelUserRead> messageReadBy;
    private List<MessageViewHolderFactory.Position> positions;
    private Date date;
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

    public void setPositions(List<MessageViewHolderFactory.Position> positions) {
        this.positions = positions;
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

        if (obj == null) {
            Log.i(TAG,"case:0: false");
            return false;
        }

        MessageListItem other = (MessageListItem) obj;

        Message newMessage = this.getMessage();
        Message oldMessage = other.getMessage();

        if (oldMessage == null || newMessage == null) {
            Log.i(TAG,"case:2: false");
            return false;
        }


        if (!oldMessage.equals(newMessage)) {
            Log.i(TAG,"case:3: false");
            return false;
        }

        if (oldMessage.getUpdatedAt() != null && oldMessage.getUpdatedAt().getTime() < newMessage.getUpdatedAt().getTime()) {
            Log.i(TAG,"case:4: false");
            return false;
        }

        if (oldMessage.getText() == null && newMessage.getText() != null){
            Log.i(TAG,"case:5: false");
            return false;
        }

        if (!TextUtils.equals(oldMessage.getText(), newMessage.getText())){
            Log.i(TAG,"case:6: false");
            return false;
        }


        if (oldMessage.getAttachments() == null && newMessage.getAttachments() != null){
            Log.i(TAG,"case:7: false");
            return false;
        }

        if (!oldMessage.getAttachments().equals(newMessage.getAttachments())){
            Log.i(TAG,"case:8: false");
            return false;
        }

        if (oldMessage.getReactionCounts() != null && oldMessage.getReactionCounts() == null && newMessage.getReactionCounts() != null){
            Log.i(TAG,"case:9: false");
            return false;
        }

        if (oldMessage.getReactionCounts() != null && !oldMessage.getReactionCounts().equals(newMessage.getReactionCounts())){
            Log.i(TAG,"case:10: false");
            return false;
        }

        if (other.getMessageReadBy().isEmpty() && !this.getMessageReadBy().isEmpty() ){
            Log.i(TAG,"case:11: false");
            return false;
        }


        if (!other.getMessageReadBy().isEmpty() && !other.getMessageReadBy().equals(this.getMessageReadBy())) {
            Log.i(TAG,"case:12: false");
            return false;
        }
        boolean equel = super.equals(obj);
        Log.i(TAG,"default: " + equel);

        return super.equals(obj);
    }
}
