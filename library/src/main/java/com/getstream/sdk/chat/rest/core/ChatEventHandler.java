package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;

public abstract class ChatEventHandler {

    public void onUserPresenceChanged(Event event) {
    }

    public void onUserWatchingStart(Channel channel, Event event) {
    }

    public void onUserWatchingStop(Channel channel, Event event) {
    }

    public void onUserUpdated(Event event) {
    }

    public void onTypingStart(Event event) {
    }

    public void onTypingStop(Event event) {
    }

    public void onMessageNew(Channel channel, Event event) {
    }

    public void onMessageUpdated(Channel channel, Event event) {
    }

    public void onMessageDeleted(Channel channel, Event event) {
    }

    public void onMessageRead(Channel channel, Event event) {
    }

    public void onReactionNew(Channel channel, Event event) {
    }

    public void onReactionDeleted(Channel channel, Event event) {
    }

    public void onMemberAdded(Channel channel, Event event) {
    }

    public void onMemberRemoved(Channel channel, Event event) {
    }

    public void onMemberUpdated(Channel channel, Event event) {
    }

    public void onChannelUpdated(Channel channel, Event event) {
    }

    public void onChannelDeleted(Channel channel, Event event) {
    }

    public void onHealthCheck(Event event) {
    }

    public void onConnectionChanged(Event event) {
    }

    public void onConnectionRecovered(Event event) {
    }

    public void onNotificationMessageNew(Channel channel, Event event) {
    }

    public void onNotificationMarkRead(Channel channel, Event event) {
    }

    public void onNotificationInvited(Channel channel, Event event) {
    }

    public void onNotificationInviteAccepted(Channel channel, Event event) {
    }

    public void onNotificationInviteRejected(Channel channel, Event event) {
    }

    public void onNotificationAddedToChannel(Channel channel, Event event) {
    }

    public void onNotificationRemovedFromChannel(Channel channel, Event event) {
    }

    public void onNotificationMutesUpdated(Channel channel, Event event) {
    }

    public void onAnyEvent(Event event) {
    }

    final Channel getChannel(Client client, Event event) {
        if (event.getCid() == null) return null;
        return client.getChannelByCid(event.getCid());
    }

    public void handleEventFromUnregisteredChannel(Client client, Event event) {
    }

    final void dispatchChannelEvent(Client client, Event event, ChannelEvent channelEventLambda) {
        Channel channel = getChannel(client, event);
        if (channel == null) {
            handleEventFromUnregisteredChannel(client, event);
        } else {
            channelEventLambda.dispatch(channel, event);
        }
    }

    final void dispatchEvent(Client client, Event event) {
        onAnyEvent(event);
        switch (event.getType()) {
            case USER_PRESENCE_CHANGED:
                onUserPresenceChanged(event);
                break;
            case USER_WATCHING_START:
                dispatchChannelEvent(client, event, this::onUserWatchingStart);
                break;
            case USER_WATCHING_STOP:
                dispatchChannelEvent(client, event, this::onUserWatchingStop);
                break;
            case USER_UPDATED:
                onUserUpdated(event);
                break;
            case TYPING_START:
                onTypingStart(event);
                break;
            case TYPING_STOP:
                onTypingStop(event);
                break;
            case MESSAGE_NEW:
                dispatchChannelEvent(client, event, this::onMessageNew);
                break;
            case MESSAGE_UPDATED:
                dispatchChannelEvent(client, event, this::onMessageUpdated);
                break;
            case MESSAGE_DELETED:
                dispatchChannelEvent(client, event, this::onMessageDeleted);
                break;
            case MESSAGE_READ:
                dispatchChannelEvent(client, event, this::onMessageRead);
                break;
            case REACTION_NEW:
                dispatchChannelEvent(client, event, this::onReactionNew);
                break;
            case REACTION_DELETED:
                dispatchChannelEvent(client, event, this::onReactionDeleted);
                break;
            case MEMBER_ADDED:
                dispatchChannelEvent(client, event, this::onMemberAdded);
                break;
            case MEMBER_REMOVED:
                dispatchChannelEvent(client, event, this::onMemberRemoved);
                break;
            case MEMBER_UPDATED:
                dispatchChannelEvent(client, event, this::onMemberUpdated);
                break;
            case CHANNEL_UPDATED:
                dispatchChannelEvent(client, event, this::onChannelUpdated);
                break;
            case CHANNEL_DELETED:
                dispatchChannelEvent(client, event, this::onChannelDeleted);
                break;
            case HEALTH_CHECK:
                onHealthCheck(event);
                break;
            case CONNECTION_CHANGED:
                onConnectionChanged(event);
                break;
            case CONNECTION_RECOVERED:
                onConnectionRecovered(event);
                break;
            case NOTIFICATION_MESSAGE_NEW:
                dispatchChannelEvent(client, event, this::onNotificationMessageNew);
                break;
            case NOTIFICATION_MARK_READ:
                dispatchChannelEvent(client, event, this::onNotificationMarkRead);
                break;
            case NOTIFICATION_INVITED:
                dispatchChannelEvent(client, event, this::onNotificationInvited);
                break;
            case NOTIFICATION_INVITE_ACCEPTED:
                dispatchChannelEvent(client, event, this::onNotificationInviteAccepted);
                break;
            case NOTIFICATION_INVITE_REJECTED:
                dispatchChannelEvent(client, event, this::onNotificationInviteRejected);
                break;
            case NOTIFICATION_ADDED_TO_CHANNEL:
                dispatchChannelEvent(client, event, this::onNotificationAddedToChannel);
                break;
            case NOTIFICATION_REMOVED_FROM_CHANNEL:
                dispatchChannelEvent(client, event, this::onNotificationRemovedFromChannel);
                break;
            case NOTIFICATION_MUTES_UPDATED:
                dispatchChannelEvent(client, event, this::onNotificationMutesUpdated);
                break;
        }
    }

    private interface ChannelEvent {
        void dispatch(Channel channel, Event event);
    }

}
