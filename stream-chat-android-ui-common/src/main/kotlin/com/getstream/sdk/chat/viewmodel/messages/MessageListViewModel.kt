package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
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
    private val messageId: String? = null,
    private val domain: ChatDomain = ChatDomain.instance(),
    private val client: ChatClient = ChatClient.instance(),
) : ViewModel() {
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
    private val _targetMessage: MutableLiveData<Message> = MutableLiveData()
    public val targetMessage: LiveData<Message> = _targetMessage

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

    private var dateSeparatorHandler: DateSeparatorHandler? =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (previousMessage == null) {
                true
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) > (1000 * 60 * 60 * 4)
            }
        }

    private var threadDateSeparatorHandler: DateSeparatorHandler? =
        DateSeparatorHandler { previousMessage: Message?, message: Message ->
            if (previousMessage == null) {
                false
            } else {
                (message.getCreatedAtOrThrow().time - previousMessage.getCreatedAtOrThrow().time) > (1000 * 60 * 60 * 4)
            }
        }

    init {
        stateMerger.addSource(MutableLiveData(State.Loading)) { stateMerger.value = it }

        domain.useCases.watchChannel(cid, MESSAGES_LIMIT).enqueue { channelControllerResult ->
            if (channelControllerResult.isSuccess) {
                val channelController = channelControllerResult.data()
                _channel.addSource(MutableLiveData(channelController.toChannel())) { _channel.value = it }
                val typingIds = Transformations.map(channelController.typing) { (_, idList) -> idList }

                messageListData = MessageListItemLiveData(
                    currentUser,
                    channelController.messages,
                    channelController.reads,
                    typingIds,
                    false,
                    dateSeparatorHandler,
                )
                _reads.addSource(channelController.reads) { _reads.value = it }
                _loadMoreLiveData.addSource(channelController.loadingOlderMessages) { _loadMoreLiveData.value = it }

                if (messageId.isNullOrEmpty()) {
                    stateMerger.apply {
                        addSource(channelController.messagesState) { messageState ->
                            when (messageState) {
                                is ChannelController.MessagesState.NoQueryActive,
                                is ChannelController.MessagesState.Loading,
                                -> value = State.Loading
                                is ChannelController.MessagesState.OfflineNoResults ->
                                    value = State.Result(MessageListItemWrapper())
                                is ChannelController.MessagesState.Result -> {
                                    removeSource(channelController.messagesState)
                                    onNormalModeEntered()
                                }
                            }
                        }
                    }
                } else {
                    domain.useCases.loadMessageById(
                        cid,
                        messageId,
                        MESSAGES_LIMIT,
                        MESSAGES_LIMIT
                    ).enqueue {
                        if (it.isSuccess) {
                            _targetMessage.value = it.data()
                            onNormalModeEntered()
                        } else {
                            stateMerger.value = State.Result(MessageListItemWrapper())
                        }
                    }
                }
            }
        }
    }

    private fun setThreadMessages(threadMessages: LiveData<List<Message>>) {
        threadListData = MessageListItemLiveData(
            currentUser,
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
            is Event.MessageReaction -> {
                onMessageReaction(event.message, event.reactionType, event.enforceUnique)
            }
            is Event.MuteUser -> {
                client.muteUser(event.user.id).enqueue()
            }
            is Event.BlockUser -> {
                val channelClient = client.channel(cid)
                channelClient.shadowBanUser(
                    targetId = event.user.id,
                    reason = null,
                    timeout = null,
                ).enqueue()
            }
            is Event.ReplyMessage -> {
                domain.useCases.setMessageForReply(event.cid, event.repliedMessage).enqueue()
            }
            is Event.AttachmentDownload -> {
                domain.useCases.downloadAttachment.invoke(event.attachment).enqueue()
            }
            is Event.ShowMessage -> {
                domain.useCases.loadMessageById(
                    cid,
                    event.messageId,
                    MESSAGES_LIMIT,
                    MESSAGES_LIMIT
                ).enqueue {
                    if (it.isSuccess) {
                        _targetMessage.value = it.data()
                    }
                }
            }
            is Event.RemoveAttachment -> {
                val attachmentToBeDeleted = event.attachment
                domain.useCases.loadMessageById(
                    cid,
                    event.messageId,
                    MESSAGES_LIMIT,
                    MESSAGES_LIMIT
                ).enqueue {
                    if (it.isSuccess) {
                        val message = it.data()
                        message.attachments.removeAll {
                            if (attachmentToBeDeleted.assetUrl != null) {
                                it.assetUrl == attachmentToBeDeleted.assetUrl
                            } else {
                                it.imageUrl == attachmentToBeDeleted.imageUrl
                            }
                        }
                        domain.useCases.editMessage(message).enqueue()
                    }
                }
            }
        }.exhaustive
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
                    messageListData?.loadingMoreChanged(true)
                    domain.useCases.loadOlderMessages(cid, MESSAGES_LIMIT).enqueue {
                        messageListData?.loadingMoreChanged(false)
                    }
                }
                is Mode.Thread -> {
                    threadListData?.loadingMoreChanged(true)
                    domain.useCases.threadLoadMore(cid, this.parentMessage.id, MESSAGES_LIMIT)
                        .enqueue {
                            threadListData?.loadingMoreChanged(false)
                        }
                }
            }.exhaustive
        }
    }

    private fun onBackButtonPressed() {
        currentMode.run {
            when (this) {
                is Mode.Normal -> {
                    stateMerger.postValue(State.NavigateUp)
                }
                is Mode.Thread -> {
                    onNormalModeEntered()
                }
            }.exhaustive
        }
    }

    private fun onThreadModeEntered(parentMessage: Message) {
        val parentId: String = parentMessage.id
        domain.useCases.getThread(cid, parentId).enqueue { threadControllerResult ->
            if (threadControllerResult.isSuccess) {
                val threadController = threadControllerResult.data()
                currentMode = Mode.Thread(parentMessage)
                setThreadMessages(threadController.messages)
                domain.useCases.threadLoadMore(cid, parentId, MESSAGES_LIMIT).enqueue()
            }
        }
    }

    private fun onMessageReaction(message: Message, reactionType: String, enforceUnique: Boolean) {
        val reaction = Reaction().apply {
            messageId = message.id
            type = reactionType
            score = 1
        }
        val currentUserId = ChatDomain.instance().currentUser.id
        if (message.latestReactions.any { it.type == reactionType && it.user?.id == currentUserId }) {
            domain.useCases.deleteReaction(cid, reaction).enqueue()
        } else {
            domain.useCases.sendReaction(cid, reaction, enforceUnique = enforceUnique).enqueue()
        }
    }

    private fun onNormalModeEntered() {
        currentMode = Mode.Normal
        resetThread()
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
        public data class MessageReaction(
            val message: Message,
            val reactionType: String,
            val enforceUnique: Boolean,
        ) : Event()
        public data class MuteUser(val user: User) : Event()
        public data class BlockUser(val user: User, val cid: String) : Event()
        public data class ReplyMessage(val cid: String, val repliedMessage: Message) : Event()
        public data class AttachmentDownload(val attachment: Attachment) : Event()
        public data class ShowMessage(val messageId: String) : Event()
        public data class RemoveAttachment(val messageId: String, val attachment: Attachment) : Event()
    }

    public sealed class Mode {
        public data class Thread(val parentMessage: Message) : Mode()
        public object Normal : Mode()
    }

    public fun interface DateSeparatorHandler {
        public fun shouldAddDateSeparator(previousMessage: Message?, message: Message): Boolean
    }

    internal companion object {
        const val MESSAGES_LIMIT = 30
    }
}
