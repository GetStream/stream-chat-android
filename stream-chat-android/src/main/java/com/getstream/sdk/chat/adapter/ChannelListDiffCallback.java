package com.getstream.sdk.chat.adapter;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;

import static com.getstream.sdk.chat.utils.LlcMigrationUtils.computeLastMessage;
import static com.getstream.sdk.chat.utils.LlcMigrationUtils.currentUserRead;
import static com.getstream.sdk.chat.utils.LlcMigrationUtils.equalsLastMessageDate;
import static com.getstream.sdk.chat.utils.LlcMigrationUtils.equalsName;
import static com.getstream.sdk.chat.utils.LlcMigrationUtils.equalsUserLists;
import static com.getstream.sdk.chat.utils.LlcMigrationUtils.getOtherUsers;
import static com.getstream.sdk.chat.utils.LlcMigrationUtils.lastMessagesAreTheSame;

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
        Channel oldItem = oldList.get(oldItemPosition);
        Channel newItem = newList.get(newItemPosition);
        return oldItem.getCid().equals(newItem.getCid());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        Channel oldChannel = oldList.get(oldItemPosition);
        Channel newChannel = newList.get(newItemPosition);

        boolean contentTheSame = true;

        if (!oldChannel.getCid().equals(newChannel.getCid())) {
            contentTheSame = false;
        } else if (oldChannel.getUpdatedAt() == null && newChannel.getUpdatedAt() != null) {
            contentTheSame = false;
        } else if (newChannel.getUpdatedAt() != null && oldChannel.getUpdatedAt().getTime() < newChannel.getUpdatedAt().getTime()) {
            contentTheSame = false;
        } else if (!oldChannel.getExtraData().equals(newChannel.getExtraData())) {
            contentTheSame = false;
        } else if (!lastMessagesAreTheSame(oldChannel, newChannel)) {
            contentTheSame = false;
        } else if (currentUserRead(oldChannel, newChannel)) {
            contentTheSame = false;
        }

        return contentTheSame;
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        ChannelItemPayloadDiff diff = new ChannelItemPayloadDiff();

        Channel oldChannel = oldList.get(oldItemPosition);
        Channel newChannel = newList.get(newItemPosition);

        Message oldLastMessage = computeLastMessage(oldChannel);
        Message newLastMessage = computeLastMessage(newChannel);

        if (oldLastMessage != null && newLastMessage != null) {
            diff.lastMessage = !oldLastMessage.getId().equals(newLastMessage.getId());
        }

        diff.name = !equalsName(newChannel, oldChannel);
        diff.avatarView = !equalsUserLists(getOtherUsers(oldChannel.getMembers()), getOtherUsers(newChannel.getMembers()));
        diff.readState = currentUserRead(oldChannel, newChannel);
        diff.lastMessageDate = !equalsLastMessageDate(oldChannel, newChannel);

        return diff;
    }


}