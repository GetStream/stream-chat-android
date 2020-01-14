package io.getstream.chat.android.core.poc.library

import android.R


abstract class ChatChannelEventHandler {
    fun onAnyEvent(event: Event?) {}
    fun onTypingStart(event: Event?) {}
    fun onTypingStop(event: Event?) {}
    fun onMessageNew(event: Event?) {}
    fun onMessageUpdated(event: Event?) {}
    fun onMessageDeleted(event: Event?) {}
    fun onMessageRead(event: Event?) {}
    fun onReactionNew(event: Event?) {}
    fun onReactionDeleted(event: Event?) {}
    fun onMemberAdded(event: Event?) {}
    fun onMemberRemoved(event: Event?) {}
    fun onMemberUpdated(event: Event?) {}
    fun onChannelUpdated(event: Event?) {}
    fun onChannelHidden(event: Event?) {}
    fun onChannelDeleted(event: Event?) {}
    fun onUserWatchingStart(event: Event?) {}
    fun onUserWatchingStop(event: Event?) {}
    fun dispatchEvent(event: Event) {
        onAnyEvent(event)
        when (event.getType()) {
            EventType.TYPING_START -> onTypingStart(event)
            EventType.TYPING_STOP -> onTypingStop(event)
            EventType.MESSAGE_NEW -> {
                event.message?.syncStatus = Sync.SYNCED
                onMessageNew(event)
            }
            EventType.MESSAGE_UPDATED -> {
                event.message?.syncStatus = Sync.SYNCED
                onMessageUpdated(event)
            }
            EventType.MESSAGE_DELETED -> {
                event.message?.syncStatus = Sync.SYNCED
                //TODO: define R.string.stream_delete_message
                event.message?.text = "Deleted message"
                //event.message?.text = StreamChat.getStrings().get(R.string.stream_delete_message)
                onMessageDeleted(event)

            }
            EventType.MESSAGE_READ -> onMessageRead(event)
            EventType.REACTION_NEW -> {
                event.message?.syncStatus = Sync.SYNCED
                onReactionNew(event)
            }
            EventType.REACTION_DELETED -> {
                event.message?.syncStatus = Sync.SYNCED
                onReactionDeleted(event)
            }
            EventType.MEMBER_ADDED -> onMemberAdded(event)
            EventType.MEMBER_REMOVED -> onMemberRemoved(
                event
            )
            EventType.MEMBER_UPDATED -> onMemberUpdated(
                event
            )
            EventType.CHANNEL_UPDATED -> onChannelUpdated(
                event
            )
            EventType.CHANNEL_HIDDEN -> onChannelHidden(
                event
            )
            EventType.CHANNEL_DELETED -> onChannelDeleted(
                event
            )
            EventType.USER_WATCHING_START -> onUserWatchingStart(
                event
            )
            EventType.USER_WATCHING_STOP -> onUserWatchingStop(
                event
            )
        }
    }
}
