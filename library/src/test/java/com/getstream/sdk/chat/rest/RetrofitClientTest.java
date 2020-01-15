package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.BuildConfig;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.utils.TestApiClientOptions;
import com.getstream.sdk.chat.rest.utils.TestCachedTokenProvider;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.Time;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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

        StreamLogger logger = mock(StreamLogger.class);

        StreamChat.setLogger(logger);

        service = RetrofitClient.getClient(testApiClientOptions, testTokenProvider, false)
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
    void checkValidTimeoutDurationTest() throws IOException, InterruptedException {
        MockResponse mockSuccessResponse = new MockResponse().setResponseCode(200).setBody("{}");
        mockSuccessResponse.setBodyDelay(BuildConfig.DEFAULT_API_TIMEOUT - 1000, TimeUnit.MILLISECONDS);
        mockWebServer.enqueue(mockSuccessResponse);

        //any request
        service.queryUsers(null, null, null).execute();
        RecordedRequest request = mockWebServer.takeRequest();
    }

    @Test
    void checkNotValidTimeoutDurationTest() throws IOException, InterruptedException {
        MockResponse mockFailResponse = new MockResponse().setResponseCode(200).setBody("{}");

        mockFailResponse.setBodyDelay(BuildConfig.DEFAULT_API_TIMEOUT + 1000, TimeUnit.MILLISECONDS);
        mockWebServer.enqueue(mockFailResponse);

        Assertions.assertThrows(SocketTimeoutException.class, () -> {
            service.queryUsers(null, null, null).execute();
        });
    }

    @Test
    void validAnonymousHeadersTest() throws IOException, InterruptedException {
        TestApiClientOptions testApiClientOptions =
                new TestApiClientOptions(mockWebServer.url("/").toString());

        service = RetrofitClient.getClient(testApiClientOptions, testTokenProvider, true)
                .create(APIService.class);

        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setBody("{}"));
        //any request
        service.queryUsers(null, null, null).execute();
        RecordedRequest request = mockWebServer.takeRequest();
        assertTrue(request.getHeaders().names().contains("Content-Type"));
        assertTrue(request.getHeaders().names().contains("stream-auth-type"));
        assertTrue(request.getHeaders().names().contains("Accept-Encoding"));
        assertEquals("application/json", request.getHeader("Content-Type"));
        assertEquals("anonymous", request.getHeader("stream-auth-type"));
        assertEquals("application/gzip", request.getHeader("Accept-Encoding"));
    }

    @Test
    void tokenExpiredTest() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(403).setBody(
                "{\"code\":40}"
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
