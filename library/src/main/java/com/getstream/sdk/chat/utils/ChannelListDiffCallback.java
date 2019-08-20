package com.getstream.sdk.chat.utils;

import android.util.Log;

import androidx.recyclerview.widget.DiffUtil;

import com.getstream.sdk.chat.model.Channel;

import java.util.List;

public class ChannelListDiffCallback extends DiffUtil.Callback {
    private final static String TAG = ChannelListDiffCallback.class.getSimpleName();
    private List<Channel> oldList, newList;

    public ChannelListDiffCallback(List<Channel> oldList, List<Channel> newList) {
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
        // read state
        // last message
        // channel members
        // channel name
        // channel extra data
        // TODO: fix this
        Channel oldChannel = oldList.get(oldItemPosition);
        Channel newChannel =  newList.get(newItemPosition);

//        Log.i(TAG, "areContentsTheSame?");
//        Log.i(TAG, oldChannel.getLastMessageDate().toString());
//        Log.i(TAG, newChannel.getLastMessageDate().toString());

        if (!oldChannel.getLastMessageDate().equals(newChannel.getLastMessageDate())) {
            Log.i(TAG, "LastMessage is different");
            return false;
        }

        if (!oldChannel.getName().contentEquals(newChannel.getName())) {
            Log.i(TAG, "channel name is different");
            return false;
        }

        return false;
    }
}