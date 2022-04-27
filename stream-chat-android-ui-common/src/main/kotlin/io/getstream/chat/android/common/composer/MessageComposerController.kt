/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.common.composer

import com.getstream.sdk.chat.utils.AttachmentConstants
import com.getstream.sdk.chat.utils.extensions.containsLinks
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelCapabilities
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
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
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
 * @param maxAttachmentCount The maximum number of attachments that can be sent in a single message.
 * @param maxAttachmentSize Tne maximum file size of each attachment in bytes. By default, 20mb for Stream CDN.
 */
@InternalStreamChatApi
@Suppress("TooManyFunctions")
public class MessageComposerController(
    private val channelId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
) {

    /**
     * Creates a [CoroutineScope] that allows us to cancel the ongoing work when the parent
     * ViewModel is disposed.
     *
     * We use the [DispatcherProvider.Immediate] variant here to make sure the UI updates don't go through the
     * process of dispatching events. This fixes several bugs where the input state breaks when deleting or typing
     * really fast.
     */
    private val scope = CoroutineScope(DispatcherProvider.Immediate)

    /**
     * Buffers typing updates.
     *
     * @see [TypingUpdateBuffer]
     */
    private val typingUpdateBuffer = TypingUpdateBuffer()

    /**
     * Holds information about the current state of the [Channel].
     */
    public val channelState: Flow<ChannelState> = chatClient.watchChannelAsState(
        cid = channelId,
        messageLimit = DefaultMessageLimit
    ).filterNotNull()

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
     */
    public val ownCapabilities: StateFlow<Set<String>> = channelState.flatMapLatest { it.channelData }
        .map { it.ownCapabilities }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = setOf()
        )

    /**
     * Signals if the user's typing will send a typing update in the given channel.
     *
     * Spun off as an individual field so that we can avoid the expense of running [Set.contains]
     * on every typing update.
     *
     * [SharingStarted.Eagerly] because this [StateFlow] has no collectors, its value is only
     * ever read directly.
     */
    private val canSendTypingUpdates = ownCapabilities.map { it.contains(ChannelCapabilities.SEND_TYPING_EVENTS) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    /**
     * Signals if the user is allowed to send links.
     *
     * Spun off as an individual field so that we can avoid the expense of running [Set.contains]
     * on every typing update.
     *
     * [SharingStarted.Eagerly] because this [StateFlow] has no collectors, its value is only
     * ever read directly.
     */
    private val canSendLinks = ownCapabilities.map { it.contains(ChannelCapabilities.SEND_LINKS) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    /**
     * Full message composer state holding all the required information.
     */
    public val state: MutableStateFlow<MessageComposerState> = MutableStateFlow(MessageComposerState())

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
    private var maxMessageLength: Int = DefaultMaxMessageLength

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
     * Current message mode, either [MessageMode.Normal] or [MessageMode.MessageThread]. Used to determine if we're
     * sending a thread reply or a regular message.
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
     * Represents the selected mentions based on the message suggestion list.
     */
    private val selectedMentions: MutableSet<User> = mutableSetOf()

    /**
     * Sets up the data loading operations such as observing the maximum allowed message length.
     */
    init {
        channelState.flatMapLatest { it.channelConfig }.onEach {
            maxMessageLength = it.maxMessageLength
            commands = it.commands
        }.launchIn(scope)

        channelState.flatMapLatest { it.members }.onEach { members ->
            users = members.map { it.user }
        }.launchIn(scope)

        channelState.flatMapLatest { it.channelData }.onEach {
            cooldownInterval = it.cooldown
        }.launchIn(scope)

        setupComposerState()
    }

    /**
     * Sets up the observing operations for various composer states.
     */
    private fun setupComposerState() {
        input.onEach { input ->
            state.value = state.value.copy(inputValue = input)
        }.launchIn(scope)

        selectedAttachments.onEach { selectedAttachments ->
            state.value = state.value.copy(attachments = selectedAttachments)
        }.launchIn(scope)

        lastActiveAction.onEach { activeAction ->
            state.value = state.value.copy(action = activeAction)
        }.launchIn(scope)

        validationErrors.onEach { validationErrors ->
            state.value = state.value.copy(validationErrors = validationErrors)
        }.launchIn(scope)

        mentionSuggestions.onEach { mentionSuggestions ->
            state.value = state.value.copy(mentionSuggestions = mentionSuggestions)
        }.launchIn(scope)

        commandSuggestions.onEach { commandSuggestions ->
            state.value = state.value.copy(commandSuggestions = commandSuggestions)
        }.launchIn(scope)

        cooldownTimer.onEach { cooldownTimer ->
            state.value = state.value.copy(coolDownTime = cooldownTimer)
        }.launchIn(scope)

        messageMode.onEach { messageMode ->
            state.value = state.value.copy(messageMode = messageMode)
        }.launchIn(scope)

        alsoSendToChannel.onEach { alsoSendToChannel ->
            state.value = state.value.copy(alsoSendToChannel = alsoSendToChannel)
        }.launchIn(scope)

        ownCapabilities.onEach { ownCapabilities ->
            state.value = state.value.copy(ownCapabilities = ownCapabilities)
        }.launchIn(scope)
    }

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        this.input.value = value

        if (canSendTypingUpdates.value) {
            typingUpdateBuffer.onTypingEvent()
        }
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
    public fun clearData() {
        input.value = ""
        selectedAttachments.value = emptyList()
        validationErrors.value = emptyList()
        alsoSendToChannel.value = false
    }

    /**
     * Sends a given message using our Stream API. Based on [isInEditMode], we either edit an existing
     * message, or we send a new message, using [ChatClient].
     *
     * It also dismisses any current message actions.
     *
     * @param message The message to send.
     */
    public fun sendMessage(message: Message) {
        val sendMessageCall = if (isInEditMode) {
            getEditMessageCall(message)
        } else {
            message.showInChannel = isInThread && alsoSendToChannel.value
            val (channelType, channelId) = message.cid.cidToTypeAndId()
            chatClient.sendMessage(channelType, channelId, message)
        }

        dismissMessageActions()
        clearData()
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

        val trimmedMessage = message.trim()
        val actionMessage = activeAction?.message ?: Message()
        val replyMessageId = (activeAction as? Reply)?.message?.id
        val mentions = filterMentions(selectedMentions, trimmedMessage)

        return if (isInEditMode) {
            actionMessage.copy(
                text = trimmedMessage,
                attachments = attachments.toMutableList(),
                mentionedUsersIds = mentions
            )
        } else {
            Message(
                cid = channelId,
                text = trimmedMessage,
                parentId = parentMessageId,
                replyMessageId = replyMessageId,
                attachments = attachments.toMutableList(),
                mentionedUsersIds = mentions
            )
        }
    }

    /**
     * Filters the current input and the mentions the user selected from the suggestion list. Removes any mentions which
     * are selected but no longer present in the input.
     *
     * @param selectedMentions The set of selected users from the suggestion list.
     * @param message The current message input.
     *
     * @return [MutableList] of user IDs of mentioned users.
     */
    private fun filterMentions(selectedMentions: Set<User>, message: String): MutableList<String> {
        val text = message.lowercase()

        val remainingMentions = selectedMentions.filter {
            text.contains("@${it.name.lowercase()}")
        }.map { it.id }

        this.selectedMentions.clear()
        return remainingMentions.toMutableList()
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
     * Cancels any pending work when the parent ViewModel is about to be destroyed.
     */
    public fun onCleared() {
        typingUpdateBuffer.clearTypingUpdates()
        scope.cancel()
    }

    /**
     * Checks the current input for validation errors.
     */
    private fun handleValidationErrors() {
        validationErrors.value = mutableListOf<ValidationError>().apply {
            val message = input.value
            val messageLength = message.length

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
            if (!canSendLinks.value && message.containsLinks()) {
                add(
                    ValidationError.ContainsLinksWhenNotAllowed
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
        selectedMentions += user
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
        val containsMention = MentionPattern.matcher(messageText).find()

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
        val containsCommand = CommandPattern.matcher(messageText).find()

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
                    delay(OneSecond)
                }
            }
        }
    }

    /**
     * Gets the edit message call using [ChatClient].
     *
     * @param message [Message]
     */
    private fun getEditMessageCall(message: Message): Call<Message> {
        return chatClient.updateMessage(message)
    }

    private companion object {
        /**
         * The default allowed number of characters in a message.
         */
        private const val DefaultMaxMessageLength: Int = 5000

        /**
         * The regex pattern used to check if the message ends with incomplete mention.
         */
        private val MentionPattern = Pattern.compile("^(.* )?@([a-zA-Z]+[0-9]*)*$", Pattern.MULTILINE)

        /**
         * The regex pattern used to check if the message ends with incomplete command.
         */
        private val CommandPattern = Pattern.compile("^/[a-z]*$")

        /**
         * The default limit for messages count in requests.
         */
        private const val DefaultMessageLimit: Int = 30

        private const val DefaultTypingUpdateIntervalMillis = 3000L
        private const val OneSecond = 1000L
    }

    /**
     * A class designed to buffer typing updates.
     * It works by sending the initial keystroke event and
     * delaying for [delayInterval] before sending a stop typing
     * event.
     *
     * Every subsequent keystroke will cancel the previous work
     * and reset the time before sending a stop typing event.
     *
     * @param delayInterval The interval between the sending the
     * keystroke event and the stop typing event.
     */
    private inner class TypingUpdateBuffer(private val delayInterval: Long = DefaultTypingUpdateIntervalMillis) {

        /**
         * If the user is currently typing or not.
         *
         * Sends out a typing related event on every value
         * change.
         */
        private var isTyping: Boolean = false
            set(value) {
                field = value
                handleTypingEvent(isTyping)
            }

        /**
         * Holds the currently running job.
         */
        var job: Job? = null

        /**
         * Used to send a stop typing event after a
         * set amount of time dictated by [delayInterval].
         */
        private suspend fun startTypingTimer() {
            delay(delayInterval)
            clearTypingUpdates()
        }

        /**
         * Sets the value of [isTyping] only if there is
         * a change in state in order to not create unnecessary events.
         *
         * It also resets the job to stop typing events after delay, debouncing keystrokes.
         */
        fun onTypingEvent() {
            if (!isTyping) {
                isTyping = true
            }
            job?.cancel()
            job = scope.launch { startTypingTimer() }
        }

        /**
         * Sets [isTyping] to false.
         *
         * Useful for clearing the state manually and in [onCleared].
         */
        fun clearTypingUpdates() {
            isTyping = false
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
            val (type, id) = channelId.cidToTypeAndId()
            if (isTyping) {
                chatClient.keystroke(type, id, parentMessageId)
            } else {
                chatClient.stopTyping(type, id, parentMessageId)
            }.enqueue()
        }
    }
}
