package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.utils.extensions.combineWith
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
    private val _maxMessageLength = MediatorLiveData<Int>()
    private val _commands = MediatorLiveData<List<Command>>()
    private val _members = MediatorLiveData<List<Member>>()
    public val maxMessageLength: LiveData<Int> = _maxMessageLength
    public val commands: LiveData<List<Command>> = _commands
    public val members: LiveData<List<Member>> = _members
    private val _messageToEdit: MutableLiveData<Message?> = MutableLiveData()
    public val messageToEdit: LiveData<Message?> = _messageToEdit
    @Deprecated(
        message = "Do not use this LiveData directly",
        replaceWith = ReplaceWith("messageToEdit: LiveData<Message?> and postMessageToEdit(message: Message?)"),
        level = DeprecationLevel.WARNING
    )
    public val editMessage: MutableLiveData<Message?> = MutableLiveData()
    private val _repliedMessage: MediatorLiveData<Message?> = MediatorLiveData()
    public val repliedMessage: LiveData<Message?> = _repliedMessage
    private val _isDirectMessage: MediatorLiveData<Boolean> = MediatorLiveData()
    public val isDirectMessage: LiveData<Boolean> = _isDirectMessage
    private val _channel = MediatorLiveData<Channel>()

    init {
        _maxMessageLength.value = Int.MAX_VALUE
        _commands.value = emptyList()
        chatDomain.watchChannel(cid, 0).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                _channel.addSource(channelController.channelData) { _channel.value = channelController.toChannel() }
                _maxMessageLength.addSource(_channel) { _maxMessageLength.value = it.config.maxMessageLength }
                _commands.addSource(_channel) { _commands.value = it.config.commands }
                _isDirectMessage.addSource(
                    _channel.combineWith(chatDomain.user) { channel, user ->
                        channel?.isDirectMessaging(user?.id ?: "") ?: true
                    }
                ) { _isDirectMessage.value = it }

                _members.addSource(channelController.members) { _members.value = it }
                _repliedMessage.addSource(channelController.repliedMessage) { _repliedMessage.value = it }
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

        chatDomain.sendMessage(message.apply(messageTransformer)).enqueue()
    }

    public fun sendMessageWithAttachments(
        messageText: String,
        attachmentFiles: List<File>,
        messageTransformer: Message.() -> Unit = { }
    ) {
        // Send message should not be cancelled when viewModel.onCleared is called
        val attachments = attachmentFiles.map { Attachment(upload = it) }.toMutableList()
        val message = Message(cid = cid, text = messageText, attachments = attachments).apply(messageTransformer)
        chatDomain.sendMessage(message).enqueue()
    }

    /**
     * Edit message
     *
     * @param message the Message sent
     */
    public fun editMessage(message: Message) {
        stopTyping()
        chatDomain.editMessage(message).enqueue()
    }

    /**
     * Sets the message to be edited
     *
     * @param message the Message to edit
     */
    public fun postMessageToEdit(message: Message?) {
        _messageToEdit.postValue(message)
    }

    /**
     * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
     * Call this on every keystroke
     */
    @Synchronized
    public fun keystroke() {
        val parentId = activeThread.value?.id
        chatDomain.keystroke(cid, parentId).enqueue()
    }

    /**
     * stopTyping - Sets last typing to null and sends the typing.stop event
     */
    public fun stopTyping() {
        val parentId = activeThread.value?.id
        chatDomain.stopTyping(cid, parentId).enqueue()
    }

    public fun dismissReply() {
        if (repliedMessage.value != null) {
            chatDomain.setMessageForReply(cid, null).enqueue()
        }
    }
}
