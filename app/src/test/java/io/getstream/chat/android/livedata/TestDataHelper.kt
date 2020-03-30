package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.models.*
import java.util.*

class TestDataHelper {

    val user1 = User("test-user-1")
    val user2 = User("test-user-2")

    val watcher = Watcher("test").apply { user = user1 }
    val member = Member().apply { user = user1; role="user" }

    val channel1 = Channel().apply {
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher)
        members = listOf(member)
    }
    val channel1Updated = Channel().apply {
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher)
        members = listOf(member)
        extraData = mutableMapOf("color" to "green")
    }
    val reaction1 = Reaction("message-1", "like", 1)

    val message1 = Message().apply { this.channel = channel1; text="hi there"; id="message-1"; user=user1 }
    val message1Updated = Message().apply { channel = channel1; text="im update now"; id="message-1"; user=user1 }
    val reactionMessage = Message().apply { channel = channel1; text="im update now"; id="message-1"; user=user1;
        reactionCounts= mutableMapOf("like" to 1); ownReactions= mutableListOf(reaction1); latestReactions=mutableListOf(reaction1) }
    val messageThread = Message().apply { channel=channel1; parentId="message-1"; id="message-2"}

    val newMessageEvent = NewMessageEvent().apply { message=message1 }
    val messageUpdatedEvent = MessageUpdatedEvent().apply { message=message1Updated }
    val userStartWatchingEvent = UserStartWatchingEvent().apply{channel = channel1}
    val reactionEvent = ReactionNewEvent().apply { message=reactionMessage}
    val newMessageWithThreadEvent = NewMessageEvent().apply { message=messageThread }
    val channelUpdatedEvent = ChannelUpdatedEvent().apply { channel=channel1Updated }
    val user1TypingStarted = TypingStartEvent().apply{user=user1}
    val user2TypingStarted = TypingStartEvent().apply{user=user2}
    val user1TypingStop = TypingStopEvent().apply{user=user1}
    val user2TypingStop = TypingStopEvent().apply{user=user2}

    val user1Read = MessageReadEvent().apply { user=user1; createdAt= Date() }
}