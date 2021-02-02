package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import java.io.File

/**
 * ViewModel class for [com.getstream.sdk.chat.view.messageinput.MessageInputView].
 * Responsible for sending and updating chat messages.
 * Can be bound to the view using [MessageInputViewModel.bindView] function.
 * @param cid the full channel id, i.e. "messaging:123"
 * @param chatDomain entry point for all livedata & offline operations
 */
public class MessageInputViewModel @JvmOverloads constructor(
    private val cid: String,
    private val chatDomain: ChatDomain = ChatDomain.instance()
) : ViewModel() {
    private var activeThread = MutableLiveData<Message?>()
    private val _maxMessageLength = MutableLiveData(Int.MAX_VALUE)
    private val _commands = MutableLiveData<List<Command>>(emptyList())
    private val _members = MediatorLiveData<List<Member>>()
    public val maxMessageLength: LiveData<Int> = _maxMessageLength
    public val commands: LiveData<List<Command>> = _commands
    public val members: LiveData<List<Member>> = _members
    public val editMessage: MutableLiveData<Message?> = MutableLiveData()
    public val repliedMessage: MediatorLiveData<Message?> = MediatorLiveData()

    private val _isDirectMessage: MutableLiveData<Boolean> = MutableLiveData()
    public val isDirectMessage: LiveData<Boolean> = _isDirectMessage

    init {
        chatDomain.useCases.watchChannel(cid, 0).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                val channel: Channel = channelController.toChannel()
                _maxMessageLength.value = channel.config.maxMessageLength
                _commands.value = channel.config.commands
                _members.addSource(channelController.members) { _members.value = it }
                _isDirectMessage.value = channel.isDirectMessaging()
                repliedMessage.addSource(channelController.repliedMessage) { repliedMessage.value = it }
            }
        }
    }

    /**
     * Sets and informs about new active thread
     */
    public fun setActiveThread(parentMessage: Message) {
        activeThread.postValue(parentMessage)
    }

    public fun getActiveThread(): LiveData<Message?> {
        return activeThread
    }

    private val isThread: Boolean
        get() = activeThread.value != null

    /**
     * Resets currently active thread
     */
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
        messageText: String,
        attachmentFiles: List<File>,
        messageTransformer: Message.() -> Unit = { }
    ) {
        // Send message should not be cancelled when viewModel.onCleared is called
        val attachments = attachmentFiles.map { Attachment(upload = it) }.toMutableList()
        val message = Message(cid = cid, text = messageText, attachments = attachments).apply(messageTransformer)
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
        val parentId = activeThread.value?.id
        chatDomain.useCases.keystroke(cid, parentId).enqueue()
    }

    /**
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */
    public fun stopTyping() {
        val parentId = activeThread.value?.id
        chatDomain.useCases.stopTyping(cid, parentId).enqueue()
    }

    public fun dismissReply() {
        if (repliedMessage.value != null) {
            chatDomain.useCases.setMessageForReply(cid, null).enqueue()
        }
    }
}
