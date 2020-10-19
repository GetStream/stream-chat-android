package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import java.io.File

public class MessageInputViewModel @JvmOverloads constructor(
    private val cid: String,
    private val chatDomain: ChatDomain = ChatDomain.instance()
) : ViewModel() {
    private val channelController: ChannelController =
        chatDomain.useCases.watchChannel(cid, 0).execute().data()

    private var channelState = MutableLiveData<Channel>(channelController.toChannel())
    public val commands: LiveData<List<Command>> = map(channelState) { it.config.commands }

    public val members: LiveData<List<Member>> = channelController.members
    private var activeThread = MutableLiveData<Message?>()
    public val editMessage: MutableLiveData<Message?> = MutableLiveData()

    public fun setActiveThread(parentMessage: Message) {
        activeThread.postValue(parentMessage)
    }

    public fun getActiveThread(): LiveData<Message?> {
        return activeThread
    }

    private val isThread: Boolean
        get() = activeThread.value != null

    public fun resetThread() {
        activeThread.postValue(null)
    }

    public fun sendMessage(messageText: String, messageTransformer: Message.() -> Unit = { }) {
        val message = Message(cid = cid, text = messageText)
        activeThread.value?.let { message.parentId = it.id }
        stopTyping()

        chatDomain.useCases.sendMessage(message.apply(messageTransformer)).enqueue()
    }

    public fun sendMessageWithAttachments(
        message: String,
        attachmentFiles: List<File>,
        messageTransformer: Message.() -> Unit = { }
    ) {
        // Send message should not be cancelled when viewModel.onCleared is called
        val attachments = attachmentFiles.map { Attachment(upload = it) }.toMutableList()
        val message =
            Message(cid = cid, text = message, attachments = attachments).apply(messageTransformer)
        chatDomain.useCases.sendMessage(message).enqueue()
    }

    /**
     * Edit message
     *
     * @param message the Message sent
     */
    public fun editMessage(message: Message) {
        stopTyping()
        chatDomain.useCases.editMessage(message).enqueue()
    }

    /**
     * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke
     */
    @Synchronized
    public fun keystroke() {
        if (isThread) return
        chatDomain.useCases.keystroke(cid).enqueue()
    }

    /**
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */
    public fun stopTyping() {
        if (isThread) return
        chatDomain.useCases.stopTyping(cid).enqueue()
    }
}
