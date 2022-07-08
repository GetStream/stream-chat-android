package io.getstream.chat.android.common.messagelist

import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Mode
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.TaggedLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.common.state.MessageMode
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.extensions.getRepliesAsState
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.loadNewerMessages
import io.getstream.chat.android.offline.extensions.loadNewestMessages
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.thread.ThreadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

public class MessageListController(
    private val channelId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) {

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
            cid = channelId,
            messageLimit = MessageListViewModel.DEFAULT_MESSAGES_LIMIT,
            coroutineScope = scope
        )

    // TODO
    private val _mode: MutableStateFlow<MessageMode> = MutableStateFlow(MessageMode.Normal)
    public val mode: StateFlow<MessageMode> = _mode

    public val isInThread: Boolean
        get() = _mode.value is MessageMode.MessageThread

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
     * The logger used to print to errors, warnings, information
     * and other things to log.
     */
    private val logger: TaggedLogger = ChatLogger.get("MessageListController")

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
                chatClient.loadNewestMessages(channelId, messageLimit).enqueue { result ->
                    if (result.isSuccess) {
                        scrollToBottom()
                    } else {
                        val error = result.error()
                        logger.logE("Could not load newest messages. Cause: ${error.cause?.message}")
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

        chatClient.loadNewerMessages(channelId, baseMessageId, messageLimit)
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
        if (chatClient.globalState.isOffline()) return

        _mode.value.run {
            when (this) {
                is MessageMode.Normal -> {
                    if (channelState.value?.endOfOlderMessages?.value == true) return
                    chatClient.loadOlderMessages(channelId, messageLimit).enqueue {
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
            logger.logW("Thread state must be not null for offline plugin thread load more!")
        }
    }

    // TODO
    public fun enterThreadMode(parentMessage: Message, onMessagesResult: (ThreadState) -> Unit) {
        val state = chatClient.getRepliesAsState(parentMessage.id, MessageListViewModel.DEFAULT_MESSAGES_LIMIT)
        _mode.value = MessageMode.MessageThread(parentMessage, state)
        onMessagesResult(state)
    }

    // TODO
    public fun enterNormalMode() {
        _mode.value = MessageMode.Normal
    }

    internal companion object {
        /**
         * The default limit of messages to load.
         */
        const val DEFAULT_MESSAGES_LIMIT = 30

        const val SEPARATOR_TIME = 1000 * 60 * 60 * 4
    }
}