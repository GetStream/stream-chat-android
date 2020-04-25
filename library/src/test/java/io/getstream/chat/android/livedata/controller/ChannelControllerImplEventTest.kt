package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDisconnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test suite verifies that ChatChannelRepo implements event handling correctly and updates it's local state
 * Offline storage for these events is handled at the ChannelRepo level, so this is only testing local state changes
 * Note that we don't rely on Room's livedata mechanism as this library needs to work without room enabled as well
 */
@RunWith(AndroidJUnit4::class)
class ChannelControllerImplEventTest : BaseDisconnectedIntegrationTest() {

    @Test
    fun eventWatcherCountUpdates() {
        val event = data.userStartWatchingEvent
        channelControllerImpl.handleEvent(event)
        Truth.assertThat(channelControllerImpl.watcherCount.getOrAwaitValue()).isEqualTo(100)
        val users = event.channel?.watchers!!.map { it.user }
        Truth.assertThat(channelControllerImpl.watchers.getOrAwaitValue()).isEqualTo(users)
    }

    @Test
    fun eventNewMessage() {
        channelControllerImpl.handleEvent(data.newMessageEvent)
        Truth.assertThat(channelControllerImpl.messages.getOrAwaitValue())
            .isEqualTo(listOf(data.newMessageEvent.message))
    }

    @Test
    fun eventUpdatedMessage() {

        channelControllerImpl.handleEvent(data.newMessageEvent)
        val event = data.messageUpdatedEvent
        channelControllerImpl.handleEvent(event)

        val messages = channelControllerImpl.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages).isEqualTo(listOf(event.message))
    }

    @Test
    fun userChangesFavoriteColor() {
        channelControllerImpl.handleEvent(data.newMessageEvent)
        channelControllerImpl.handleEvent(data.reactionEvent)
        channelControllerImpl.handleEvent(data.user1UpdatedEvent)
        val channel = channelControllerImpl.toChannel()
        Truth.assertThat(channel.createdBy.extraData.get("color")).isEqualTo("green")

        val message = channelControllerImpl.getMessage(data.message1.id)
        Truth.assertThat(message!!.user.extraData.get("color")).isEqualTo("green")
        Truth.assertThat(message.latestReactions.first().user!!.extraData["color"])
            .isEqualTo("green")
    }

    @Test
    fun memberAddedEvent() {
        // ensure the channel data is initialized:
        channelControllerImpl.upsertMember(data.channel1.members[0])
        var members = channelControllerImpl.members.getOrAwaitValue()
        Truth.assertThat(members.size).isEqualTo(1)
        // add a member, we should go from list size 1 to 2
        channelControllerImpl.handleEvent(data.memberAddedToChannelEvent)
        members = channelControllerImpl.members.getOrAwaitValue()
        Truth.assertThat(members.size).isEqualTo(2)
    }

    @Test
    fun typingEvents() {
        channelControllerImpl.handleEvent(data.user1TypingStarted)
        channelControllerImpl.handleEvent(data.user2TypingStarted)
        channelControllerImpl.handleEvent(data.user1TypingStop)
        val typing = channelControllerImpl.typing.getOrAwaitValue()
        Truth.assertThat(typing).isEqualTo(listOf(data.user2))
    }

    @Test
    fun readEvents() {
        channelControllerImpl.handleEvent(data.user1Read)
        val reads = channelControllerImpl.reads.getOrAwaitValue()
        Truth.assertThat(reads.size).isEqualTo(1)
        Truth.assertThat(reads[0].user.id).isEqualTo(data.user1.id)
    }

    @Test
    fun eventMessageWithThread() {
        channelControllerImpl.handleEvent(data.newMessageEvent)
        channelControllerImpl.handleEvent(data.newMessageWithThreadEvent)
        val parentId = data.newMessageWithThreadEvent.message.parentId!!

        val messages = channelControllerImpl.getThreadMessages(parentId).getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(2)
    }

    @Test
    fun eventUpdatedChannel() {
        channelControllerImpl.handleEvent(data.channelUpdatedEvent)
        val channel = channelControllerImpl.channel.getOrAwaitValue()
        Truth.assertThat(channel.extraData.get("color")).isEqualTo("green")
    }

    @Test
    fun eventReaction() {
        channelControllerImpl.handleEvent(data.reactionEvent)
        val messages = channelControllerImpl.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages[0]).isEqualTo(data.reactionEvent.message)
    }
}
