package com.getstream.sdk.chat;

import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserGsonAdapterTest {

    @org.junit.jupiter.api.Test
    void encodeUserSimpleTest() {
        User user = new User();
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastActive(new Date());
        user.setId("123");
        user.setOnline(true);
        user.setTotalUnreadCount(1);
        user.setUnreadChannels(2);
        user.setImage("image-url");

        String json = GsonConverter.Gson().toJson(user);
        assertEquals("{\"image\":\"image-url\",\"id\":\"123\"}", json);
    }

    @org.junit.jupiter.api.Test
    void encodeEmptyUserTest() {
        User user = new User();
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastActive(new Date());
        user.setId("123");

        String json = GsonConverter.Gson().toJson(user);
        assertEquals("{\"id\":\"123\"}", json);
    }
}
