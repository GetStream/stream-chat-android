/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.utils.message.isModerationError
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.globalStateFlow
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.typing.TypingSuggester
import io.getstream.chat.android.ui.common.feature.messages.composer.typing.TypingSuggestionOptions
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageInput
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.MessageValidator
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.chat.android.ui.common.utils.typing.TypingUpdatesBuffer
import io.getstream.chat.android.ui.common.utils.typing.internal.DefaultTypingUpdatesBuffer
import io.getstream.chat.android.uiutils.extension.addSchemeToUrlIfNeeded
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.doOnResult
import io.getstream.result.call.map
import io.getstream.result.onSuccessSuspend
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
 * @param channelCid The CID of the channel we're chatting in.
 * @param chatClient The client used to communicate to the API.
 * @param channelState The current state of the channel.
 * @param mediaRecorder The media recorder used to record audio messages.
 * @param userLookupHandler The handler used to lookup users for mentions.
 * @param fileToUri The function used to convert a file to a URI.
 * @param config The configuration for the message composer.
 * @param globalState A flow emitting the current [GlobalState].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@InternalStreamChatApi
@Suppress("TooManyFunctions")
public class MessageComposerController(
    private val channelCid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    public val channelState: StateFlow<ChannelState?>,
    mediaRecorder: StreamMediaRecorder,
    private val userLookupHandler: UserLookupHandler,
    fileToUri: (File) -> String,
    private val config: Config = Config(),
    private val globalState: Flow<GlobalState> = chatClient.globalStateFlow,
) {

    private val channelType = channelCid.cidToTypeAndId().first
    private val channelId = channelCid.cidToTypeAndId().second
    private val messageValidator = MessageValidator(
        appSettings = chatClient.getAppSettings(),
        maxAttachmentCount = config.maxAttachmentCount,
    )

    private var currentDraftId: String? = null

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
        chatClient.audioPlayer,
        mediaRecorder,
        fileToUri,
        scope,
    )

    /**
     * Buffers typing updates.
     *
     * @see [DefaultTypingUpdatesBuffer]
     */
    public var typingUpdatesBuffer: TypingUpdatesBuffer = DefaultTypingUpdatesBuffer(
        onTypingStarted = ::sendKeystrokeEvent,
        onTypingStopped = ::sendStopTypingEvent,
        coroutineScope = scope,
    )

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [ChannelCapabilities].
     */
    public val ownCapabilities: StateFlow<Set<String>> = channelState
        .filterNotNull()
        .flatMapLatest { it.channelData }
        .map {
            messageValidator.canSendLinks = it.ownCapabilities.contains(ChannelCapabilities.SEND_LINKS)
            it.ownCapabilities
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = setOf(),
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
    private val canSendTypingUpdates = ownCapabilities
        .map { it.contains(ChannelCapabilities.TYPING_EVENTS) || it.contains(ChannelCapabilities.SEND_TYPING_EVENTS) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false,
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
            initialValue = false,
        )

    /**
     * Signals whether slow-mode should be ignored (even if it's active).
     *
     * Some users can have an exceptions (e.g. moderators) and in this case we should ignore
     * slow-mode completely.
     *
     * [SharingStarted.Eagerly] because this [StateFlow] has no collectors, its value is only
     * ever read directly.
     */
    private val isSlowModeDisabled = ownCapabilities.map { it.contains(ChannelCapabilities.SKIP_SLOW_MODE) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    /** The flow of channel draft messages from the GlobalState. */
    private val channelDraftMessages = globalState
        .flatMapLatest { it.channelDraftMessages }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    /** The flow of thread draft messages from the GlobalState. */
    private val threadDraftMessages = globalState
        .flatMapLatest { it.threadDraftMessages }
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    /**
     * Full message composer state holding all the required information.
     */
    public val state: MutableStateFlow<MessageComposerState> = MutableStateFlow(MessageComposerState())

    /**
     * UI state of the current composer input.
     */
    public val messageInput: MutableStateFlow<MessageInput> = MutableStateFlow(MessageInput())

    /**
     * UI state of the current composer input.
     */
    @Deprecated(
        message = "Use messageInput instead",
        replaceWith = ReplaceWith("messageInput"),
    )
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
     * Represents the list of links that can be previewed.
     */
    public val linkPreviews: MutableStateFlow<List<LinkPreview>> = MutableStateFlow(emptyList())

    /**
     * Represents the list of users in the channel.
     */
    private var users: List<User> = emptyList()

    /**
     * Represents the list of available commands in the channel.
     */
    private var commands: List<Command> = emptyList()

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
        get() = messageInput.value.text

    /**
     * Gives us information if the composer is in the "thread" mode.
     */
    private val isInThread: Boolean
        get() = messageMode.value is MessageMode.MessageThread

    /**
     * Represents the selected mentions based on the message suggestion list.
     */
    private val selectedMentions: MutableSet<Mention> = mutableSetOf()

    private val mentionSuggester = TypingSuggester(
        TypingSuggestionOptions(symbol = MENTION_START_SYMBOL),
    )

    /**
     * Sets up the data loading operations such as observing the maximum allowed message length.
     */
    init {
        channelState
            .filterNotNull()
            .flatMapLatest { it.channelConfig }
            .onEach {
                messageValidator.maxMessageLength = it.maxMessageLength
                commands = it.commands
                state.value = state.value.copy(
                    hasCommands = commands.isNotEmpty(),
                    pollsEnabled = it.pollsEnabled,
                )
            }.launchIn(scope)

        channelState
            .filterNotNull()
            .flatMapLatest { it.members }.onEach { members ->
                users = members.map { it.user }
            }.launchIn(scope)

        channelState
            .filterNotNull()
            .flatMapLatest { combine(it.channelData, it.lastSentMessageDate, ::Pair) }
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
    @Suppress("LongMethod")
    private fun setupComposerState() {
        fetchDraftMessage(messageMode.value)
        messageInput.onEach { value ->
            input.value = value.text
            state.value = state.value.copy(inputValue = value.text)

            if (canSendTypingUpdates.value) {
                typingUpdatesBuffer.onKeystroke(value.text)
            }
            handleCommandSuggestions()
            handleValidationErrors()
        }.debounce(TEXT_INPUT_DEBOUNCE_TIME).onEach {
            scope.launch { handleMentionSuggestions() }
            scope.launch { handleLinkPreviews() }
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

        linkPreviews.onEach { linkPreviews ->
            state.value = state.value.copy(linkPreviews = linkPreviews)
        }.launchIn(scope)

        cooldownTimer.onEach { cooldownTimer ->
            state.value = state.value.copy(coolDownTime = cooldownTimer)
        }.launchIn(scope)

        messageMode
            .distinctUntilChanged { old, new ->
                when (old) {
                    is MessageMode.Normal -> new is MessageMode.Normal
                    is MessageMode.MessageThread ->
                        old.parentMessage.id == (new as? MessageMode.MessageThread)?.parentMessage?.id
                }
            }
            .onEach { messageMode ->
                saveDraftMessage(state.value.messageMode)
                state.value = state.value.copy(messageMode = messageMode)
                fetchDraftMessage(messageMode)
            }.launchIn(scope)

        alsoSendToChannel.onEach { alsoSendToChannel ->
            state.value = state.value.copy(alsoSendToChannel = alsoSendToChannel)
        }.launchIn(scope)

        ownCapabilities.onEach { ownCapabilities ->
            state.value = state.value.copy(ownCapabilities = ownCapabilities)
        }.launchIn(scope)

        chatClient.clientState.user.onEach { currentUser ->
            state.value = state.value.copy(currentUser = currentUser)
        }.launchIn(scope)

        audioRecordingController.recordingState.onEach { recording ->
            logger.d { "[onRecordingState] recording: $recording" }
            state.value = state.value.copy(recording = recording)
            if (recording is RecordingState.Complete) {
                selectedAttachments.value = selectedAttachments.value + recording.attachment
            }
        }.launchIn(scope)

        if (config.isDraftMessageEnabled) {
            channelDraftMessages.onEach {
                if (it[channelCid] == null &&
                    !currentDraftId.isNullOrEmpty() &&
                    messageMode.value is MessageMode.Normal
                ) {
                    clearData()
                }
            }.launchIn(scope)

            threadDraftMessages.onEach {
                if (it[parentMessageId] == null &&
                    !currentDraftId.isNullOrEmpty() &&
                    messageMode.value is MessageMode.MessageThread
                ) {
                    clearData()
                }
            }.launchIn(scope)
        }
    }

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        if (this.messageInput.value.text == value) return
        this.messageInput.value = MessageInput(value, MessageInput.Source.External)
    }

    private fun setMessageInputInternal(value: String, source: MessageInput.Source) {
        if (this.messageInput.value.text == value) return
        this.messageInput.value = MessageInput(value, source)
    }

    private suspend fun saveDraftMessage(messageMode: MessageMode) {
        if (!config.isDraftMessageEnabled) return
        currentDraftId = null
        when (val messageText = messageInput.value.text) {
            "" -> clearDraftMessage(messageMode)
            else -> {
                getDraftMessageOrEmpty(messageMode).let {
                    chatClient.createDraftMessage(
                        channelType = channelType,
                        channelId = channelId,
                        message = it.copy(
                            text = messageText,
                            showInChannel = alsoSendToChannel.value,
                            replyMessage = (messageActions.value.firstOrNull { it is Reply } as? Reply)?.message,
                        ),
                    ).await()
                }
            }
        }
    }

    private fun fetchDraftMessage(messageMode: MessageMode) {
        if (!config.isDraftMessageEnabled) return
        getDraftMessageOrEmpty(messageMode).let { draftMessage ->
            currentDraftId = draftMessage.id
            setMessageInputInternal(draftMessage.text, MessageInput.Source.DraftMessage)
            setAlsoSendToChannel(draftMessage.showInChannel)
            draftMessage.replyMessage
                ?.let { performMessageAction(Reply(it)) }
                ?: run {
                    messageActions.value = messageActions.value.filterNot { it is Reply }.toSet()
                }
        }
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
                messageActions.value = (messageActions.value.filterNot { it is Reply } + messageAction).toSet()
            }
            is Edit -> {
                setMessageInputInternal(messageAction.message.text, MessageInput.Source.Edit)
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
            setMessageInputInternal("", MessageInput.Source.Default)
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
            if (it.name != null && it.mimeType?.isNotEmpty() == true) {
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
     * Creates a poll with the given [pollConfig].
     *
     * @param pollConfig Configuration for creating a poll.
     */
    public fun createPoll(pollConfig: PollConfig, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.sendPoll(
            channelType = channelType,
            channelId = channelId,
            pollConfig = pollConfig,
        ).enqueue { response ->
            onResult(response)
        }
    }

    /**
     * Clears all the data from the input - both the current [input] value and the
     * [selectedAttachments].
     */
    public fun clearData() {
        logger.i { "[clearData]" }
        dismissMessageActions()
        scope.launch { clearDraftMessage(messageMode.value) }
        messageInput.value = MessageInput()
        selectedAttachments.value = emptyList()
        validationErrors.value = emptyList()
        alsoSendToChannel.value = false
    }

    private suspend fun clearDraftMessage(messageMode: MessageMode) {
        if (!config.isDraftMessageEnabled) return
        getDraftMessage(messageMode)?.let { draftMessage ->
            chatClient.deleteDraftMessages(
                channelType = channelType,
                channelId = channelId,
                message = draftMessage,
            ).await()
        }
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
    public fun sendMessage(message: Message, callback: Call.Callback<Message>) {
        logger.i { "[sendMessage] message.attachments.size: ${message.attachments.size}" }
        val activeMessage = activeAction?.message ?: message

        val currentUserId = chatClient.getCurrentUser()?.id
        val sendMessageCall = if (isInEditMode && !activeMessage.isModerationError(currentUserId)) {
            if (activeMessage.text == message.text) {
                logger.i { "[sendMessage] No changes in the message text, skipping edit." }
                clearData()
                return
            }
            getEditMessageCall(message)
        } else {
            val (channelType, channelId) = message.cid.cidToTypeAndId()
            if (activeMessage.isModerationError(currentUserId)) {
                chatClient.deleteMessage(activeMessage.id, true).enqueue()
            }

            chatClient.sendMessage(
                channelType,
                channelId,
                message.copy(showInChannel = isInThread && alsoSendToChannel.value),
            ).doOnResult(scope) { result ->
                result.onSuccessSuspend { resultMessage ->
                    if (channelState.value?.channelConfig?.value?.markMessagesPending == false) {
                        chatClient.markMessageRead(
                            channelType = channelType,
                            channelId = channelId,
                            messageId = resultMessage.id,
                        )
                            .await()
                    }
                }
            }
        }
        clearData()
        sendMessageCall.enqueue(callback)
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

        val currentUserId = chatClient.getCurrentUser()?.id
        val trimmedMessage = message.trim()
        val activeMessage = activeAction?.message ?: Message()
        val replyMessageId = (activeAction as? Reply)?.message?.id
        val mentions = filterMentions(selectedMentions, trimmedMessage)

        return if (isInEditMode && !activeMessage.isModerationError(currentUserId)) {
            activeMessage.copy(
                text = trimmedMessage,
                attachments = attachments.toMutableList(),
                mentionedUsersIds = mentions,
            )
        } else {
            Message(
                cid = channelCid,
                text = trimmedMessage,
                parentId = parentMessageId,
                replyMessageId = replyMessageId,
                attachments = attachments.toMutableList(),
                mentionedUsersIds = mentions,
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
    private fun filterMentions(selectedMentions: Set<Mention>, message: String): MutableList<String> {
        // Ignore custom, non-user mentions (for now)
        val userMentions = selectedMentions.filterIsInstance<Mention.User>()
        val text = message.lowercase()
        val remainingMentions = userMentions.filter {
            text.contains("@${it.user.name.lowercase()}")
        }.map { it.user.id }
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
        scope.launch {
            saveDraftMessage(messageMode.value)
            scope.cancel()
        }
    }

    /**
     * Checks the current input for validation errors.
     */
    private fun handleValidationErrors() {
        validationErrors.value = messageValidator.validateMessage(messageInput.value.text, selectedAttachments.value)
    }

    /**
     * Autocompletes the current text input with the mention from the selected user.
     *
     * @param user The user that is used to autocomplete the mention.
     */
    public fun selectMention(user: User) {
        selectMention(Mention.User(user))
    }

    /**
     * Autocompletes the current text input with the mention from the selected mention.
     *
     * IMPORTANT: The SDK supports only user mentions (see [Mention.User]). Custom mentions are purely visual, and will
     * not be submitted to the server.
     *
     * @param mention The mention that is used for the autocomplete.
     */
    public fun selectMention(mention: Mention) {
        val display = mention.display
        val augmentedMessageText = "${messageText.substringBeforeLast("@")}@$display "
        setMessageInputInternal(augmentedMessageText, MessageInput.Source.MentionSelected)

        selectedMentions += mention
        state.update { it.copy(selectedMentions = selectedMentions) }
    }

    /**
     * Switches the message composer to the command input mode.
     *
     * @param command The command that was selected.
     */
    public fun selectCommand(command: Command) {
        setMessageInputInternal("/${command.name} ", MessageInput.Source.CommandSelected)
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

    /**
     * Starts audio recording and moves [MessageComposerState.recording] state
     * from [RecordingState.Idle] to [RecordingState.Hold].
     */
    public fun startRecording(offset: Pair<Float, Float>? = null) {
        scope.launch {
            audioRecordingController.startRecording(offset)
        }
    }

    /**
     * Sends coordinates of the touch event to the [AudioRecordingController].
     */
    public fun holdRecording(offset: Pair<Float, Float>? = null) {
        audioRecordingController.holdRecording(offset)
    }

    /**
     * Moves [MessageComposerState.recording] state to [RecordingState.Locked].
     */
    public fun lockRecording(): Unit = audioRecordingController.lockRecording()

    /**
     * Cancels audio recording and moves [MessageComposerState.recording] state to [RecordingState.Idle].
     */
    public fun cancelRecording(): Unit = audioRecordingController.cancelRecording()

    /**
     * Toggles audio recording playback if [MessageComposerState.recording] is instance of [RecordingState.Overview].
     */
    public fun toggleRecordingPlayback(): Unit = audioRecordingController.toggleRecordingPlayback()

    /**
     * Stops audio recording and moves [MessageComposerState.recording] state to [RecordingState.Overview].
     */
    public fun stopRecording() {
        scope.launch {
            audioRecordingController.stopRecording()
        }
    }

    /**
     * Completes audio recording and moves [MessageComposerState.recording] state to [RecordingState.Complete].
     * Also, it wil update [MessageComposerState.attachments] list.
     */
    public fun completeRecording() {
        scope.launch {
            audioRecordingController.completeRecording()
        }
    }

    /**
     * Pauses audio recording and sets [RecordingState.Overview.isPlaying] to false.
     */
    public fun pauseRecording(): Unit = audioRecordingController.pauseRecording()

    /**
     * Pauses audio recording and seeks to the given [progress].
     * Sets [RecordingState.Overview.isPlaying] to false.
     * Sets [RecordingState.Overview.playingProgress] to the given progress.
     */
    public fun seekRecordingTo(progress: Float): Unit = audioRecordingController.seekRecordingTo(progress)

    /**
     * Completes the active audio recording and sends the recorded audio as an attachment.
     */
    public fun sendRecording() {
        scope.launch {
            audioRecordingController.completeRecordingSync().onSuccess { recording ->
                val attachments = selectedAttachments.value + recording
                sendMessage(buildNewMessage(messageInput.value.text, attachments), callback = {})
            }
        }
    }

    /**
     * Shows the mention suggestion list popup if necessary.
     */
    private fun handleMentionSuggestions() {
        val messageInput = messageInput.value
        if (messageInput.source == MessageInput.Source.MentionSelected) {
            logger.v { "[handleMentionSuggestions] rejected (messageInput came from mention selection)" }
            mentionSuggestions.value = emptyList()
            return
        }
        val inputText = messageInput.text
        scope.launch(DispatcherProvider.IO) {
            val suggestion = mentionSuggester.typingSuggestion(inputText)
            logger.v { "[handleMentionSuggestions] suggestion: $suggestion" }
            val result = if (suggestion != null) {
                userLookupHandler.handleUserLookup(suggestion.text)
            } else {
                emptyList()
            }
            withContext(DispatcherProvider.Main) {
                mentionSuggestions.value = result
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
        val isSlowModeActive = cooldownInterval > 0 && isSlowModeActive.value && !isSlowModeDisabled.value

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
                        delay(ONE_SECOND)
                    }
                }
            } else {
                updateCooldownTime(0)
            }
        }
    }

    /**
     * Shows link previews if necessary.
     */
    private suspend fun handleLinkPreviews() {
        if (!config.isLinkPreviewEnabled) return
        val urls = LinkPattern.findAll(messageText).map {
            it.value
        }.toList()
        logger.v { "[handleLinkPreviews] urls: $urls" }
        val previews = urls.take(1)
            .map { url -> chatClient.enrichPreview(url).await() }
            .filterIsInstance<Result.Success<LinkPreview>>()
            .map { it.value }

        logger.v { "[handleLinkPreviews] previews: ${previews.map { it.originUrl }}" }
        linkPreviews.value = previews
    }

    /**
     * Gets the edit message call using [ChatClient].
     *
     * @param message [Message]
     */
    private fun getEditMessageCall(message: Message): Call<Message> {
        return chatClient.partialUpdateMessage(
            messageId = message.id,
            set = mapOf(
                "text" to message.text,
                "attachments" to message.attachments,
                "mentioned_users" to message.mentionedUsersIds,
            ),
        )
    }

    /**
     * Makes an API call signaling that a typing event has occurred.
     */
    private fun sendKeystrokeEvent() {
        chatClient.keystroke(channelType, channelId, parentMessageId).enqueue()
    }

    /**
     * Makes an API call signaling that a stop typing event has occurred.
     */
    private fun sendStopTypingEvent() {
        chatClient.stopTyping(channelType, channelId, parentMessageId).enqueue()
    }

    private fun ChatClient.enrichPreview(url: String): Call<LinkPreview> {
        val urlWithScheme = url.addSchemeToUrlIfNeeded()
        return this.enrichUrl(urlWithScheme).map { LinkPreview(url, it) }
    }

    internal companion object {

        /**
         * The character used to start a mention.
         */
        private const val MENTION_START_SYMBOL: String = "@"

        /**
         * The regex pattern used to check if the message ends with incomplete command.
         */
        private val CommandPattern = Pattern.compile("^/[a-z]*$")

        internal val LinkPattern = Regex(
            "(http://|https://)?([a-zA-Z0-9]+(\\.[a-zA-Z0-9-]+)*\\.([a-zA-Z]{2,}))(/[\\w-./?%&=]*)?",
        )

        private const val ONE_SECOND = 1000L

        /**
         * The amount of time we debounce computing mention suggestions and link previews.
         */
        private const val TEXT_INPUT_DEBOUNCE_TIME = 300L
    }

    /**
     * Configuration for the message composer controller.
     *
     * @param maxAttachmentCount The maximum number of attachments allowed in a message.
     * @param isLinkPreviewEnabled If link previews are enabled.
     * @param isDraftMessageEnabled If draft messages are enabled.
     */
    @InternalStreamChatApi
    public data class Config(
        val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
        val isLinkPreviewEnabled: Boolean = false,
        val isDraftMessageEnabled: Boolean = false,
    )

    private fun getDraftMessageOrEmpty(messageMode: MessageMode): DraftMessage =
        getDraftMessage(messageMode) ?: messageMode.emptyDraftMessage()

    private fun getDraftMessage(messageMode: MessageMode): DraftMessage? = when (messageMode) {
        is MessageMode.MessageThread -> threadDraftMessages.value[messageMode.parentMessage.id]
        else -> channelDraftMessages.value[channelCid]
    }

    private fun MessageMode.emptyDraftMessage(): DraftMessage = when (this) {
        is MessageMode.MessageThread -> DraftMessage(cid = channelCid, parentId = parentMessage.id)
        else -> DraftMessage(cid = channelCid)
    }
}
