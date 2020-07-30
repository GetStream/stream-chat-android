package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.Filters;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Config;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.PaginationOptions;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.providers.ApiServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.StorageProvider;
import com.getstream.sdk.chat.rest.core.providers.UploadStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.WebSocketServiceProvider;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.GetDevicesCallback;
import com.getstream.sdk.chat.rest.interfaces.GetReactionsCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.MuteUserCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.interfaces.SearchMessagesCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.HideChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.request.QueryUserRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SearchMessagesRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FlagResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetReactionsResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.rest.response.MuteUserResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.rest.response.SearchMessagesResponse;
import com.getstream.sdk.chat.rest.storage.BaseStorage;
import com.getstream.sdk.chat.rest.utils.CallFake;
import com.getstream.sdk.chat.rest.utils.TestTokenProvider;
import com.getstream.sdk.chat.storage.Storage;
import com.getstream.sdk.chat.storage.Sync;
import com.getstream.sdk.chat.utils.strings.StubStringsProvider;
import com.google.gson.internal.LinkedTreeMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @Mock
    private Storage storage;

    private Client client;


    @BeforeEach
    void initTest() throws IOException {

        StreamChat.setStringsProvider(new StubStringsProvider());

        MockitoAnnotations.initMocks(this);

        WebSocketServiceProvider webSocketServiceProvider = mock(WebSocketServiceProvider.class);
        doReturn(webSocketService).when(webSocketServiceProvider).provideWebSocketService(
                any(), any(), any(), anyBoolean()
        );

        ApiServiceProvider apiServiceProvider = mock(ApiServiceProvider.class);
        doReturn(apiService).when(apiServiceProvider).provideApiService(any(), anyBoolean());

        UploadStorageProvider uploadStorageProvider = mock(UploadStorageProvider.class);
        doReturn(uploadStorage).when(uploadStorageProvider).provideUploadStorage(any(), any());

        StorageProvider storageProvider = mock(StorageProvider.class);
        doReturn(storage).when(storageProvider).provideStorage(any(), any(), anyBoolean());

        StreamLogger logger = mock(StreamLogger.class);
        StreamChat.setLogger(logger);

        client = new Client(TEST_API_KEY,
                new ApiClientOptions(),
                apiServiceProvider,
                webSocketServiceProvider,
                uploadStorageProvider,
                storageProvider,
                null);

        simulateConnection();
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

        HideChannelRequest request = new HideChannelRequest(true);

        when(apiService.hideChannel(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.hideChannel(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), request, callback);

        verify(apiService).hideChannel(TEST_CHANNEL_TYPE, TEST_CHANNEL_ID,
                TEST_API_KEY, TEST_CLIENT_ID,
                request);
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
        ChannelResponse response = new ChannelResponse();
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
        ChannelResponse response = new ChannelResponse();
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.deleteChannel(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.deleteChannel(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).deleteChannel(TEST_CHANNEL_TYPE, TEST_CHANNEL_ID, TEST_API_KEY, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void acceptInviteSuccessTest() {
        ChannelResponse response = new ChannelResponse();
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
    void rejectInviteSuccessTest() {
        ChannelResponse response = new ChannelResponse();
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.rejectInvite(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.rejectInvite(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), callback);

        verify(apiService).rejectInvite(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.isRejectInvite()));
        verify(callback).onSuccess(response);
    }

    @Test
    void sendMessageSuccessTest() {
        String testMessageId = "testMessageId";
        String testMessageText = "testMessageText";
        Message message = new Message();
        message.setSyncStatus(Sync.LOCAL_ONLY);
        message.setId(testMessageId);
        message.setText(testMessageText);

        MessageResponse response = new MessageResponse();
        response.setMessage(message);
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.sendMessage(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.sendMessage(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), message, callback);

        verify(apiService).sendMessage(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> ((LinkedTreeMap) argument.get("message")).get("id").equals(testMessageId)
                        && ((LinkedTreeMap) argument.get("message")).get("text").equals(testMessageText)
                ));
        verify(callback).onSuccess(response);
        assertEquals((int) response.getMessage().getSyncStatus(), Sync.SYNCED);
    }

    @Test
    void updateMessageSuccessTest() {
        MessageResponse response = new MessageResponse();
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.updateMessage(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        String testMessageText = "testMessageText";
        Message message = new Message();
        message.setId(testMessageId);
        message.setText(testMessageText);

        client.updateMessage(message, callback);

        verify(apiService).updateMessage(eq(message.getId()),
                eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> ((LinkedTreeMap) argument.get("message")).get("id").equals(testMessageId)
                        && ((LinkedTreeMap) argument.get("message")).get("text").equals(testMessageText)
                ));
        verify(callback).onSuccess(response);
    }

    @Test
    void getMessageSuccessTest() {
        MessageResponse response = new MessageResponse();
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.getMessage(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";

        client.getMessage(testMessageId, callback);

        verify(apiService).getMessage(testMessageId, TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void deleteMessageSuccessTest() {
        MessageResponse response = new MessageResponse();
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.deleteMessage(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";

        client.deleteMessage(testMessageId, callback);

        verify(apiService).deleteMessage(testMessageId, TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void markReadSuccessTest() {
        EventResponse response = new EventResponse();
        EventCallback callback = mock(EventCallback.class);

        when(apiService.markRead(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        MarkReadRequest markReadRequest = new MarkReadRequest(testMessageId);

        Config config = new Config();
        config.setReadEvents(true);
        client.addChannelConfig(TEST_CHANNEL_TYPE, config);

        client.markRead(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), markReadRequest, callback);

        verify(apiService).markRead(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID), eq(TEST_API_KEY),
                eq(TEST_USER_ID), eq(TEST_CLIENT_ID), eq(markReadRequest));
        verify(callback).onSuccess(response);
    }

    @Test
    void markReadErrorReadEventsDisabledTest() {
        EventResponse response = new EventResponse();
        EventCallback callback = mock(EventCallback.class);

        when(apiService.markRead(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        MarkReadRequest markReadRequest = new MarkReadRequest(testMessageId);

        Config config = new Config();
        config.setReadEvents(false);
        client.addChannelConfig(TEST_CHANNEL_TYPE, config);

        client.markRead(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), markReadRequest, callback);

        verify(callback).onError(Mockito.any(), Mockito.eq(-1));
    }

    @Test
    void markReadErrorMissingConfigTest() {
        EventResponse response = new EventResponse();
        EventCallback callback = mock(EventCallback.class);

        when(apiService.markRead(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        MarkReadRequest markReadRequest = new MarkReadRequest(testMessageId);

        client.markRead(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), markReadRequest, callback);

        verify(callback).onError(Mockito.any(), Mockito.eq(-1));
    }

    @Test
    void searchMessagesSuccessTest() {
        SearchMessagesResponse response = new SearchMessagesResponse();
        SearchMessagesCallback callback = mock(SearchMessagesCallback.class);

        when(apiService.searchMessages(any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        FilterObject filter = Filters.and(Filters.eq("user_id", "testUserId"));
        SearchMessagesRequest request = new SearchMessagesRequest(filter, "testQuery");
        String payload = GsonConverter.Gson().toJson(request);

        client.searchMessages(request, callback);

        verify(apiService).searchMessages(TEST_API_KEY, TEST_CLIENT_ID, payload);
        verify(callback).onSuccess(response);
    }

    @Test
    void markAllReadSuccessTest() {
        EventResponse response = new EventResponse();
        EventCallback callback = mock(EventCallback.class);

        when(apiService.markAllRead(any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.markAllRead(callback);

        verify(apiService).markAllRead(TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void getRepliesWithIdLtSuccessTest() {
        GetRepliesResponse response = new GetRepliesResponse();
        GetRepliesCallback callback = mock(GetRepliesCallback.class);

        when(apiService.getRepliesMore(any(), any(), any(), any(), anyInt(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        String idLt = "testIdLt";
        int limit = 10;

        client.getReplies(testMessageId, limit, idLt, callback);

        verify(apiService).getRepliesMore(testMessageId, TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID,
                limit, idLt);
        verify(callback).onSuccess(response);
    }

    @Test
    void getRepliesWithoutIdLtSuccessTest() {
        GetRepliesResponse response = new GetRepliesResponse();
        GetRepliesCallback callback = mock(GetRepliesCallback.class);

        when(apiService.getReplies(any(), any(), any(), any(), anyInt()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        int limit = 10;

        client.getReplies(testMessageId, limit, null, callback);

        verify(apiService).getReplies(testMessageId, TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID, limit);
        verify(callback).onSuccess(response);
    }

    @Test
    void sendReactionSuccessTest() {
        MessageResponse response = new MessageResponse();
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.sendReaction(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        String reactionType = "like";
        Reaction reaction = new Reaction();
        reaction.setMessageId(testMessageId);
        reaction.setType(reactionType);
        ReactionRequest request = new ReactionRequest(reaction);

        client.sendReaction(request, callback);

        verify(apiService).sendReaction(eq(testMessageId), eq(TEST_API_KEY), eq(TEST_USER_ID),
                eq(TEST_CLIENT_ID), eq(request));
        verify(callback).onSuccess(response);
    }

    @Test
    void deleteReactionSuccessTest() {
        MessageResponse response = new MessageResponse();
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.deleteReaction(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        String reactionType = "like";

        client.deleteReaction(testMessageId, reactionType, callback);

        verify(apiService).deleteReaction(testMessageId, reactionType,
                TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void getReactionsWithPaginationParamsSuccessTest() {
        GetReactionsResponse response = new GetReactionsResponse();
        GetReactionsCallback callback = mock(GetReactionsCallback.class);

        when(apiService.getReactions(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        int limit = 10;
        int offset = 5;
        PaginationOptions paginationOptions = new PaginationOptions.Builder()
                .limit(limit)
                .offset(offset)
                .build();

        client.getReactions(testMessageId, paginationOptions, callback);

        verify(apiService).getReactions(testMessageId, TEST_API_KEY, TEST_CLIENT_ID, limit, offset);
        verify(callback).onSuccess(response);
    }

    @Test
    void getReactionsWithoutPaginationParamsSuccessTest() {
        GetReactionsResponse response = new GetReactionsResponse();
        GetReactionsCallback callback = mock(GetReactionsCallback.class);

        when(apiService.getReactions(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";

        client.getReactions(testMessageId, callback);

        verify(apiService).getReactions(testMessageId, TEST_API_KEY, TEST_CLIENT_ID, 10, 0);
        verify(callback).onSuccess(response);
    }

    @Test
    void sendEventSuccessTest() {
        EventResponse response = new EventResponse();
        EventCallback callback = mock(EventCallback.class);

        when(apiService.sendEvent(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        SendEventRequest request =
                new SendEventRequest(Collections.singletonMap("type", EventType.MESSAGE_READ));

        client.sendEvent(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), request, callback);

        verify(apiService).sendEvent(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID), eq(request));
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

    @Test
    void queryUsersSuccessTest() {
        QueryUserListResponse response = new QueryUserListResponse();
        QueryUserListCallback callback = mock(QueryUserListCallback.class);
        ClientState mockedState = mock(ClientState.class);

        when(apiService.queryUsers(any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        FilterObject filter = Filters.and(Filters.eq("type", "messaging"));
        QuerySort sort = new QuerySort().asc("created_at");
        QueryUserRequest request = new QueryUserRequest(filter, sort).withPresence()
                .withLimit(10).withOffset(0);
        String payload = GsonConverter.Gson().toJson(request);

        client.setState(mockedState);
        client.queryUsers(request, callback);

        verify(apiService).queryUsers(TEST_API_KEY, TEST_CLIENT_ID, payload);
        verify(mockedState).updateUsers(response.getUsers());
        verify(callback).onSuccess(response);
    }

    @Test
    void muteUserSuccessTest() {
        MuteUserResponse response = new MuteUserResponse();
        MuteUserCallback callback = mock(MuteUserCallback.class);

        when(apiService.muteUser(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId = "testUserId";

        client.muteUser(targetUserId, callback);

        verify(apiService).muteUser(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.get("target_id").equals(targetUserId) &&
                        argument.get("user_id").equals(TEST_USER_ID)
                )
        );
        verify(callback).onSuccess(response);
    }

    @Test
    void unmuteUserSuccessTest() {
        MuteUserResponse response = new MuteUserResponse();
        MuteUserCallback callback = mock(MuteUserCallback.class);

        when(apiService.unMuteUser(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId = "testUserId";

        client.unmuteUser(targetUserId, callback);

        verify(apiService).unMuteUser(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.get("target_id").equals(targetUserId) &&
                        argument.get("user_id").equals(TEST_USER_ID)
                )
        );
        verify(callback).onSuccess(response);
    }

    @Test
    void flagUserSuccessTest() {
        FlagResponse response = new FlagResponse();
        FlagCallback callback = mock(FlagCallback.class);

        when(apiService.flag(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId = "testUserId";

        client.flagUser(targetUserId, callback);

        verify(apiService).flag(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.get("target_user_id").equals(targetUserId))
        );
        verify(callback).onSuccess(response);
    }

    @Test
    void unFlagUserSuccessTest() {
        FlagResponse response = new FlagResponse();
        FlagCallback callback = mock(FlagCallback.class);

        when(apiService.unFlag(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId = "testUserId";

        client.unFlagUser(targetUserId, callback);

        verify(apiService).unFlag(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.get("target_user_id").equals(targetUserId))
        );
        verify(callback).onSuccess(response);
    }

    @Test
    void addMembersSuccessTest() {
        ChannelResponse response = new ChannelResponse();
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.addMembers(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId1 = "target_user_id_1";
        String targetUserId2 = "target_user_id_2";
        ArrayList<String> members = new ArrayList<>();
        members.add(targetUserId1);
        members.add(targetUserId2);
        client.addMembers(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), members, callback);

        verify(apiService).addMembers(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY), eq(TEST_CLIENT_ID),
                argThat(argument ->
                        argument.getMembers().get(0).equals(targetUserId1)
                                && argument.getMembers().get(1).equals(targetUserId2)
                ));
        verify(callback).onSuccess(response);
    }

    @Test
    void removeMembersSuccessTest() {
        ChannelResponse response = new ChannelResponse();
        ChannelCallback callback = mock(ChannelCallback.class);

        when(apiService.removeMembers(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetUserId1 = "target_user_id_1";
        String targetUserId2 = "target_user_id_2";
        ArrayList<String> members = new ArrayList<>();
        members.add(targetUserId1);
        members.add(targetUserId2);
        client.removeMembers(new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID), members, callback);

        verify(apiService).removeMembers(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID),
                eq(TEST_API_KEY), eq(TEST_CLIENT_ID),
                argThat(argument ->
                        argument.getMembers().get(0).equals(targetUserId1)
                                && argument.getMembers().get(1).equals(targetUserId2)
                ));
        verify(callback).onSuccess(response);
    }

    @Test
    void sendActionSuccessTest() {
        MessageResponse response = new MessageResponse();
        MessageCallback callback = mock(MessageCallback.class);

        when(apiService.sendAction(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testMessageId = "testMessageId";
        SendActionRequest request = new SendActionRequest(TEST_CHANNEL_ID, testMessageId,
                "testType", null);

        client.sendAction(testMessageId, request, callback);

        verify(apiService).sendAction(testMessageId, TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID,
                request);
        verify(callback).onSuccess(response);
    }

    @Test
    void addDeviceSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.addDevices(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testDeviceId = "testDeviceId";

        client.addDevice(testDeviceId, callback);

        verify(apiService).addDevices(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.getId().equals(testDeviceId)));
        verify(callback).onSuccess(response);
    }

    @Test
    void getDevicesSuccessTest() {
        GetDevicesResponse response = new GetDevicesResponse();
        GetDevicesCallback callback = mock(GetDevicesCallback.class);

        when(apiService.getDevices(any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        client.getDevices(callback);

        verify(apiService).getDevices(TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void removeDeviceSuccessTest() {
        CompletableResponse response = new CompletableResponse();
        CompletableCallback callback = mock(CompletableCallback.class);

        when(apiService.deleteDevice(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String testDeviceId = "testDeviceId";

        client.removeDevice(testDeviceId, callback);

        verify(apiService).deleteDevice(testDeviceId, TEST_API_KEY, TEST_USER_ID, TEST_CLIENT_ID);
        verify(callback).onSuccess(response);
    }

    @Test
    void flagMessageSuccessTest() {
        FlagResponse response = new FlagResponse();
        FlagCallback callback = mock(FlagCallback.class);

        when(apiService.flag(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetMessageId = "testMessageId";

        client.flagMessage(targetMessageId, callback);

        verify(apiService).flag(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.get("target_message_id").equals(targetMessageId))
        );
        verify(callback).onSuccess(response);
    }

    @Test
    void unFlagMessageSuccessTest() {
        FlagResponse response = new FlagResponse();
        FlagCallback callback = mock(FlagCallback.class);

        when(apiService.unFlag(any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));

        String targetMessageId = "testMessageId";

        client.unFlagMessage(targetMessageId, callback);

        verify(apiService).unFlag(eq(TEST_API_KEY), eq(TEST_USER_ID), eq(TEST_CLIENT_ID),
                argThat(argument -> argument.get("target_message_id").equals(targetMessageId))
        );
        verify(callback).onSuccess(response);
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
        verify(storage).insertQueryWithChannels(any(QueryChannelsQ.class), eq(response.getChannels()));
    }

    @Test
    void queryChannelWithIdSuccessTest() {
        ArrayList<Message> messages = new ArrayList<>();
        Message message = new Message();
        Date date = new Date();
        message.setCreatedAt(date);
        message.setUpdatedAt(date);

        messages.add(message);
        HashMap<String, Object> extra = new HashMap<>();
        String testChannelName = "testName";
        extra.put("name", testChannelName);

        Channel channelResponse = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID, extra);
        ChannelState response = new ChannelState();
        response.setMessages(messages);
        response.setChannel(channelResponse);
        QueryChannelCallback callback = mock(QueryChannelCallback.class);

        Channel channel = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID, extra);
        channel.setChannelState(new ChannelState(channel));

        when(apiService.queryChannel(any(), any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));
        ChannelQueryRequest request = new ChannelQueryRequest()
                .withPresence()
                .withWatch();

        client.queryChannel(channel, request, callback);

        verify(apiService).queryChannel(eq(TEST_CHANNEL_TYPE), eq(TEST_CHANNEL_ID), eq(TEST_API_KEY),
                eq(TEST_USER_ID), eq(TEST_CLIENT_ID), argThat(argument ->
                        argument.isWatch()
                                && argument.isPresence()
                                && argument.getData().get("name").equals(testChannelName)));
        verify(callback).onSuccess(response);
        verify(storage).insertMessagesForChannel(channel, response.getMessages());
    }

    @Test
    void queryChannelWithoutIdSuccessTest() {
        ArrayList<Message> messages = new ArrayList<>();
        Message message = new Message();

        Date date = new Date();
        message.setCreatedAt(date);
        message.setUpdatedAt(date);

        messages.add(message);
        HashMap<String, Object> extra = new HashMap<>();
        String testChannelName = "testName";
        extra.put("name", testChannelName);

        Channel channelResponse = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID, extra);
        ChannelState response = new ChannelState();
        response.setMessages(messages);
        response.setChannel(channelResponse);
        QueryChannelCallback callback = mock(QueryChannelCallback.class);

        Channel channel = new Channel(client, TEST_CHANNEL_TYPE, null, extra);
        channel.setChannelState(new ChannelState(channel));

        when(apiService.queryChannel(any(), any(), any(), any(), any()))
                .thenReturn(CallFake.buildSuccess(response));
        ChannelQueryRequest request = new ChannelQueryRequest()
                .withPresence()
                .withWatch();

        client.queryChannel(channel, request, callback);

        verify(apiService).queryChannel(eq(TEST_CHANNEL_TYPE), eq(TEST_API_KEY),
                eq(TEST_USER_ID), eq(TEST_CLIENT_ID), argThat(argument ->
                        argument.isWatch()
                                && argument.isPresence()
                                && argument.getData().get("name").equals(testChannelName)));
        verify(callback).onSuccess(response);
        verify(storage).insertMessagesForChannel(channel, response.getMessages());
    }

    @Test
    void disconnectTest() {
        client.disconnect();
        verify(webSocketService).disconnect();
        assertNull(client.tokenProvider);
        assertNull(client.cacheUserToken);
        assertFalse(client.fetchingToken);
        assertTrue(client.getActiveChannelMap().isEmpty());
    }

    private void simulateConnection() {
        client.setUser(new User(TEST_USER_ID), new TestTokenProvider());
        Event event = new Event();
        event.setConnectionId(TEST_CLIENT_ID);
        client.connectionResolved(event);
    }

}
