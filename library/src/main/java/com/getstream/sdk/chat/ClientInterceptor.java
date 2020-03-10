package com.getstream.sdk.chat;

import android.content.Context;

import com.getstream.sdk.chat.storage.InMemoryCache;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.*;
import io.getstream.chat.android.client.call.Call;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.models.*;
import io.getstream.chat.android.client.socket.InitConnectionListener;
import io.getstream.chat.android.client.socket.SocketListener;
import io.getstream.chat.android.client.token.TokenProvider;
import io.getstream.chat.android.client.utils.ProgressCallback;
import io.getstream.chat.android.client.utils.observable.ChatObservable;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Temp interceptor to keep in memory data
 */
public class ClientInterceptor implements ChatClient, InMemoryCache {

    private final ChatClient client;

    private Map<String, User> idUsersMap = new ConcurrentHashMap<>();

    public ClientInterceptor(ChatClient client) {
        this.client = client;
    }

    @androidx.annotation.Nullable
    @Override
    public User getUserById(String userId) {
        return idUsersMap.get(userId);
    }

    @NotNull
    @Override
    public Call<Channel> acceptInvite(@NotNull String s, @NotNull String s1, @NotNull String s2) {
        return client.acceptInvite(s, s1, s2);
    }

    @NotNull
    @Override
    public Call<Unit> addDevice(@NotNull String s) {
        return client.addDevice(s);
    }

    @NotNull
    @Override
    public Call<ChannelResponse> addMembers(@NotNull String s, @NotNull String s1, @NotNull List<String> list) {
        return client.addMembers(s, s1, list);
    }

    @Override
    public void addSocketListener(@NotNull SocketListener socketListener) {
        client.addSocketListener(socketListener);
    }

    @NotNull
    @Override
    public Call<CompletableResponse> banUser(@NotNull String s, @NotNull String s1, @NotNull String s2, @NotNull String s3, int i) {
        return client.banUser(s, s1, s2, s3, i);
    }

    @NotNull
    @Override
    public Call<Channel> deleteChannel(@NotNull String s, @NotNull String s1) {
        return client.deleteChannel(s, s1);
    }

    @NotNull
    @Override
    public Call<Unit> deleteDevice(@NotNull String s) {
        return client.deleteDevice(s);
    }

    @NotNull
    @Override
    public Call<Unit> deleteFile(@NotNull String s, @NotNull String s1, @NotNull String s2) {
        return client.deleteFile(s, s1, s2);
    }

    @NotNull
    @Override
    public Call<Unit> deleteImage(@NotNull String s, @NotNull String s1, @NotNull String s2) {
        return client.deleteImage(s, s1, s2);
    }

    @NotNull
    @Override
    public Call<Message> deleteMessage(@NotNull String s) {
        return client.deleteMessage(s);
    }

    @NotNull
    @Override
    public Call<Message> deleteReaction(@NotNull String s, @NotNull String s1) {
        return client.deleteReaction(s, s1);
    }

    @Override
    public void disconnect() {
        client.disconnect();
    }

    @Override
    public void disconnectSocket() {
        client.disconnectSocket();
    }

    @NotNull
    @Override
    public ChatObservable events() {
        return client.events();
    }

    @NotNull
    @Override
    public Call<FlagResponse> flag(@NotNull String s) {
        return client.flag(s);
    }

    @Nullable
    @Override
    public String getConnectionId() {
        return client.getConnectionId();
    }

    @Nullable
    @Override
    public User getCurrentUser() {
        return client.getCurrentUser();
    }

    @NotNull
    @Override
    public Call<List<Device>> getDevices() {
        return client.getDevices();
    }

    @NotNull
    @Override
    public Call<TokenResponse> getGuestToken(@NotNull String s, @NotNull String s1) {
        return client.getGuestToken(s, s1);
    }

    @NotNull
    @Override
    public Call<Message> getMessage(@NotNull String s) {
        return client.getMessage(s);
    }

    @NotNull
    @Override
    public Call<List<Reaction>> getReactions(@NotNull String s, int i, int i1) {
        return client.getReactions(s, i, i1);
    }

    @NotNull
    @Override
    public Call<List<Message>> getReplies(@NotNull String s, int i) {
        return client.getReplies(s, i);
    }

    @NotNull
    @Override
    public Call<List<Message>> getRepliesMore(@NotNull String s, @NotNull String s1, int i) {
        return client.getRepliesMore(s, s1, i);
    }

    @NotNull
    @Override
    public Call<List<User>> getUsers(@NotNull QueryUsersRequest queryUsersRequest) {
        return getUsers(queryUsersRequest).onNext(users -> {

            for (User user : users)
                idUsersMap.put(user.getId(), user);

            return null;
        });
    }

    @NotNull
    @Override
    public Call<Unit> hideChannel(@NotNull String s, @NotNull String s1, boolean b) {
        return client.hideChannel(s, s1, b);
    }

    @Override
    public boolean isSocketConnected() {
        return client.isSocketConnected();
    }

    @NotNull
    @Override
    public Call<ChatEvent> markAllRead() {
        return client.markAllRead();
    }

    @NotNull
    @Override
    public Call<Unit> markRead(@NotNull String s, @NotNull String s1, @NotNull String s2) {
        return client.markRead(s, s1, s2);
    }

    @NotNull
    @Override
    public Call<MuteUserResponse> muteUser(@NotNull String s) {
        return client.muteUser(s);
    }

    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage, @NotNull Context context) {
        client.onMessageReceived(remoteMessage, context);
    }

    @Override
    public void onNewTokenReceived(@NotNull String s, @NotNull Context context) {
        client.onNewTokenReceived(s, context);
    }

    @NotNull
    @Override
    public Call<Channel> queryChannel(@NotNull String s, @NotNull String s1, @NotNull ChannelQueryRequest channelQueryRequest) {
        return client.queryChannel(s, s1, channelQueryRequest);
    }

    @NotNull
    @Override
    public Call<List<Channel>> queryChannels(@NotNull QueryChannelsRequest queryChannelsRequest) {
        return client.queryChannels(queryChannelsRequest);
    }

    @Override
    public void reconnectSocket() {
        client.reconnectSocket();
    }

    @NotNull
    @Override
    public Call<Channel> rejectInvite(@NotNull String s, @NotNull String s1) {
        return client.rejectInvite(s, s1);
    }

    @NotNull
    @Override
    public Call<Channel> removeMembers(@NotNull String s, @NotNull String s1, @NotNull List<String> list) {
        return client.removeMembers(s, s1, list);
    }

    @Override
    public void removeSocketListener(@NotNull SocketListener socketListener) {
        client.removeSocketListener(socketListener);
    }

    @NotNull
    @Override
    public Call<List<Message>> searchMessages(@NotNull SearchMessagesRequest searchMessagesRequest) {
        return client.searchMessages(searchMessagesRequest);
    }

    @NotNull
    @Override
    public Call<Message> sendAction(@NotNull SendActionRequest sendActionRequest) {
        return client.sendAction(sendActionRequest);
    }

    @NotNull
    @Override
    public Call<ChatEvent> sendEvent(@NotNull String s, @NotNull String s1, @NotNull String s2, @NotNull Map<Object, ?> map) {
        return client.sendEvent(s, s1, s2, map);
    }

    @NotNull
    @Override
    public Call<String> sendFile(@NotNull String s, @NotNull String s1, @NotNull File file, @NotNull String s2) {
        return client.sendFile(s, s1, file, s2);
    }

    @Override
    public void sendFile(@NotNull String s, @NotNull String s1, @NotNull File file, @NotNull String s2, @NotNull ProgressCallback progressCallback) {
        client.sendFile(s, s1, file, s2, progressCallback);
    }

    @NotNull
    @Override
    public Call<Message> sendMessage(@NotNull String s, @NotNull String s1, @NotNull Message message) {
        return client.sendMessage(s, s1, message);
    }

    @NotNull
    @Override
    public Call<Reaction> sendReaction(@NotNull String s, @NotNull String s1) {
        return client.sendReaction(s, s1);
    }



    @NotNull
    @Override
    public Call<Unit> showChannel(@NotNull String s, @NotNull String s1) {
        return client.showChannel(s, s1);
    }

    @NotNull
    @Override
    public Call<Unit> stopWatching(@NotNull String s, @NotNull String s1) {
        return client.stopWatching(s, s1);
    }

    @NotNull
    @Override
    public Call<CompletableResponse> unBanUser(@NotNull String s, @NotNull String s1, @NotNull String s2) {
        return client.unBanUser(s, s1, s2);
    }

    @NotNull
    @Override
    public Call<MuteUserResponse> unMuteUser(@NotNull String s) {
        return client.unMuteUser(s);
    }

    @NotNull
    @Override
    public Call<Channel> updateChannel(@NotNull String s, @NotNull String s1, @NotNull Message message, @NotNull Map<String, ?> map) {
        return client.updateChannel(s, s1, message, map);
    }

    @NotNull
    @Override
    public Call<Message> updateMessage(@NotNull Message message) {
        return client.updateMessage(message);
    }

    @Override
    public void setAnonymousUser(@Nullable InitConnectionListener initConnectionListener) {
        client.setAnonymousUser(initConnectionListener);
    }

    @Override
    public void setUser(@NotNull User user, @NotNull TokenProvider tokenProvider, @Nullable InitConnectionListener initConnectionListener) {
        client.setUser(user, tokenProvider, initConnectionListener);
    }

    @Override
    public void setUser(@NotNull User user, @NotNull String s, @Nullable InitConnectionListener initConnectionListener) {
        client.setUser(user, s, initConnectionListener);
    }
}
