package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.request.ChannelWatchRequest;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelWatchRequestTest {
    @Test
    void channelWatchReuestTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        ArrayList<String> members = new ArrayList<>();
        members.add("thierry");
        members.add("adrian");
        extraData.put("members", members);

        ChannelWatchRequest request = new ChannelWatchRequest().withData(extraData);
        String json = GsonConverter.Gson().toJson(request);
        assertEquals("{\"data\":{\"members\":[\"thierry\",\"adrian\"]},\"state\":true,\"watch\":true,\"presence\":false}", json);
    }

    @Test
    void channelWatchRequestWithPresenceTest() {
        ChannelWatchRequest request = new ChannelWatchRequest().withPresence();
        String json = GsonConverter.Gson().toJson(request);
        assertEquals("{\"state\":true,\"watch\":true,\"presence\":true}", json);
    }
}
