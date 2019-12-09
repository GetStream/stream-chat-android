package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Subscription;

import java.util.List;

public interface MessagesRepository {
    Subscription<List<Message>> getMessages(int offset, int limit, String channelId);
}