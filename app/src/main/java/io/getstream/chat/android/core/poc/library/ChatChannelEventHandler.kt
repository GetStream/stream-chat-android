package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.events.ChatEvent


abstract class ChatChannelEventHandler {
    fun onAnyEvent(event: ChatEvent) {}
    fun onTypingStart(event: ChatEvent) {}
    fun onTypingStop(event: ChatEvent) {}
    fun onMessageNew(event: ChatEvent) {}
    fun onMessageUpdated(event: ChatEvent) {}
    fun onMessageDeleted(event: ChatEvent) {}
    fun onMessageRead(event: ChatEvent) {}
    fun onReactionNew(event: ChatEvent) {}
    fun onReactionDeleted(event: ChatEvent) {}
    fun onMemberAdded(event: ChatEvent) {}
    fun onMemberRemoved(event: ChatEvent) {}
    fun onMemberUpdated(event: ChatEvent) {}
    fun onChannelUpdated(event: ChatEvent) {}
    fun onChannelHidden(event: ChatEvent) {}
    fun onChannelDeleted(event: ChatEvent) {}
    fun onUserWatchingStart(event: ChatEvent) {}
    fun onUserWatchingStop(event: ChatEvent) {}
    fun dispatchEvent(event: ChatEvent) {
        onAnyEvent(event)
        when (event.getType()) {
            EventType.TYPING_START -> onTypingStart(event)
            EventType.TYPING_STOP -> onTypingStop(event)
            EventType.MESSAGE_NEW -> {
                onMessageNew(event)
            }
            EventType.MESSAGE_UPDATED -> {
                onMessageUpdated(event)
            }
            EventType.MESSAGE_DELETED -> {
                //TODO: define R.string.stream_delete_message
                //event.message?.text = "Deleted message"
                //event.message?.text = StreamChat.getStrings().get(R.string.stream_delete_message)
                onMessageDeleted(event)

            }
            EventType.MESSAGE_READ -> onMessageRead(event)
            EventType.REACTION_NEW -> {
                onReactionNew(event)
            }
            EventType.REACTION_DELETED -> {
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
