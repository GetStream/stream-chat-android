package com.getstream.sdk.chat.adapter;

import androidx.recyclerview.widget.DiffUtil;

import com.getstream.sdk.chat.model.Channel;

import java.util.List;

public class ChannelListDiffCallback extends DiffUtil.Callback {
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
        Channel oldChannel = oldList.get(oldItemPosition);
        Channel newChannel = newList.get(newItemPosition);

        if (oldChannel.getUpdatedAt() == null && newChannel.getUpdatedAt() != null) {
            return false;
        }

        if (newChannel.getUpdatedAt() != null && oldChannel.getUpdatedAt().getTime() < newChannel.getUpdatedAt().getTime()) {
            return false;
        }

        if (oldChannel.getLastMessageDate() == null && newChannel.getLastMessageDate() != null) {
            return false;
        }

        if (newChannel.getLastMessageDate() != null && oldChannel.getLastMessageDate().getTime() < newChannel.getLastMessageDate().getTime()) {
            return false;
        }

        if (!oldChannel.getExtraData().equals(newChannel.getExtraData())) {
            return false;
        }

        return oldChannel.getChannelState().getLastReader() == newChannel.getChannelState().getLastReader();
    }
}