package com.getstream.sdk.chat.adapter;

import com.getstream.sdk.chat.StreamChat;

import java.util.List;

import androidx.recyclerview.widget.DiffUtil;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;

import static com.getstream.sdk.chat.utils.LlcMigrationUtils.computeLastMessage;

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
        // Check Message Update
        Message oldLastMessage = computeLastMessage(oldChannel);
        Message newLastMessage = computeLastMessage(newChannel);
        if (oldLastMessage != null &&
                newLastMessage != null &&
                newLastMessage.getUpdatedAt() != null &&
                oldLastMessage.getUpdatedAt() != null &&
                oldLastMessage.getUpdatedAt().getTime() < newLastMessage.getUpdatedAt().getTime()) {
            return false;
        }
        // Check Message Delete
        if (oldLastMessage != null && !oldLastMessage.equals(newLastMessage)) {
            return false;
        }

        User oldChannelUser = getLastReader(oldChannel);
        User newChannelUser = getLastReader(newChannel);

        if (oldChannelUser != null && newChannelUser != null) {
            return oldChannelUser.getId().equals(newChannelUser.getId());
        } else {
            return false;
        }
    }


    public static User getLastReader(Channel channel) {
        List<io.getstream.chat.android.client.models.ChannelUserRead> read = channel.getRead();
        if (read == null || read.isEmpty()) return null;
        User lastReadUser = null;
        for (int i = read.size() - 1; i >= 0; i--) {

            User currentUser = StreamChat.getInstance().getCurrentUser();

            ChannelUserRead channelUserRead = read.get(i);

            if (currentUser != null) {
                String id = currentUser.getId();
                String readUserId = channelUserRead.user.getId();

                if (!id.equals(readUserId)) {
                    lastReadUser = channelUserRead.getUser();
                    break;
                }
            }
        }
        return lastReadUser;
    }
}