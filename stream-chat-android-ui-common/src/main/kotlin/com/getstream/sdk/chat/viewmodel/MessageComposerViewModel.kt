package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.getstream.sdk.chat.state.Edit
import com.getstream.sdk.chat.state.MessageAction
import com.getstream.sdk.chat.state.MessageMode
import com.getstream.sdk.chat.state.Normal
import com.getstream.sdk.chat.state.Reply
import com.getstream.sdk.chat.state.Thread
import com.getstream.sdk.chat.state.ThreadReply
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.ChatDomain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling the composing and sending of messages.
 */
public open class MessageComposerViewModel(
    private val chatClient: ChatClient,
    private val chatDomain: ChatDomain,
    private val channelId: String,
) : ViewModel() {

    /**
     * UI state of the current composer input.
     */
    public var input: MutableStateFlow<String> = MutableStateFlow("")
        private set

    /**
     * Represents the currently selected attachments, that are shown within the composer UI.
     */
    public var selectedAttachments: MutableStateFlow<List<Attachment>> = MutableStateFlow(emptyList())
        private set

    /**
     * Current message mode, either [Normal] or [Thread]. Used to determine if we're sending a thread
     * reply or a regular message.
     */
    private var messageMode: MessageMode = Normal

    /**
     * Set of currently active message actions. These are used to display different UI in the composer,
     * as well as help us decorate the message with information, such as the quoted message id.
     */
    private var messageActions = MutableStateFlow<Set<MessageAction>>(mutableSetOf())

    /**
     * Gets the active [Edit] or [Reply] action, whichever is last, to show on the UI.
     */
    public val activeAction: MessageAction?
        get() = messageActions.value.lastOrNull { it is Edit || it is Reply }

    /**
     * Gives us information if the active action is Edit, for business logic purposes.
     */
    private val isInEditMode: Boolean
        get() = activeAction is Edit

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        this.input.value = value
    }

    /**
     * Called when the message mode changes and the internal state needs to be updated.
     *
     * This affects the business logic.
     *
     * @param messageMode The current message mode.
     */
    public fun setMessageMode(messageMode: MessageMode) {
        this.messageMode = messageMode
    }

    /**
     * Handles selected [messageAction]. We only have three actions we can react to in the composer:
     * - [ThreadReply] - We change the [messageMode] so we can send the message to a thread.
     * - [Reply] - We need to reply to a message and set up the reply UI.
     * - [Edit] - We need to change the [input] to the message we want to edit and change the UI to
     * match the editing action.
     *
     * @param messageAction The newly selected action.
     */
    public fun performMessageAction(messageAction: MessageAction) {
        when (messageAction) {
            is ThreadReply -> {
                setMessageMode(Thread(messageAction.message))
            }
            is Reply -> {
                messageActions.value = messageActions.value + messageAction
            }
            is Edit -> {
                input.value = messageAction.message.text
                selectedAttachments.value = messageAction.message.attachments
                messageActions.value = messageActions.value + messageAction
            }
            else -> {
                // no op, custom user action
            }
        }
    }

    /**
     * Dismisses all message actions from the UI and clears the input if [isInEditMode] is true.
     */
    public fun dismissMessageActions() {
        if (isInEditMode) {
            setMessageInput("")
            this.selectedAttachments.value = emptyList()
        }

        this.messageActions.value = emptySet()
    }

    /**
     * Stores the selected attachments from the attachment picker. These will be shown in the UI,
     * within the composer component. We upload and send these attachments once the user taps on the
     * send button.
     *
     * @param attachments The attachments to store and show in the composer.
     */
    public fun addSelectedAttachments(attachments: List<Attachment>) {
        val newAttachments = (selectedAttachments.value + attachments).distinctBy {
            if (it.name != null) {
                it.name
            } else {
                it
            }
        }

        selectedAttachments.value = newAttachments
    }

    /**
     * Removes a selected attachment from the list, when the user taps on the cancel/delete button.
     *
     * This will update the UI to remove it from the composer component.
     *
     * @param attachment The attachment to remove.
     */
    public fun removeSelectedAttachment(attachment: Attachment) {
        selectedAttachments.value = selectedAttachments.value - attachment
    }

    /**
     * Clears all the data from the input - both the current [input] value and the
     * [selectedAttachments].
     */
    private fun clearData() {
        input.value = ""
        selectedAttachments.value = emptyList()
    }

    /**
     * Sends a given message using our Stream API. Based on [isInEditMode], we either edit an existing
     * message, or we send a new message, using the [ChatDomain].
     *
     * It also dismisses any current message actions.
     *
     * @param message The message to send.
     */
    public fun sendMessage(message: Message) {
        viewModelScope.launch {
            val sendMessageCall = if (isInEditMode) {
                chatDomain.editMessage(message)
            } else {
                chatDomain.sendMessage(message)
            }

            dismissMessageActions()
            sendMessageCall.enqueue()
        }
        clearData()
    }

    /**
     * Builds a new [Message] to send to our API. If [isInEditMode] is true, we use the current
     * action's message and apply the given changes.
     *
     * If we're not editing a message, we fill in the required data for the message.
     *
     * @param message Message text.
     * @param attachments Message attachments.
     *
     * @return [Message] object, with all the data required to send it to the API.
     */
    public fun buildNewMessage(
        message: String,
        attachments: List<Attachment> = emptyList(),
    ): Message {
        val activeAction = activeAction
        val messageMode = messageMode

        val actionMessage = activeAction?.message ?: Message()
        val replyMessageId = (activeAction as? Reply)?.message?.id
        val parentMessageId = (messageMode as? Thread)?.parentMessage?.id

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
     * user left the relevant thread.
     */
    public fun leaveThread() {
        setMessageMode(Normal)
        dismissMessageActions()
    }
}
