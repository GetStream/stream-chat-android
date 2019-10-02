package com.getstream.sdk.chat;

import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChannelQueryRequestTest {

    @org.junit.jupiter.api.Test
    void channelQueryRequestWithMembersTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        ArrayList<String> members = new ArrayList<>();
        members.add("thierry");
        members.add("adrian");
        extraData.put("members", members);

        ChannelQueryRequest request = new ChannelQueryRequest().withData(extraData);
        String json = GsonConverter.Gson().toJson(request);
        assertEquals("{\"data\":{\"members\":[\"thierry\",\"adrian\"]},\"state\":true,\"watch\":false,\"presence\":false}", json);
    }

    @org.junit.jupiter.api.Test
    void channelQueryRequestWithPresenceTest() {
        ChannelQueryRequest request = new ChannelQueryRequest().withPresence();
        String json = GsonConverter.Gson().toJson(request);
        assertEquals("{\"state\":true,\"watch\":false,\"presence\":true}", json);
    }

    @org.junit.jupiter.api.Test
    void channelQueryRequestWithoutStateTest() {
        ChannelQueryRequest request = new ChannelQueryRequest().noState();
        String json = GsonConverter.Gson().toJson(request);
        assertEquals("{\"state\":false,\"watch\":false,\"presence\":false}", json);
    }
}
