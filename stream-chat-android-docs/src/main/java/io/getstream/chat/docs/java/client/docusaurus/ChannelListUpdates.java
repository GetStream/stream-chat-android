package io.getstream.chat.docs.java.client.docusaurus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.api.models.FilterObject;
import io.getstream.chat.android.client.events.ChatEventHandler;
import io.getstream.chat.android.client.events.CidEvent;
import io.getstream.chat.android.client.events.EventHandlingResult;
import io.getstream.chat.android.client.events.HasChannel;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.setup.state.ClientState;
import io.getstream.chat.android.offline.event.handler.chat.DefaultChatEventHandler;
import io.getstream.chat.android.offline.event.handler.chat.factory.ChatEventHandlerFactory;
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel;
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory;
import kotlinx.coroutines.flow.StateFlow;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/channel-list-updates/">Channel List Updates</a>
 */
public class ChannelListUpdates {

    public void publicChannelsChatEventHandler() {
        class PublicChatEventHandler extends DefaultChatEventHandler {
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

        class PublicChatEventHandlerFactory extends ChatEventHandlerFactory {
            @NonNull
            @Override
            public ChatEventHandler chatEventHandler(@NonNull StateFlow<? extends Map<String, Channel>> channels) {
                return new PublicChatEventHandler(channels, ChatClient.instance().getClientState());
            }
        }
    }

    public void privateChannelsChatEventHandler() {
        class PrivateChatEventHandler extends DefaultChatEventHandler {
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

        class PrivateChatEventHandlerFactory extends ChatEventHandlerFactory {
            @NonNull
            @Override
            public ChatEventHandler chatEventHandler(@NonNull StateFlow<? extends Map<String, Channel>> channels) {
                return new PrivateChatEventHandler(channels, ChatClient.instance().getClientState());
            }
        }
    }

    public void applyToViewModel(ChatEventHandlerFactory chatEventHandlerFactory) {
        ChannelListViewModelFactory factory = new ChannelListViewModelFactory(
                null,
                ChannelListViewModel.DEFAULT_SORT,
                30,
                1,
                30,
                chatEventHandlerFactory
        );
    }
}
