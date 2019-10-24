package com.getstream.sdk.chat.rest.providers;

import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.providers.WebSocketServiceProvider;
import com.getstream.sdk.chat.rest.utils.TestApiClientOptions;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class WebSocketServiceProviderTest {

    @Test
    void getWsUrlValidTest() {
        User user = new User("testId");
        user.setName("TestName");
        user.setImage("testImageUrl");
        HashMap<String, Object> userDetails = new HashMap<>();
        userDetails.put("testParamKey", "testParam");
        user.setExtraData(userDetails);
        WebSocketServiceProvider provider = new WebSocketServiceProvider(
                new TestApiClientOptions("test-base-url://"), "testApiKey");
        String wsUrl = provider.getWsUrl("testUserToken", user);
        assertEquals("test-base-url://connect?json={\"server_determines_connection_id\":true," +
                "\"user_id\":\"testId\",\"user_details\":{\"name\":\"TestName\",\"image\":\"testImageUrl\"," +
                "\"testParamKey\":\"testParam\",\"id\":\"testId\"}}&api_key=testApiKey&" +
                "authorization=testUserToken&stream-auth-type=jwt", wsUrl);
    }
}
