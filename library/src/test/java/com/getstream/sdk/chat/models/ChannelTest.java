package com.getstream.sdk.chat.models;

import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.PaginationOptions;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.GetReactionsCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryWatchCallback;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.ChannelWatchRequest;
import com.getstream.sdk.chat.rest.request.HideChannelRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.storage.BaseStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/*
 * Created by Anton Bevza on 2019-10-30.
 */
public class ChannelTest {

    private static String TEST_CHANNEL_TYPE = "testChannelType";
    private static String TEST_CHANNEL_ID = "testChannelId";

    @Mock
    Client client;

    private Channel channel;

    @BeforeEach
    void initTest() {
        MockitoAnnotations.initMocks(this);

        channel = new Channel(client, TEST_CHANNEL_TYPE, TEST_CHANNEL_ID);
    }

    @Test
    void sendMessageTest() {
        MessageCallback callback = mock(MessageCallback.class);
        Message message = new Message();

        when(client.isConnected()).thenReturn(true);
        channel.sendMessage(message, callback);

        verify(client).sendMessage(channel, message, callback);
    }

    @Test
    void sendReactionParamsTest() {
        MessageCallback callback = mock(MessageCallback.class);
        String messageId = "testMessageId";
        String reactionType = "like";
        Map<String, Object> extra = Collections.singletonMap("testKey", "testValue");

        channel.sendReaction(messageId, reactionType, extra, callback);

        verify(client).sendReaction(argThat(argument -> argument.getReaction().getMessageId().equals(messageId)
                        && argument.getReaction().getType().equals(reactionType)
                        && argument.getReaction().getExtraData().equals(extra)),
                eq(callback));
    }

    @Test
    void sendReactionTest() {
        MessageCallback callback = mock(MessageCallback.class);
        Reaction reaction = new Reaction();

        channel.sendReaction(reaction, callback);

        verify(client).sendReaction(argThat(argument -> argument.getReaction().equals(reaction)), eq(callback));
    }

    @Test
    void sendEventTest() {
        EventCallback callback = mock(EventCallback.class);

        channel.sendEvent(EventType.MESSAGE_READ, callback);

        verify(client).sendEvent(eq(channel),
                argThat(argument ->
                        argument.getEvent().get("type").equals(EventType.MESSAGE_READ.label)),
                eq(callback));
    }

    @Test
    void watchTest() {
        ChannelWatchRequest request = new ChannelWatchRequest();
        QueryWatchCallback callback = mock(QueryWatchCallback.class);

        channel.watch(request, callback);

        verify(client).queryChannel(eq(channel), eq(request), any(QueryChannelCallback.class));
    }

    @Test
    void queryTest() {
        ChannelQueryRequest request = new ChannelQueryRequest();
        QueryChannelCallback callback = mock(QueryChannelCallback.class);

        channel.query(request, callback);

        verify(client).queryChannel(channel, request, callback);
    }

    @Test
    void createTest() {
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("name", "test channel name");

        List<String> members = new ArrayList<>();
        members.add("test_user_Id");
        extraData.put("members", members);

        channel.setExtraData(extraData);
        QueryChannelCallback callback = mock(QueryChannelCallback.class);
        channel.query(callback);

        verify(client).queryChannel(eq(channel), argThat(argument ->
                argument.isWatch() &&
                        argument.getData().get("name").equals(channel.getName()) &&
                        argument.getData().get("members").equals(members)), eq(callback));
    }

    @Test
    void getRepliesTest() {
        GetRepliesCallback callback = mock(GetRepliesCallback.class);
        String parentId = "testParentId";
        int limit = 10;
        String firstMessageId = "testMessageId";

        channel.getReplies(parentId, limit, firstMessageId, callback);

        verify(client).getReplies(parentId, limit, firstMessageId, callback);
    }

    @Test
    void updateMessageTest() {
        MessageCallback callback = mock(MessageCallback.class);
        Message message = new Message();

        channel.updateMessage(message, callback);

        verify(client).updateMessage(message, callback);
    }

    @Test
    void deleteMessageTest() {
        MessageCallback callback = mock(MessageCallback.class);
        Message message = new Message();

        channel.deleteMessage(message, callback);

        verify(client).deleteMessage(message.getId(), callback);
    }

    @Test
    void sendImageTest() throws IOException {
        BaseStorage uploadStorage = mock(BaseStorage.class);
        when(client.getUploadStorage()).thenReturn(uploadStorage);

        UploadFileCallback callback = mock(UploadFileCallback.class);
        File file = File.createTempFile("temp", ".tmp");
        String filePath = file.getPath();
        String mimeType = "testMimeType";

        channel.sendImage(filePath, mimeType, callback);

        verify(uploadStorage).sendFile(eq(channel),
                argThat(argument -> argument.getPath().equals(filePath)), eq(mimeType), eq(callback));
    }

    @Test
    void sendFileTest() throws IOException {
        BaseStorage uploadStorage = mock(BaseStorage.class);
        when(client.getUploadStorage()).thenReturn(uploadStorage);

        UploadFileCallback callback = mock(UploadFileCallback.class);
        File file = File.createTempFile("temp", ".tmp");
        String filePath = file.getPath();
        String mimeType = "testMimeType";

        channel.sendFile(filePath, mimeType, callback);

        verify(uploadStorage).sendFile(eq(channel),
                argThat(argument -> argument.getPath().equals(filePath)), eq(mimeType), eq(callback));
    }

    @Test
    void deleteFileTest() {
        BaseStorage uploadStorage = mock(BaseStorage.class);
        when(client.getUploadStorage()).thenReturn(uploadStorage);

        CompletableCallback callback = mock(CompletableCallback.class);
        String fileUrl = "testFileUrl";

        channel.deleteFile(fileUrl, callback);

        verify(uploadStorage).deleteFile(channel, fileUrl, callback);
    }

    @Test
    void deleteImageTest() {
        BaseStorage uploadStorage = mock(BaseStorage.class);
        when(client.getUploadStorage()).thenReturn(uploadStorage);

        CompletableCallback callback = mock(CompletableCallback.class);
        String fileUrl = "testFileUrl";

        channel.deleteImage(fileUrl, callback);

        verify(uploadStorage).deleteImage(channel, fileUrl, callback);
    }

    @Test
    void getReactionsWithPaginationOptionsTest() {
        GetReactionsCallback callback = mock(GetReactionsCallback.class);
        PaginationOptions paginationOptions = new PaginationOptions.Builder().build();
        String messageId = "testMessageId";

        channel.getReactions(messageId, paginationOptions, callback);

        verify(client).getReactions(messageId, paginationOptions, callback);
    }

    @Test
    void getReactionsTest() {
        GetReactionsCallback callback = mock(GetReactionsCallback.class);
        String messageId = "testMessageId";

        channel.getReactions(messageId, callback);

        verify(client).getReactions(messageId, callback);
    }

    @Test
    void deleteReactionTest() {
        MessageCallback callback = mock(MessageCallback.class);
        String messageId = "testMessageId";
        String reactionType = "like";

        channel.deleteReaction(messageId, reactionType, callback);

        verify(client).deleteReaction(messageId, reactionType, callback);
    }

    @Test
    void sendActionTest() {
        MessageCallback callback = mock(MessageCallback.class);
        String messageId = "testMessageId";
        SendActionRequest request = new SendActionRequest(TEST_CHANNEL_ID, messageId, "testType",
                Collections.emptyMap());

        channel.sendAction(messageId, request, callback);

        verify(client).sendAction(messageId, request, callback);
    }

    @Test
    void flagMessageTest() {
        FlagCallback callback = mock(FlagCallback.class);
        String messageId = "testMessageId";

        channel.flagMessage(messageId, callback);

        verify(client).flagMessage(messageId, callback);
    }

    @Test
    void unFlagMessageTest() {
        FlagCallback callback = mock(FlagCallback.class);
        String messageId = "testMessageId";

        channel.unFlagMessage(messageId, callback);

        verify(client).unFlagMessage(messageId, callback);
    }

    @Test
    void banUserTest() {
        CompletableCallback callback = mock(CompletableCallback.class);
        String targetUserId = "testUserId";
        String reason = "testReason";
        int timeout = 100;

        channel.banUser(targetUserId, reason, timeout, callback);

        verify(client).banUser(targetUserId, channel, reason, timeout, callback);
    }

    @Test
    void unBanUserTest() {
        CompletableCallback callback = mock(CompletableCallback.class);
        String targetUserId = "testUserId";

        channel.unBanUser(targetUserId, callback);

        verify(client).unBanUser(targetUserId, channel, callback);
    }

    @Test
    void addMembersTest() {
        ChannelCallback callback = mock(ChannelCallback.class);
        ArrayList<String> members = new ArrayList<>();

        channel.addMembers(members, callback);

        verify(client).addMembers(channel, members, callback);
    }

    @Test
    void removeMembersTest() {
        ChannelCallback callback = mock(ChannelCallback.class);
        ArrayList<String> members = new ArrayList<>();

        channel.removeMembers(members, callback);

        verify(client).removeMembers(channel, members, callback);
    }

    @Test
    void acceptInviteTest() {
        ChannelCallback callback = mock(ChannelCallback.class);
        String message = "testMessage";

        channel.acceptInvite(message, callback);

        verify(client).acceptInvite(channel, message, callback);
    }

    @Test
    void rejectInviteTest() {
        ChannelCallback callback = mock(ChannelCallback.class);

        channel.rejectInvite(callback);

        verify(client).rejectInvite(channel, callback);
    }

    @Test
    void stopTypingTest() {
        channel.setLastStartTypingEvent(new Date());

        EventCallback callback = mock(EventCallback.class);

        channel.stopTyping(callback);

        assertNull(channel.getLastStartTypingEvent());
        verify(client).sendEvent(eq(channel), argThat(argument ->
                argument.getEvent().get("type").equals(EventType.TYPING_STOP.label)), eq(callback));
    }

    @Test
    void markReadTest() {
        EventCallback callback = mock(EventCallback.class);

        channel.markRead(callback);

        verify(client).markRead(eq(channel),
                argThat(argument -> argument.getEvent().get("message_id") == null),
                eq(callback));
    }

    @Test
    void hideTestWithClearHistory() {
        CompletableCallback callback = mock(CompletableCallback.class);

        HideChannelRequest request = new HideChannelRequest(true);
        channel.hide(request, callback);

        verify(client).hideChannel(channel, request, callback);
    }

    @Test
    void hideTest() {
        CompletableCallback callback = mock(CompletableCallback.class);

        HideChannelRequest request = new HideChannelRequest();
        channel.hide(request, callback);

        verify(client).hideChannel(channel, request, callback);
    }

    @Test
    void showTest() {
        CompletableCallback callback = mock(CompletableCallback.class);

        channel.show(callback);

        verify(client).showChannel(channel, callback);
    }

    @Test
    void updateWithMessageTest() {
        ChannelCallback callback = mock(ChannelCallback.class);
        Message message = new Message();

        channel.update(message, callback);

        verify(client).updateChannel(channel, message, callback);
    }

    @Test
    void updateTest() {
        ChannelCallback callback = mock(ChannelCallback.class);

        channel.update(callback);

        verify(client).updateChannel(channel, null, callback);
    }

    @Test
    void deleteTest() {
        ChannelCallback callback = mock(ChannelCallback.class);

        channel.delete(callback);

        verify(client).deleteChannel(channel, callback);
    }

    @Test
    void stopWatchingTest() {
        CompletableCallback callback = mock(CompletableCallback.class);

        channel.stopWatching(callback);

        verify(client).stopWatchingChannel(channel, callback);
    }
}
