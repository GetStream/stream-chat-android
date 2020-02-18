package com.getstream.sdk.chat.viewmodel;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;

public class EventHandlerOld extends ChatEventHandler {
    private ChannelListViewModel channelListViewModel;
    protected ChannelListViewModel.EventInterceptor interceptor;

    public EventHandlerOld(ChannelListViewModel channelListViewModel, ChannelListViewModel.EventInterceptor interceptor) {
        this.channelListViewModel = channelListViewModel;
        this.interceptor = interceptor;
    }

    @Override
    public void onUserDisconnected() {
        channelListViewModel.clean();
    }

    @Override
    public void onConnectionChanged(Event event) {
        if (!event.getOnline()) {
            channelListViewModel.retryLooper.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onNotificationMessageNew(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        if (!channelListViewModel.updateChannel(channel, true)) {
            channelListViewModel.upsertChannel(channel);
        }
    }

    @Override
    public void onNotificationAddedToChannel(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        if (!channelListViewModel.updateChannel(channel, true)) {
            channelListViewModel.upsertChannel(channel);
        }
    }

    @Override
    public void onChannelVisible(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        if (!channelListViewModel.updateChannel(channel, true)) {
            channelListViewModel.upsertChannel(channel);
        }
    }

    @Override
    public void onChannelHidden(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.deleteChannel(channel);
    }

    @Override
    public void onNotificationRemovedFromChannel(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.deleteChannel(channel);
    }

    @Override
    public void onMessageNew(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, true);
    }

    @Override
    public void onMessageUpdated(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, true);
    }

    @Override
    public void onMessageDeleted(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, false);
    }

    @Override
    public void onChannelDeleted(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.deleteChannel(channel);
    }

    @Override
    public void onChannelUpdated(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, false);
    }

    @Override
    public void onMessageRead(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, false);
    }

    @Override
    public void onMemberAdded(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, false);
    }

    @Override
    public void onMemberUpdated(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, false);
    }

    @Override
    public void onMemberRemoved(Channel channel, Event event) {
        if (interceptor.shouldDiscard(event, channel)) return;
        channelListViewModel.updateChannel(channel, false);
    }
}
