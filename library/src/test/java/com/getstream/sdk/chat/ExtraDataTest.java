package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.Gson;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExtraDataTest {

    @org.junit.jupiter.api.Test
    void channelWriteExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("id", "the-channel-test-id");
        extraData.put("name", "Test Channel");
        extraData.put("image", "https://bit.ly/321RmWb");

        Channel channel = new Channel(null, "messaging", "the-channel-id", extraData);
        String json = GsonConverter.Gson().toJson(channel);
        assertEquals("{\"image\":\"https://bit.ly/321RmWb\",\"name\":\"Test Channel\",\"id\":\"the-channel-id\",\"type\":\"messaging\"}", json);
    }

    @org.junit.jupiter.api.Test
    void channelExtraReadTest() {
        String json = "{\"image\":\"https://bit.ly/321RmWb\",\"name\":\"Test Channel\",\"id\":\"the-channel-id\",\"type\":\"messaging\"}";
        Channel channel = GsonConverter.Gson().fromJson(json, Channel.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Test Channel");
        extraData.put("image", "https://bit.ly/321RmWb");
        assertEquals(extraData, channel.getExtraData());
    }

    @org.junit.jupiter.api.Test
    void userWriteExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Tommaso");
        extraData.put("id", "the-user-id");

        User user = new User("tommaso", extraData);
        String json = GsonConverter.Gson().toJson(user);
        assertEquals("{\"name\":\"Tommaso\",\"id\":\"tommaso\"}", json);
    }

    @org.junit.jupiter.api.Test
    void userExtraReadTest() {
        String json = "{\"name\":\"Tommaso\",\"id\":\"tommaso\"}";
        User user = GsonConverter.Gson().fromJson(json, User.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Tommaso");
        assertEquals(extraData, user.getExtraData());
    }

    @org.junit.jupiter.api.Test
    void messageWriteExtraTest() {
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
    void messageExtraReadTest() {
        String json = "{\"id\":\"the-message-id\",\"group\":\"group chat\"}";
        Message message = GsonConverter.Gson().fromJson(json, Message.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("group", "group chat");
        assertEquals(extraData, message.getExtraData());
    }

    @org.junit.jupiter.api.Test
    void attachmentWriteExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("isAchieved", true);

        Attachment attachment = new Attachment();
        attachment.setExtraData(extraData);
        String json = GsonConverter.Gson().toJson(attachment);
        assertEquals("{\"isAchieved\":true}", json);
    }

    @org.junit.jupiter.api.Test
    void attachmentExtraReadTest() {
        String json = "{\"isAchieved\":true}";
        Attachment attachment = GsonConverter.Gson().fromJson(json, Attachment.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("isAchieved", true);
        assertEquals(extraData, attachment.getExtraData());
    }
}
