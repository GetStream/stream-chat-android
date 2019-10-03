package com.getstream.sdk.chat;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

//TODO: add tests for all fields!
public class MessageGsonAdapterTest {

    @org.junit.jupiter.api.Test
    void messageWithMentionedUserDecodeTest() {
        String json = "{\n" +
                "      \"id\": \"bender-ca9ebb91-c196-4bf3-af9e-e8fb78766e8d\",\n" +
                "      \"text\": \"@Broken waterfall There?\",\n" +
                "      \"html\": \"\\u003cp\\u003e@Broken waterfall There?\\u003c/p\\u003e\\n\",\n" +
                "      \"type\": \"regular\",\n" +
                "      \"user\": {\n" +
                "        \"id\": \"bender\",\n" +
                "        \"role\": \"user\",\n" +
                "        \"created_at\": \"2019-08-26T10:49:19.667963Z\",\n" +
                "        \"updated_at\": \"2019-10-01T20:30:15.42807Z\",\n" +
                "        \"last_active\": \"2019-10-01T20:58:39.441510045Z\",\n" +
                "        \"online\": false,\n" +
                "        \"unread_count\": 9,\n" +
                "        \"name\": \"Bender\",\n" +
                "        \"image\": \"https://bit.ly/321RmWb\"\n" +
                "      },\n" +
                "      \"attachments\": [],\n" +
                "      \"latest_reactions\": [],\n" +
                "      \"own_reactions\": [],\n" +
                "      \"reaction_counts\": {},\n" +
                "      \"reply_count\": 0,\n" +
                "      \"created_at\": \"2019-09-30T22:52:13.316242Z\",\n" +
                "      \"updated_at\": \"2019-09-30T22:52:13.316242Z\",\n" +
                "      \"mentioned_users\": [\n" +
                "        {\n" +
                "          \"id\": \"broken-waterfall-5\",\n" +
                "          \"role\": \"user\",\n" +
                "          \"created_at\": \"2019-03-08T14:45:03.243237Z\",\n" +
                "          \"updated_at\": \"2019-10-01T20:51:42.57776Z\",\n" +
                "          \"last_active\": \"2019-10-01T21:00:42.680318396Z\",\n" +
                "          \"online\": true,\n" +
                "          \"name\": \"Broken waterfall\",\n" +
                "          \"image\": \"https://getstream.io/random_svg/?id=broken-waterfall-5\\u0026amp;name=Broken+waterfall\",\n" +
                "          \"niceName\": \"Test Nicename\"\n" +
                "        }\n" +
                "      ]\n" +
                "    }";
        Message message = GsonConverter.Gson().fromJson(json, Message.class);
        assertEquals(message.getMentionedUsers().size(), 1);
        assertEquals(message.getMentionedUsers().get(0).getClass(), User.class);
    }

}
