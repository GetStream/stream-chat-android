package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.livedata.BaseDisconnectedIntegrationTest
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This test suite verifies that ChatChannelRepo implements event handling correctly and updates it's local state
 * Offline storage for these events is handled at the ChannelRepo level, so this is only testing local state changes
 * Note that we don't rely on Room's livedata mechanism as this library needs to work without room enabled as well
 */
@RunWith(AndroidJUnit4::class)
internal class ChannelControllerImplEventTest : BaseDisconnectedIntegrationTest() {

    @Test
    fun eventWatcherCountUpdates() = runBlockingTest {
        Truth.assertThat(channelControllerImpl.watcherCount.getOrAwaitValue()).isEqualTo(100)

        val event = data.userStartWatchingEvent
        channelControllerImpl.handleEvent(event)

        Truth.assertThat(channelControllerImpl.watcherCount.getOrAwaitValue()).isEqualTo(1)
        Truth.assertThat(channelControllerImpl.watchers.getOrAwaitValue()).isEqualTo(listOf(event.user))
    }

    @Test
    fun eventNewMessage() = runBlockingTest {
        channelControllerImpl.handleEvent(data.newMessageEvent)
        Truth.assertThat(channelControllerImpl.messages.getOrAwaitValue())
            .isEqualTo(listOf(data.newMessageEvent.message))
    }

    @Test
    fun eventUpdatedMessage() = runBlockingTest {

        channelControllerImpl.handleEvent(data.newMessageEvent)
        val event = data.messageUpdatedEvent
        channelControllerImpl.handleEvent(event)

        val messages = channelControllerImpl.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages).isEqualTo(listOf(event.message))
    }

    @Test
    fun userChangesFavoriteColor() = runBlockingTest {
        channelControllerImpl.handleEvent(data.newMessageEvent)
        channelControllerImpl.handleEvent(data.reactionEvent)
        channelControllerImpl.handleEvent(data.user1UpdatedEvent)
        val channel = channelControllerImpl.toChannel()
        Truth.assertThat(channel.createdBy.extraData["color"]).isEqualTo("green")

        val message = channelControllerImpl.getMessage(data.message1.id)
        Truth.assertThat(message!!.user.extraData["color"]).isEqualTo("green")
        Truth.assertThat(message.latestReactions.first().user!!.extraData["color"])
            .isEqualTo("green")
    }

    @Test
    fun memberAddedEvent() = runBlockingTest {
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
    fun typingEvents() = runBlockingTest {
        channelControllerImpl.handleEvent(data.user1TypingStarted)
        channelControllerImpl.handleEvent(data.user2TypingStarted)
        channelControllerImpl.handleEvent(data.user1TypingStop)
        val typing = channelControllerImpl.typing.getOrAwaitValue()
        Truth.assertThat(typing).isEqualTo(TypingEvent(channelControllerImpl.channelId, listOf(data.user2)))
    }

    @Test
    fun hideEvent() = runBlockingTest {
        channelControllerImpl.handleEvent(data.channelHiddenEvent)
        val hidden = channelControllerImpl.hidden.getOrAwaitValue()
        Truth.assertThat(hidden).isTrue()
    }

    @Test
    fun showEvent() = runBlockingTest {
        channelControllerImpl.handleEvent(data.channelHiddenEvent)
        channelControllerImpl.handleEvent(data.channelVisibleEvent)
        val hidden = channelControllerImpl.hidden.getOrAwaitValue()
        Truth.assertThat(hidden).isFalse()
    }

    @Test
    fun readEvents() = runBlockingTest {
        channelControllerImpl.handleEvent(data.user1ReadNotification)
        val reads = channelControllerImpl.reads.getOrAwaitValue()
        Truth.assertThat(reads.size).isEqualTo(1)
        Truth.assertThat(reads[0].user.id).isEqualTo(data.user1.id)
    }

    @Test
    fun readEventNotification() = runBlockingTest {
        channelControllerImpl.handleEvent(data.user1Read)
        val reads = channelControllerImpl.reads.getOrAwaitValue()
        Truth.assertThat(reads.size).isEqualTo(1)
        Truth.assertThat(reads[0].user.id).isEqualTo(data.user1.id)
    }

    @Test
    fun eventUpdatedChannel() = runBlocking {
        val channel1 = channelControllerImpl.channelData.getOrAwaitValue()
        Truth.assertThat(channel1.extraData["color"]).isNull()

        channelControllerImpl.handleEvent(data.channelUpdatedEvent)
        val channel2 = channelControllerImpl.channelData.getOrAwaitValue()
        Truth.assertThat(channel2.extraData["color"]).isEqualTo("green")
    }

    @Test
    fun eventReaction() = runBlockingTest {
        val messageWithCid = data.reactionEvent.message

        channelControllerImpl.handleEvent(data.reactionEvent)

        val messages = channelControllerImpl.messages.getOrAwaitValue()
        Truth.assertThat(messages).isEqualTo(listOf(messageWithCid))
    }
}
