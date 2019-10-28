package com.getstream.sdk.chat.rest.providers;

import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.providers.StreamWebSocketServiceProvider;
import com.getstream.sdk.chat.rest.utils.TestApiClientOptions;

import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class StreamWebSocketServiceProviderTest {

    @Test
    void getWsUrlValidTest() throws UnsupportedEncodingException {
        User user = new User("testId");
        user.setName("TestName");
        user.setImage("testImageUrl");
        HashMap<String, Object> userDetails = new HashMap<>();
        userDetails.put("testParamKey", "testParam");
        user.setExtraData(userDetails);
        StreamWebSocketServiceProvider provider = new StreamWebSocketServiceProvider(
                new TestApiClientOptions("test-base-url://"), "testApiKey");
        String wsUrl = provider.getWsUrl("testUserToken", user);
        String userExpectedJson = "{\"server_determines_connection_id\":true," +
                "\"user_id\":\"testId\",\"user_details\":{\"name\":\"TestName\",\"image\":\"testImageUrl\"," +
                "\"testParamKey\":\"testParam\",\"id\":\"testId\"}}";
        userExpectedJson = URLEncoder.encode(userExpectedJson, StandardCharsets.UTF_8.toString());
        String expectedUrl = "test-base-url://connect?json=" + userExpectedJson + "&api_key=testApiKey&" +
                "authorization=testUserToken&stream-auth-type=jwt";
        assertEquals(expectedUrl, wsUrl);
    }
}
