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

        MessageListItem oldMessageItem = oldList.get(oldItemPosition);
        MessageListItem newMessageItem = newList.get(newItemPosition);

        Message oldMessage = oldMessageItem.getMessage();
        Message newMessage = newMessageItem.getMessage();

//        Log.i(TAG,"oldItemPosition :" +oldItemPosition);
//        Log.i(TAG,"newItemPosition :" +newItemPosition);
//        try {
//            Log.i(TAG,"oldMessage :" +oldMessageItem.getMessage().getText());
//            Log.i(TAG,"newMessage :" +newMessageItem.getMessage().getText());
//        }catch (Exception e){}

        if (oldMessage == null && newMessage == null) {
//            Log.i(TAG,"case:0: true");
            return true;
        }

        if (oldMessage == null || newMessage == null) {
//            Log.i(TAG,"case:1: false");
            return false;
        }

        if (oldMessage.getId() != null && oldMessage.getId().equals(newMessage.getId())) {
//            Log.i(TAG,"case:2: true");
            return true;
        }
        boolean equal = oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
//        Log.i(TAG,"default: " + equal);

        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        MessageListItem oldMessageItem = oldList.get(oldItemPosition);
        MessageListItem newMessageItem = newList.get(newItemPosition);

//        Log.i(TAG,"oldItemPosition :" +oldItemPosition);
//        Log.i(TAG,"newItemPosition :" +newItemPosition);
//        try {
//            Log.i(TAG,"oldMessage :" +oldMessageItem.getMessage().getText());
//            Log.i(TAG,"newMessage :" +newMessageItem.getMessage().getText());
//        }catch (Exception e){}

        return newMessageItem.equals(oldMessageItem);
    }
}