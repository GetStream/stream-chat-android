package io.getstream.chat.android.common.composer

import com.getstream.sdk.chat.utils.AttachmentConstants
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.common.state.ValidationError
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.extensions.keystroke
import io.getstream.chat.android.offline.extensions.stopTyping
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.regex.Pattern

/**
 * Controller responsible for handling the composing and sending of messages.
 *
 * It acts as a central place for both the core business logic and state required to create and send messages, handle
 * attachments, message actions and more.
 *
 * If you require more state and business logic, compose this Controller with your code and apply the necessary changes.
 *
 * @param channelId The ID of the channel we're chatting in.
 * @param chatClient The client used to communicate to the API.
 * @param chatDomain The domain used to communicate to the API and store data offline.
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param maxAttachmentSize Tne maximum file size of each attachment in bytes. By default, 20mb for Stream CDN.
 */
@InternalStreamChatApi
public class MessageComposerController(
    private val channelId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
) {
    /**
     * Creates a [CoroutineScope] that allows us to cancel the ongoing work when the parent
     * ViewModel is disposed.
     */
    private val scope = CoroutineScope(DispatcherProvider.Main)

    /**
     * UI state of the current composer input.
     */
    public val input: MutableStateFlow<String> = MutableStateFlow("")

    /**
     * If the message will be shown in the channel after it is sent.
     */
    public val alsoSendToChannel: MutableStateFlow<Boolean> = MutableStateFlow(false)

    /**
     * Represents the remaining time until the user is allowed to send the next message.
     */
    public val cooldownTimer: MutableStateFlow<Int> = MutableStateFlow(0)

    /**
     * Represents the currently selected attachments, that are shown within the composer UI.
     */
    public val selectedAttachments: MutableStateFlow<List<Attachment>> = MutableStateFlow(emptyList())

    /**
     * Represents the list of validation errors for the current text input and the currently selected attachments.
     */
    public val validationErrors: MutableStateFlow<List<ValidationError>> = MutableStateFlow(emptyList())

    /**
     * Represents the list of users that can be used to autocomplete the current mention input.
     */
    public val mentionSuggestions: MutableStateFlow<List<User>> = MutableStateFlow(emptyList())

    /**
     * Represents the list of commands that can be executed for the channel.
     */
    public val commandSuggestions: MutableStateFlow<List<Command>> = MutableStateFlow(emptyList())

    /**
     * Represents the list of users in the channel.
     */
    private var users: List<User> = emptyList()

    /**
     * Represents the list of available commands in the channel.
     */
    private var commands: List<Command> = emptyList()

    /**
     * Represents the maximum allowed message length in the message input.
     */
    private var maxMessageLength: Int = DEFAULT_MAX_MESSAGE_LENGTH

    /**
     * Represents the coroutine [Job] used to update the countdown
     */
    private var cooldownTimerJob: Job? = null

    /**
     * Represents the cooldown interval in seconds.
     *
     * When slow mode is enabled, users can only send messages every [cooldownInterval] time interval.
     */
    private var cooldownInterval: Int = 0

    /**
     * Current message mode, either [MessageMode.Normal] or [MessageMode.MessageThread]. Used to determine if we're sending a thread
     * reply or a regular message.
     */
    public val messageMode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)

    /**
     * Set of currently active message actions. These are used to display different UI in the composer,
     * as well as help us decorate the message with information, such as the quoted message id.
     */
    public val messageActions: MutableStateFlow<Set<MessageAction>> = MutableStateFlow(mutableSetOf())

    /**
     * Represents a Flow that holds the last active [MessageAction] that is either the [Edit] or [Reply] action.
     */
    public val lastActiveAction: Flow<MessageAction?>
        get() = messageActions.map { actions -> actions.lastOrNull { it is Edit || it is Reply } }

    /**
     * Gets the active [Edit] or [Reply] action, whichever is last, to show on the UI.
     */
    private val activeAction: MessageAction?
        get() = messageActions.value.lastOrNull { it is Edit || it is Reply }

    /**
     * Gives us information if the active action is Edit, for business logic purposes.
     */
    private val isInEditMode: Boolean
        get() = activeAction is Edit

    /**
     * Gets the parent message id if we are in thread mode, or null otherwise.
     */
    private val parentMessageId: String?
        get() = (messageMode.value as? MessageMode.MessageThread)?.parentMessage?.id

    /**
     * Gets the current text input in the message composer.
     */
    private val messageText: String
        get() = input.value

    /**
     * Gives us information if the composer is in the "thread" mode.
     */
    private val isInThread: Boolean
        get() = messageMode.value is MessageMode.MessageThread

    /**
     * Sets up the data loading operations such as observing the maximum allowed message length.
     */
    init {
        scope.launch {
            val result = chatDomain.watchChannel(channelId, 0).await()

            if (result.isSuccess) {
                val channelController = result.data()

                channelController.channelConfig.onEach {
                    maxMessageLength = it.maxMessageLength
                    commands = it.commands
                }.launchIn(scope)

                channelController.members.onEach { members ->
                    users = members.map { it.user }
                }.launchIn(scope)

                channelController.channelData.onEach {
                    cooldownInterval = it.cooldown
                }.launchIn(scope)
            }
        }
    }

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        this.input.value = value

        handleTypingEvent(isTyping = value.isNotEmpty())
        handleMentionSuggestions()
        handleCommandSuggestions()
        handleValidationErrors()
    }

    /**
     * Called when the message mode changes and the internal state needs to be updated.
     *
     * This affects the business logic.
     *
     * @param messageMode The current message mode.
     */
    public fun setMessageMode(messageMode: MessageMode) {
        this.messageMode.value = messageMode
    }

    /**
     * Called when the "Also send as a direct message" checkbox is checked or unchecked.
     *
     * @param alsoSendToChannel If the message will be shown in the channel after it is sent.
     */
    public fun setAlsoSendToChannel(alsoSendToChannel: Boolean) {
        this.alsoSendToChannel.value = alsoSendToChannel
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
                setMessageMode(MessageMode.MessageThread(messageAction.message))
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

        handleValidationErrors()
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

        handleValidationErrors()
    }

    /**
     * Clears all the data from the input - both the current [input] value and the
     * [selectedAttachments].
     */
    private fun clearData() {
        input.value = ""
        selectedAttachments.value = emptyList()
        validationErrors.value = emptyList()
        alsoSendToChannel.value = false
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
        val sendMessageCall = if (isInEditMode) {
            chatDomain.editMessage(message)
        } else {
            message.showInChannel = isInThread && alsoSendToChannel.value
            chatDomain.sendMessage(message)
        }

        dismissMessageActions()
        clearData()
        handleTypingEvent(isTyping = false)
        handleCooldownTimer()

        sendMessageCall.enqueue()
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

        val actionMessage = activeAction?.message ?: Message()
        val replyMessageId = (activeAction as? Reply)?.message?.id

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
     * Updates the UI state when leaving the thread, to switch back to the [MessageMode.Normal], by
     * calling [setMessageMode].
     *
     * It also dismisses any currently active message actions, such as [Edit] and [Reply], as the
     * user left the relevant thread.
     */
    public fun leaveThread() {
        setMessageMode(MessageMode.Normal)
        dismissMessageActions()
    }

    /**
     * Sends the `typing.start` or `typing.stop` event depending on the [isTyping] parameter.
     *
     * The `typing.start` event is sent if more than 3 seconds passed since the last keystroke.
     * The `typing.stop` is automatically sent when the user stops typing for 5 seconds.
     *
     * @param isTyping If the user is currently typing.
     */
    private fun handleTypingEvent(isTyping: Boolean) {
        if (isTyping) {
            chatClient.keystroke(channelId, parentMessageId)
        } else {
            chatClient.stopTyping(channelId, parentMessageId)
        }.enqueue()
    }

    /**
     * Cancels any pending work when the parent ViewModel is about to be destroyed.
     */
    public fun onCleared() {
        scope.cancel()
    }

    /**
     * Checks the current input for validation errors.
     */
    private fun handleValidationErrors() {
        validationErrors.value = mutableListOf<ValidationError>().apply {
            val messageLength = input.value.length
            if (messageLength > maxMessageLength) {
                add(
                    ValidationError.MessageLengthExceeded(
                        messageLength = messageLength,
                        maxMessageLength = maxMessageLength
                    )
                )
            }

            val attachmentCount = selectedAttachments.value.size
            if (attachmentCount > maxAttachmentCount) {
                add(
                    ValidationError.AttachmentCountExceeded(
                        attachmentCount = attachmentCount,
                        maxAttachmentCount = maxAttachmentCount
                    )
                )
            }

            val attachments: List<Attachment> = selectedAttachments.value
                .filter { it.fileSize > maxAttachmentSize }
            if (attachments.isNotEmpty()) {
                add(
                    ValidationError.AttachmentSizeExceeded(
                        attachments = attachments,
                        maxAttachmentSize = maxAttachmentSize
                    )
                )
            }
        }
    }

    /**
     * Autocompletes the current text input with the mention from the selected user.
     *
     * @param user The user that is used to autocomplete the mention.
     */
    public fun selectMention(user: User) {
        val augmentedMessageText = "${messageText.substringBeforeLast("@")}@${user.name} "

        setMessageInput(augmentedMessageText)
    }

    /**
     * Switches the message composer to the command input mode.
     *
     * @param command The command that was selected.
     */
    public fun selectCommand(command: Command) {
        setMessageInput("/${command.name} ")
    }

    /**
     * Toggles the visibility of the command suggestion list popup.
     */
    public fun toggleCommandsVisibility() {
        val isHidden = commandSuggestions.value.isEmpty()

        commandSuggestions.value = if (isHidden) commands else emptyList()
    }

    /**
     * Shows the mention suggestion list popup if necessary.
     */
    private fun handleMentionSuggestions() {
        val containsMention = MENTION_PATTERN.matcher(messageText).find()

        mentionSuggestions.value = if (containsMention) {
            users.filter { it.name.contains(messageText.substringAfterLast("@"), true) }
        } else {
            emptyList()
        }
    }

    /**
     * Shows the command suggestion list popup if necessary.
     */
    private fun handleCommandSuggestions() {
        val containsCommand = COMMAND_PATTERN.matcher(messageText).find()

        commandSuggestions.value = if (containsCommand) {
            val commandPattern = messageText.removePrefix("/")
            commands.filter { it.name.startsWith(commandPattern) }
        } else {
            emptyList()
        }
    }

    /**
     * Shows cooldown countdown timer instead of send button when slow mode is enabled.
     */
    private fun handleCooldownTimer() {
        if (cooldownInterval > 0) {
            cooldownTimerJob?.cancel()
            cooldownTimerJob = scope.launch {
                for (timeRemaining in cooldownInterval downTo 0) {
                    cooldownTimer.value = timeRemaining
                    delay(1000)
                }
            }
        }
    }

    private companion object {
        /**
         * The default allowed number of characters in a message.
         */
        private const val DEFAULT_MAX_MESSAGE_LENGTH: Int = 5000

        /**
         * The regex pattern used to check if the message ends with incomplete mention.
         */
        private val MENTION_PATTERN = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$")

        /**
         * The regex pattern used to check if the message ends with incomplete command.
         */
        private val COMMAND_PATTERN = Pattern.compile("^/[a-z]*$")
    }
}
