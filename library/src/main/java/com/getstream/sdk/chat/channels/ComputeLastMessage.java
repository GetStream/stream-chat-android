package com.getstream.sdk.chat.channels;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.response.ChannelState;

import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;

public class ComputeLastMessage {
    @Nullable
    public Message computeLastMessage(Channel channel) {
        Message lastMessage = null;
        List<Message> messages = channel.getChannelState().getMessages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            Message message = messages.get(i);
            if (message.getDeletedAt() == null && message.getType().equals(ModelType.message_regular)) {
                lastMessage = message;
                break;
            }
        }
        Message.setStartDay(Collections.singletonList(lastMessage), null);

        return lastMessage;
    }
}
