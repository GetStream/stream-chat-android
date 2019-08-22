package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.model.Event;

public abstract class ChatEventHandler {

    public void onUserPresenceChanged(Event event) {}
    public void onUserWatchingStart(Event event) {}
    public void onUserWatchingStop(Event event) {}
    public void onUserUpdated(Event event) {}
    public void onTypingStart(Event event) {}
    public void onTypingStop(Event event) {}
    public void onMessageNew(Event event) {}
    public void onMessageUpdated(Event event) {}
    public void onMessageDeleted(Event event) {}
    public void onMessageRead(Event event) {}
    public void onMessageReaction(Event event) {}
    public void onReactionNew(Event event) {}
    public void onReactionDeleted(Event event) {}
    public void onMemberAdded(Event event) {}
    public void onMemberRemoved(Event event) {}
    public void onChannelUpdated(Event event) {}
    public void onChannelDeleted(Event event) {}
    public void onHealthCheck(Event event) {}
    public void onConnectionChanged(Event event) {}
    public void onConnectionRecovered(Event event) {}
    public void onNotificationMessageNew(Event event) {}
    public void onNotificationMarkRead(Event event) {}
    public void onNotificationInvited(Event event) {}
    public void onNotificationInviteAccepted(Event event) {}
    public void onNotificationAddedToChannel(Event event) {}
    public void onNotificationRemovedFromChannel(Event event) {}
    public void onAnyEvent(Event event) {}

    public final void dispatchEvent(Event event){
        onAnyEvent(event);
        switch (event.getType()) {
            case USER_PRESENCE_CHANGED:
                onUserPresenceChanged(event);
                break;
            case USER_WATCHING_START:
                onUserWatchingStart(event);
                break;
            case USER_WATCHING_STOP:
                onUserWatchingStop(event);
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
                onMessageNew(event);
                break;
            case MESSAGE_UPDATED:
                onMessageUpdated(event);
                break;
            case MESSAGE_DELETED:
                onMessageDeleted(event);
                break;
            case MESSAGE_READ:
                onMessageRead(event);
                break;
            case MESSAGE_REACTION:
                onMessageReaction(event);
                break;
            case REACTION_NEW:
                onReactionNew(event);
                break;
            case REACTION_DELETED:
                onReactionDeleted(event);
                break;
            case MEMBER_ADDED:
                onMemberAdded(event);
                break;
            case MEMBER_REMOVED:
                onMemberRemoved(event);
                break;
            case CHANNEL_UPDATED:
                onChannelUpdated(event);
                break;
            case CHANNEL_DELETED:
                onChannelDeleted(event);
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
                onNotificationMessageNew(event);
                break;
            case NOTIFICATION_MARK_READ:
                onNotificationMarkRead(event);
                break;
            case NOTIFICATION_INVITED:
                onNotificationInvited(event);
                break;
            case NOTIFICATION_INVITE_ACCEPTED:
                onNotificationInviteAccepted(event);
                break;
            case NOTIFICATION_ADDED_TO_CHANNEL:
                onNotificationAddedToChannel(event);
                break;
            case NOTIFICATION_REMOVED_FROM_CHANNEL:
                onNotificationRemovedFromChannel(event);
                break;
        }
    }

}
