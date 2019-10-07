package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelGsonAdapterTest {

    @org.junit.jupiter.api.Test
    void channelDecodeTest() {
        String json = "{\n" +
                "        \"id\": \"general\",\n" +
                "        \"type\": \"messaging\",\n" +
                "        \"cid\": \"messaging:general\",\n" +
                "        \"last_message_at\": \"2019-10-03T11:09:28.48544Z\",\n" +
                "        \"created_by\": {\n" +
                "          \"id\": \"jack\",\n" +
                "          \"role\": \"user\",\n" +
                "          \"created_at\": \"2019-04-03T03:30:52.624477Z\",\n" +
                "          \"updated_at\": \"2019-05-23T07:20:24.729535Z\",\n" +
                "          \"last_active\": \"2019-05-23T07:20:24.727535Z\",\n" +
                "          \"online\": false,\n" +
                "          \"name\": \"Jack Three\"\n" +
                "        },\n" +
                "        \"frozen\": false,\n" +
                "        \"member_count\": 3,\n" +
                "        \"config\": {\n" +
                "          \"created_at\": \"2019-03-21T15:49:15.40182Z\",\n" +
                "          \"updated_at\": \"2019-06-20T10:52:25.909754Z\",\n" +
                "          \"name\": \"messaging\",\n" +
                "          \"typing_events\": true,\n" +
                "          \"read_events\": true,\n" +
                "          \"connect_events\": true,\n" +
                "          \"search\": false,\n" +
                "          \"reactions\": true,\n" +
                "          \"replies\": true,\n" +
                "          \"mutes\": true,\n" +
                "          \"message_retention\": \"infinite\",\n" +
                "          \"max_message_length\": 5000,\n" +
                "          \"automod\": \"AI\",\n" +
                "          \"automod_behavior\": \"flag\",\n" +
                "          \"commands\": [\n" +
                "            {\n" +
                "              \"name\": \"giphy\",\n" +
                "              \"description\": \"Post a random gif to the channel\",\n" +
                "              \"args\": \"[text]\",\n" +
                "              \"set\": \"fun_set\"\n" +
                "            }\n" +
                "          ]\n" +
                "        },\n" +
                "        \"image\": \"https://avatars3.githubusercontent.com/u/8597527?s=200\\u0026v=4\",\n" +
                "        \"name\": \"General\",\n" +
                "        \"title\": \"General\",\n" +
                "        \"description\": \"General\",\n" +
                "        \"example\": 1\n" +
                "}";

        Channel channel = GsonConverter.Gson().fromJson(json, Channel.class);
        assertEquals("messaging:general", channel.getCid());
        assertEquals(1.0, channel.getExtraData().get("example"));
        assertEquals("General", channel.getExtraData().get("description"));
        assertEquals("General", channel.getExtraData().get("title"));
        assertEquals("General", channel.getExtraData().get("name"));
        assertEquals("https://avatars3.githubusercontent.com/u/8597527?s=200&v=4", channel.getExtraData().get("image"));
        assertEquals("general", channel.getId());
        assertEquals("messaging", channel.getType());
        assertEquals("3 Oct 2019 11:09:28 GMT", channel.getLastMessageDate().toGMTString());
        assertEquals(5, channel.getExtraData().size());
    }

}
