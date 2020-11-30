package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.utils.extensions.exhaustive
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import kotlin.properties.Delegates

/**
 * View model class for [com.getstream.sdk.chat.view.MessageListView].
 * Responsible for updating the list of messages.
 * Can be bound to the view using [MessageListViewModel.bindView] function.
 * @param cid the full channel id, i.e. "messaging:123"
 * @param domain entry point for all livedata & offline operations
 * @param client entry point for all low-level operations
 */
public class MessageListViewModel @JvmOverloads constructor(
    private val cid: String,
    private val domain: ChatDomain = ChatDomain.instance(),
    private val client: ChatClient = ChatClient.instance()
) : ViewModel() {
    private var threadMessages: LiveData<List<Message>> = MutableLiveData()
    private var messageListData: MessageListItemLiveData? = null
    private var threadListData: MessageListItemLiveData? = null
    private val stateMerger = MediatorLiveData<State>()
    private var currentMode: Mode by Delegates.observable(Mode.Normal as Mode) { _, _, newMode -> mode.postValue(newMode) }
    private val _reads: MediatorLiveData<List<ChannelUserRead>> = MediatorLiveData()
    private val reads: LiveData<List<ChannelUserRead>> = _reads
    private val _loadMoreLiveData = MediatorLiveData<Boolean>()
    public val loadMoreLiveData: LiveData<Boolean> = _loadMoreLiveData
    private val _channel = MediatorLiveData<Channel>()
    public val channel: LiveData<Channel> = _channel

    /**
     * Whether the user is viewing a thread
     * @see Mode
     */
    public val mode: MutableLiveData<Mode> = MutableLiveData(currentMode)

    /**
     * Current message list state
     * @see State
     */
    public val state: LiveData<State> = stateMerger
    public val currentUser: User = domain.currentUser

    init {

        domain.useCases.watchChannel(cid, MESSAGES_LIMIT).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                _channel.addSource(MutableLiveData(channelController.toChannel())) { _channel.value = it }
                val typingIds = Transformations.map(channelController.typing) { (_, idList) -> idList }
                messageListData = MessageListItemLiveData(
                    currentUser,
                    channelController.messages,
                    channelController.oldMessages,
                    channelController.reads,
                    typingIds,
                    false,
                    ::dateSeparator
                ).also { mld ->
                    stateMerger.apply {
                        addSource(MutableLiveData<State>(State.Loading)) { value = it }
                        addSource(mld) { value = State.Result(it) }
                    }
                }
                _reads.addSource(channelController.reads) { _reads.value = it }
                _loadMoreLiveData.addSource(channelController.loadingOlderMessages) { _loadMoreLiveData.value = it }
            }
        }
    }

    private fun dateSeparator(previous: Message?, message: Message): Boolean {
        return if (previous == null) {
            true
        } else {
            (message.getCreatedAtOrThrow().time - previous.getCreatedAtOrThrow().time) > (1000 * 60 * 60 * 4)
        }
    }

    private fun threadDateSeparator(previous: Message?, message: Message): Boolean {
        return if (previous == null) {
            false
        } else {
            (message.getCreatedAtOrThrow().time - previous.getCreatedAtOrThrow().time) > (1000 * 60 * 60 * 4)
        }
    }

    private fun setThreadMessages(threadMessages: LiveData<List<Message>>) {
        threadListData = MessageListItemLiveData(
            currentUser,
            threadMessages,
            MutableLiveData(),
            reads,
            null,
            true,
            ::threadDateSeparator
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

    private fun resetThread() {
        threadListData?.let {
            stateMerger.removeSource(it)
        }
        messageListData?.let {
            stateMerger.addSource(it) { stateMerger.value = State.Result(it) }
        }
    }

    /**
     * Handles an [event] coming from the View layer
     * @see Event
     */
    public fun onEvent(event: Event) {
        when (event) {
            is Event.EndRegionReached -> {
                onEndRegionReached()
            }
            is Event.LastMessageRead -> {
                domain.useCases.markRead(cid).enqueue()
            }
            is Event.ThreadModeEntered -> {
                onThreadModeEntered(event.parentMessage)
            }
            is Event.BackButtonPressed -> {
                onBackButtonPressed()
            }
            is Event.DeleteMessage -> {
                domain.useCases.deleteMessage(event.message).enqueue()
            }
            is Event.FlagMessage -> {
                client.flagMessage(event.message.id).enqueue()
            }
            is Event.GiphyActionSelected -> {
                onGiphyActionSelected(event)
            }
            is Event.RetryMessage -> {
                domain.useCases.sendMessage(event.message).enqueue()
            }
        }.exhaustive
    }

    private fun onGiphyActionSelected(event: Event.GiphyActionSelected) {
        when (event.action) {
            GiphyAction.SEND -> {
                domain.useCases.sendGiphy(event.message).enqueue()
            }
            GiphyAction.SHUFFLE -> {
                domain.useCases.shuffleGiphy(event.message).enqueue()
            }
            GiphyAction.CANCEL -> {
                domain.useCases.cancelMessage(event.message).enqueue()
            }
        }.exhaustive
    }

    private fun onEndRegionReached() {
        messageListData?.loadingMoreChanged(true)
        currentMode.run {
            when (this) {
                is Mode.Normal -> {
                    domain.useCases.loadOlderMessages(cid, MESSAGES_LIMIT).enqueue {
                        messageListData?.loadingMoreChanged(false)
                    }
                }
                is Mode.Thread -> {
                    domain.useCases.threadLoadMore(cid, this.parentMessage.id, MESSAGES_LIMIT)
                        .enqueue {
                            messageListData?.loadingMoreChanged(false)
                        }
                }
            }.exhaustive
        }
    }

    private fun onBackButtonPressed() {
        currentMode.run {
            when (this) {
                is Mode.Normal -> { stateMerger.postValue(State.NavigateUp) }
                is Mode.Thread -> { onNormalModeEntered() }
            }.exhaustive
        }
    }

    private fun onThreadModeEntered(parentMessage: Message) {
        val parentId: String = parentMessage.id
        domain.useCases.getThread(cid, parentId).enqueue { threadControllerResult ->
            if (threadControllerResult.isSuccess) {
                val threadController = threadControllerResult.data()
                currentMode = Mode.Thread(parentMessage)
                threadMessages = threadController.messages
                setThreadMessages(threadMessages)
                domain.useCases.threadLoadMore(cid, parentId, MESSAGES_LIMIT).enqueue()
            }
        }
    }

    private fun onNormalModeEntered() {
        currentMode = Mode.Normal
        resetThread()
        threadMessages = MutableLiveData()
    }

    public sealed class State {
        public object Loading : State()
        public data class Result(val messageListItem: MessageListItemWrapper) : State()
        public object NavigateUp : State()
    }

    public sealed class Event {
        public object BackButtonPressed : Event()
        public object EndRegionReached : Event()
        public object LastMessageRead : Event()
        public data class ThreadModeEntered(val parentMessage: Message) : Event()
        public data class DeleteMessage(val message: Message) : Event()
        public data class FlagMessage(val message: Message) : Event()
        public data class GiphyActionSelected(val message: Message, val action: GiphyAction) : Event()
        public data class RetryMessage(val message: Message) : Event()
    }

    public sealed class Mode {
        public data class Thread(val parentMessage: Message) : Mode()
        public object Normal : Mode()
    }

    internal companion object {
        const val MESSAGES_LIMIT = 30
    }
}
