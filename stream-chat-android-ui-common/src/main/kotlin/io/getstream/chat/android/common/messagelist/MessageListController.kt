package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Mode
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.common.state.React
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.offline.extensions.cancelEphemeralMessage
import io.getstream.chat.android.offline.extensions.getRepliesAsState
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.loadNewerMessages
import io.getstream.chat.android.offline.extensions.loadNewestMessages
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import io.getstream.logging.StreamLog
import io.getstream.logging.TaggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

public class MessageListController(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) {

    /**
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger: TaggedLogger = StreamLog.getLogger("MessageListController")

    /**
     * Creates a [CoroutineScope] that allows us to cancel the ongoing work when the parent
     * ViewModel is disposed.
     *
     * We use the [DispatcherProvider.Immediate] variant here to make sure the UI updates don't go through to process of
     * dispatching events. This fixes several bugs where the input state breaks when deleting or typing really fast.
     */
    private val scope = CoroutineScope(DispatcherProvider.Immediate)

    /**
     * Holds information about the current channel and is actively updated.
     */
    public val channelState: StateFlow<ChannelState?> =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = MessageListViewModel.DEFAULT_MESSAGES_LIMIT,
            coroutineScope = scope
        )

    /**
     * Holds information about the abilities the current user
     * is able to exercise in the given channel.
     *
     * e.g. send messages, delete messages, etc...
     * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
     */
    public val ownCapabilities: StateFlow<Set<String>> = channelState.filterNotNull()
        .flatMapLatest { it.channelData }
        .map { it.ownCapabilities }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = setOf()
        )

    /**
     * The information for the current [Channel].
     */
    private val _channel: MutableStateFlow<Channel> = MutableStateFlow(Channel())
    public val channel: StateFlow<Channel> = _channel

    /**
     * Holds the current [MessageMode] that's used for the messages list. [MessageMode.Normal] by default.
     */
    private val _mode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)
    public val mode: StateFlow<MessageMode> = _mode

    /**
     * Gives us information if we're currently in the [Thread] message mode.
     */
    public val isInThread: Boolean
        get() = _mode.value is MessageMode.MessageThread

    /**
     * The list of typing users.
     */
    private val _typingUsers: MutableStateFlow<List<User>> = MutableStateFlow(listOf())
    public val typingUsers: StateFlow<List<User>> = _typingUsers

    init {
        observeTypingUsers()
    }

    /**
     * Starts observing the list of typing users.
     */
    private fun observeTypingUsers() {
        scope.launch {
            channelState.filterNotNull().flatMapLatest { it.typing }.collectLatest {
                _typingUsers.value = it.users
            }
        }
    }

    /**
     * Sets the current channel, used to show info in the UI.
     */
    public fun setCurrentChannel(channel: Channel) {
        _channel.value = channel
    }

    /**
     * When the user clicks the scroll to bottom button we need to take the user to the bottom of the newest
     * messages. If the messages are not loaded we need to load them first and then scroll to the bottom of the
     * list.
     */
    public fun scrollToBottom(messageLimit: Int = DEFAULT_MESSAGES_LIMIT, scrollToBottom: () -> Unit) {
        if (_mode.value is MessageMode.MessageThread) {
            scrollToBottom()
        } else {
            if (channelState.value?.endOfNewerMessages?.value == true) {
                scrollToBottom()
            } else {
                chatClient.loadNewestMessages(cid, messageLimit).enqueue { result ->
                    if (result.isSuccess) {
                        scrollToBottom()
                    } else {
                        val error = result.error()
                        logger.e { "Could not load newest messages. Cause: ${error.cause?.message}" }
                    }
                }
            }
        }
    }

    /**
     * Loads newer messages of a channel following the currently newest loaded message. In case of threads this will
     * do nothing.
     *
     * @param baseMessageId The id of the most new [Message] inside the messages list.
     * @param onResult Callback handler for load newer messages result.
     *
     * @return Whether the data is being loaded or not.
     */
    public fun loadNewerMessages(
        baseMessageId: String,
        messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
        onResult: (Result<Channel>) -> Unit = {},
    ): Boolean {
        if (_mode.value !is MessageMode.Normal ||
            chatClient.globalState.isOffline() ||
            channelState.value?.endOfNewerMessages?.value == true
        ) return false

        chatClient.loadNewerMessages(cid, baseMessageId, messageLimit)
            .enqueue { result -> onResult(result) }

        return true
    }

    /**
     * Loads more messages if we have reached
     * the oldest message currently loaded.
     */
    public fun loadOlderMessages(
        messageLimit: Int = DEFAULT_MESSAGES_LIMIT,
        onResult: (Result<List<Message>>) -> Unit = {},
    ) {
        if (chatClient.clientState.isOffline) return

        _mode.value.run {
            when (this) {
                is MessageMode.Normal -> {
                    if (channelState.value?.endOfOlderMessages?.value == true) return
                    chatClient.loadOlderMessages(cid, messageLimit).enqueue {
                        onResult(it.map { it.messages })
                    }
                }
                is MessageMode.MessageThread -> threadLoadMore(this, onResult)
            }
        }
    }

    /**
     * Load older messages for the specified thread [Mode.Thread.parentMessage].
     *
     * @param threadMode Current thread mode.
     */
    private fun threadLoadMore(threadMode: MessageMode.MessageThread, onResult: (Result<List<Message>>) -> Unit = {}) {
        if (threadMode.threadState != null) {
            chatClient.getRepliesMore(
                messageId = threadMode.parentMessage.id,
                firstId = threadMode.threadState.oldestInThread.value?.id ?: threadMode.parentMessage.id,
                limit = MessageListViewModel.DEFAULT_MESSAGES_LIMIT,
            ).enqueue { onResult(it) }
        } else {
            onResult(Result.error(ChatError("Thread state must be not null for offline plugin thread load more!")))
            logger.w { "Thread state must be not null for offline plugin thread load more!" }
        }
    }

    /**
     *  Changes the current [_mode] to be [Thread] with [ThreadState] and Loads thread data using ChatClient
     *  directly. The data is observed by using [ThreadState].
     *
     * @param parentMessage The message with the thread we want to observe.
     * @param onMessagesResult Handler when the messages get loaded.
     */
    public fun enterThreadMode(parentMessage: Message, onMessagesResult: (ThreadState) -> Unit) {
        val state = chatClient.getRepliesAsState(parentMessage.id, MessageListViewModel.DEFAULT_MESSAGES_LIMIT)
        _mode.value = MessageMode.MessageThread(parentMessage, state)
        onMessagesResult(state)
    }

    /**
     * Leaves the thread we're in.
     */
    public fun enterNormalMode() {
        _mode.value = MessageMode.Normal
    }

    /**
     * Deletes the given [message].
     *
     * @param message Message to delete.
     * @param hard Whether we do a hard delete or not.
     */
    public fun deleteMessage(message: Message, hard: Boolean = false) {
        chatClient.deleteMessage(message.id, hard)
            .enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not delete message: ${chatError.message}, Hard: ${hard}. " +
                            "Cause: ${chatError.cause?.message}. If you're using OfflinePlugin, the message " +
                            "should be deleted in the database and it will be deleted in the backend when " +
                            "the SDK sync its information."
                    }
                }
            )
    }

    /**
     * Marks that the last message in the list was read.
     */
    public fun markLastMessageRead() {
        cid.cidToTypeAndId().let { (channelType, channelId) ->
            chatClient.markRead(channelType, channelId).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not mark cid: $channelId as read. Error message: ${chatError.message}. " +
                            "Cause message: ${chatError.cause?.message}"
                    }
                }
            )
        }
    }

    /**
     * Flags the selected message.
     *
     * @param message Message to delete.
     * @param onResult Handler that notifies the flag message result.
     */
    public fun flagMessage(message: Message, onResult: (Result<Flag>) -> Unit = {}) {
        chatClient.flagMessage(message.id).enqueue { result ->
            onResult(result)
            if (result.isError) {
                logger.e { "Could not flag message: ${result.error().message}" }
            }
        }
    }

    /**
     * Pins or unpins the message from the current channel based on its state.
     *
     * @param message The message to update the pin state of.
     * @param onResult Handler that propagates the result of the pin or unpin action.
     */
    public fun updateMessagePin(message: Message, onResult: (Result<Message>) -> Unit = {}) {
        if (message.pinned) {
            unpinMessage(message, onResult)
        } else {
            pinMessage(message, onResult)
        }
    }

    /**
     * Pins the message from the current channel.
     *
     * @param message The message to pin.
     * @param onResult Handler that propagates the result of the pin action.
     */
    public fun pinMessage(message: Message, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.pinMessage(message).enqueue { result ->
            onResult(result)
            if (result.isError) {
                logger.e { "Could not pin message: ${result.error().message}. Cause: ${result.error().cause?.message}" }
            }
        }
    }

    /**
     * Unpins the message from the current channel.
     *
     * @param message The message to unpin.
     * @param onResult Handler that propagates the result of the unpin action.
     */
    public fun unpinMessage(message: Message, onResult: (Result<Message>) -> Unit = {}) {
        chatClient.unpinMessage(message).enqueue { result ->
            onResult(result)
            if (result.isError) {
                logger.e {
                    "Could not unpin message: ${result.error().message}. Cause: ${result.error().cause?.message}"
                }
            }
        }
    }

    /**
     * Resends a failed message.
     *
     * @param message The [Message] to be resent.
     */
    public fun resendMessage(message: Message) {
        val (channelType, channelId) = message.cid.cidToTypeAndId()
        chatClient.sendMessage(channelType, channelId, message)
            .enqueue(
                onError = { chatError ->
                    logger.e {
                        "(Retry) Could not send message: ${chatError.message}. " +
                            "Cause: ${chatError.cause?.message}"
                    }
                }
            )
    }

    /**
     * Mutes or unmutes a user for the current user based on the users mute state.
     *
     * @param user The [User] for which to toggle the mute state.
     */
    public fun updateUserMute(user: User) {
        val isUserMuted = chatClient.globalState.muted.value.any { it.target.id == user.id }

        if (isUserMuted) {
            unmuteUser(user)
        } else {
            muteUser(user)
        }
    }

    /**
     * Mutes the given user.
     *
     * @param user The [User] we wish to mute.
     * @param onResult Handler that notifies the result of the mute action.
     */
    public fun muteUser(user: User, onResult: (Result<Mute>) -> Unit = {}) {
        chatClient.muteUser(user.id).enqueue { result ->
            onResult(result)
            if (result.isError) {
                logger.e { "Could not mute user: ${result.error().message}" }
            }
        }
    }

    /**
     * Unmutes the given user.
     *
     * @param user The [User] we wish to unmute.
     * @param onResult Handler that notifies the result of the mute action.
     */
    public fun unmuteUser(user: User, onResult: (Result<Unit>) -> Unit = {}) {
        chatClient.unmuteUser(user.id).enqueue { result ->
            onResult(result)
            if (result.isError) {
                logger.e { "Could not unmute user: ${result.error().message}" }
            }
        }
    }

    /**
     * Triggered when the user chooses the [React] action for the currently selected message. If the
     * message already has that reaction, from the current user, we remove it. Otherwise we add a new
     * reaction.
     *
     * @param reaction The reaction to add or remove.
     * @param message The currently selected message.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     */
    public fun reactToMessage(reaction: Reaction, message: Message, enforceUnique: Boolean) {
        if (message.ownReactions.any { it.type == reaction.type }) {
            chatClient.deleteReaction(
                messageId = message.id,
                reactionType = reaction.type,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not delete reaction for message with id: ${reaction.messageId} " +
                            "Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                    }
                }
            )
        } else {
            chatClient.sendReaction(
                enforceUnique = enforceUnique,
                reaction = reaction,
                cid = cid
            ).enqueue(
                onError = { chatError ->
                    logger.e {
                        "Could not send reaction for message with id: ${reaction.messageId} " +
                            "Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
                    }
                }
            )
        }
    }

    /**
     * Executes one of the actions for the given ephemeral giphy message.
     *
     * @param action The action to be executed.
     * @param message The [Message] containing the giphy.
     */
    public fun performGiphyAction(action: GiphyAction, message: Message) {
        when (action) {
            GiphyAction.SEND -> chatClient.sendGiphy(message)
            GiphyAction.SHUFFLE -> chatClient.shuffleGiphy(message)
            GiphyAction.CANCEL -> chatClient.cancelEphemeralMessage(message)
        }.exhaustive.enqueue(onError = { chatError ->
            logger.e {
                "Could not ${action.name} giphy for message id: ${message.id}. " +
                    "Error: ${chatError.message}. Cause: ${chatError.cause?.message}"
            }
        })
    }

    internal companion object {
        /**
         * The default limit of messages to load.
         */
        const val DEFAULT_MESSAGES_LIMIT = 30
    }
}