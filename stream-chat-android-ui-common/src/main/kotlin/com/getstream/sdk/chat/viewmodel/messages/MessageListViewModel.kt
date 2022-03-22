package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.DateSeparatorHandler
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.TaggedLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.extensions.cancelEphemeralMessage
import io.getstream.chat.android.offline.extensions.getRepliesAsState
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.loadMessageById
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.extensions.setMessageForReply
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import kotlinx.coroutines.flow.map
import kotlin.properties.Delegates
import io.getstream.chat.android.livedata.utils.Event as EventWrapper

/**
 * View model class for [com.getstream.sdk.chat.view.MessageListView].
 * Responsible for updating the list of messages.
 * Can be bound to the view using [MessageListViewModel.bindView] function.
 *
 * @param cid The full channel id, i.e. "messaging:123"
 * @param chatClient Entry point for all low-level operations.
 * @param globalState Global state of OfflinePlugin. Contains information
 * such as the current user, connection state, unread counts etc.
 */
public class MessageListViewModel(
    private val cid: String,
    private val messageId: String? = null,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val globalState: GlobalState = chatClient.globalState,
) : ViewModel() {

    /**
     * Contains a list of messages along with additional
     * information about the message list.
     */
    private var messageListData: MessageListItemLiveData? = null

    /**
     * Contains a list of messages along with additional
     * information about the message list. Sets the
     */
    private var threadListData: MessageListItemLiveData? = null

    /**
     * Represents the current state of the message list
     * that is a product of multiple sources.
     */
    private val stateMerger = MediatorLiveData<State>()

    /**
     * Current message list state.
     * @see State
     */
    public val state: LiveData<State> = stateMerger

    /**
     * Whether the user is viewing a thread.
     * @see Mode
     */
    private var currentMode: Mode by Delegates.observable(Mode.Normal as Mode) { _, _, newMode ->
        _mode.postValue(newMode)
    }

    /**
     * Whether the user is viewing a thread.
     * @see Mode
     */
    private val _mode: MutableLiveData<Mode> = MutableLiveData(currentMode)

    /**
     * Whether the user is viewing a thread.
     * @see Mode
     */
    public val mode: LiveData<Mode> = _mode

    /**
     * Holds information about the number of unread messages
     * by the current user in the given channel.
     */
    private val _reads: MediatorLiveData<List<ChannelUserRead>> = MediatorLiveData()

    /**
     * Holds information about the number of unread messages
     * by the current user in the given channel.
     */
    private val reads: LiveData<List<ChannelUserRead>> = _reads

    /**
     * Emits true if we should load more messages.
     */
    private val _loadMoreLiveData = MediatorLiveData<Boolean>()

    /**
     * Emits true if we should load more messages.
     */
    public val loadMoreLiveData: LiveData<Boolean> = _loadMoreLiveData

    /**
     *  The current channel used to load the message list data.
     */
    private val _channel = MediatorLiveData<Channel>()

    /**
     *  The current channel used to load the message list data.
     */
    public val channel: LiveData<Channel> = _channel

    /**
     * The target message that the list should scroll to.
     * Used when scrolling to a pinned message, a message opened from
     * a push notification or similar.
     */
    private val _targetMessage: MutableLiveData<Message> = MutableLiveData()

    /**
     * The target message that the list should scroll to.
     * Used when scrolling to a pinned message, a message opened from
     * a push notification or similar.
     */
    public val targetMessage: LiveData<Message> = _targetMessage

    /**
     * Emits error events.
     */
    private val _errorEvents: MutableLiveData<EventWrapper<ErrorEvent>> = MutableLiveData()

    /**
     * Emits error events.
     */
    public val errorEvents: LiveData<EventWrapper<ErrorEvent>> = _errorEvents

    /**
     * The currently logged in user.
     */
    public val user: LiveData<User?> = globalState.user.asLiveData()

    /**
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger: TaggedLogger = ChatLogger.get("MessageListViewModel")

    /**
     * Evaluates whether date separators should be added to the message list.
     */
    private var dateSeparatorHandler: DateSeparatorHandler? =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (previousMessage == null) {
                true
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) > (1000 * 60 * 60 * 4)
            }
        }

    /**
     * Evaluates whether thread separators should be added to the message list.
     */
    private var threadDateSeparatorHandler: DateSeparatorHandler? =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) > (1000 * 60 * 60 * 4)
            }
        }

    init {
        initWithOfflinePlugin()
    }

    /**
     * Initializes the ViewModel with offline capabilities using
     * [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
     */
    private fun initWithOfflinePlugin() {
        stateMerger.addSource(MutableLiveData(State.Loading)) { stateMerger.value = it }

        val channelState = chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = DEFAULT_MESSAGES_LIMIT,
            coroutineScope = viewModelScope
        )

        ChatClient.dismissChannelNotifications(
            channelType = channelState.channelType,
            channelId = channelState.channelId
        )

        val channelDataLiveData = channelState.channelData.asLiveData()
        _channel.addSource(channelDataLiveData) {
            _channel.value = channelState.toChannel()
            // Channel should be propagated only once because it's used to initialize MessageListView
            _channel.removeSource(channelDataLiveData)
        }
        val typingIds = channelState.typing.map { (_, idList) -> idList }.asLiveData()

        messageListData = MessageListItemLiveData(
            user,
            channelState.messages.asLiveData(),
            channelState.reads.asLiveData(),
            typingIds,
            false,
            dateSeparatorHandler,
        )
        _reads.addSource(channelState.reads.asLiveData()) { _reads.value = it }
        _loadMoreLiveData.addSource(channelState.loadingOlderMessages.asLiveData()) { _loadMoreLiveData.value = it }

        stateMerger.apply {
            val messagesStateLiveData = channelState.messagesState.asLiveData()
            addSource(messagesStateLiveData) { messageState ->
                when (messageState) {
                    MessagesState.Loading,
                    MessagesState.NoQueryActive,
                    -> value = State.Loading
                    MessagesState.OfflineNoResults -> value = State.Result(MessageListItemWrapper())
                    is MessagesState.Result -> {
                        removeSource(messagesStateLiveData)
                        onNormalModeEntered()
                    }
                }
            }
        }
        messageId.takeUnless { it.isNullOrBlank() }?.let { targetMessageId ->
            stateMerger.observeForever(object : Observer<State> {
                override fun onChanged(state: State?) {
                    if (state is State.Result) {
                        onEvent(Event.ShowMessage(targetMessageId))
                        stateMerger.removeObserver(this)
                    }
                }
            })
        }
    }

    /**
     * Moves the message list into thread mode.
     *
     * @param threadMessages The messages that belong to the thread.
     */
    private fun setThreadMessages(threadMessages: LiveData<List<Message>>) {
        threadListData = MessageListItemLiveData(
            user,
            threadMessages,
            reads,
            null,
            true,
            threadDateSeparatorHandler,
        )
        threadListData?.let { tld ->
            messageListData?.let { mld ->
                stateMerger.apply {
                    removeSource(mld)
                    addSource(tld) { value = State.Result(it) }
                }
            }
        }
    }

    /**
     * Moves the message list into normal mode.
     */
    private fun resetThread() {
        threadListData?.let {
            stateMerger.removeSource(it)
        }
        messageListData?.let {
            stateMerger.addSource(it) { messageListItemWrapper ->
                stateMerger.value = State.Result(messageListItemWrapper)
            }
        }
    }

    /**
     * Handles an [event] coming from the View layer.
     * @see Event
     */
    public fun onEvent(event: Event) {
        when (event) {
            is Event.EndRegionReached -> {
                onEndRegionReached()
            }
            is Event.LastMessageRead -> {
                cid.cidToTypeAndId().let { (channelType, channelId) ->
                    chatClient.markRead(channelType, channelId).enqueue(
                        onError = { chatError ->
                            logger.logE("Could not mark cid: $cid as read. Error message: ${chatError.message}. Cause message: ${chatError.cause?.message}")
                        }
                    )
                }
            }
            is Event.ThreadModeEntered -> {
                onThreadModeEntered(event.parentMessage)
            }
            is Event.BackButtonPressed -> {
                onBackButtonPressed()
            }
            is Event.DeleteMessage -> {
                chatClient.deleteMessage(event.message.id, event.hard)
                    .enqueue(
                        onError = { chatError ->
                            logger.logE(
                                "Could not delete message: ${chatError.message}, Hard: ${event.hard}. Cause: ${chatError.cause?.message}. " +
                                    "If you're using OfflinePlugin, the message should be deleted in the database and " +
                                    "it will be deleted in the backend when the SDK sync its information."
                            )
                        }
                    )
            }
            is Event.FlagMessage -> {
                chatClient.flagMessage(event.message.id).enqueue { result ->
                    event.resultHandler(result)
                    if (result.isError) {
                        logger.logE("Could not flag message: ${result.error().message}")
                        _errorEvents.postValue(EventWrapper(ErrorEvent.FlagMessageError(result.error())))
                    }
                }
            }
            is Event.PinMessage -> {
                chatClient.pinMessage(Message(id = event.message.id)).enqueue(
                    onError = { chatError ->
                        logger.logE("Could not flag message: ${chatError.message}. Cause: ${chatError.cause?.message}")
                        _errorEvents.postValue(EventWrapper(ErrorEvent.PinMessageError(chatError)))
                    }
                )
            }
            is Event.UnpinMessage -> {
                chatClient.unpinMessage(Message(id = event.message.id)).enqueue(
                    onError = { chatError ->
                        logger.logE("Could not unpin message: ${chatError.message}. Cause: ${chatError.cause?.message}")
                        _errorEvents.postValue(EventWrapper(ErrorEvent.UnpinMessageError(chatError)))
                    }
                )
            }
            is Event.GiphyActionSelected -> {
                onGiphyActionSelected(event)
            }
            is Event.RetryMessage -> {
                val (channelType, channelId) = event.message.cid.cidToTypeAndId()
                chatClient.sendMessage(channelType, channelId, event.message)
                    .enqueue(
                        onError = { chatError ->
                            logger.logE("(Retry) Could not send message: ${chatError.message}. Cause: ${chatError.cause?.message}")
                        }
                    )
            }
            is Event.MessageReaction -> {
                onMessageReaction(event.message, event.reactionType, event.enforceUnique)
            }
            is Event.MuteUser -> {
                chatClient.muteUser(event.user.id).enqueue(
                    onError = { chatError ->
                        logger.logE("Could not mute user: ${chatError.message}")
                        _errorEvents.postValue(EventWrapper(ErrorEvent.MuteUserError(chatError)))
                    }
                )
            }
            is Event.UnmuteUser -> {
                chatClient.unmuteUser(event.user.id).enqueue(
                    onError = { chatError ->
                        logger.logE("Could not unmute user: ${chatError.message}")
                        _errorEvents.postValue(EventWrapper(ErrorEvent.UnmuteUserError(chatError)))
                    }
                )
            }
            is Event.BlockUser -> {
                val channelClient = chatClient.channel(cid)
                channelClient.shadowBanUser(
                    targetId = event.user.id,
                    reason = null,
                    timeout = null,
                ).enqueue(
                    onError = { chatError ->
                        logger.logE("Could not block user: ${chatError.message}")
                        _errorEvents.postValue(EventWrapper(ErrorEvent.BlockUserError(chatError)))
                    }
                )
            }
            is Event.ReplyMessage -> {
                chatClient.setMessageForReply(event.cid, event.repliedMessage).enqueue(
                    onError = { chatError ->
                        logger.logE("Could not reply message: ${chatError.message}. Cause: ${chatError.cause?.message}")
                    }
                )
            }
            is Event.DownloadAttachment -> {
                event.downloadAttachmentCall().enqueue(
                    onError = { chatError ->
                        logger.logE("Attachment download error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                    }
                )
            }
            is Event.ShowMessage -> {
                val message = messageListData?.value
                    ?.items
                    ?.asSequence()
                    ?.filterIsInstance<MessageListItem.MessageItem>()
                    ?.find { messageItem -> messageItem.message.id == event.messageId }
                    ?.message

                if (message != null) {
                    _targetMessage.value = message!!
                } else {
                    chatClient.loadMessageById(
                        cid,
                        event.messageId,
                        DEFAULT_MESSAGES_LIMIT,
                        DEFAULT_MESSAGES_LIMIT
                    ).enqueue { result ->
                        if (result.isSuccess) {
                            _targetMessage.value = result.data()
                        } else {
                            val error = result.error()
                            logger.logE("Could not load message: ${error.message}. Cause: ${error.cause?.message}")
                        }
                    }
                }
            }
            is Event.RemoveAttachment -> {
                val attachmentToBeDeleted = event.attachment
                chatClient.loadMessageById(
                    cid,
                    event.messageId,
                    DEFAULT_MESSAGES_LIMIT,
                    DEFAULT_MESSAGES_LIMIT
                ).enqueue { result ->
                    if (result.isSuccess) {
                        val message = result.data()
                        message.attachments.removeAll { attachment ->
                            if (attachmentToBeDeleted.assetUrl != null) {
                                attachment.assetUrl == attachmentToBeDeleted.assetUrl
                            } else {
                                attachment.imageUrl == attachmentToBeDeleted.imageUrl
                            }
                        }

                        chatClient.updateMessage(message).enqueue(
                            onError = { chatError ->
                                logger.logE("Could not edit message to remove its attachments: ${chatError.message}. Cause: ${chatError.cause?.message}")
                            }
                        )
                    } else {
                        logger.logE("Could not load message: ${result.error()}")
                    }
                }
            }
            is Event.ReplyAttachment -> {
                val messageId = event.repliedMessageId
                val cid = event.cid
                chatClient.loadMessageById(
                    cid,
                    messageId,
                    DEFAULT_MESSAGES_LIMIT,
                    DEFAULT_MESSAGES_LIMIT
                ).enqueue { result ->
                    if (result.isSuccess) {
                        val message = result.data()
                        onEvent(Event.ReplyMessage(cid, message))
                    } else {
                        val error = result.error()
                        logger.logE("Could not load message to reply: ${error.message}. Cause: ${error.cause?.message}")
                    }
                }
            }
        }
    }

    /**
     * Sets the date separator handler which determines when to add date separators.
     * By default, a date separator will be added if the difference between two messages' dates is greater than 4h.
     *
     * @param dateSeparatorHandler The handler to use. If null, [messageListData] won't contain date separators.
     */
    public fun setDateSeparatorHandler(dateSeparatorHandler: DateSeparatorHandler?) {
        this.dateSeparatorHandler = dateSeparatorHandler
    }

    /**
     * Sets thread date separator handler which determines when to add date separators inside the thread.
     * @see setDateSeparatorHandler
     *
     * @param threadDateSeparatorHandler The handler to use. If null, [messageListData] won't contain date separators.
     */
    public fun setThreadDateSeparatorHandler(threadDateSeparatorHandler: DateSeparatorHandler?) {
        this.threadDateSeparatorHandler = threadDateSeparatorHandler
    }

    /**
     * Handles the send, shuffle and cancel Giphy actions.
     *
     * @param event The type of action the user has selected.
     */
    private fun onGiphyActionSelected(event: Event.GiphyActionSelected) {
        when (event.action) {
            GiphyAction.SEND -> {
                chatClient.sendGiphy(event.message).enqueue(
                    onError = { chatError ->
                        logger.logE(
                            "Could not send giphy for message id: ${event.message.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                        )
                    }
                )
            }
            GiphyAction.SHUFFLE -> {
                chatClient.shuffleGiphy(event.message).enqueue(
                    onError = { chatError ->
                        logger.logE(
                            "Could not shuffle giphy for message id: ${event.message.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                        )
                    }
                )
            }
            GiphyAction.CANCEL -> {
                chatClient.cancelEphemeralMessage(event.message).enqueue(
                    onError = { chatError ->
                        logger.logE(
                            "Could not cancel giphy for message id: ${event.message.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                        )
                    }
                )
            }
        }
    }

    /**
     * Loads more messages if we have reached
     * the oldest message currently loaded.
     */
    private fun onEndRegionReached() {
        currentMode.run {
            when (this) {
                is Mode.Normal -> {
                    messageListData?.loadingMoreChanged(true)
                    chatClient.loadOlderMessages(cid, DEFAULT_MESSAGES_LIMIT).enqueue {
                        messageListData?.loadingMoreChanged(false)
                    }
                }
                is Mode.Thread -> threadLoadMore(this)
            }
        }
    }

    /**
     * Load older messages for the specified thread [Mode.Thread.parentMessage].
     *
     * @param threadMode Current thread mode.
     */
    private fun threadLoadMore(threadMode: Mode.Thread) {
        threadListData?.loadingMoreChanged(true)
        if (threadMode.threadState != null) {
            chatClient.getRepliesMore(
                messageId = threadMode.parentMessage.id,
                firstId = threadMode.threadState.oldestInThread.value?.id ?: threadMode.parentMessage.id,
                limit = DEFAULT_MESSAGES_LIMIT,
            ).enqueue {
                threadListData?.loadingMoreChanged(false)
            }
        } else {
            threadListData?.loadingMoreChanged(false)
            logger.logW("Thread state must be not null for offline plugin thread load more!")
        }
    }

    /**
     * Evaluates whether a navigation event should occur
     * or if we should switch from thread mode back to
     * normal mode.
     */
    private fun onBackButtonPressed() {
        currentMode.run {
            when (this) {
                is Mode.Normal -> {
                    stateMerger.postValue(State.NavigateUp)
                }
                is Mode.Thread -> {
                    onNormalModeEntered()
                }
            }
        }
    }

    /**
     * Handles an event to move to thread mode.
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun onThreadModeEntered(parentMessage: Message) {
        loadThreadWithOfflinePlugin(parentMessage)
    }

    /**
     * Move [currentMode] to [Mode.Thread] and loads thread data using ChatClient directly. The data is observed by
     * using [ThreadState].
     *
     * @param parentMessage The message with the thread we want to observe.
     */
    private fun loadThreadWithOfflinePlugin(parentMessage: Message) {
        val state = chatClient.getRepliesAsState(parentMessage.id, DEFAULT_MESSAGES_LIMIT)
        currentMode = Mode.Thread(parentMessage, state)
        setThreadMessages(state.messages.asLiveData())
    }

    /**
     * Handles reacting to messages while taking into account if unique reactions are enforced.
     *
     * @param message The message the user is reacting to.
     * @param reactionType The exact reaction type.
     * @param enforceUnique Whether the user is able to leave multiple reactions.
     */
    private fun onMessageReaction(message: Message, reactionType: String, enforceUnique: Boolean) {
        val reaction = Reaction().apply {
            messageId = message.id
            type = reactionType
            score = 1
        }
        if (message.ownReactions.any { it.type == reactionType }) {
            chatClient.deleteReaction(
                messageId = message.id,
                reactionType = reaction.type,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.logE(
                        "Could not delete reaction for message with id: ${reaction.messageId} Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                    )
                }
            )
        } else {
            chatClient.sendReaction(
                enforceUnique = enforceUnique,
                reaction = reaction,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.logE(
                        "Could not send reaction for message with id: ${reaction.messageId} Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                    )
                }
            )
        }
    }

    /**
     * Called when upon initialization or exiting thread mode.
     */
    private fun onNormalModeEntered() {
        currentMode = Mode.Normal
        resetThread()
    }

    /**
     * The current state of the message list.
     */
    public sealed class State {

        /**
         * Signifies that the message list is loading.
         */
        public object Loading : State()

        /**
         * Signifies that the messages have successfully loaded.
         *
         * @param messageListItem Contains the requested messages along with additional information.
         */
        public data class Result(val messageListItem: MessageListItemWrapper) : State()

        /**
         * Signals that the View should navigate back.
         */
        public object NavigateUp : State()
    }

    /**
     * Represents events coming from the View class.
     */
    public sealed class Event {

        /**
         * When the back button is pressed.
         */
        public object BackButtonPressed : Event()

        /**
         * When the oldest loaded message in the list has been reached.
         */
        public object EndRegionReached : Event()

        /**
         * When the newest message in the channel has been read.
         */
        public object LastMessageRead : Event()

        /**
         * When the users enters thread mode.
         *
         * @param parentMessage The original message the thread was spun off from.
         */
        public data class ThreadModeEntered(val parentMessage: Message) : Event()

        /**
         * When the user deletes a message.
         *
         * @param message The message to be deleted.
         * @param hard Determines whether the message will be soft or hard deleted.
         *
         * Soft delete - Deletes the message on the client side but it remains available
         * via server-side export functions.
         * Hard delete - message is deleted everywhere.
         */
        public data class DeleteMessage(val message: Message, val hard: Boolean = false) : Event()

        /**
         * When the user flags a message.
         *
         * @param message The message to be flagged.
         * @param resultHandler Lambda function that handles the result of the operation.
         * e.g. if the message was successfully flagged or not.
         */
        public data class FlagMessage(val message: Message, val resultHandler: ((Result<Flag>) -> Unit) = { }) : Event()

        /**
         * When the user pins a message.
         *
         * @param message The message to be pinned.
         */
        public data class PinMessage(val message: Message) : Event()

        /**
         * When the user unpins a message.
         *
         * @param message The message to be unpinned.
         */
        public data class UnpinMessage(val message: Message) : Event()

        /**
         * When the user selects a Giphy message.
         * e.g. send, shuffle or cancel.
         *
         * @param message The Giphy message.
         * @param action The Giphy action. e.g. send, shuffle or cancel.
         */
        public data class GiphyActionSelected(val message: Message, val action: GiphyAction) : Event()

        /**
         * Retry sending a message that has failed to send.
         *
         * @param message The message that will be re-sent.
         */
        public data class RetryMessage(val message: Message) : Event()

        /**
         * When the user leaves a reaction to a message.
         *
         * @param message The message the user is reacting to
         * @param reactionType The reaction type.
         * @param enforceUnique Whether the user is able to leave multiple reactions.
         */
        public data class MessageReaction(
            val message: Message,
            val reactionType: String,
            val enforceUnique: Boolean,
        ) : Event()

        /**
         * When the user mutes a user.
         *
         * @param user The user to be muted.
         */
        public data class MuteUser(val user: User) : Event()

        /**
         * When the user unmutes a user.
         *
         * @param user The user to be unmuted.
         */
        public data class UnmuteUser(val user: User) : Event()

        /**
         * When the user blocks another user.
         *
         * @param user The user to be blocked.
         * @param cid The full channel id, i.e. "messaging:123".
         */
        public data class BlockUser(val user: User, val cid: String) : Event()

        /**
         * When the user replies to a message.
         *
         * @param cid The full channel id, i.e. "messaging:123".
         * @param repliedMessage The message the user is replying to.
         */
        public data class ReplyMessage(val cid: String, val repliedMessage: Message) : Event()

        /**
         * When the user is replying to a single attachment.
         * Usually triggered when replying from gallery.
         *
         * @param cid The full channel id, i.e. "messaging:123".
         * @param repliedMessageId The message the user is replying to.
         */
        public data class ReplyAttachment(val cid: String, val repliedMessageId: String) : Event()

        /**
         * When the user downloads an attachment.
         *
         * @param downloadAttachmentCall A handler for downloading that returns a [Call]
         * with the option of asynchronous operation.
         */
        public data class DownloadAttachment(val downloadAttachmentCall: () -> Call<Unit>) : Event()

        /**
         * When we need to display a particular message to the user.
         * Usually triggered by clicking on pinned messages or navigation
         * to the message list via push notifications.
         *
         * @param messageId The id of the message we need to navigate to.
         */
        public data class ShowMessage(val messageId: String) : Event()

        /**
         * When the user removes an attachment from a message that was previously sent.
         *
         * @param messageId The message from which an attachment will be deleted.
         * @param attachment The attachment to be deleted.
         */
        public data class RemoveAttachment(val messageId: String, val attachment: Attachment) : Event()
    }

    /**
     * The modes the message list can be in.
     */
    public sealed class Mode {

        /**
         * Thread mode. Occurs when a user enters a thread.
         *
         * @param parentMessage The original message all messages in a thread are replying to.
         * @param threadState Contains information about the state of the thread, such as
         * if we are loading older messages, have reached the oldest message available, etc.
         */
        public data class Thread(val parentMessage: Message, val threadState: ThreadState? = null) : Mode()

        /**
         * Normal mode. When the user is not participating in a thread.
         */
        public object Normal : Mode()
    }

    /**
     * A class designed for error event propagation.
     *
     * @param chatError Contains the original [Throwable] along with a message.
     */
    public sealed class ErrorEvent(public open val chatError: ChatError) {

        /**
         * When an error occurs while muting a user.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class MuteUserError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while unmuting a user.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class UnmuteUserError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while flagging a message.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class FlagMessageError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while blocking a user.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class BlockUserError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while pinning a message.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class PinMessageError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * When an error occurs while unpinning a message.
         *
         * @param chatError Contains the original [Throwable] along with a message.
         */
        public data class UnpinMessageError(override val chatError: ChatError) : ErrorEvent(chatError)
    }

    /**
     * A SAM designed to evaluate if a date separator should be added between messages.
     */
    public fun interface DateSeparatorHandler {
        public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean
    }

    internal companion object {
        /**
         * The default limit of messages to load.
         */
        const val DEFAULT_MESSAGES_LIMIT = 30
    }
}
