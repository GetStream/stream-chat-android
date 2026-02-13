package io.getstream.chat.docs.java.client.docusaurus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.events.CidEvent;
import io.getstream.chat.android.client.events.HasChannel;
import io.getstream.chat.android.client.setup.state.ClientState;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.FilterObject;
import io.getstream.chat.android.models.querysort.QuerySorter;
import io.getstream.chat.android.client.api.event.ChatEventHandler;
import io.getstream.chat.android.client.api.event.DefaultChatEventHandler;
import io.getstream.chat.android.client.api.event.EventHandlingResult;
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel;
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory;
import kotlinx.coroutines.flow.StateFlow;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/channel-list-updates/">Channel List Updates</a>
 */
public class ChannelListUpdates {

    public final class PublicChatEventHandler extends DefaultChatEventHandler {
        public PublicChatEventHandler(@NonNull StateFlow<? extends Map<String, Channel>> channels, @NonNull ClientState clientState) {
            super(channels, clientState);
        }

        @NonNull
        @Override
        public EventHandlingResult handleChannelEvent(@NonNull HasChannel event, @NonNull FilterObject filter) {
            // If the channel event matches "public" type, handle it
            if (event.getChannel().getCid().startsWith("public")) {
                return super.handleChannelEvent(event, filter);
            } else {
                // Otherwise skip
                return EventHandlingResult.Skip.INSTANCE;
            }
        }

        @NonNull
        @Override
        public EventHandlingResult handleCidEvent(@NonNull CidEvent event, @NonNull FilterObject filter, @Nullable Channel cachedChannel) {
            // If the channel event matches "public" type, handle it
            if (event.getCid().startsWith("public")) {
                return super.handleCidEvent(event, filter, cachedChannel);
            } else {
                // Otherwise skip
                return EventHandlingResult.Skip.INSTANCE;
            }
        }
    }

    public final class PublicChatEventHandlerFactory extends ChatEventHandlerFactory {
        @NonNull
        @Override
        public ChatEventHandler chatEventHandler(@NonNull StateFlow<? extends Map<String, Channel>> channels) {
            ChatClient chatClient = ChatClient.instance();
            return new PublicChatEventHandler(channels, chatClient.getClientState());
        }
    }

    public final class PrivateChatEventHandler extends DefaultChatEventHandler {
        public PrivateChatEventHandler(@NonNull StateFlow<? extends Map<String, Channel>> channels, @NonNull ClientState clientState) {
            super(channels, clientState);
        }

        @NonNull
        @Override
        public EventHandlingResult handleChannelEvent(@NonNull HasChannel event, @NonNull FilterObject filter) {
            // If the channel event matches "private" type, handle it
            if (event.getChannel().getCid().startsWith("private")) {
                return super.handleChannelEvent(event, filter);
            } else {
                // Otherwise skip
                return EventHandlingResult.Skip.INSTANCE;
            }
        }

        @NonNull
        @Override
        public EventHandlingResult handleCidEvent(@NonNull CidEvent event, @NonNull FilterObject filter, @Nullable Channel cachedChannel) {
            // If the channel event matches "private" type, handle it
            if (event.getCid().startsWith("private")) {
                return super.handleCidEvent(event, filter, cachedChannel);
            } else {
                // Otherwise skip
                return EventHandlingResult.Skip.INSTANCE;
            }
        }
    }

    public final class PrivateChatEventHandlerFactory extends ChatEventHandlerFactory {
        @NonNull
        @Override
        public ChatEventHandler chatEventHandler(@NonNull StateFlow<? extends Map<String, Channel>> channels) {
            ChatClient chatClient = ChatClient.instance();
            return new PrivateChatEventHandler(channels, chatClient.getClientState());
        }
    }

    public void applyToViewModel(ChatEventHandlerFactory chatEventHandlerFactory) {
        FilterObject filter = null;
        QuerySorter<Channel> sort = ChannelListViewModel.DEFAULT_SORT;
        int limit = 30;
        int messageLimit = 1;
        int memberLimit = 30;
        boolean isDraftMessagesEnabled = false;
        ChannelListViewModelFactory factory = new ChannelListViewModelFactory(
                filter,
                sort,
                limit,
                messageLimit,
                memberLimit,
                isDraftMessagesEnabled,
                chatEventHandlerFactory
        );
    }
}
