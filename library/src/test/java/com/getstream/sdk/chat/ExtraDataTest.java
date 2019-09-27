package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraDataTest {

    @org.junit.jupiter.api.Test
    void channelExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("id", "the-channel-test-id");
        extraData.put("name", "Test Channel");
        extraData.put("image", "https://bit.ly/321RmWb");

        Channel channel = new Channel(null, "messaging", "the-channel-id", extraData);
        String json = GsonConverter.Gson().toJson(channel);
        assertEquals("{\"image\":\"https://bit.ly/321RmWb\",\"name\":\"Test Channel\",\"id\":\"the-channel-id\",\"type\":\"messaging\"}", json);
    }

    @org.junit.jupiter.api.Test
    void userExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("gender", "male");
        extraData.put("id", "the-user-test-id");

        User user = new User("the-user-id", extraData);
        String json = GsonConverter.Gson().toJson(user);
        assertEquals("{\"gender\":\"male\",\"id\":\"the-user-id\"}", json);
    }

    @org.junit.jupiter.api.Test
    void messageExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("group", "group chat");
        extraData.put("id", "the-message-test-id");

        Message message = new Message();
        message.setId("the-message-id");
        message.setExtraData(extraData);
        String json = GsonConverter.Gson().toJson(message);
        assertEquals("{\"id\":\"the-message-id\",\"group\":\"group chat\"}", json);
    }

    @org.junit.jupiter.api.Test
    void attachmentExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("isAchieved", true);

        Attachment attachment = new Attachment();
        attachment.setExtraData(extraData);
        String json = GsonConverter.Gson().toJson(attachment);
        assertEquals("{\"isAchieved\":true}", json);
    }

}
