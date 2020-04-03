package io.getstream.chat.android.livedata.utils

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.models.*
import java.util.*

class TestDataHelper {

    val user1 = User("broad-lake-3")
    val user1Token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"
    val user2 = User("test-user-2")

    val watcher1 = Watcher("test").apply { user = user1 }
    val member1 = Member().apply { user = user1; role="user" }
    val member2 = Member().apply { user=user2; role="user"  }

    val channel1 = Channel().apply {
        type = "messaging"
        id = "123-testing"
        cid = "messaging:123-testing"
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1)
    }
    val channel1Updated = Channel().apply {
        type = "messaging"
        id = "123-testing"
        cid = "messaging:123-testing"
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1)
        extraData = mutableMapOf("color" to "green")
    }
    val reaction1 = Reaction("message-1", "like", 1).apply { user=user1 }
    val reaction2 = Reaction("message-1", "like", 1).apply { user=user2 }

    val message1 = Message().apply { this.channel = channel1; text="hi there"; id="message-1"; user=user1; createdAt=Date(2020, 1,1) }
    val message1Updated = Message().apply { channel = channel1; text="im update now"; id="message-1"; user=user1 }
    val reactionMessage = Message().apply { channel = channel1; text="im update now"; id="message-1"; user=user1;
        reactionCounts= mutableMapOf("like" to 1); ownReactions= mutableListOf(reaction1); latestReactions=mutableListOf(reaction1) }
    val messageThread = Message().apply { channel=channel1; parentId="message-1"; id="message-2"}
    val message2Older = Message().apply { this.channel = channel1; text="message2"; id="message-2"; user=user1; createdAt=Date(2019, 1,1) }


    val connectedEvent = ConnectedEvent()
    val connectedEvent2 = ConnectedEvent().apply { totalUnreadCount=3; unreadChannels=2 }

    val disconnectedEvent = DisconnectedEvent()
    val newMessageEvent = NewMessageEvent().apply { message=message1 }
    val messageUpdatedEvent = MessageUpdatedEvent().apply { message=message1Updated }
    val userStartWatchingEvent = UserStartWatchingEvent().apply{channel = channel1; user = user1}
    val reactionEvent = ReactionNewEvent().apply { message=reactionMessage; reaction=reaction1}
    val reactionEvent2 = ReactionNewEvent().apply { reaction=reaction2}

    val newMessageWithThreadEvent = NewMessageEvent().apply { message=messageThread }
    val channelUpdatedEvent = ChannelUpdatedEvent().apply { channel=channel1Updated; cid=channel1Updated.cid }
    val user1TypingStarted = TypingStartEvent().apply{user=user1}
    val user2TypingStarted = TypingStartEvent().apply{user=user2}
    val user1TypingStop = TypingStopEvent().apply{user=user1}
    val user2TypingStop = TypingStopEvent().apply{user=user2}
    val readEvent = MessageReadEvent().apply{message=message1;user=user1;cid=channel1.cid; createdAt=Date()}

    val user1Read = MessageReadEvent().apply { user=user1; createdAt= Date() }
    val memberAddedToChannelEvent = MemberAddedEvent().apply { member=member2; cid=channel1.cid }

    // TODO: figure out the structure of the added To channel event
    val notificationAddedToChannelEvent = NotificationAddedToChannelEvent().apply {member=member1; channel=channel1}

}