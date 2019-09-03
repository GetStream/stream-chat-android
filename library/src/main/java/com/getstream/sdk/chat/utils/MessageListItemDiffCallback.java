package com.getstream.sdk.chat.utils;


import android.util.Log;

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
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}