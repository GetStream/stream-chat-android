package com.getstream.sdk.chat.rest.providers;

import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.providers.StreamWebSocketServiceProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class StreamWebSocketServiceProviderTest {

    StreamWebSocketServiceProvider provider;
    String apiKey;

    @BeforeEach
    void setUp() {
        apiKey = "test-key";
        provider = new StreamWebSocketServiceProvider(new ApiClientOptions(), apiKey);
    }

    @Test
    void getWsUrlTest() throws UnsupportedEncodingException {
        User user = new User();
        user.setId("123");
        user.setImage("image \" url");

        String url = provider.getWsUrl("token", user, false);
        assertEquals("wss://chat-us-east-1.stream-io-api.com/connect?json=%7B%22user_id%22%3A%22123%22%2C%22user_details%22%3A%7B%22image%22%3A%22image+%5C%22+url%22%2C%22id%22%3A%22123%22%7D%2C%22server_determines_connection_id%22%3Atrue%7D&api_key=test-key&authorization=token&stream-auth-type=jwt", url);

        url = provider.getWsUrl("token", user, true);
        assertEquals("wss://chat-us-east-1.stream-io-api.com/connect?json=%7B%22user_id%22%3A%22123%22%2C%22user_details%22%3A%7B%22image%22%3A%22image+%5C%22+url%22%2C%22id%22%3A%22123%22%7D%2C%22server_determines_connection_id%22%3Atrue%7D&api_key=test-key&stream-auth-type=anonymous", url);
    }

    @Test
    void buildUserDetailJSONTest() {
        User user = new User();
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastActive(new Date());
        user.setId("123");

        String json = provider.buildUserDetailJSON(user);
        assertEquals("{\"user_id\":\"123\",\"user_details\":{\"id\":\"123\"},\"server_determines_connection_id\":true}", json);
    }

    @Test
    void buildUserWithImageDetailJSONTest() {
        User user = new User();
        user.setImage("imageurl");
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastActive(new Date());
        user.setId("123");

        String json = provider.buildUserDetailJSON(user);
        assertEquals("{\"user_id\":\"123\",\"user_details\":{\"image\":\"imageurl\",\"id\":\"123\"},\"server_determines_connection_id\":true}", json);
    }

}
