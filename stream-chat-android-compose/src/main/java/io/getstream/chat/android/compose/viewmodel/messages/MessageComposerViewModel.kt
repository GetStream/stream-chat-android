package io.getstream.chat.android.compose.viewmodel.messages

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.MessageMode
import io.getstream.chat.android.compose.state.messages.Normal
import io.getstream.chat.android.compose.state.messages.Thread
import io.getstream.chat.android.compose.state.messages.list.Edit
import io.getstream.chat.android.compose.state.messages.list.MessageAction
import io.getstream.chat.android.compose.state.messages.list.Reply
import io.getstream.chat.android.compose.state.messages.list.ThreadReply
import io.getstream.chat.android.offline.ChatDomain
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling the composing and sending of messages.
 * */
class MessageComposerViewModel(
    val chatClient: ChatClient,
    val chatDomain: ChatDomain,
    private val channelId: String,
) : ViewModel() {

    /**
     * UI state of the current composer input.
     * */
    var input by mutableStateOf("")
        private set

    /**
     * Represents the currently selected attachments, that are shown within the composer UI.
     * */
    var selectedAttachments by mutableStateOf(emptyList<Attachment>())
        private set

    /**
     * Current message mode, either [Normal] or [Thread]. Used to determine if we're sending a thread
     * reply or a regular message.
     * */
    private var messageMode: MessageMode = Normal

    /**
     * Set of currently active message actions. These are used to display different UI in the composer,
     * as well as help us decorate the message with information, such as the quoted message id.
     * */
    private var messageActions by mutableStateOf<Set<MessageAction>>(mutableSetOf())

    /**
     * Gets the active [Edit] or [Reply] action, whichever is last, to show on the UI.
     * */
    val activeAction: MessageAction?
        get() = messageActions.lastOrNull { it is Edit || it is Reply }

    /**
     * Gives us information if the active action is Edit, for business logic purposes.
     * */
    private val isInEditMode: Boolean
        get() = activeAction is Edit

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value - Current state value.
     * */
    fun onInputChange(value: String) {
        this.input = value
    }

    /**
     * Called when the message mode changes and the internal state needs to be updated.
     *
     * This affects the business logic.
     *
     * @param messageMode - The current message mode.
     * */
    fun setMessageMode(messageMode: MessageMode) {
        this.messageMode = messageMode
    }

    /**
     * Handles selected [messageAction]. We only have three actions we can react to in the composer:
     * - [ThreadReply] - We change the [messageMode] so we can send the message to a thread.
     * - [Reply] - We need to reply to a message and set up the reply UI.
     * - [Edit] - We need to change the [input] to the message we want to edit and change the UI to
     * match the editing action.
     *
     * @param messageAction - The newly selected action.
     * */
    fun onMessageAction(messageAction: MessageAction) {
        when (messageAction) {
            is ThreadReply -> {
                setMessageMode(Thread(messageAction.message))
            }
            is Reply -> {
                messageActions = messageActions + messageAction
            }
            is Edit -> {
                this.input = messageAction.message.text
                messageActions = messageActions + messageAction
            }
            else -> {
                // no op, custom user action
            }
        }
    }

    /**
     * Dismisses all message actions from the UI and clears the input if [isInEditMode] is true.
     * */
    fun onDismissMessageActions() {
        if (isInEditMode) {
            onInputChange("")
        }

        this.messageActions = emptySet()
    }

    /**
     * Stores the selected attachments from the attachment picker. These will be shown in the UI,
     * within the composer component. We upload and send these attachments once the user taps on the
     * send button.
     *
     * @param attachments - The attachments to store and show in the composer.
     * */
    fun onAttachmentsSelected(attachments: List<Attachment>) {
        this.selectedAttachments = attachments
    }

    /**
     * Removes a selected attachment from the list, when the user taps on the cancel/delete button.
     *
     * This will update the UI to remove it from the composer component.
     *
     * @param attachment - The attachment to remove.
     * */
    fun removeSelectedAttachment(attachment: Attachment) {
        this.selectedAttachments = this.selectedAttachments - attachment
    }

    /**
     * Sends a basic text message.  We first prepare the data to send the message,
     * using [buildNewMessage], after which we send the message using [sendMessage].
     *
     * @param message - The message to send.
     * */
    fun onSendMessage(message: String) {
        val messageToSend = buildNewMessage(message, selectedAttachments)

        sendMessage(messageToSend)
        clearData()
    }

    /**
     * Clears all the data from the input - both the current [input] value and the
     * [selectedAttachments].
     * */
    private fun clearData() {
        input = ""
        selectedAttachments = emptyList()
    }

    /**
     * Sends a given message using our Stream API. Based on [isInEditMode], we either edit an existing
     * message, or we send a new message, using the [ChatDomain].
     *
     * It also dismisses any current message actions, using [onDismissMessageActions].
     *
     * @param message - The message to send.
     * */
    private fun sendMessage(message: Message) {
        viewModelScope.launch {
            val sendMessageCall = if (isInEditMode) {
                chatDomain.editMessage(message)
            } else {
                chatDomain.sendMessage(message)
            }

            onDismissMessageActions()
            sendMessageCall.enqueue()
        }
    }

    /**
     * Builds a new [Message] to send to our API. If [isInEditMode] is true, we use the current
     * action's message and apply the given changes.
     *
     * If we're not editing a message, we fill in the required data for the message.
     *
     * @param message - Message text.
     * @param attachments - Message attachments.
     *
     * @return [Message] object, with all the data required to send it to the API.
     * */
    private fun buildNewMessage(
        message: String,
        attachments: List<Attachment> = emptyList(),
    ): Message {
        val activeAction = activeAction
        val messageMode = messageMode

        val actionMessage = activeAction?.message ?: Message()
        val replyMessageId = if (activeAction is Reply) activeAction.message.id else null
        val parentMessageId = if (messageMode is Thread) messageMode.parentMessage.id else null

        return if (isInEditMode) {
            actionMessage.copy(
                text = message,
                attachments = attachments.toMutableList()
            )
        } else {
            Message(
                cid = channelId,
                text = message,
                parentId = parentMessageId,
                replyMessageId = replyMessageId,
                attachments = attachments.toMutableList()
            )
        }
    }

    /**
     * Updates the UI state when leaving the thread, to switch back to the [Normal] message mode, by
     * calling [setMessageMode].
     *
     * It also dismisses any currently active message actions, such as [Edit] and [Reply], as the
     * user left the relevant thread, by calling [onDismissMessageActions].
     * */
    fun leaveThread() {
        setMessageMode(Normal)
        onDismissMessageActions()
    }
}