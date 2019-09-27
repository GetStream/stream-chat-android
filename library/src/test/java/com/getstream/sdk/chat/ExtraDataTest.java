package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.core.Client;
import com.google.gson.Gson;

import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraDataTest {

    @org.junit.jupiter.api.Test
    void channelExtraTest() {
        HashMap<String, Object> extraData = new HashMap<String, Object>() {{
            put("color", "blue");
            put("id", "the-channel-id");
        }};
        Channel channel = new Channel(null, "messaging", "test", extraData);
        String json = GsonConverter.Gson().toJson(channel);
        assertEquals("{\"cid\":\"messaging:test\",\"id\":\"test\",\"type\":\"messaging\",\"frozen\":false,\"extra_data\":{\"color\":\"blue\"}}", json);
    }

    @org.junit.jupiter.api.Test
    void userExtraTest() {
        HashMap<String, Object> extraData = new HashMap<String, Object>() {{
            put("id", "the-message");
            put("gender", "male");
        }};
        User user = new User("the-user-id", extraData);
        String json = new Gson().toJson(user);
        assertEquals("{\"id\":\"the-user-id\",\"online\":false,\"extra_data\":{\"gender\":\"male\"}}", json);
    }

    @org.junit.jupiter.api.Test
    void messageExtraTest() {
        HashMap<String, Object> extraData = new HashMap<String, Object>() {{
            put("color", "blue");
            put("id", "the-message-id");
        }};
        Message message = new Message();
        message.setExtraData(extraData);
        String json = new Gson().toJson(message);
        assertEquals("{\"color\":\"blue\",\"id\":\"the-message-id\"}", json);
    }

    @org.junit.jupiter.api.Test
    void attachmentExtraTest() {
        HashMap<String, Object> extraData = new HashMap<String, Object>() {{
            put("color", "blue");
            put("id", "the-message-id");
        }};
        Attachment attachment = new Attachment();
        attachment.setExtraData(extraData);
        String json = new Gson().toJson(attachment);
        assertEquals("{\"color\":\"blue\",\"id\":\"the-message-id\"}", json);
    }

}
