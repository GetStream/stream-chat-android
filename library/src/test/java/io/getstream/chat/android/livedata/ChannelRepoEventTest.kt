package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import org.junit.*
import org.junit.runner.RunWith

/**
 * This test suite verifies that ChatChannelRepo implements event handling correctly and updates it's local state
 * Offline storage for these events is handled at the ChannelRepo level, so this is only testing local state changes
 * Note that we don't rely on Room's livedata mechanism as this library needs to work without room enabled as well
 */
@RunWith(AndroidJUnit4::class)
class ChatChannelControllerEventDomainTest: BaseDomainTest() {

    @Before
    fun setup() {

        client = createClient()
        setupRepo(client, false)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }

    @Test
    fun eventWatcherCountUpdates() {
        val event = data.userStartWatchingEvent
        channelController.handleEvent(event)
        Truth.assertThat(channelController.watcherCount.getOrAwaitValue()).isEqualTo(100)
        val users = event.channel?.watchers!!.map { it.user }
        Truth.assertThat(channelController.watchers.getOrAwaitValue()).isEqualTo(users)
    }

    @Test
    fun eventNewMessage() {
        channelController.handleEvent(data.newMessageEvent)
        Truth.assertThat(channelController.messages.getOrAwaitValue()).isEqualTo(listOf(data.newMessageEvent.message))
    }

    @Test
    fun eventUpdatedMessage() {

        channelController.handleEvent(data.newMessageEvent)
        val event = data.messageUpdatedEvent
        channelController.handleEvent(event)

        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages).isEqualTo(listOf(event.message))
    }

    @Test
    @Ignore
    fun userChangesFavoriteColor() {
        channelController.handleEvent(data.newMessageEvent)
        channelController.handleEvent(data.reactionEvent)
        channelController.handleEvent(data.user1UpdatedEvent)
        val message = channelController.getMessage(data.message1.id)
        Truth.assertThat(message!!.user.extraData.get("color")).isEqualTo("green")
        Truth.assertThat(message!!.latestReactions.first().user!!.extraData["color"]).isEqualTo("green")

    }

    @Test
    fun memberAddedEvent() {
        // ensure the channel data is initialized:
        channelController.upsertMember(data.channel1.members[0])
        var members = channelController.members.getOrAwaitValue()
        Truth.assertThat(members.size).isEqualTo(1)
        // add a member, we should go from list size 1 to 2
        channelController.handleEvent(data.memberAddedToChannelEvent)
        members = channelController.members.getOrAwaitValue()
        Truth.assertThat(members.size).isEqualTo(2)
    }

    @Test
    fun typingEvents() {
        channelController.handleEvent(data.user1TypingStarted)
        channelController.handleEvent(data.user2TypingStarted)
        channelController.handleEvent(data.user1TypingStop)
        val typing = channelController.typing.getOrAwaitValue()
        Truth.assertThat(typing).isEqualTo(listOf(data.user2))
    }

    @Test
    fun readEvents() {
        channelController.handleEvent(data.user1Read)
        val reads = channelController.reads.getOrAwaitValue()
        Truth.assertThat(reads.size).isEqualTo(1)
        Truth.assertThat(reads[0].user.id).isEqualTo(data.user1.id)
    }

    @Test
    fun eventMessageWithThread() {
        channelController.handleEvent(data.newMessageEvent)
        channelController.handleEvent(data.newMessageWithThreadEvent)
        val parentId = data.newMessageWithThreadEvent.message.parentId!!

        val messages = channelController.getThreadMessages(parentId).getOrAwaitValue()
        Truth.assertThat(messages?.size).isEqualTo(2)
    }

    @Test
    fun eventUpdatedChannel() {
        channelController.handleEvent(data.channelUpdatedEvent)
        val channel = channelController.channel.getOrAwaitValue()
        Truth.assertThat(channel.extraData.get("color")).isEqualTo("green")
    }

    @Test
    fun eventReaction() {
        channelController.handleEvent(data.reactionEvent)
        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages[0]).isEqualTo(data.reactionEvent.message)
    }
}