package io.getstream.chat.android.compose.viewModel.messages.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.handlers.ClipboardHandler
import io.getstream.chat.android.compose.state.messages.*
import io.getstream.chat.android.compose.state.messages.list.*
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.thread.ThreadController
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling all the business logic & state for the list of messages.
 * */
class MessageListViewModel(
    val chatClient: ChatClient,
    val chatDomain: ChatDomain,
    private val channelId: String,
    private val messageLimit: Int = 0,
    private val clipboardHandler: ClipboardHandler
) : ViewModel() {

    /**
     * State handler for the UI, which holds all the information the UI needs to render messages.
     *
     * It chooses between [threadMessagesState] and [messagesState] based on if we're in a thread or not.
     * */
    val currentMessagesState: MessagesState
        get() = if (isInThread) threadMessagesState else messagesState

    /**
     * State of the screen, for the [Normal] [messageMode].
     * */
    private var messagesState: MessagesState by mutableStateOf(MessagesState())

    /**
     * State of the screen, for the [Thread] [messageMode].
     * */
    private var threadMessagesState: MessagesState by mutableStateOf(MessagesState())

    /**
     * Holds the current [MessageMode] that's used for the messages list. [Normal] by default.
     * */
    var messageMode by mutableStateOf<MessageMode>(Normal)
        private set

    /**
     * The information for the current [Channel].
     * */
    var channel: Channel by mutableStateOf(Channel())
        private set

    /**
     * Set of currently active [MessageAction]s. Used to show things like edit, reply, delete and
     * similar actions.
     * */
    var messageActions by mutableStateOf<Set<MessageAction>>(mutableSetOf())
        private set

    /**
     * Gives us information if we're currently in the [Thread] message mode.
     * */
    val isInThread: Boolean
        get() = messageMode is Thread

    /**
     * Gives us information if we're showing the selected message overlay.
     * */
    val isShowingOverlay: Boolean
        get() = messagesState.selectedMessage != null || threadMessagesState.selectedMessage != null

    /**
     * Gives us information about the online state of the device.
     * */
    val isOnline: StateFlow<Boolean>
        get() = chatDomain.online

    /**
     * Gives us information about the logged in user state.
     * */
    val user: StateFlow<User?>
        get() = chatDomain.user

    /**
     * [Job] that's used to keep the thread data loading operations. We cancel it when the user goes
     * out of the thread state.
     * */
    private var threadJob: Job? = null

    /**
     * Represents the last loaded message in the list, for comparison when determining the
     * [NewMessageState] for the screen.
     * */
    private var lastLoadedMessage: Message? = null

    /**
     * Sets up the core data loading operations - such as observing the current channel and loading
     * messages and other pieces of information.
     * */
    fun start() {
        viewModelScope.launch {
            val result =
                chatDomain.watchChannel(channelId, messageLimit)
                    .await()

            if (result.isSuccess) {
                val controller = result.data()

                observeConversation(controller)
            } else {
                result.error().cause?.printStackTrace()
                onQueryError()
            }
        }
    }

    /**
     * Starts observing the current conversation using the [controller]. We observe the
     * 'loadingOlderMessages', 'messagesState', 'user' and 'endOfOlderMessages' states from our
     * controller, as well as build the `newMessageState` using [getNewMessageState] and combine it
     * into a [MessagesState] that holds all the information required for the screen.
     *
     * @param controller - The controller for the channel with the current [channelId].
     * */
    private fun observeConversation(controller: ChannelController) {
        viewModelScope.launch {
            controller.messagesState
                .combine(user) { state, user ->
                    when (state) {
                        is ChannelController.MessagesState.NoQueryActive,
                        is ChannelController.MessagesState.Loading ->
                            messagesState.copy(isLoading = true)
                        is ChannelController.MessagesState.OfflineNoResults -> messagesState.copy(
                            isLoading = false,
                            messages = emptyList()
                        )
                        is ChannelController.MessagesState.Result -> {
                            messagesState.copy(
                                isLoading = false,
                                messages = filterDeletedMessages(state.messages),
                                isLoadingMore = false,
                                endOfMessages = controller.endOfOlderMessages.value,
                                currentUser = user
                            )
                        }
                    }
                }.collect { newState ->
                    val hasNewMessage = lastLoadedMessage != null
                        && messagesState.messages.isNotEmpty()
                        && newState.messages.lastOrNull()?.id != lastLoadedMessage?.id

                    messagesState = if (hasNewMessage) {
                        newState.copy(newMessageState = getNewMessageState(newState.messages.lastOrNull()))
                    } else {
                        newState
                    }
                    lastLoadedMessage = newState.messages.lastOrNull()
                    setChanel(controller.toChannel())
                }
        }
    }

    /**
     * Sets the current channel, used to show info in the UI.
     * */
    private fun setChanel(channel: Channel) {
        this.channel = channel
    }

    /**
     * Used to filter messages deleted by other users.
     *
     * @param messages - List of all messages.
     * @return - Filtered messages.
     * */
    private fun filterDeletedMessages(messages: List<Message>): List<Message> {
        val currentUser = user.value

        return messages.filter { !(it.user.id != currentUser?.id && it.deletedAt != null) }
    }

    /**
     * Builds the [NewMessageState] for the UI, whenever the message state changes. This is used to
     * allow us to show the user a floating button giving them the option to scroll to the bottom
     * when needed.
     *
     * @param lastMessage - Last message in the list, used for comparison.
     * */
    private fun getNewMessageState(lastMessage: Message?): NewMessageState? {
        val lastLoadedMessage = lastLoadedMessage
        val currentUser = user.value

        return if (lastMessage != null && lastLoadedMessage != null && lastMessage.id != lastLoadedMessage.id) {
            if (lastMessage.user.id == currentUser?.id) {
                MyOwn
            } else {
                Other
            }
        } else {
            null
        }
    }

    /**
     * If there's an error, we just set the current state to be empty - 'isLoading' as false and
     * 'messages' as an empty list.
     * */
    private fun onQueryError() {
        messagesState = messagesState.copy(isLoading = false, messages = emptyList())
    }

    /**
     * Triggered when the user loads more data by reaching the end of the current messages.
     * */
    fun onLoadMore() {
        val messageMode = messageMode

        if (messageMode is Thread) {
            threadMessagesState = threadMessagesState.copy(isLoadingMore = true)
            chatDomain.threadLoadMore(channelId, messageMode.parentMessage.id, messageLimit)
                .enqueue()
        } else {
            messagesState = messagesState.copy(isLoadingMore = true)
            chatDomain.loadOlderMessages(channelId, messageLimit).enqueue()
        }
    }

    /**
     * Triggered when the user long taps on and selects a message. This updates the internal state
     * and allows our UI to re-render it and show an overlay.
     *
     * @param message - The selected message.
     * */
    fun onMessageSelected(message: Message?) {
        if (isInThread) {
            threadMessagesState = threadMessagesState.copy(selectedMessage = message)
        } else {
            messagesState = messagesState.copy(selectedMessage = message)
        }
    }

    /**
     * Triggered when the user taps on a message that has a thread active. This changes the current
     * [messageMode] to [Thread] and loads the thread data.
     *
     * @param message - The selected message with a thread.
     * */
    fun onMessageThreadClick(message: Message) {
        this.messageMode = Thread(message)

        loadThread(message)
    }

    /**
     * Used to dismiss a specific message action, such as delete, reply, edit or something similar.
     *
     * @param messageAction - The action to dismiss.
     * */
    fun dismissMessageAction(messageAction: MessageAction) {
        this.messageActions = messageActions - messageAction
    }

    /**
     * Dismisses all message actions, when we cancel them in the rest of the UI.
     * */
    fun dismissAllMessageActions() {
        this.messageActions = emptySet()
    }

    /**
     * Triggered when the user selects a new message action, in the message overlay.
     *
     * We first remove the overlay, after which we consume the event and based on the type of the event,
     * we do different things, such as starting a thread & loading thread data, showing delete or flag
     * events and dialogs, copying the message, muting users and more.
     *
     * @param messageAction - The action the user chose.
     * */
    fun onMessageAction(messageAction: MessageAction) {
        removeOverlay()

        when (messageAction) {
            is ThreadReply -> {
                messageActions = messageActions + Reply(messageAction.message)
                loadThread(messageAction.message)
            }
            is Delete, is Flag -> {
                messageActions = messageActions + messageAction
            }
            is Copy -> copyMessage(messageAction.message)
            is MuteUser -> muteUser(messageAction.message.user)
            is React -> reactToMessage(messageAction.reaction, messageAction.message)
            else -> {
                // no op, custom user action
            }
        }
    }

    /**
     * Loads the thread data and changes the current [messageMode] to be [Thread].
     *
     * The data is loaded by fetching the [ThreadController] first, based on the [parentMessage],
     * after which we observe specific data from the thread.
     *
     * @param parentMessage - The message with the thread we want to observe.
     * */
    private fun loadThread(parentMessage: Message) {
        messageMode = Thread(parentMessage)

        chatDomain.getThread(channelId, parentMessage.id).enqueue { result ->
            if (result.isSuccess) {
                val controller = result.data()

                observeThreadMessages(controller)
            } else {
                messageMode = Normal
            }
        }
    }

    /**
     * Observes the currently active thread data, based on our [ThreadController]. In process, this
     * creates a [threadJob] that we can cancel once we leave the thread.
     *
     * The data consists of the 'loadingOlderMessages', 'messages' and 'endOfOlderMessages' states,
     * that are combined into one [MessagesState].
     *
     * @param controller - The controller for the active thread.
     * */
    private fun observeThreadMessages(controller: ThreadController) {
        threadJob = viewModelScope.launch {
            controller.loadingOlderMessages
                .combine(controller.messages) { loadingOlderMessages, messages ->
                    loadingOlderMessages to messages
                }
                .combine(controller.endOfOlderMessages) { (loadingOlderMessages, messages), endOfOlderMessages ->
                    threadMessagesState.copy(
                        isLoading = false,
                        messages = messages,
                        isLoadingMore = loadingOlderMessages,
                        endOfMessages = endOfOlderMessages
                    )
                }.collect { newState -> threadMessagesState = newState }
        }
    }

    /**
     * Removes the delete actions from our [messageActions], as well as the overlay, before deleting
     * the selected message.
     *
     * @param message - Message to delete.
     * */
    fun deleteMessage(message: Message) {
        messageActions = messageActions - messageActions.filterIsInstance<Delete>()
        removeOverlay()

        chatDomain.deleteMessage(message).enqueue()
    }

    /**
     * Copies the message content using the [ClipboardHandler] we provide. This can copy both
     * attachment and text messages.
     *
     * @param message - Message with the content to copy.
     * */
    private fun copyMessage(message: Message) {
        clipboardHandler.copyMessage(message)
    }

    /**
     * Mutes the user that sent a particular message.
     *
     * @param user - The user to mute.
     * */
    private fun muteUser(user: User) {
        chatClient.muteUser(user.id).enqueue()
    }

    /**
     * Triggered when the user chooses the [React] action for the currently selected message. If the
     * message already has that reaction, from the current user, we remove it. Otherwise we add a new
     * reaction.
     *
     * @param reaction - The reaction to add or remove.
     * @param message - The currently selected message.
     * */
    private fun reactToMessage(reaction: Reaction, message: Message) {
        if (message.ownReactions.any { it.messageId == reaction.messageId && it.type == reaction.type }) {
            chatDomain.deleteReaction(channelId, reaction).enqueue()
        } else {
            // todo check if we need to enforce unique
            chatDomain.sendReaction(channelId, reaction, true).enqueue()
        }
    }

    /**
     * Leaves the thread we're in and resets the state of the [messageMode] and both of the [MessagesState]s.
     *
     * It also cancels the [threadJob] to clean up resources.
     * */
    fun leaveThread() {
        messageMode = Normal
        messagesState = messagesState.copy(selectedMessage = null)
        threadMessagesState = MessagesState()
        threadJob?.cancel()
    }

    /**
     * Resets the [MessagesState]s, to remove the message overlay, by setting 'selectedMessage' to null.
     * */
    fun removeOverlay() {
        threadMessagesState = threadMessagesState.copy(selectedMessage = null)
        messagesState = messagesState.copy(selectedMessage = null)
    }

    /**
     * Clears the [NewMessageState] from our UI state, after the user taps on the "Scroll to bottom"
     * or "New Message" actions in the list or simply scrolls to the bottom.
     * */
    fun onScrolledToBottom() {
        threadMessagesState = threadMessagesState.copy(newMessageState = null)
        messagesState = messagesState.copy(newMessageState = null)
    }
}