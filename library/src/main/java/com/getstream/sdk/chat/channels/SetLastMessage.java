package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;

import androidx.annotation.Nullable;

public class SetLastMessage {

    private final ComputeLastMessage computeLastMessage;

    public SetLastMessage(ComputeLastMessage computeLastMessage) {

        this.computeLastMessage = computeLastMessage;
    }

    public void setLastMessage(Channel channel, @Nullable Message lastMessage) {
        if (lastMessage == null) return;

        ChannelState state = channel.getChannelState();

        if (lastMessage.getDeletedAt() != null) {
            state.lastMessage = computeLastMessage.computeLastMessage(channel);
            return;
        }
        state.lastMessage = lastMessage;
    }
}
