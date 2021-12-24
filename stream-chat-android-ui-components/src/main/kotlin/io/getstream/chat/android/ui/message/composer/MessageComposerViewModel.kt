package io.getstream.chat.android.ui.message.composer

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.composer.MessageComposerController
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageInputState
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel responsible for handling the composing and sending of messages.
 *
 * It relays all its core actions to a shared data source, as a central place for all the Composer logic.
 * Additionally, all the core data that can be reused across our SDKs is available through shared data sources, while
 * implementation-specific data is stored in respective in the [ViewModel].
 *
 * @param messageComposerController The controller used to relay all the actions and fetch all the state.
 */
public class MessageComposerViewModel(
    private val messageComposerController: MessageComposerController,
) : ViewModel() {

    /**
     * Current UI state of message input.
     */
    private val _messageInputState: MutableStateFlow<MessageInputState> = MutableStateFlow(MessageInputState())

    /**
     * UI state of the message input component.
     */
    public val messageInputState: StateFlow<MessageInputState> = _messageInputState

    /**
     * UI state of the current composer input.
     */
    public val input: MutableStateFlow<String> = messageComposerController.input

    /**
     * If the message will be shown in the channel after it is sent.
     */
    public val alsoSendToChannel: MutableStateFlow<Boolean> = messageComposerController.alsoSendToChannel

    /**
     * Represents the currently selected attachments, that are shown within the composer UI.
     */
    public val selectedAttachments: MutableStateFlow<List<Attachment>> = messageComposerController.selectedAttachments

    /**
     * Gets the active [Edit] or [Reply] action, whichever is last, to show on the UI.
     */
    public val lastActiveAction: Flow<MessageAction?> = messageComposerController.lastActiveAction

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        messageComposerController.setMessageInput(value)
        updateMessageInputState()
    }

    /**
     * Called when the "Also send as a direct message" checkbox is checked or unchecked.
     *
     * @param alsoSendToChannel If the message will be shown in the channel after it is sent.
     */
    public fun setAlsoSendToChannel(alsoSendToChannel: Boolean) {
        messageComposerController.setAlsoSendToChannel(alsoSendToChannel)
        updateMessageInputState()
    }

    /**
     * Called when the message mode changes and the internal state needs to be updated.
     *
     * This affects the business logic.
     *
     * @param messageMode The current message mode.
     */
    public fun setMessageMode(messageMode: MessageMode) {
        messageComposerController.setMessageMode(messageMode)
        updateMessageInputState()
    }

    /**
     * Handles the selected [messageAction].
     *
     * @param messageAction The newly selected action.
     */
    public fun performMessageAction(messageAction: MessageAction): Unit =
        messageComposerController.performMessageAction(messageAction)

    /**
     * Dismisses all message actions from the UI and clears the input based on the internal state.
     */
    public fun dismissMessageActions(): Unit = messageComposerController.dismissMessageActions()

    /**
     * Stores the selected attachments from the attachment picker. These will be shown in the UI,
     * within the composer component. We upload and send these attachments once the user taps on the
     * send button.
     *
     * @param attachments The attachments to store and show in the composer.
     */
    public fun addSelectedAttachments(attachments: List<Attachment>) {
        messageComposerController.addSelectedAttachments(attachments)
        updateMessageInputState()
    }

    /**
     * Removes a selected attachment from the list, when the user taps on the cancel/delete button.
     *
     * This will update the UI to remove it from the composer component.
     *
     * @param attachment The attachment to remove.
     */
    public fun removeSelectedAttachment(attachment: Attachment) {
        messageComposerController.removeSelectedAttachment(attachment)
        updateMessageInputState()
    }

    /**
     * Sends a given message using our Stream API. Based on the internal state, we either edit an existing message,
     * or we send a new message, using our API.
     *
     * It also dismisses any current message actions.
     *
     * @param message The message to send.
     */
    public fun sendMessage(message: Message): Unit = messageComposerController.sendMessage(message).also {
        setMessageInput("")
        clearSelectedAttachments()
    }

    /**
     * Removes all the selected attachments.
     */
    private fun clearSelectedAttachments() {
        messageComposerController.selectedAttachments.value = emptyList()
        updateMessageInputState()
    }

    /**
     * Builds a new [Message] to send to our API. Based on the internal state, we use the current action's message and
     * apply the given changes.
     *
     * If we're not editing a message, we'll fill in the required data for the message.
     *
     * @param message Message text.
     * @param attachments Message attachments.
     *
     * @return [Message] object, with all the data required to send it to the API.
     */
    public fun buildNewMessage(
        message: String = input.value,
        attachments: List<Attachment> = selectedAttachments.value,
    ): Message = messageComposerController.buildNewMessage(message, attachments)

    /**
     * Updates the UI state when leaving the thread, to switch back to the [MessageMode.Normal] message mode, by
     * calling [setMessageMode].
     *
     * It also dismisses any currently active message actions, such as [Edit] and [Reply], as the
     * user left the relevant thread.
     */
    public fun leaveThread() {
        messageComposerController.leaveThread()
        updateMessageInputState()
    }

    /**
     * Autocompletes the current text input with the mention from the selected user.
     *
     * @param user The user that is used to autocomplete the mention.
     */
    public fun selectMention(user: User) {
        messageComposerController.selectMention(user)
        updateMessageInputState()
    }

    /**
     * Disposes the inner [MessageComposerController].
     */
    override fun onCleared() {
        super.onCleared()
        messageComposerController.onCleared()
    }

    /**
     * Recomputes current [MessageInputState] and updates [MessageComposerViewModel._messageInputState].
     */
    private fun updateMessageInputState() {
        val oldState = _messageInputState.value
        _messageInputState.value = oldState.copy(
            inputValue = messageComposerController.input.value,
            attachments = messageComposerController.selectedAttachments.value,
            validationErrors = messageComposerController.validationErrors.value,
            mentionSuggestions = messageComposerController.mentionSuggestions.value,
            messageMode = messageComposerController.messageMode.value,
            alsoSendToChannel = messageComposerController.alsoSendToChannel.value,
        )
    }
}
