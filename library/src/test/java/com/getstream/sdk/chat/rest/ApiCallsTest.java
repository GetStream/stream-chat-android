package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.rest.utils.MockResponseFileReader;
import com.getstream.sdk.chat.rest.utils.TestApiClientOptions;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.request.AcceptInviteRequest;
import com.getstream.sdk.chat.rest.request.AddMembersRequest;
import com.getstream.sdk.chat.rest.request.BanUserRequest;
import com.getstream.sdk.chat.rest.request.RejectInviteRequest;
import com.getstream.sdk.chat.rest.request.RemoveMembersRequest;
import com.getstream.sdk.chat.rest.request.UpdateChannelRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import retrofit2.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Created by Anton Bevza on 2019-10-18.
 */
@RunWith(MockitoJUnitRunner.class)
class ApiCallsTest {

    private static String TEST_API_KEY = "testApiKey";
    private static String TEST_CLIENT_ID = "testClientId";

    private MockWebServer mockWebServer;
    private APIService service;

    @Spy
    private TestTokenProvider testTokenProvider = new TestTokenProvider();

    @BeforeEach
    void initTest() throws IOException {
        MockitoAnnotations.initMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        TestApiClientOptions testApiClientOptions =
                new TestApiClientOptions(mockWebServer.url("/").toString());
        service = RetrofitClient.getAuthorizedClient((CachedTokenProvider) testTokenProvider, testApiClientOptions)
                .create(APIService.class);
    }

    @AfterEach
    void shutdown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void validHeadersTest() throws IOException, InterruptedException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        //any request
        service.queryUsers(null, null, null).execute();
        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(request.getHeaders().names().contains("Content-Type"));
        assertTrue(request.getHeaders().names().contains("stream-auth-type"));
        assertTrue(request.getHeaders().names().contains("Accept-Encoding"));
        assertTrue(request.getHeaders().names().contains("Authorization"));
        assertEquals("application/json", request.getHeader("Content-Type"));
        assertEquals("jwt", request.getHeader("stream-auth-type"));
        assertEquals("application/gzip", request.getHeader("Accept-Encoding"));
        //check user token
        assertEquals(TestTokenProvider.TEST_TOKEN, request.getHeader("Authorization"));
    }

    @Test
    void tokenExpiredTest() throws IOException {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403).setBody(
                new MockResponseFileReader("token_expired_error.json").getContent()
        ));
        try {
            //any request
            service.queryUsers(null, null, null).execute();
        } catch (IOException e) {
            //ignore
        }
        //Mockito.verify(testTokenProvider).tokenExpired();
    }

    @Test
    void hideChannelSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        Response<CompletableResponse> response = service.hideChannel(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, Collections.emptyMap()).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "/hide?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        //Check response
        assertEquals("999", response.body().getDuration());
    }

    @Test
    void showChannelSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        Response<CompletableResponse> response = service.hideChannel(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, Collections.emptyMap()).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "/hide?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        //Check response
        assertEquals("999", response.body().getDuration());
    }

    @Test
    void acceptInviteSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        String testMessage = "test message";
        AcceptInviteRequest body = new AcceptInviteRequest(testMessage);
        Response<ChannelResponse> response = service.acceptInvite(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, body).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        String expectedBody = "{\"accept_invite\":true,\"message\":{\"text\":\"test message\"}}";
        assertEquals(expectedBody, recordedRequest.getBody().readUtf8());
        //Check response
        assertNotNull(response.body().getChannel());
    }

    @Test
    void rejectInviteSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        RejectInviteRequest body = new RejectInviteRequest();
        Response<ChannelResponse> response = service.rejectInvite(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, body).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        assertEquals("{\"reject_invite\":true}", recordedRequest.getBody().readUtf8());
        //Check response
        assertNotNull(response.body().getChannel());
    }

    @Test
    void stopWatchingSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        Response<CompletableResponse> response = service.stopWatching(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, Collections.emptyMap()).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "/stop-watching?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        //Check response
        assertEquals("999", response.body().getDuration());
    }

    @Test
    void deleteChannelSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        Response<ChannelResponse> response = service.deleteChannel(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        //Check response
        assertNotNull(response.body().getChannel());
    }

    @Test
    void updateChannelSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        HashMap<String, Object> extraFields = new HashMap<>();
        extraFields.put("name", "testName");
        extraFields.put("title", "testTitle");
        Message updateMessage = new Message();
        updateMessage.setText("testUpdate");
        UpdateChannelRequest request = new UpdateChannelRequest(extraFields, updateMessage);
        Response<ChannelResponse> response = service.updateChannel(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, request).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        String expectedBody = "{\"data\":{\"name\":\"testName\",\"title\":\"testTitle\"}," +
                "\"message\":{\"attachments\":[],\"text\":\"testUpdate\"}}";
        assertEquals(expectedBody,
                recordedRequest.getBody().readUtf8());
        //Check response
        assertNotNull(response.body().getChannel());
    }

    @Test
    void addMembersSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        ArrayList<String> members = new ArrayList<>();
        members.add("test1");
        members.add("test2");
        AddMembersRequest request = new AddMembersRequest(members);
        Response<ChannelResponse> response = service.addMembers(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, request).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        String expectedBody = "{\"add_members\":[\"test1\",\"test2\"]}";
        assertEquals(expectedBody, recordedRequest.getBody().readUtf8());
        //Check response
        assertNotNull(response.body().getChannel());
    }

    @Test
    void removeMembersSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("channel_success.json").getContent()
        ));
        ArrayList<String> members = new ArrayList<>();
        members.add("test1");
        members.add("test2");
        RemoveMembersRequest request = new RemoveMembersRequest(members);
        Response<ChannelResponse> response = service.removeMembers(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID, request).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/channels/" + channelType + "/" + channelId + "?api_key="
                + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID, recordedRequest.getPath());
        String expectedBody = "{\"remove_members\":[\"test1\",\"test2\"]}";
        assertEquals(expectedBody, recordedRequest.getBody().readUtf8());
        //Check response
        assertNotNull(response.body().getChannel());
    }

    @Test
    void banUserSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        BanUserRequest body = new BanUserRequest("userIdTest", 999,
                "test reason", channelType, channelId);
        Response<CompletableResponse> response = service.banUser(TEST_API_KEY, TEST_CLIENT_ID, body).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/moderation/ban?api_key=" + TEST_API_KEY + "&client_id=" +
                TEST_CLIENT_ID, recordedRequest.getPath());
        String expectedBody = "{\"target_user_id\":\"userIdTest\",\"timeout\":999," +
                "\"reason\":\"test reason\",\"type\":\"testType\",\"id\":\"testId\"}";
        assertEquals(expectedBody, recordedRequest.getBody().readUtf8());
        //Check response
        assertNotNull(response.body().getDuration());
    }

    @Test
    void unUserSuccessTest() throws IOException, InterruptedException {
        String channelType = "testType";
        String channelId = "testId";
        String targetUserId = "testUserId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("completable_success.json").getContent()
        ));
        Response<CompletableResponse> response = service.unBanUser(TEST_API_KEY, TEST_CLIENT_ID,
                targetUserId, channelType, channelId).execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        //Check request
        assertEquals("/moderation/ban?api_key=" + TEST_API_KEY + "&client_id=" + TEST_CLIENT_ID +
                        "&target_user_id=" + targetUserId + "&type=" + channelType + "&id=" + channelId,
                recordedRequest.getPath());
        //Check response
        assertNotNull(response.body().getDuration());
    }

    @Test
    void queryChannelsSuccessTest() throws IOException, InterruptedException {
        String testUserId = "testUserId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("query_channels_success.json").getContent()
        ));
        Response<QueryChannelsResponse> response = service.queryChannels(TEST_API_KEY,
                testUserId, TEST_CLIENT_ID, "").execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/channels?api_key=" + TEST_API_KEY + "&user_id=" + testUserId +
                        "&client_id=" + TEST_CLIENT_ID + "&payload=",
                recordedRequest.getPath());
        //Check response
        assertEquals(2, response.body().getChannelStates().size());
    }

    @Test
    void queryChannelSuccessTest() throws IOException, InterruptedException {
        String testUserId = "testUserId";
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody(
                new MockResponseFileReader("query_channels_success.json").getContent()
        ));
        Response<QueryChannelsResponse> response = service.queryChannels(TEST_API_KEY,
                testUserId, TEST_CLIENT_ID, "").execute();
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertEquals("/channels?api_key=" + TEST_API_KEY + "&user_id=" + testUserId +
                        "&client_id=" + TEST_CLIENT_ID + "&payload=",
                recordedRequest.getPath());
        //Check response
        assertEquals(2, response.body().getChannelStates().size());
    }

    //TODO queryChannel
    //TODO queryUsers
    //TODO muteUser
    //TODO unMuteUser
    //TODO flag
    //TODO unFlag
    //TODO sendMessage
    //TODO updateMessage
    //TODO getMessage
    //TODO sendAction
    //TODO deleteMessage
    //TODO sendReaction
    //TODO deleteReaction
    //TODO getReactions
    //TODO getReplies
    //TODO getRepliesMore
    //TODO sendEvent
    //TODO markRead
    //TODO markAllRead
    //TODO sendImage
    //TODO sendFile
    //TODO searchMessages
    //TODO getDevices
    //TODO addDevices
    //TODO deleteDevice

}
