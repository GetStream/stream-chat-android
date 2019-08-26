package com.getstream.sdk.chat.utils;


import androidx.recyclerview.widget.DiffUtil;

import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.rest.Message;


import java.util.List;

public class MessageListItemDiffCallback extends DiffUtil.Callback {

    private static final String TAG = MessageListItemDiffCallback.class.getSimpleName();

    protected List<MessageListItem> oldList, newList;

    public MessageListItemDiffCallback(List<MessageListItem> oldList, List<MessageListItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null ? 0 : oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList == null ? 0 : newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {

        MessageListItem oldMessageItem = oldList.get(oldItemPosition);
        MessageListItem newMessageItem = newList.get(newItemPosition);

        Message oldMessage = oldMessageItem.getMessage();
        Message newMessage = newMessageItem.getMessage();

        if (oldMessage == null || newMessage == null) {
            return false;
        }

        if (oldMessage != null && oldMessage.getId() != null && oldMessage.getId().equals(newMessage.getId())) {
            return true;
        }

        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        MessageListItem oldMessageItem = oldList.get(oldItemPosition);
        MessageListItem newMessageItem = newList.get(newItemPosition);

        return newMessageItem.equals(oldMessageItem);
    }
}