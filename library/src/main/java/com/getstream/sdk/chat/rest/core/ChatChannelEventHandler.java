package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.model.Event;

public abstract class ChatChannelEventHandler {
    public void onAnyEvent(Event event) {}
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
    public void onUserWatchingStart(Event event) {}
    public void onUserWatchingStop(Event event) {}

    public final void dispatchEvent(Event event){
        onAnyEvent(event);
        switch (event.getType()) {
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
            case USER_WATCHING_START:
                onUserWatchingStart(event);
                break;
            case USER_WATCHING_STOP:
                onUserWatchingStop(event);
                break;
        }
    }
}
