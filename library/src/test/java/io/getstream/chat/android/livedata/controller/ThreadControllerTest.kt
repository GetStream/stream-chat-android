package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThreadControllerTest : BaseConnectedIntegrationTest() {

    @Test
    fun threads() = runBlocking(Dispatchers.IO) {
        val channelRepo = chatDomain.channel("messaging", "testabc")
        val message = data.createMessage()
        message.id = "thisisaparent"
        message.replyCount = 1
        channelRepo.upsertMessages(listOf(message))

        val threadController = channelRepo.getThread(message.id)
        val messages = threadController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
    }

    @Test
    fun threads2() = runBlocking(Dispatchers.IO) {
        val channelRepo = chatDomain.channel("messaging", "testabc")
        val message = data.createMessage()
        val message2 = data.createMessage()
        message.id = "theparent"
        message2.id = "thechild"
        message2.parentId = "theparent"
        channelRepo.upsertMessages(listOf(message, message2))

        val threadController = channelRepo.getThread(message.id)
        val messages = threadController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(2)
    }

    @Test
    fun newThread() = runBlocking(Dispatchers.IO) {
        val channelRepo = chatDomain.channel("messaging", "testabc")
        val message = data.createMessage()
        message.id = "theparent"
        channelRepo.upsertMessages(listOf(message))
        // note there is no reply count or parent, so we don't know this is a thread
        // calling getThread should initialize the thread
        val threadController = channelRepo.getThread(message.id)
        val messages = threadController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
    }

    @Test
    fun newThreadAndMessage() = runBlocking(Dispatchers.IO) {
        val channelRepo = chatDomain.channel("messaging", "testabc")
        channelRepo.updateChannel(data.channel1)
        val message = data.createMessage()
        message.id = "theparent"
        channelRepo.upsertMessages(listOf(message))
        // note there is no reply count or parent, so we don't know this is a thread
        // calling getThread should initialize the thread
        val threadController = channelRepo.getThread(message.id)
        var messages = threadController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)

        // sending a message should add to the thread
        val message2 = data.createMessage()

        message2.parentId = message.id
        channelRepo.sendMessage(message2)

        messages = threadController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(2)
    }
}
