package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReactionGsonAdapterTest {

    @org.junit.jupiter.api.Test
    void reactionDecodeTest() {
        String json = "{\n" +
                "    \"message_id\": \"muddy-king-2-e76fa735-6f23-4422-9c03-bfafed66a5eb\",\n" +
                "    \"user_id\": \"broken-waterfall-5\",\n" +
                "    \"user\": {\n" +
                "      \"id\": \"broken-waterfall-5\",\n" +
                "      \"role\": \"user\",\n" +
                "      \"created_at\": \"2019-03-08T14:45:03.243237Z\",\n" +
                "      \"updated_at\": \"2019-10-13T16:17:51.717822Z\",\n" +
                "      \"last_active\": \"2019-10-13T17:17:38.433043601Z\",\n" +
                "      \"online\": true,\n" +
                "      \"name\": \"Broken waterfall\",\n" +
                "      \"image\": \"https://getstream.io/random_svg/?id=broken-waterfall-5\\u0026amp;name=Broken+waterfall\",\n" +
                "      \"niceName\": \"Test Nicename\"\n" +
                "    },\n" +
                "    \"type\": \"love\",\n" +
                "    \"created_at\": \"2019-10-13T17:17:48.307313Z\"\n" +
                "  }";

        Reaction reaction = GsonConverter.Gson().fromJson(json, Reaction.class);
        assertEquals("muddy-king-2-e76fa735-6f23-4422-9c03-bfafed66a5eb", reaction.getMessageId());
        assertEquals("broken-waterfall-5", reaction.getUserID());
        assertEquals("love", reaction.getType());
        assertEquals("13 Oct 2019 17:17:48 GMT", reaction.getCreatedAt().toGMTString());
        assertEquals("broken-waterfall-5", reaction.getUser().getId());
        assertEquals("Test Nicename", reaction.getUser().getExtraData().get("niceName"));
    }
}
