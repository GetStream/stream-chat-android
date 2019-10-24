package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.utils.MockResponseFileReader;
import com.getstream.sdk.chat.rest.utils.TestApiClientOptions;
import com.getstream.sdk.chat.rest.utils.TestCachedTokenProvider;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*
 * Created by Anton Bevza on 2019-10-18.
 */
@RunWith(MockitoJUnitRunner.class)
class RetrofitClientTest {

    private MockWebServer mockWebServer;
    private APIService service;

    @Spy
    private TestCachedTokenProvider testTokenProvider = new TestCachedTokenProvider();

    @BeforeEach
    void initTest() throws IOException {
        MockitoAnnotations.initMocks(this);
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        TestApiClientOptions testApiClientOptions =
                new TestApiClientOptions(mockWebServer.url("/").toString());
        service = RetrofitClient.getAuthorizedClient(testTokenProvider, testApiClientOptions)
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
        Mockito.verify(testTokenProvider).tokenExpired();
    }
}
