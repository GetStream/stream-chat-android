package com.getstream.sdk.chat.rest.core;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.storage.Sync;

public abstract class ChatChannelEventHandler {
    public void onAnyEvent(Event event) {
    }

    public void onTypingStart(Event event) {
    }

    public void onTypingStop(Event event) {
    }

    public void onMessageNew(Event event) {
    }

    public void onMessageUpdated(Event event) {
    }

    public void onMessageDeleted(Event event) {
    }

    public void onMessageRead(Event event) {
    }

    public void onReactionNew(Event event) {
    }

    public void onReactionDeleted(Event event) {
    }

    public void onMemberAdded(Event event) {
    }

    public void onMemberRemoved(Event event) {
    }

    public void onMemberUpdated(Event event) {
    }

    public void onChannelUpdated(Event event) {
    }

    public void onChannelHidden(Event event) {
    }

    public void onChannelDeleted(Event event) {
    }

    public void onUserWatchingStart(Event event) {
    }

    public void onUserWatchingStop(Event event) {
    }

    public final void dispatchEvent(Event event) {
        onAnyEvent(event);
        switch (event.getType()) {
            case TYPING_START:
                onTypingStart(event);
                break;
            case TYPING_STOP:
                onTypingStop(event);
                break;
            case MESSAGE_NEW:
                event.getMessage().setSyncStatus(Sync.SYNCED);
                onMessageNew(event);
                break;
            case MESSAGE_UPDATED:
                event.getMessage().setSyncStatus(Sync.SYNCED);
                onMessageUpdated(event);
                break;
            case MESSAGE_DELETED:
                event.getMessage().setSyncStatus(Sync.SYNCED);
                event.getMessage().setText(StreamChat.getStrings().get(R.string.stream_delete_message));
                onMessageDeleted(event);
                break;
            case MESSAGE_READ:
                onMessageRead(event);
                break;
            case REACTION_NEW:
                event.getMessage().setSyncStatus(Sync.SYNCED);
                onReactionNew(event);
                break;
            case REACTION_DELETED:
                event.getMessage().setSyncStatus(Sync.SYNCED);
                onReactionDeleted(event);
                break;
            case MEMBER_ADDED:
                onMemberAdded(event);
                break;
            case MEMBER_REMOVED:
                onMemberRemoved(event);
                break;
            case MEMBER_UPDATED:
                onMemberUpdated(event);
                break;
            case CHANNEL_UPDATED:
                onChannelUpdated(event);
                break;
            case CHANNEL_HIDDEN:
                onChannelHidden(event);
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
