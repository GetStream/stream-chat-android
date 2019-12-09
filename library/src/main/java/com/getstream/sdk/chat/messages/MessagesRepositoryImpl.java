package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.rest.Message;

import java.util.Collections;
import java.util.List;

public class MessagesRepositoryImpl implements MessagesRepository {
    @Override
    public List<Message> getMessages(int offset, int limit, String channelId) {
        return Collections.emptyList();
    }
}
