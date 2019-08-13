package com.getstream.sdk.chat.utils;

import android.text.TextUtils;

import androidx.recyclerview.widget.DiffUtil;

import com.getstream.sdk.chat.model.Channel;


import java.util.List;

public class ChannelListDiffCallback extends DiffUtil.Callback {
    protected List<Channel> oldList, newList;

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
        return TextUtils.equals(oldList.get(oldItemPosition).getCid(), newList.get(newItemPosition).getCid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        // read state
        // last message
        // channel members
        // channel name
        // TODO: fix this
        Channel oldChannel = oldList.get(oldItemPosition);
        Channel newChannel =  newList.get(newItemPosition);


        return true;
    }
}