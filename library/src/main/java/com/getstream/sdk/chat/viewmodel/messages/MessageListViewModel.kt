package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.utils.exhaustive
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import kotlin.properties.Delegates

class MessageListViewModel @JvmOverloads constructor(
    private val cid: String,
    private val domain: ChatDomain = ChatDomain.instance(),
    private val client: ChatClient = ChatClient.instance()
) : ViewModel() {
    private val loading = MutableLiveData<State>()
    private var threadMessages: LiveData<List<Message>> = MutableLiveData()
    private val messageListData: MessageListItemLiveData
    private var threadListData: MessageListItemLiveData? = null
    private val stateMerger = MediatorLiveData<State>()
    private var currentMode: Mode by Delegates.observable(Mode.Normal as Mode) { _, _, newMode -> mode.postValue(newMode) }
    private var reads: LiveData<List<ChannelUserRead>>

    val mode: MutableLiveData<Mode> = MutableLiveData(currentMode)
    val state: LiveData<State> = stateMerger
    val channel: Channel
    val currentUser: User

    init {
        loading.value = State.Loading

        val result = domain.useCases.watchChannel(cid, MESSAGES_LIMIT).execute()
        val channelController = result.data()
        channel = channelController.toChannel()
        currentUser = domain.currentUser
        reads = channelController.reads

        messageListData = MessageListItemLiveData(
            currentUser,
            channelController.messages,
            reads,
            channelController.typing,
            false,
            ::dateSeparator
        )
        stateMerger.apply {
            addSource(loading) { value = it }
            addSource(messageListData) { value = State.Result(it) }
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
            reads,
            null,
            true,
            ::threadDateSeparator
        )
        threadListData?.let {
            stateMerger.apply {
                removeSource(messageListData)
                addSource(it) { value = State.Result(it) }
            }
        }
    }

    private fun resetThread() {
        threadListData?.let {
            stateMerger.removeSource(it)
        }
        stateMerger.apply {
            addSource(messageListData) { value = State.Result(it) }
        }
    }

    fun onEvent(event: Event) {
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
        currentMode.run {
            when (this) {
                is Mode.Normal -> {
                    domain.useCases.loadOlderMessages(cid, MESSAGES_LIMIT).enqueue()
                }
                is Mode.Thread -> {
                    domain.useCases.threadLoadMore(cid, this.parentMessage.id, MESSAGES_LIMIT).enqueue()
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
        currentMode = Mode.Thread(parentMessage)
        val parentId: String = parentMessage.id
        val threadController = domain.useCases.getThread(cid, parentId).execute().data()
        threadMessages = threadController.messages
        setThreadMessages(threadMessages)
        domain.useCases.threadLoadMore(cid, parentId, MESSAGES_LIMIT).enqueue()
    }

    private fun onNormalModeEntered() {
        currentMode = Mode.Normal
        resetThread()
        threadMessages = MutableLiveData()
    }

    sealed class State {
        object Loading : State()
        data class Result(val messageListItem: MessageListItemWrapper) : State()
        object NavigateUp : State()
    }

    sealed class Event {
        object BackButtonPressed : Event()
        object EndRegionReached : Event()
        object LastMessageRead : Event()
        data class ThreadModeEntered(val parentMessage: Message) : Event()
        data class DeleteMessage(val message: Message) : Event()
        data class FlagMessage(val message: Message) : Event()
        data class GiphyActionSelected(val message: Message, val action: GiphyAction) : Event()
        data class RetryMessage(val message: Message) : Event()
    }

    sealed class Mode {
        data class Thread(val parentMessage: Message) : Mode()
        object Normal : Mode()
    }

    companion object {
        internal const val MESSAGES_LIMIT = 30
    }
}
