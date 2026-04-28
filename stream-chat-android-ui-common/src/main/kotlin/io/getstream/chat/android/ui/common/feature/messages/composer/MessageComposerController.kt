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

import androidx.lifecycle.SavedStateHandle
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.api.state.globalStateFlow
import io.getstream.chat.android.client.api.state.loadNewestMessages
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.utils.message.isModerationError
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.CreatePollParams
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.UserLookupHandler
import io.getstream.chat.android.ui.common.feature.messages.composer.typing.TypingSuggester
import io.getstream.chat.android.ui.common.feature.messages.composer.typing.TypingSuggestionOptions
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.state.messages.MessageInput
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerViewEvent
import io.getstream.chat.android.ui.common.state.messages.composer.MessageValidator
import io.getstream.chat.android.ui.common.state.messages.composer.RecordingState
import io.getstream.chat.android.ui.common.state.messages.composer.isAvailableFor
import io.getstream.chat.android.ui.common.state.messages.composer.sortedByAvailability
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.chat.android.ui.common.utils.extensions.addSchemeToUrlIfNeeded
import io.getstream.chat.android.ui.common.utils.typing.TypingUpdatesBuffer
import io.getstream.chat.android.ui.common.utils.typing.internal.DefaultTypingUpdatesBuffer
import io.getstream.log.StreamLog
import io.getstream.log.TaggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.doOnResult
import io.getstream.result.call.doOnStart
import io.getstream.result.call.map
import io.getstream.result.onSuccessSuspend
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
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
 * @param savedStateHandle Handle used to persist and restore picker selections and edit-mode state
 * across process death (e.g. caused by opening the system file picker while editing a message).
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
    savedStateHandle: SavedStateHandle = SavedStateHandle(),
) {

    private val channelType = channelCid.cidToTypeAndId().first
    private val channelId = channelCid.cidToTypeAndId().second
    private val messageValidator = MessageValidator(
        appSettings = chatClient.getAppSettings(),
        maxAttachmentCount = config.maxAttachmentCount,
    )

    private var currentDraftId: String? = null

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
    private val ownCapabilities: StateFlow<Set<String>> = channelState
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

    private val _inputFocusEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /**
     * Emits each time the message input field should request focus (e.g. after a command is selected).
     */
    public val inputFocusEvents: SharedFlow<Unit> = _inputFocusEvents.asSharedFlow()

    private val _events = MutableSharedFlow<MessageComposerViewEvent>(extraBufferCapacity = 1)

    /**
     * One-shot events emitted by the composer, such as feedback for unavailable commands. UI
     * layers collect this flow and react with transient UI (e.g. a snackbar).
     */
    public val events: SharedFlow<MessageComposerViewEvent> = _events.asSharedFlow()

    // Insertion-ordered map keyed by attachment key (EXTRA_SOURCE_URI or fallback).
    // Tracks picker selections independently of edit-mode attachments, so selections
    // survive entering and exiting edit mode.
    private val _selectedAttachments = MutableStateFlow(linkedMapOf<String, Attachment>())

    // Holds the base attachments from the message being edited. Cleared when edit mode is dismissed.
    private val _editModeAttachments = MutableStateFlow<List<Attachment>>(emptyList())

    // Holds the message being edited, or null when not in edit mode.
    private val _editModeMessage = MutableStateFlow<Message?>(null)

    // Holds the attachment produced by a completed audio recording, if any.
    // Cleared when the composer is reset (e.g. after sending).
    private val _recordingAttachment = MutableStateFlow<Attachment?>(null)

    private val sessionRepository = ComposerSessionRepository(savedStateHandle)

    private val _state = MutableStateFlow(MessageComposerState())

    /** Full message composer state holding all the required information. */
    public val state: StateFlow<MessageComposerState> = _state.asStateFlow()

    private val _messageInput = MutableStateFlow(MessageInput())

    /** UI state of the current composer input. */
    public val messageInput: StateFlow<MessageInput> = _messageInput.asStateFlow()

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
     * Represents the coroutine [Job] resolving link previews for the current input.
     */
    private var linkPreviewJob: Job? = null

    /**
     * The URL whose link preview the user explicitly dismissed via [cancelLinkPreview].
     * `null` when no preview has been dismissed. Reset when the detected URL changes
     * or when the composer is cleared.
     */
    private var dismissedLinkPreviewUrl: String? = null

    private val _messageActions = MutableStateFlow<Set<MessageAction>>(mutableSetOf())

    /**
     * Set of currently active message actions. These are used to display different UI in the composer,
     * as well as help us decorate the message with information, such as the quoted message id.
     */
    public val messageActions: StateFlow<Set<MessageAction>> = _messageActions.asStateFlow()

    /**
     * Gets the active [Edit] or [Reply] action, whichever is last, to show on the UI.
     */
    private val activeAction: MessageAction?
        get() = _messageActions.value.lastOrNull { it is Edit || it is Reply }

    /**
     * Gives us information if the active action is Edit, for business logic purposes.
     */
    private val isInEditMode: Boolean
        get() = activeAction is Edit

    /**
     * Gets the parent message id if we are in thread mode, or null otherwise.
     */
    private val parentMessageId: String?
        get() = (_state.value.messageMode as? MessageMode.MessageThread)?.parentMessage?.id

    /**
     * Gets the current text input in the message composer.
     */
    private val messageText: String
        get() = _messageInput.value.text

    /**
     * Gives us information if the composer is in the "thread" mode.
     */
    private val isInThread: Boolean
        get() = _state.value.messageMode is MessageMode.MessageThread

    /**
     * Represents the selected mentions based on the message suggestion list.
     */
    private val selectedMentions: MutableSet<Mention> = mutableSetOf()

    /**
     * Snapshot of pre-command composer state captured when entering command mode under
     * [Config.activeCommandEnabled]. Restored on [clearActiveCommand] and discarded on
     * [clearData]. `null` when no command is active or when command mode is disabled.
     */
    private var commandStash: CommandStash? = null

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
                _state.value = _state.value.copy(
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
        restoreSession()
        observeSessionChanges()
    }

    /**
     * Sets up the observing operations for various composer states.
     */
    @OptIn(FlowPreview::class)
    @Suppress("LongMethod")
    private fun setupComposerState() {
        fetchDraftMessage(_state.value.messageMode)
        _messageInput.onEach { value ->
            _state.value = _state.value.copy(inputValue = value.text)

            if (canSendTypingUpdates.value) {
                typingUpdatesBuffer.onKeystroke(value.text)
            }
            handleCommandSuggestions()
            handleValidationErrors()
        }.debounce(TEXT_INPUT_DEBOUNCE_TIME).onEach {
            scope.launch { handleMentionSuggestions() }
            linkPreviewJob?.cancel()
            linkPreviewJob = scope.launch { handleLinkPreview() }
        }.launchIn(scope)

        _messageActions.onEach { actions ->
            val activeAction = actions.lastOrNull { it is Edit || it is Reply }
            _state.update { it.copy(action = activeAction) }
            // Re-derive command suggestions so the list re-sorts by availability for the new
            // action and the edit-mode guard fires if a command is currently being typed.
            handleCommandSuggestions()
        }.launchIn(scope)

        ownCapabilities.onEach { ownCapabilities ->
            _state.value = _state.value.copy(ownCapabilities = ownCapabilities)
        }.launchIn(scope)

        chatClient.clientState.user.onEach { currentUser ->
            _state.value = _state.value.copy(currentUser = currentUser)
        }.launchIn(scope)

        audioRecordingController.recordingState.onEach { recording ->
            logger.d { "[onRecordingState] recording: $recording" }
            if (recording is RecordingState.Complete) {
                _recordingAttachment.value = recording.attachment
            }
            _state.update { it.copy(recording = recording) }
            syncAttachments()
        }.launchIn(scope)

        if (config.draftMessageEnabled) {
            channelDraftMessages.onEach {
                if (it[channelCid] == null &&
                    !currentDraftId.isNullOrEmpty() &&
                    _state.value.messageMode is MessageMode.Normal
                ) {
                    clearData()
                }
            }.launchIn(scope)

            threadDraftMessages.onEach {
                if (it[parentMessageId] == null &&
                    !currentDraftId.isNullOrEmpty() &&
                    _state.value.messageMode is MessageMode.MessageThread
                ) {
                    clearData()
                }
            }.launchIn(scope)
        }
    }

    private fun restoreSession() {
        val restoredAttachments = sessionRepository.restoreSelectedAttachments()
        if (restoredAttachments.isNotEmpty()) {
            addAttachments(restoredAttachments)
        }
        sessionRepository.restoreEditMode()?.let { editMode ->
            restoreEditMode(editMode.message, editMode.attachments)
        }
    }

    private fun observeSessionChanges() {
        combine(
            _selectedAttachments,
            _editModeMessage,
            _editModeAttachments,
        ) { selected, editMessage, editAttachments ->
            Triple(selected, editMessage, editAttachments)
        }.onEach { (selected, editMessage, editAttachments) ->
            sessionRepository.save(
                selectedAttachments = selected.values.toList(),
                editMode = editMessage?.let { ComposerSessionRepository.EditMode(it, editAttachments) },
            )
        }.launchIn(scope)
    }

    private fun restoreEditMode(message: Message, attachments: List<Attachment>) {
        val fullMessage = channelState.value?.getMessageById(message.id) ?: message
        setMessageInputInternal(message.text, MessageInput.Source.Edit)
        _editModeMessage.value = fullMessage
        _editModeAttachments.value = attachments
        _messageActions.update { it + Edit(fullMessage) }
        syncAttachments()
    }

    /**
     * Called when the input changes and the internal state needs to be updated.
     *
     * @param value Current state value.
     */
    public fun setMessageInput(value: String) {
        setMessageInputInternal(value, MessageInput.Source.External)
    }

    private fun setMessageInputInternal(value: String, source: MessageInput.Source) {
        if (_messageInput.value.text == value) return
        if (dismissedLinkPreviewUrl != null &&
            !LinkPattern.find(value)?.value.equals(dismissedLinkPreviewUrl, ignoreCase = true)
        ) {
            dismissedLinkPreviewUrl = null
        }
        _messageInput.value = MessageInput(value, source)
    }

    private suspend fun saveDraftMessage(messageMode: MessageMode) {
        if (!config.draftMessageEnabled) return
        currentDraftId = null
        val inputText = _messageInput.value.text
        // In legacy mode the slash + name + args is encoded inside `text`, so command/args fields
        // would duplicate what's already there and confuse cross-SDK readers.
        val activeCommand = _state.value.activeCommand.takeIf { config.activeCommandEnabled }
        if (inputText.isEmpty() && activeCommand == null) {
            clearDraftMessage(messageMode)
            return
        }
        getDraftMessageOrEmpty(messageMode).let {
            chatClient.createDraftMessage(
                channelType = channelType,
                channelId = channelId,
                message = it.copy(
                    text = inputText,
                    command = activeCommand?.name,
                    args = if (activeCommand != null) inputText else null,
                    showInChannel = _state.value.alsoSendToChannel,
                    replyMessage = (_messageActions.value.firstOrNull { it is Reply } as? Reply)?.message,
                ),
            ).await()
        }
    }

    private fun fetchDraftMessage(messageMode: MessageMode) {
        if (!config.draftMessageEnabled) return
        // Drop any active command + stash carried over from the previous mode so leftover state
        // doesn't block the new mode's reply-action restore or leak into a later cancel-restore.
        _state.update { it.copy(activeCommand = null) }
        discardCommandStash()
        getDraftMessageOrEmpty(messageMode).let { draftMessage ->
            currentDraftId = draftMessage.id
            // Restore the reply action first so command-availability checks see the right action.
            draftMessage.replyMessage
                ?.let { performMessageAction(Reply(it)) }
                ?: run {
                    _messageActions.value = _messageActions.value.filterNot { it is Reply }.toSet()
                }
            val command = restoreDraftCommandIfAvailable(draftMessage.command)
            val inputText = if (command != null) draftMessage.args ?: draftMessage.text else draftMessage.text
            setMessageInputInternal(inputText, MessageInput.Source.DraftMessage)
            setAlsoSendToChannel(draftMessage.showInChannel)
        }
    }

    /**
     * Activates the command from a fetched draft when [Config.activeCommandEnabled] is `true`,
     * the command is known to the channel, and it is available for the current composer action.
     * Updates state directly rather than via [selectCommand] — draft restore must not emit
     * [MessageComposerViewEvent.CommandUnavailable] or trigger the pre-command stash.
     *
     * @param commandName The command name from the draft, or `null` for plain-text drafts.
     * @return The restored [Command], or `null` when no command was applied.
     */
    private fun restoreDraftCommandIfAvailable(commandName: String?): Command? {
        if (!config.activeCommandEnabled) return null
        val name = commandName ?: return null
        val command = commands.firstOrNull { it.name == name } ?: return null
        if (!command.isAvailableFor(activeAction)) return null
        _state.update { it.copy(activeCommand = command) }
        return command
    }

    /**
     * Called when the message mode changes and the internal state needs to be updated.
     *
     * This affects the business logic.
     *
     * @param messageMode The current message mode.
     */
    public fun setMessageMode(messageMode: MessageMode) {
        val previousMode = _state.value.messageMode
        if (isSameMessageMode(previousMode, messageMode)) return
        scope.launch(start = CoroutineStart.UNDISPATCHED) {
            _state.update { it.copy(messageMode = messageMode) }
            saveDraftMessage(previousMode)
            fetchDraftMessage(messageMode)
        }
    }

    private fun isSameMessageMode(old: MessageMode, new: MessageMode): Boolean = when {
        old is MessageMode.Normal && new is MessageMode.Normal -> true
        old is MessageMode.MessageThread && new is MessageMode.MessageThread ->
            old.parentMessage.id == new.parentMessage.id
        else -> false
    }

    /**
     * Called when the "Also send as a direct message" checkbox is checked or unchecked.
     *
     * @param alsoSendToChannel If the message will be shown in the channel after it is sent.
     */
    public fun setAlsoSendToChannel(alsoSendToChannel: Boolean) {
        _state.update { it.copy(alsoSendToChannel = alsoSendToChannel) }
    }

    /**
     * Handles selected [messageAction]. We only have three actions we can react to in the composer:
     * - [ThreadReply] - We change the message mode so we can send the message to a thread.
     * - [Reply] - We need to reply to a message and set up the reply UI.
     * - [Edit] - We need to change the [messageInput] to the message we want to edit and change the UI to
     * match the editing action.
     *
     * @param messageAction The newly selected action.
     */
    public fun performMessageAction(messageAction: MessageAction) {
        val activeCommand = _state.value.activeCommand
        if (config.activeCommandEnabled &&
            activeCommand != null &&
            !activeCommand.isAvailableFor(messageAction)
        ) {
            _events.tryEmit(MessageComposerViewEvent.CancelCommandRequired(messageAction))
            return
        }
        when (messageAction) {
            is ThreadReply -> setMessageMode(MessageMode.MessageThread(messageAction.message))
            is Reply ->
                _messageActions.value =
                    (_messageActions.value.filterNot { it is Reply } + messageAction).toSet()

            is Edit -> {
                setMessageInputInternal(messageAction.message.text, MessageInput.Source.Edit)
                _editModeMessage.value = messageAction.message
                _editModeAttachments.value = messageAction.message.attachments
                _messageActions.update { it + messageAction }
                syncAttachments()
            }

            else -> Unit
        }
    }

    /**
     * Dismisses all message actions from the UI and clears the input if [isInEditMode] is true.
     */
    public fun dismissMessageActions() {
        if (isInEditMode) {
            setMessageInputInternal("", MessageInput.Source.Default)
            _editModeMessage.value = null
            _editModeAttachments.value = emptyList()
            syncAttachments()
        }

        _messageActions.value = emptySet()
    }

    /**
     * Adds [attachments] to the staged list, preserving insertion order.
     *
     * Each attachment is keyed by its [EXTRA_SOURCE_URI] if present, or by a deterministic
     * fallback derived from [Attachment.name] and [Attachment.mimeType]. If a key is already
     * present, its value is updated in place without changing its position.
     *
     * @param attachments The attachments to stage.
     */
    public fun addAttachments(attachments: List<Attachment>) {
        _selectedAttachments.update { current ->
            LinkedHashMap(current).also { updated ->
                attachments.forEach { attachment ->
                    updated[attachment.attachmentKey()] = attachment
                }
            }
        }
        syncAttachments()
    }

    /**
     * Removes [attachment] from the staged list.
     *
     * Searches picker selections first (by key), then edit-mode attachments, then the recording
     * attachment. Removes from the first list that contains it.
     *
     * @param attachment The attachment to remove.
     */
    public fun removeAttachment(attachment: Attachment) {
        val key = attachment.attachmentKey()
        when {
            _selectedAttachments.value.containsKey(key) -> {
                _selectedAttachments.update { LinkedHashMap(it).also { map -> map.remove(key) } }
            }
            _editModeAttachments.value.any(attachment::equals) -> {
                _editModeAttachments.update { it.filterNot(attachment::equals) }
            }
            _recordingAttachment.value == attachment -> {
                _recordingAttachment.value = null
            }
        }
        syncAttachments()
    }

    /**
     * Removes all staged attachments whose URI string key is contained in [uris].
     *
     * @param uris The URI string keys to remove.
     */
    public fun removeAttachmentsByUris(uris: Set<String>) {
        if (uris.isEmpty()) return
        _selectedAttachments.update { current -> LinkedHashMap(current).also { it.keys.removeAll(uris) } }
        syncAttachments()
    }

    /**
     * Removes all staged attachments and updates the composer state.
     * Clears picker selections, edit-mode base attachments, and any completed recording attachment.
     */
    public fun clearAttachments() {
        _editModeAttachments.value = emptyList()
        _selectedAttachments.value = linkedMapOf()
        _recordingAttachment.value = null
        syncAttachments()
    }

    /**
     * Creates a poll with the given [createPollParams].
     *
     * @param createPollParams Configuration for creating a poll.
     */
    public fun createPoll(createPollParams: CreatePollParams, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.sendPoll(
            channelType = channelType,
            channelId = channelId,
            createPollParams = createPollParams,
        ).enqueue { onResult(it) }
    }

    /**
     * Clears all the data from the input — text, attachments, validation errors, actions, and active command.
     * In thread mode, [MessageComposerState.alsoSendToChannel] is preserved until the user toggles it;
     * otherwise it is reset.
     */
    public fun clearData() {
        logger.i { "[clearData]" }
        dismissMessageActions()
        scope.launch { clearDraftMessage(_state.value.messageMode) }
        _messageInput.value = MessageInput()
        clearAttachments()
        discardCommandStash()
        clearActiveCommand()
        linkPreviewJob?.cancel()
        dismissedLinkPreviewUrl = null
        _state.update { it.copy(validationErrors = emptyList()) }
        if (!isInThread) {
            _state.update { it.copy(alsoSendToChannel = false) }
        }
    }

    private suspend fun clearDraftMessage(messageMode: MessageMode) {
        if (!config.draftMessageEnabled) return
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
    public fun sendMessage(
        message: Message,
        callback: Call.Callback<Message>,
        resolveAttachments: (suspend (List<Attachment>) -> List<Attachment>)? = null,
    ) {
        logger.i { "[sendMessage] message.attachments.size: ${message.attachments.size}" }
        val blocking = detectBlockingCommand(message.text, activeAction)
        if (blocking != null) {
            _events.tryEmit(blocking)
            return
        }
        val activeMessage = activeAction?.message ?: message
        val currentUserId = chatClient.getCurrentUser()?.id

        if (isInEditMode && !activeMessage.isModerationError(currentUserId)) {
            if (activeMessage.text == message.text && activeMessage.attachments == message.attachments) {
                logger.i { "[sendMessage] No changes in the message, skipping edit." }
                clearData()
                return
            }
            clearData()
            enqueueEditMessage(
                message = message.copy(skipEnrichUrl = shouldSkipEnrichUrl(message)),
                callback = callback,
                resolveAttachments = resolveAttachments,
            )
            return
        }

        if (activeMessage.isModerationError(currentUserId)) {
            chatClient.deleteMessage(activeMessage.id, true).enqueue()
        }
        val preparedMessage = message.copy(
            showInChannel = isInThread && _state.value.alsoSendToChannel,
            skipEnrichUrl = shouldSkipEnrichUrl(message),
        )
        clearData()

        if (resolveAttachments != null) {
            scope.launch {
                val resolved = resolveAttachments(preparedMessage.attachments)
                enqueueSendMessage(preparedMessage.copy(attachments = resolved), callback)
            }
        } else {
            enqueueSendMessage(preparedMessage, callback)
        }
    }

    private fun enqueueEditMessage(
        message: Message,
        callback: Call.Callback<Message>,
        resolveAttachments: (suspend (List<Attachment>) -> List<Attachment>)?,
    ) {
        scope.launch {
            val resolvedMessage = resolveAttachments?.let { resolve ->
                message.copy(attachments = resolve(message.attachments))
            } ?: message
            chatClient.editMessage(channelType, channelId, resolvedMessage).enqueue(callback)
        }
    }

    private fun enqueueSendMessage(
        message: Message,
        callback: Call.Callback<Message>,
    ) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        chatClient.sendMessage(channelType, channelId, message)
            .doOnStart(scope) { loadLatestMessagesIfNeeded() }
            .doOnResult(scope) { result ->
                result.onSuccessSuspend { resultMessage ->
                    if (channelState.value?.channelConfig?.value?.markMessagesPending == false) {
                        chatClient.markMessageRead(
                            channelType = channelType,
                            channelId = channelId,
                            messageId = resultMessage.id,
                        ).await()
                    }
                }
            }
            .enqueue(callback)
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
        val fullText = if (config.activeCommandEnabled) {
            _state.value.activeCommand?.let { "/${it.name} $message" } ?: message
        } else {
            message
        }
        val trimmedMessage = fullText.trim()
        val activeMessage = activeAction?.message ?: Message()
        val replyMessage = (activeAction as? Reply)?.message
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
                replyMessageId = replyMessage?.id,
                replyTo = replyMessage,
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
        _state.update { it.copy(selectedMentions = emptySet()) }
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
            saveDraftMessage(_state.value.messageMode)
            scope.cancel()
        }
    }

    private fun syncAttachments() {
        _state.update {
            it.copy(
                attachments = _editModeAttachments.value +
                    _selectedAttachments.value.values.toList() +
                    listOfNotNull(_recordingAttachment.value),
            )
        }
        handleValidationErrors()
    }

    /**
     * Checks the current input for validation errors.
     */
    private fun handleValidationErrors() {
        _state.update {
            it.copy(validationErrors = messageValidator.validateMessage(_messageInput.value.text, it.attachments))
        }
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
        _state.update { it.copy(selectedMentions = selectedMentions.toSet()) }
    }

    /**
     * Switches the message composer to the command input mode.
     *
     * Sets [MessageComposerState.activeCommand] and clears the text input so the user can type
     * the command arguments. The full `/command args` string is assembled in [buildNewMessage].
     *
     * When [Config.activeCommandEnabled] is `true`, any pre-command input value, picker-selected
     * attachments, and mention selections are stashed so [clearActiveCommand] can restore them if
     * the user dismisses the command. Re-selecting a command while one is already active does not
     * overwrite an existing stash (in-command input is command-specific and not preserved across
     * command switches).
     *
     * When [Config.activeCommandEnabled] is `true` and the command is not available for the
     * current composer action (edit mode or a moderation command during reply), emits a
     * [MessageComposerViewEvent.CommandUnavailable] on [events] and returns without changing the
     * active command. Legacy mode skips this check and sets the command unconditionally.
     *
     * @param command The command that was selected.
     */
    public fun selectCommand(command: Command) {
        val action = activeAction
        if (config.activeCommandEnabled && !command.isAvailableFor(action)) {
            if (action != null) _events.tryEmit(MessageComposerViewEvent.CommandUnavailable(action))
            return
        }
        if (config.activeCommandEnabled && commandStash == null) {
            stashPreCommandState()
        }
        _state.update { it.copy(activeCommand = command) }
        setMessageInputInternal(
            value = if (config.activeCommandEnabled) "" else "/${command.name} ",
            source = MessageInput.Source.CommandSelected,
        )
        _inputFocusEvents.tryEmit(Unit)
    }

    /**
     * Dismisses the active command, clearing [MessageComposerState.activeCommand].
     *
     * When a pre-command stash exists (populated by [selectCommand] under
     * [Config.activeCommandEnabled]), the stashed input, attachments, and mentions are restored
     * and any text typed inside command mode is discarded.
     * When no stash exists, the input is reset to empty.
     */
    public fun clearActiveCommand() {
        _state.update { it.copy(activeCommand = null) }
        if (!restorePreCommandStateIfAny()) {
            setMessageInputInternal("", MessageInput.Source.Default)
        }
    }

    private fun stashPreCommandState() {
        val currentText = _messageInput.value.text
        // Pure command triggers ("/", "/gi", "/mute ") must stash as empty — restoring them on
        // cancel would re-fire the popup or auto-select and make the chip uncancelable.
        val isPureTrigger = CommandText.isTrigger(currentText) || CommandText.isAutoSelectTrigger(currentText)
        val stashedInput = if (isPureTrigger) "" else currentText
        commandStash = CommandStash(
            input = stashedInput,
            attachments = LinkedHashMap(_selectedAttachments.value),
            recordingAttachment = _recordingAttachment.value,
            mentions = selectedMentions.toSet(),
        )
        _selectedAttachments.value = linkedMapOf()
        _recordingAttachment.value = null
        selectedMentions.clear()
        _state.update { it.copy(selectedMentions = emptySet()) }
        syncAttachments()
    }

    private fun restorePreCommandStateIfAny(): Boolean {
        val stash = commandStash ?: return false
        discardCommandStash()
        setMessageInputInternal(stash.input, MessageInput.Source.Default)
        _selectedAttachments.value = LinkedHashMap(stash.attachments)
        _recordingAttachment.value = stash.recordingAttachment
        selectedMentions.clear()
        selectedMentions.addAll(stash.mentions)
        _state.update { it.copy(selectedMentions = selectedMentions.toSet()) }
        syncAttachments()
        return true
    }

    private fun discardCommandStash() {
        commandStash = null
    }

    private data class CommandStash(
        val input: String,
        val attachments: Map<String, Attachment>,
        val recordingAttachment: Attachment?,
        val mentions: Set<Mention>,
    )

    /**
     * Toggles the visibility of the command suggestion list popup.
     */
    public fun toggleCommandsVisibility() {
        _state.update {
            val showCommands = it.commandSuggestions.isEmpty()
            it.copy(
                commandSuggestions = if (showCommands) {
                    commands.orderedForComposer()
                } else {
                    emptyList()
                },
            )
        }
    }

    /**
     * Sorts by availability when [Config.activeCommandEnabled] is `true`; returns the list
     * unchanged in legacy mode.
     */
    private fun List<Command>.orderedForComposer(): List<Command> =
        if (config.activeCommandEnabled) sortedByAvailability(activeAction) else this

    /**
     * Dismisses the suggestions popup above the message composer.
     */
    public fun dismissSuggestionsPopup() {
        _state.update {
            it.copy(
                mentionSuggestions = emptyList(),
                commandSuggestions = emptyList(),
            )
        }
    }

    /**
     * Starts audio recording and moves [MessageComposerState.recording] state
     * from [RecordingState.Idle] to [RecordingState.Hold].
     */
    public fun startRecording() {
        scope.launch {
            audioRecordingController.startRecording()
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
     * Completes audio recording and updates the [MessageComposerState.attachments] list.
     *
     * @param onComplete Optional callback invoked with the result of the recording once the recording has been
     * finalized. On success, the recorded [Attachment] is added to the attachment list before the callback
     * is invoked, so callers can safely build and send a message using the received attachment.
     */
    public fun completeRecording(onComplete: ((Result<Attachment>) -> Unit)? = null) {
        scope.launch {
            if (onComplete != null) {
                val result = audioRecordingController.completeRecordingSync()
                if (result is Result.Success) {
                    addAttachments(listOf(result.value))
                }
                onComplete(result)
            } else {
                audioRecordingController.completeRecording()
            }
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
                _recordingAttachment.value = recording
                syncAttachments()
                sendMessage(buildNewMessage(_messageInput.value.text, _state.value.attachments), callback = {})
            }
        }
    }

    /**
     * Shows the mention suggestion list popup if necessary.
     */
    private fun handleMentionSuggestions() {
        val currentInput = _messageInput.value
        if (currentInput.source == MessageInput.Source.MentionSelected) {
            logger.v { "[handleMentionSuggestions] rejected (messageInput came from mention selection)" }
            _state.update { it.copy(mentionSuggestions = emptyList()) }
            return
        }
        val inputText = currentInput.text
        scope.launch(DispatcherProvider.IO) {
            val suggestion = mentionSuggester.typingSuggestion(inputText)
            logger.v { "[handleMentionSuggestions] suggestion: $suggestion" }
            val result = if (suggestion != null) {
                userLookupHandler.handleUserLookup(suggestion.text)
            } else {
                emptyList()
            }
            withContext(DispatcherProvider.Main) {
                _state.update { it.copy(mentionSuggestions = result) }
            }
        }
    }

    /**
     * Shows the command suggestion list popup if necessary.
     *
     * Under [Config.activeCommandEnabled], typing a command trigger while in edit mode suppresses
     * the popup and emits a [MessageComposerViewEvent.CommandUnavailable]; suggestions are also
     * sorted by availability. Legacy mode always shows the popup and preserves server ordering.
     */
    private fun handleCommandSuggestions() {
        if (tryAutoSelectCommand(messageText)) return
        if (suppressSuggestionsInEditMode()) return
        _state.update { it.copy(commandSuggestions = filteredSuggestionsFor(messageText)) }
    }

    /**
     * When [Config.activeCommandEnabled] is `true` and the composer is in edit mode, suppresses
     * the suggestions popup for a command trigger and emits a
     * [MessageComposerViewEvent.CommandUnavailable].
     *
     * @return `true` when suppression fired; callers should skip the default suggestion handling.
     */
    private fun suppressSuggestionsInEditMode(): Boolean {
        if (!config.activeCommandEnabled) return false
        val action = activeAction as? Edit ?: return false
        if (!CommandText.isTrigger(messageText)) return false
        val prefix = messageText.removePrefix("/")
        if (commands.none { it.name.startsWith(prefix) }) return false
        _state.update { it.copy(commandSuggestions = emptyList()) }
        _events.tryEmit(MessageComposerViewEvent.CommandUnavailable(action))
        return true
    }

    private fun filteredSuggestionsFor(text: String): List<Command> {
        if (!CommandText.isTrigger(text)) return emptyList()
        val prefix = text.removePrefix("/")
        return commands.filter { it.name.startsWith(prefix) }.orderedForComposer()
    }

    /**
     * Detects a `/{name} ` pattern in [text] matching a channel command and enters command mode
     * for it via [selectCommand]. Gated on [Config.activeCommandEnabled] — legacy mode keeps the
     * text-based flow.
     *
     * @return `true` when a command was auto-selected; callers should skip their default suggestion
     * handling to avoid redundant work.
     */
    private fun tryAutoSelectCommand(text: String): Boolean {
        if (!config.activeCommandEnabled) return false
        val name = CommandText.autoSelectName(text) ?: return false
        val command = commands.firstOrNull { it.name == name } ?: return false
        selectCommand(command)
        return true
    }

    /**
     * Returns a [MessageComposerViewEvent.CommandUnavailable] when [text] starts with a command
     * name that is unavailable for [action], or `null` when the message is safe to send. Gated on
     * [Config.activeCommandEnabled] — legacy mode sends raw text verbatim.
     *
     * The `activeCommand != null` / unavailable-for-action combination is prevented by
     * [performMessageAction]'s guard, so only the typed-text path can reach this code.
     */
    private fun detectBlockingCommand(
        text: String,
        action: MessageAction?,
    ): MessageComposerViewEvent? {
        if (!config.activeCommandEnabled) return null
        val currentAction = action ?: return null
        val name = CommandText.inlineCommandName(text) ?: return null
        val command = commands.firstOrNull { it.name == name } ?: return null
        if (command.isAvailableFor(currentAction)) return null
        return MessageComposerViewEvent.CommandUnavailable(currentAction)
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
                _state.update { it.copy(coolDownTime = timeRemaining) }
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
     * Resolves and displays the link preview for the first URL in the current input.
     * Skips enrichment when the feature is disabled or the URL was explicitly dismissed.
     */
    private suspend fun handleLinkPreview() {
        val url = LinkPattern.find(messageText)?.value
        logger.v { "[handleLinkPreview] url: $url" }
        val preview = url
            ?.takeIf { config.linkPreviewEnabled && !it.equals(dismissedLinkPreviewUrl, ignoreCase = true) }
            ?.let { chatClient.enrichPreview(it).await().getOrNull() }
        logger.v { "[handleLinkPreview] preview: ${preview?.originUrl}" }
        _state.update { it.copy(linkPreview = preview) }
    }

    private fun loadLatestMessagesIfNeeded() {
        if (isInThread) return
        val endReached = channelState.value?.endOfNewerMessages?.value ?: true
        if (endReached) return
        logger.d { "[loadLatestMessagesIfNeeded] loading latest messages" }
        chatClient.loadNewestMessages(channelCid, messageLimit = 30).enqueue()
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

    private fun ChatClient.enrichPreview(url: String): Call<LinkPreview> =
        enrichUrl(url.addSchemeToUrlIfNeeded()).map { LinkPreview(url, it) }

    internal companion object {

        /**
         * The character used to start a mention.
         */
        private const val MENTION_START_SYMBOL: String = "@"

        /**
         * Recognises the shapes of command text the composer cares about.
         */
        private object CommandText {

            private val Trigger = Pattern.compile("^/[a-z]*$")
            private val AutoSelectTrigger = Pattern.compile("^/([a-z]+) $")
            private val InlineCommand = Pattern.compile("^/([a-z]+)(?:\\s|$)")

            /** `true` for a popup trigger in progress, e.g. `/`, `/gi`. */
            fun isTrigger(text: String): Boolean = Trigger.matcher(text).find()

            /** `true` for the exact auto-select form `/name ` (space-terminated). */
            fun isAutoSelectTrigger(text: String): Boolean = AutoSelectTrigger.matcher(text).matches()

            /** The command name from an auto-select trigger, or `null` when [text] isn't one. */
            fun autoSelectName(text: String): String? =
                AutoSelectTrigger.matcher(text).takeIf { it.matches() }?.group(1)

            /** The leading command name from [text] (`/name` optionally followed by whitespace). */
            fun inlineCommandName(text: String): String? =
                InlineCommand.matcher(text).takeIf { it.find() }?.group(1)
        }

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
     * @param linkPreviewEnabled If link previews are enabled.
     * @param draftMessageEnabled If draft messages are enabled.
     * @param activeCommandEnabled When `true`, the active command is state and the input holds
     * only its arguments; a pre-command draft is stashed for [clearActiveCommand]. When `false`,
     * the `/name ` prefix lives in the input and is sent verbatim.
     */
    @InternalStreamChatApi
    public data class Config(
        val maxAttachmentCount: Int = AttachmentConstants.MAX_ATTACHMENTS_COUNT,
        val linkPreviewEnabled: Boolean = false,
        val draftMessageEnabled: Boolean = true,
        val activeCommandEnabled: Boolean = false,
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

    /**
     * Dismisses the current link preview and marks enrichment as skipped.
     * When a message is sent after dismissal, the backend will not enrich its URLs
     * unless the detected URL in the input changes (e.g. the user replaces the link).
     */
    public fun cancelLinkPreview() {
        logger.d { "[cancelLinkPreview] url: ${LinkPattern.find(messageText)?.value}" }
        dismissedLinkPreviewUrl = LinkPattern.find(messageText)?.value
        linkPreviewJob?.cancel()
        _state.update { it.copy(linkPreview = null) }
    }

    /**
     * Determines whether the backend should skip URL enrichment for the given [message].
     *
     * Returns `true` when:
     * - the caller already requested skipping (e.g. integrator override via [Message.skipEnrichUrl]),
     * - the user explicitly dismissed the link preview, or
     * - the message text contains no URLs.
     *
     * @param message The message about to be sent or edited.
     */
    private fun shouldSkipEnrichUrl(message: Message): Boolean =
        message.skipEnrichUrl || dismissedLinkPreviewUrl != null || !LinkPattern.containsMatchIn(message.text)
}

private fun Attachment.sourceUriString(): String? = extraData[EXTRA_SOURCE_URI]?.toString()

private fun Attachment.attachmentKey(): String =
    sourceUriString() ?: "fallback:${name.orEmpty()}:${mimeType.orEmpty()}:$fileSize"
