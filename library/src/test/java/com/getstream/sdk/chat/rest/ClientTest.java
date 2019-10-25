package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.core.providers.ApiServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.UploadStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.WebSocketServiceProvider;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.storage.BaseStorage;
import com.getstream.sdk.chat.rest.utils.CallFake;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;

/*
 * Created by Anton Bevza on 2019-10-23.
 */
public class ClientTest {

    private static String TEST_API_KEY = "testApiKey";
    private static String TEST_CLIENT_ID = "testClientId";

    @Mock
    private APIService apiService;
    @Mock
    private BaseStorage uploadStorage;
    @Mock
    private WebSocketService webSocketService;

    private Client client;

    @BeforeEach
    void initTest() throws IOException {
        MockitoAnnotations.initMocks(this);

        WebSocketServiceProvider webSocketServiceProvider = Mockito.mock(WebSocketServiceProvider.class);
        Mockito.doReturn(webSocketService).when(webSocketServiceProvider).provideWebSocketService(
                Mockito.any(), Mockito.any(), Mockito.any()
        );

        ApiServiceProvider apiServiceProvider = Mockito.mock(ApiServiceProvider.class);
        Mockito.doReturn(apiService).when(apiServiceProvider).provideApiService(Mockito.any());

        UploadStorageProvider uploadStorageProvider = Mockito.mock(UploadStorageProvider.class);
        Mockito.doReturn(uploadStorage).when(uploadStorageProvider).provideUploadStorage(Mockito.any(), Mockito.any());

        client = new Client(TEST_API_KEY,
                apiServiceProvider,
                webSocketServiceProvider,
                uploadStorageProvider,
                null);

        simulateConnection();
    }

    @Test
    void hideChannelSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = Mockito.mock(CompletableCallback.class);

        Mockito.when(apiService.hideChannel(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(CallFake.buildSuccess(response));

        String channelType = "testType";
        String channelId = "testId";

        client.hideChannel(new Channel(client, channelType, channelId), callback);

        Mockito.verify(apiService).hideChannel(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID,
                Collections.emptyMap());
        Mockito.verify(callback).onSuccess(response);
    }


    @Test
    void showChannelSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = Mockito.mock(CompletableCallback.class);

        Mockito.when(apiService.showChannel(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(CallFake.buildSuccess(response));

        String channelType = "testType";
        String channelId = "testId";

        client.showChannel(new Channel(client, channelType, channelId), callback);

        Mockito.verify(apiService).showChannel(channelType, channelId,
                TEST_API_KEY, TEST_CLIENT_ID,
                Collections.emptyMap());
        Mockito.verify(callback).onSuccess(response);
    }

    @Test
    void updateChannelSuccessTest() {
        ChannelResponse response = new ChannelResponse("100", new Channel());
        ChannelCallback callback = Mockito.mock(ChannelCallback.class);

        Mockito.when(apiService.updateChannel(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(CallFake.buildSuccess(response));

        String channelType = "testType";
        String channelId = "testId";
        Channel channel = new Channel(client, channelType, channelId);
        channel.setName("test name");
        Message updateMessage = new Message();
        updateMessage.setText("test text");

        client.updateChannel(channel, updateMessage, callback);

        Mockito.verify(apiService).updateChannel(Mockito.eq(channelType),
                Mockito.eq(channelId),
                Mockito.eq(TEST_API_KEY),
                Mockito.eq(TEST_CLIENT_ID),
                Mockito.argThat(argument -> argument.getData().get("name").equals(channel.getName())
                        && argument.getUpdateMessage().getText().equals(updateMessage.getText())));
        Mockito.verify(callback).onSuccess(response);
    }

    @Test
    void deleteChannelSuccessTest() {
        ChannelResponse response = new ChannelResponse("100", new Channel());
        ChannelCallback callback = Mockito.mock(ChannelCallback.class);

        Mockito.when(apiService.deleteChannel(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(CallFake.buildSuccess(response));

        String channelType = "testType";
        String channelId = "testId";

        client.deleteChannel(new Channel(client, channelType, channelId), callback);

        Mockito.verify(apiService).deleteChannel(channelType, channelId, TEST_API_KEY, TEST_CLIENT_ID);
        Mockito.verify(callback).onSuccess(response);
    }

    @Test
    void banUserSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = Mockito.mock(CompletableCallback.class);

        Mockito.when(apiService.banUser(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(CallFake.buildSuccess(response));

        String channelType = "testType";
        String channelId = "testId";
        String targetUserId = "testUserId";
        String reason = "testReason";
        int timeout = 100;

        client.banUser(targetUserId, new Channel(client, channelType, channelId),
                reason, timeout, callback);

        Mockito.verify(apiService).banUser(Mockito.eq(TEST_API_KEY), Mockito.eq(TEST_CLIENT_ID),
                Mockito.argThat(argument ->
                        argument.getChannelType().equals(channelType) &&
                                argument.getChannelId().equals(channelId) &&
                                argument.getReason().equals(reason) &&
                                argument.getTimeout() == timeout &&
                                argument.getTargetUserId().equals(targetUserId)
                )
        );
        Mockito.verify(callback).onSuccess(response);
    }

    @Test
    void unBanUserSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = Mockito.mock(CompletableCallback.class);

        Mockito.when(apiService.unBanUser(Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any()))
                .thenReturn(CallFake.buildSuccess(response));

        String channelType = "testType";
        String channelId = "testId";
        String targetUserId = "testUserId";

        client.unBanUser(targetUserId, new Channel(client, channelType, channelId), callback);

        Mockito.verify(apiService).unBanUser(TEST_API_KEY, TEST_CLIENT_ID,
                targetUserId, channelType, channelId
        );
        Mockito.verify(callback).onSuccess(response);
    }

    private void simulateConnection() {
        client.setUser(new User("testUserId"), new TestTokenProvider());
        Event event = new Event();
        event.setConnectionId(TEST_CLIENT_ID);
        client.connectionResolved(event);
    }
}
