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

package io.getstream.chat.android.ui.common.feature.messages.composer

import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.chat.android.ui.common.utils.extensions.isModerationFailed
import io.getstream.chat.android.ui.common.utils.typing.TypingUpdatesBuffer
import io.getstream.chat.android.ui.common.utils.typing.internal.DefaultTypingUpdatesBuffer
import io.getstream.chat.android.uiutils.extension.containsLinks
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date
import java.util.concurrent.TimeUnit
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
 * @param maxAttachmentSize Tne maximum file size of each attachment in bytes. By default, 100 MB for Stream CDN.
 * @param messageId The id of a message we wish to scroll to in messages list. Used to control the number of channel
 * queries executed on screen initialization.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
@Suppress("TooManyFunctions")
public class MessageComposerController(
    private val channelId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val mediaRecorder: StreamMediaRecorder,
    private val fileToUri: (File) -> String,
    private val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
    private val maxAttachmentSize: Long = AttachmentConstants.MAX_UPLOAD_FILE_SIZE,
    messageId: String? = null,
) {

    /**
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger: TaggedLogger = StreamLog.getLogger("Chat:MessageComposerController")

    /**
     * Creates a [CoroutineScope] that allows us to cancel the ongoing work when the parent
     * ViewModel is disposed.
     *
     * We use the [DispatcherProvider.Immediate] variant here to make sure the UI updates don't go through the
     * process of dispatching events. This fixes several bugs where the input state breaks when deleting or typing
     * really fast.
     */
    private val scope = CoroutineScope(DispatcherProvider.Immediate)

    private val audioRecordingController = AudioRecordingController(
        channelId, chatClient, mediaRecorder, fileToUri, scope
    )

    /**
     * Buffers typing updates.
     *
     * @see [DefaultTypingUpdatesBuffer]
     */
    public var typingUpdatesBuffer: TypingUpdatesBuffer = DefaultTypingUpdatesBuffer(
        onTypingStarted = ::sendKeystrokeEvent,
        onTypingStopped = ::sendStopTypingEvent,
        coroutineScope = scope
    )

    /**
     * Holds information about the current state of the [Channel].
     */
    public val channelState: Flow<ChannelState> = chatClient.watchChannelAsState(
        cid = channelId,
        messageLimit = if (messageId != null) 0 else DefaultMessageLimit,
        coroutineScope = scope,
    ).filterNotNull()

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [ChannelCapabilities].
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
     * Signals if the user needs to wait before sending the next message.
     *
     * Depending on roles & permissions setup in the dashboard, some user groups are allowed
     * to send messages instantly even if the slow mode is enabled for the channel.
     *
     * [SharingStarted.Eagerly] because this [StateFlow] has no collectors, its value is only
     * ever read directly.
     */
    private val isSlowModeActive = ownCapabilities.map { it.contains(ChannelCapabilities.SLOW_MODE) }
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
     * Represents a Flow that holds the last active [MessageAction] that is either the [Edit], [Reply].
     */
    public val lastActiveAction: Flow<MessageAction?>
        get() = messageActions.map { actions ->
            actions.lastOrNull { it is Edit || it is Reply }
        }

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
            state.value = state.value.copy(hasCommands = commands.isNotEmpty())
        }.launchIn(scope)

        channelState.flatMapLatest { it.members }.onEach { members ->
            users = members.map { it.user }
        }.launchIn(scope)

        channelState.flatMapLatest { combine(it.channelData, it.lastSentMessageDate, ::Pair) }
            .distinctUntilChangedBy { (_, lastSentMessageDate) -> lastSentMessageDate }
            .onEach { (channelData, lastSentMessageDate) ->
                handleLastSentMessageDate(channelData.cooldown, lastSentMessageDate)
            }.launchIn(scope)

        setupComposerState()
    }

    /**
     * Sets up the observing operations for various composer states.
     */
    @OptIn(FlowPreview::class)
    private fun setupComposerState() {
        input.onEach { input ->
            state.value = state.value.copy(inputValue = input)

            if (canSendTypingUpdates.value) {
                typingUpdatesBuffer.onKeystroke()
            }
            handleCommandSuggestions()
            handleValidationErrors()
        }.debounce(ComputeMentionSuggestionsDebounceTime)
            .onEach {
                handleMentionSuggestions()
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

        chatClient.globalState.user.onEach { currentUser ->
            state.value = state.value.copy(currentUser = currentUser)
        }.launchIn(scope)

        audioRecordingController.recordingState.onEach { recording ->
            logger.d { "[onRecordingState] recording: $recording" }
            state.value = state.value.copy(recording = recording)
            if (recording is RecordingState.Complete) {
                selectedAttachments.value = selectedAttachments.value + recording.attachment
            }
        }.launchIn(scope)
    }

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        if (this.input.value == value) return
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
        logger.d { "[addSelectedAttachments] attachments: $attachments" }
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
        logger.i { "[clearData]" }
        input.value = ""
        selectedAttachments.value = emptyList()
        validationErrors.value = emptyList()
        alsoSendToChannel.value = false
    }

    /**
     * Sends a given message using our Stream API. Based on [isInEditMode], we either edit an existing message, or we
     * send a new message, using [ChatClient]. In case the message is a moderated message the old one is deleted before
     * the replacing one is sent.
     *
     * It also dismisses any current message actions.
     *
     * @param message The message to send.
     */
    public fun sendMessage(message: Message) {
        logger.i { "[sendMessage] message.attachments.size: ${message.attachments.size}" }
        val activeMessage = activeAction?.message ?: message

        val sendMessageCall = if (isInEditMode && !activeMessage.isModerationFailed(currentUser = chatClient.getCurrentUser())) {
            getEditMessageCall(message)
        } else {
            message.showInChannel = isInThread && alsoSendToChannel.value
            val (channelType, channelId) = message.cid.cidToTypeAndId()

            if (activeMessage.isModerationFailed(chatClient.getCurrentUser())) {
                chatClient.deleteMessage(activeMessage.id, true).enqueue()
            }

            chatClient.sendMessage(channelType, channelId, message)
        }

        dismissMessageActions()
        clearData()

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
        val activeMessage = activeAction?.message ?: Message()
        val replyMessageId = (activeAction as? Reply)?.message?.id
        val mentions = filterMentions(selectedMentions, trimmedMessage)

        return if (isInEditMode && !activeMessage.isModerationFailed(chatClient.getCurrentUser())) {
            activeMessage.copy(
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
        typingUpdatesBuffer.clear()
        audioRecordingController.onCleared()
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
     * Dismisses the suggestions popup above the message composer.
     */
    public fun dismissSuggestionsPopup() {
        mentionSuggestions.value = emptyList()
        commandSuggestions.value = emptyList()
    }

    public fun startRecording(): Unit = audioRecordingController.startRecording()

    public fun lockRecording(): Unit = audioRecordingController.lockRecording()

    public fun cancelRecording(): Unit = audioRecordingController.cancelRecording()

    public fun deleteRecording(): Unit = audioRecordingController.deleteRecording()

    public fun toggleRecordingPlayback(): Unit = audioRecordingController.toggleRecordingPlayback()

    public fun stopRecording(): Unit = audioRecordingController.stopRecording()

    public fun completeRecording(): Unit = audioRecordingController.completeRecording()

    public fun pauseRecording(): Unit = audioRecordingController.pauseRecording()

    public fun seekRecordingTo(progress: Float): Unit = audioRecordingController.seekRecordingTo(progress)

    /**
     * Shows the mention suggestion list popup if necessary.
     */
    private suspend fun handleMentionSuggestions() {
        val containsMention = MentionPattern.matcher(messageText).find()

        mentionSuggestions.value = if (containsMention) {
            logger.v { "[handleMentionSuggestions] Input contains the mention prefix @." }
            val userNameContains = messageText.substringAfterLast("@")

            val localMentions = users.filter { it.name.contains(userNameContains, true) }

            when {
                localMentions.isNotEmpty() -> {
                    logger.v { "[handleMentionSuggestions] Mention found in the local state." }
                    localMentions
                }
                userNameContains.count() > 1 -> {
                    logger.v { "[handleMentionSuggestions] Querying the server for members who match the mention." }
                    val (channelType, channelId) = channelId.cidToTypeAndId()

                    queryMembersByUserNameContains(
                        channelType = channelType,
                        channelId = channelId,
                        contains = userNameContains
                    )
                }
                else -> emptyList()
            }
        } else {
            emptyList()
        }
    }

    /**
     * Queries the backend for channel members whose username contains the string represented by the argument
     * [contains].
     *
     * @param channelType The type of channel we are querying for members.
     * @param channelId The ID of the channel we are querying for members.
     * @param contains The string for which we are querying the backend in order to see if it is contained
     * within a member's username.
     *
     * @return A list of users whose username contains the string represented by [contains] or an empty list in case
     * no usernames contain the given string.
     */
    private suspend fun queryMembersByUserNameContains(
        channelType: String,
        channelId: String,
        contains: String,
    ): List<User> {
        logger.v { "[queryMembersByUserNameContains] Querying the backend for members." }

        val result = chatClient.queryMembers(
            channelType = channelType,
            channelId = channelId,
            offset = queryMembersRequestOffset,
            limit = queryMembersMemberLimit,
            filter = Filters.autocomplete(
                fieldName = "name",
                value = contains
            ),
            sort = QuerySortByField(),
            members = listOf()
        ).await()

        return when (result) {
            is Result.Success -> {
                result.value
                    .filter { it.user.name.contains(contains, true) }
                    .map { it.user }
            }
            is Result.Failure -> {
                logger.e {
                    "[queryMembersByUserNameContains] Could not query members: " +
                        result.value.message
                }

                emptyList()
            }
        }
    }

    /**
     * Shows the command suggestion list popup if necessary.
     */
    private fun handleCommandSuggestions() {
        val containsCommand = CommandPattern.matcher(messageText).find()

        commandSuggestions.value = if (containsCommand && selectedAttachments.value.isEmpty()) {
            val commandPattern = messageText.removePrefix("/")
            commands.filter { it.name.startsWith(commandPattern) }
        } else {
            emptyList()
        }
    }

    /**
     * Shows the amount of time left until the user is allowed to send the next message.
     *
     * @param cooldownInterval The cooldown interval in seconds.
     * @param lastSentMessageDate The date of the last message.
     */
    private fun handleLastSentMessageDate(cooldownInterval: Int, lastSentMessageDate: Date?) {
        val isSlowModeActive = cooldownInterval > 0 && isSlowModeActive.value

        if (isSlowModeActive && lastSentMessageDate != null && !isInEditMode) {
            // Time passed since the last message was successfully sent to the server
            val elapsedTime: Long = TimeUnit.MILLISECONDS
                .toSeconds(System.currentTimeMillis() - lastSentMessageDate.time)
                .coerceAtLeast(0)

            fun updateCooldownTime(timeRemaining: Int) {
                cooldownTimer.value = timeRemaining
                state.value = state.value.copy(coolDownTime = timeRemaining)
            }

            // If the user is still unable to send messages show the timer
            if (elapsedTime < cooldownInterval) {
                cooldownTimerJob?.cancel()
                cooldownTimerJob = scope.launch {
                    for (timeRemaining in cooldownInterval - elapsedTime downTo 0) {
                        updateCooldownTime(timeRemaining.toInt())
                        delay(OneSecond)
                    }
                }
            } else {
                updateCooldownTime(0)
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

    /**
     * Makes an API call signaling that a typing event has occurred.
     */
    private fun sendKeystrokeEvent() {
        val (type, id) = channelId.cidToTypeAndId()

        chatClient.keystroke(type, id, parentMessageId).enqueue()
    }

    /**
     * Makes an API call signaling that a stop typing event has occurred.
     */
    private fun sendStopTypingEvent() {
        val (type, id) = channelId.cidToTypeAndId()

        chatClient.stopTyping(type, id, parentMessageId).enqueue()
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

        private const val OneSecond = 1000L

        /**
         * The amount of time we debounce computing mention suggestions.
         * We debounce those computations in the case of being unable to find mentions from local data, we will query
         * the BE for members.
         */
        private const val ComputeMentionSuggestionsDebounceTime = 300L

        /**
         * Pagination offset for the member query.
         */
        private const val queryMembersRequestOffset: Int = 0

        /**
         * The upper limit of members the query is allowed to return.
         */
        private const val queryMembersMemberLimit: Int = 30
    }
}
