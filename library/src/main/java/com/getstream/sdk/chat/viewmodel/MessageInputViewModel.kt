package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListItemLiveData
import io.getstream.chat.android.client.logger.ChatLogger.Companion.get
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class MessageInputViewModel(private val cid: String, private val chatDomain: ChatDomain = ChatDomain.instance()) : ViewModel() {
	val members: LiveData<List<Member>>
	val commands: LiveData<List<Command>>
	private var unreadCount: LiveData<Int>

	/**
	 * The numbers of users currently watching this channel
	 */
	private var watcherCount: LiveData<Int>

	/**
	 * The list of users currently typing
	 */
	private var typingUsers: LiveData<List<User>> = MutableLiveData()


	private val channelController: ChannelController = chatDomain.useCases.watchChannel.invoke(cid, 30).execute().data()
	private var online: LiveData<Boolean> = MutableLiveData(false)
	private var activeThread = MutableLiveData<Message?>(null)
	private var threadMessages: LiveData<List<Message>>
	private var threadLoadingMore: LiveData<Boolean>? = null

	private var initialized: LiveData<Boolean> = MutableLiveData(false)
	private var reachedEndOfPagination: LiveData<Boolean>
	private var reachedEndOfPaginationThread: LiveData<Boolean>? = null
	private var loading: LiveData<Boolean> = MutableLiveData(false)
	private var messageListScrollUp = MutableLiveData(false)
	private var loadingMore: LiveData<Boolean> = MutableLiveData(false)
	private var editMessage: MutableLiveData<Message?>
	private var channelState = MutableLiveData<Channel>()

	private var reads: LiveData<List<ChannelUserRead>> = MutableLiveData()
	private var entities: MessageListItemLiveData
	private val logger = get(MessageInputViewModel::class.java.name)

	@get:Deprecated("")
	val channel: Channel
		get() = channelController.toChannel()

	fun getEditMessage(): LiveData<Message?> {
		return editMessage
	}

	fun setEditMessage(editMessage: Message?) {
		this.editMessage.postValue(editMessage)
	}

	fun getMessageListScrollUp(): LiveData<Boolean> {
		return messageListScrollUp
	}

	val isEditing: Boolean
		get() = getEditMessage().value != null

	// region Thread
	fun setActiveThread(parentMessage: Message) {
		activeThread.postValue(parentMessage)
	}

	fun getActiveThread(): LiveData<Message?> {
		return activeThread
	}

	val isThread: Boolean
		get() = activeThread.value != null

	fun resetThread() {
		val thread = activeThread.value
		if (thread != null) {
			activeThread.postValue(null)
			threadMessages = MutableLiveData()
		}
		reachedEndOfPaginationThread = MutableLiveData(false)
		threadLoadingMore = MutableLiveData(false)
	}

	override fun onCleared() {
		super.onCleared()
		logger.logI("onCleared")
	}

	fun sendMessage(messageText: String) {
		val message = Message(cid = cid, text = messageText)
		if (isThread) {
			val parentMessageId = getActiveThread().value !!.id
			message.parentId = parentMessageId
		}
		stopTyping()
		message.channel = channelController.toChannel()
		chatDomain.useCases.sendMessage.invoke(message).execute()
	}

	fun sendMessageWithAttachments(message: String, attachmentFiles: List<File>) {
		GlobalScope.launch(Dispatchers.IO) {
			chatDomain.useCases.sendMessageWithAttachments(cid, Message(cid = cid, text = message), attachmentFiles).execute()
		}
	}

	/**
	 * Edit message
	 *
	 * @param message the Message sent
	 */
	fun editMessage(message: Message?) {
		stopTyping()
		chatDomain.useCases.editMessage.invoke(message !!).execute()
	}

	/**
	 * keystroke - First of the typing.start and typing.stop events based on the users keystrokes.
	 * Call this on every keystroke
	 */
	@Synchronized
	fun keystroke() {
		if (isThread) return
		chatDomain.useCases.keystroke.invoke(cid).execute()
	}

	/**
	 * stopTyping - Sets last typing to null and sends the typing.stop event
	 */
	fun stopTyping() {
		if (isThread) return
		chatDomain.useCases.stopTyping.invoke(cid).execute()
	}

	init {
		channelState.postValue(channelController.toChannel())

		// connect livedata objects
		initialized = chatDomain.initialized
		online = chatDomain.online
		watcherCount = channelController.watcherCount
		typingUsers = channelController.typing
		reads = channelController.reads
		members = channelController.members
		commands = map(channelState) { it.config.commands }
		typingUsers = channelController.typing
		loading = channelController.loading
		loadingMore = channelController.loadingOlderMessages
		reachedEndOfPagination = channelController.endOfOlderMessages
		unreadCount = channelController.unreadCount
		threadMessages = MutableLiveData()
		logger.logI("instance created")
		val currentUser = chatDomain.currentUser
		entities = MessageListItemLiveData(currentUser, channelController.messages, threadMessages, typingUsers, reads)
		editMessage = MutableLiveData()
	}
}