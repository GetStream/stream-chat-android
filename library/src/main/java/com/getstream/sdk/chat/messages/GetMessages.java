package com.getstream.sdk.chat.messages;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Subscription;

import java.util.List;

public class GetMessages {

    private final MessagesRepository repository;

    public GetMessages(MessagesRepository repository) {
        this.repository = repository;
    }

    public Subscription<List<Message>> getMessages(int offset, int limit, String channelId) {
        return repository.getMessages(offset, limit, channelId);
    }
}
