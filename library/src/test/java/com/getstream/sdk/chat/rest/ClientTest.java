package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.Filters;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.core.providers.ApiServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.UploadStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.WebSocketServiceProvider;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.storage.BaseStorage;
import com.getstream.sdk.chat.rest.utils.CallFake;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/*
 * Created by Anton Bevza on 2019-10-23.
 */
public class ClientTest {

    private static String TEST_API_KEY = "testApiKey";
    private static String TEST_CLIENT_ID = "testClientId";
    private static String TEST_USER_ID = "testUserId";
    private static String TEST_CHANNEL_TYPE = "testChannelType";
    private static String TEST_CHANNEL_ID = "testChannelId";

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

        WebSocketServiceProvider webSocketServiceProvider = mock(WebSocketServiceProvider.class);
        doReturn(webSocketService).when(webSocketServiceProvider).provideWebSocketService(
                any(), any(), any()
        );

        ApiServiceProvider apiServiceProvider = mock(ApiServiceProvider.class);
        doReturn(apiService).when(apiServiceProvider).provideApiService(any());

        UploadStorageProvider uploadStorageProvider = mock(UploadStorageProvider.class);
        doReturn(uploadStorage).when(uploadStorageProvider).provideUploadStorage(any(), any());

        client = new Client(TEST_API_KEY,
                apiServiceProvider,
                webSocketServiceProvider,
                uploadStorageProvider,
                null);

        simulateConnection();
    }

    @Test
    void queryChannelsSuccessTest() {
        QueryChannelsResponse response = new QueryChannelsResponse();
        response.setChannelStates(new ArrayList<>());
        QueryChannelListCallback callback = mock(QueryChannelListCallback.class);

        when(apiService.queryChannels(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));
        FilterObject filter = Filters.and(Filters.eq("type", "messaging"));
        QuerySort sort = new QuerySort().asc("created_at");
        QueryChannelsRequest request = new QueryChannelsRequest(filter, sort)
                .withMessageLimit(10)
                .withPresence()
                .withOffset(5)
                .withLimit(10);

        client.queryChannels(request, callback);
        String payload = GsonConverter.Gson().toJson(request);

        verify(apiService).queryChannels(TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID, payload);
        verify(callback).onSuccess(response);

        //TODO client.queryChannels have a lot of side effects. Need to refactor the method.
    }

    @Test
    void stopWatchingChannelSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.stopWatching(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.stopWatchingChannel(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).stopWatching(TEST_CHANNEL_TYPE, TEST_CHANNEL_ID,
                TEST_API_KEY, TEST_CLIENT_ID,
                Collections.emptyMap());
        verify(callback).onSuccess(response);
    }

    @Test
    void hideChannelSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.hideChannel(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.hideChannel(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).hideChannel(TEST_CHANNEL_TYPE, TEST_CHANNEL_ID,
                TEST_API_KEY, TEST_CLIENT_ID,
                Collections.emptyMap());
        verify(callback).onSuccess(response);
    }


    @Test
    void showChannelSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.showChannel(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.showChannel(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).showChannel(TEST_CHANNEL_TYPE, TEST_CHANNEL_ID,
                TEST_API_KEY, TEST_CLIENT_ID,
                Collections.emptyMap());
        verify(callback).onSuccess(response);
    }

    @Test
    void updateChannelSuccessTest() {
        ChannelResponse response = new ChannelResponse("100", new Channel());
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.updateChannel(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        Channel channel = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID);
        channel.setName("test name");
        Message updateMessage = new Message();
        updateMessage.setText("test text");

        client.updateChannel(channel, updateMessage, callback);

        verify(apiService).updateChannel(eq(TEST_CHANNEL_TYPE),
                eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY),
                eq(TEST_CLIENT_ID),
                argThat(argument -> argument.getData().get("name").equals(channel.getName())
                        && argument.getUpdateMessage().getText().equals(updateMessage.getText())));
        verify(callback).onSuccess(response);
    }

    @Test
    void deleteChannelSuccessTest() {
        ChannelResponse response = new ChannelResponse("100", new Channel());
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.deleteChannel(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.deleteChannel(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).deleteChannel(TEST_CHANNEL_TYPE, TEST_CHANNEL_ID, TEST_API_KEY, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void acceptInviteSuccessTest() {
        ChannelResponse response = new ChannelResponse("100", new Channel());
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.acceptInvite(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String updateMessage = "testMessage";

        client.acceptInvite(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), updateMessage, callback);

        verify(apiService).acceptInvite(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY), eq(TEST_CLIENT_ID),
                argThat(argument ->
                        argument.isAcceptInvite() && argument.getMessage().getText().equals(updateMessage)
                ));
        verify(callback).onSuccess(response);
    }

    @Test
    void banUserSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.banUser(any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId = "testUserId";
        String reason = "testReason";
        int timeout = 100;

        client.banUser(targetUserId, new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID),
                reason, timeout, callback);

        verify(apiService).banUser(eq(TEST_API_KEY), eq(TEST_CLIENT_ID),
                argThat(argument ->
                        argument.getChannelType().equals(TEST_CHANNEL_TYPE) &&
                                argument.getChannelId().equals(TEST_CHANNEL_ID) &&
                                argument.getReason().equals(reason) &&
                                argument.getTimeout() == timeout &&
                                argument.getTargetUserId().equals(targetUserId)
                )
        );
        verify(callback).onSuccess(response);
    }

    @Test
    void unBanUserSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.unBanUser(any(), any(), any(),
                any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId = "testUserId";

        client.unBanUser(targetUserId, new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).unBanUser(TEST_API_KEY, TEST_CLIENT_ID,
                targetUserId, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID
        );
        verify(callback).onSuccess(response);
    }

    private void simulateConnection() {
        client.setUser(new User(TEST_USER_ID), new TestTokenProvider());
        Event event = new Event();
        event.setConnectionId(TEST_CLIENT_ID);
        client.connectionResolved(event);
    }
}
