package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.core.providers.ApiServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.UploadStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.WebSocketServiceProvider;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.utils.MockResponseFileReader;
import com.getstream.sdk.chat.rest.utils.TestApiClientOptions;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;
import com.getstream.sdk.chat.rest.utils.TestWebSocketService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
 * Created by Anton Bevza on 2019-10-23.
 */
public class ClientTest {

    private static String TEST_API_KEY = "testApiKey";
    private static String TEST_CLIENT_ID = "testClientId";

    private MockWebServer mockWebServer;

    @Spy
    private TestTokenProvider testTokenProvider = new TestTokenProvider();
    private Client client;

    @BeforeEach
    void initTest() throws IOException {
        MockitoAnnotations.initMocks(this);
        WebSocketServiceProvider webSocketServiceProvider = Mockito.mock(WebSocketServiceProvider.class);
        Mockito.doReturn(new TestWebSocketService(null, "", null))
                .when(webSocketServiceProvider).provideWebSocketService(
                Mockito.any(), Mockito.any(), Mockito.any()
        );
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        TestApiClientOptions testApiClientOptions =
                new TestApiClientOptions(mockWebServer.url("/").toString());
        client = new Client(TEST_API_KEY,
                new ApiServiceProvider(testApiClientOptions),
                webSocketServiceProvider,
                new UploadStorageProvider(testApiClientOptions),
                null);
    }

    @Test
    void updateChannelSuccessTest() throws IOException, InterruptedException {
        simulateConnection();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        final Object syncObject = new Object();
        ChannelCallback callback = new ChannelCallback() {
            @Override
            public void onSuccess(ChannelResponse response) {
                assertNotNull(response.getChannel());
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        };
        HashMap<String, Object> extraFields = new HashMap<>();
        extraFields.put("name", "testName");
        extraFields.put("title", "testTitle");
        Channel channel = new Channel(client, "testType", "testId", extraFields);
        Message message = new Message();
        message.setText("test message");
        client.updateChannel(channel, message, callback);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/channels/testType/testId?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        String expectedBody = "{\"data\":{\"name\":\"testName\",\"title\":\"testTitle\"}," +
                "\"message\":{\"attachments\":[],\"text\":\"test message\"}}";
        assertEquals(expectedBody, recordedRequest.getBody().readUtf8());

        synchronized (syncObject) {
            syncObject.wait();
        }
    }

    @Test
    void deleteChannelSuccessTest() throws IOException, InterruptedException {
        simulateConnection();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        final Object syncObject = new Object();
        ChannelCallback callback = new ChannelCallback() {
            @Override
            public void onSuccess(ChannelResponse response) {
                assertNotNull(response.getChannel());
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        };
        Channel channel = new Channel(client, "testType", "testId");
        client.deleteChannel(channel, callback);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/channels/testType/testId?api_key=" + TEST_API_KEY +
                "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());

        synchronized (syncObject) {
            syncObject.wait();
        }
    }



    @Test
    void hideChannelSuccessTest() throws IOException, InterruptedException {
        simulateConnection();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        final Object syncObject = new Object();
        CompletableCallback callback = new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                assertEquals("999", response.getDuration());
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        };
        client.hideChannel(new Channel(client, "testType", "testId"), callback);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/channels/testType/testId/hide?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        assertEquals("{}", recordedRequest.getBody().readUtf8());

        synchronized (syncObject) {
            syncObject.wait();
        }
    }

    @Test
    void showChannelSuccessTest() throws IOException, InterruptedException {
        simulateConnection();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        final Object syncObject = new Object();
        CompletableCallback callback = new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                assertEquals("999", response.getDuration());
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        };
        client.showChannel(new Channel(client, "testType", "testId"), callback);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/channels/testType/testId/show?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        assertEquals("{}", recordedRequest.getBody().readUtf8());

        synchronized (syncObject) {
            syncObject.wait();
        }
    }

    @Test
    void banUserSuccessTest() throws IOException, InterruptedException {
        simulateConnection();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        final Object syncObject = new Object();
        CompletableCallback callback = new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                assertEquals("999", response.getDuration());
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        };
        client.banUser("userId", new Channel(client, "testType", "testId"),
                "testReason", 100, callback);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/moderation/ban?api_key=" + TEST_API_KEY + "&client_id=" +
                TEST_CLIENT_ID, recordedRequest.getPath());
        assertEquals("{\"target_user_id\":\"userId\",\"timeout\":100," +
                        "\"reason\":\"testReason\",\"type\":\"testType\",\"id\":\"testId\"}",
                recordedRequest.getBody().readUtf8());

        synchronized (syncObject) {
            syncObject.wait();
        }
    }

    @Test
    void unBanUserSuccessTest() throws IOException, InterruptedException {
        simulateConnection();
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        final Object syncObject = new Object();
        CompletableCallback callback = new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                assertEquals("999", response.getDuration());
                synchronized (syncObject) {
                    syncObject.notify();
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        };
        client.unBanUser("userId", new Channel(client, "testType", "testId"), callback);
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/moderation/ban?api_key=" + TEST_API_KEY + "&client_id=" +
                        TEST_CLIENT_ID + "&target_user_id=userId&type=testType&id=testId",
                recordedRequest.getPath());

        synchronized (syncObject) {
            syncObject.wait();
        }
    }

    private void simulateConnection() {
        client.setUser(new User("testUserId"), testTokenProvider);
        Event event = new Event();
        event.setConnectionId(TEST_CLIENT_ID);
        client.connectionResolved(event);
    }
}
