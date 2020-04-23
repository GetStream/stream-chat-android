package com.getstream.sdk.chat.rest.providers;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.providers.StreamWebSocketServiceProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class StreamWebSocketServiceProviderTest {

    StreamWebSocketServiceProvider provider;
    String apiKey;
    String imageUrl = "imageurl";
    String userId = "123";
    String wssUrl = "wss://chat-us-east-1.stream-io-api.com";

    @BeforeEach
    void setUp() {
        apiKey = "test-key";
        provider = new StreamWebSocketServiceProvider(new ApiClientOptions(), apiKey);
        StreamLogger logger = mock(StreamLogger.class);
        StreamChat.setLogger(logger);
    }

    @Test
    void getWsUrlTest() throws UnsupportedEncodingException {
        User user = new User();

        user.setId(userId);
        user.setImage(imageUrl);

        String url = provider.getWsUrl("token", user, false);
        assertTrue(url.contains(imageUrl));
        assertTrue(url.contains(userId));
        assertTrue(url.contains(wssUrl));

        url = provider.getWsUrl("token", user, true);
        assertTrue(url.contains(imageUrl));
        assertTrue(url.contains(userId));
        assertTrue(url.contains(wssUrl));
    }

    @Test
    void buildUserDetailJSONTest() {
        User user = new User();
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastActive(new Date());
        user.setId(userId);

        String json = provider.buildUserDetailJSON(user);
        assertTrue(json.contains(userId));
        assertTrue(json.contains("X-STREAM-CLIENT"));
    }

    @Test
    void buildUserWithImageDetailJSONTest() {
        User user = new User();

        user.setImage(imageUrl);
        user.setCreatedAt(new Date());
        user.setUpdatedAt(new Date());
        user.setLastActive(new Date());
        user.setId(userId);

        String json = provider.buildUserDetailJSON(user);
        assertTrue(json.contains(imageUrl));
        assertTrue(json.contains(userId));
    }

}
