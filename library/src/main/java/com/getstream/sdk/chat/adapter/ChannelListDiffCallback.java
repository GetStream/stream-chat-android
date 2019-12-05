package com.getstream.sdk.chat.adapter;

import androidx.recyclerview.widget.DiffUtil;

import com.getstream.sdk.chat.model.Channel;

import java.util.List;
import java.util.Objects;

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
        return Objects.equals(oldList.get(oldItemPosition).getCid(), newList.get(newItemPosition).getCid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }
}