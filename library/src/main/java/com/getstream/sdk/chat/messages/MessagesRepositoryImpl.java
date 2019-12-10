package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Subscription;

import java.util.Collections;
import java.util.List;

public class MessagesRepositoryImpl implements MessagesRepository {
    @Override
    public Subscription<List<Message>> getMessages(int offset, int limit, String channelId) {
        return null;
    }

    @Override
    public Subscription<Void> sendMessage(Message message) {
        return null;
    }
}
