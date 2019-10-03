package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import java.util.HashMap;

import static com.getstream.sdk.chat.enums.EventType.NOTIFICATION_MARK_READ;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GSONDecodeTest {

    @org.junit.jupiter.api.Test
    void decodeEventTest() {
        String json = "{\"type\":\"notification.mark_read\",\"channel\":{\"id\":\"godevs\",\"type\":\"messaging\",\"cid\":\"messaging:godevs\",\"last_message_at\":\"2019-10-03T12:32:49.811652Z\",\"created_by\":null,\"frozen\":false,\"config\":{\"created_at\":\"2019-03-21T15:49:15.40182Z\",\"updated_at\":\"2019-06-20T10:52:25.909754Z\",\"name\":\"messaging\",\"typing_events\":true,\"read_events\":true,\"connect_events\":true,\"search\":false,\"reactions\":true,\"replies\":true,\"mutes\":true,\"message_retention\":\"infinite\",\"max_message_length\":5000,\"automod\":\"AI\",\"automod_behavior\":\"flag\",\"commands\":[{\"name\":\"giphy\",\"description\":\"Post a random gif to the channel\",\"args\":\"[text]\",\"set\":\"fun_set\"}]},\"name\":\"Talk about Go\",\"image\":\"https://cdn.chrisshort.net/testing-certificate-chains-in-go/GOPHER_MIC_DROP.png\"},\"created_at\":\"2019-10-03T12:37:32.33081696Z\",\"unread_channels\":3,\"unread_count\":11,\"total_unread_count\":11}";
        Event event = GsonConverter.Gson().fromJson(json, Event.class);
        assertEquals(NOTIFICATION_MARK_READ, event.getType());
    }

    @org.junit.jupiter.api.Test
    void channelWriteExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("id", "the-channel-test-id");
        extraData.put("name", "Test Channel");
        extraData.put("color", "blue");

        Channel channel = new Channel(null, "messaging", "the-channel-id", extraData);
        String json = GsonConverter.Gson().toJson(channel);
        assertEquals("{\"color\":\"blue\",\"name\":\"Test Channel\",\"id\":\"the-channel-id\",\"type\":\"messaging\",\"cid\":\"messaging:the-channel-id\"}", json);
    }

    @org.junit.jupiter.api.Test
    void channelExtraReadTest() {
        String json = "{\"color\":\"blue\",\"name\":\"Test Channel\",\"id\":\"the-channel-id\",\"type\":\"messaging\"}";
        Channel channel = GsonConverter.Gson().fromJson(json, Channel.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("color", "blue");
        extraData.put("name", "Test Channel");
        assertEquals(extraData, channel.getExtraData());
    }

    @org.junit.jupiter.api.Test
    void userWriteExtraTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Tommaso");
        extraData.put("id", "the-user-id");
        extraData.put("gender", "male");

        User user = new User("tommaso", extraData);
        String json = GsonConverter.Gson().toJson(user);
        assertEquals("{\"gender\":\"male\",\"name\":\"Tommaso\",\"id\":\"tommaso\"}", json);
    }

    @org.junit.jupiter.api.Test
    void userExtraReadTest() {
        String json = "{\"gender\":\"male\",\"name\":\"Tommaso\",\"id\":\"tommaso\"}";
        User user = GsonConverter.Gson().fromJson(json, User.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("gender", "male");
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
        assertEquals("{\"attachments\":[],\"id\":\"the-message-id\",\"group\":\"group chat\"}", json);
    }

    @org.junit.jupiter.api.Test
    void messageExtraReadTest() {
        String json = "{\"attachments\":[],\"id\":\"the-message-id\",\"reply_count\":0,\"group\":\"group chat\"}";
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
        assertEquals("{\"isAchieved\":true,\"file_size\":0}", json);
    }

    @org.junit.jupiter.api.Test
    void attachmentExtraReadTest() {
        String json = "{\"isAchieved\":true,\"file_size\":0}";
        Attachment attachment = GsonConverter.Gson().fromJson(json, Attachment.class);
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("isAchieved", true);
        assertEquals(extraData, attachment.getExtraData());
    }
}
