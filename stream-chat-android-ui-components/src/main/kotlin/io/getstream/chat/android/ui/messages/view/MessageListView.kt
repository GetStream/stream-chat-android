package io.getstream.chat.android.ui.messages.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.StartStopBuffer
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.EndlessScrollListener
import com.getstream.sdk.chat.view.IMessageListView
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageListViewBinding
import io.getstream.chat.android.ui.messages.adapter.ListenerContainer
import io.getstream.chat.android.ui.messages.adapter.ListenerContainerImpl
import io.getstream.chat.android.ui.messages.adapter.MessageListItemAdapter
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.reactions.ReactionsOverlayDialogFragment
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReadStateClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.UserClickListener
import io.getstream.chat.android.ui.options.MessageOptionsOverlayDialogFragment
import io.getstream.chat.android.ui.options.MessageOptionsView
import io.getstream.chat.android.ui.utils.ReactionType
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import kotlin.math.max
import kotlin.math.min

/**
 * MessageListView renders a list of messages and extends the [RecyclerView]
 * The most common customizations are
 * - Disabling Reactions
 * - Disabling Threads
 * - Customizing the click and longCLick (via the adapter)
 * - The list_item_message template to use (perhaps, multiple ones...?)
 */
public class MessageListView : ConstraintLayout, IMessageListView {

    private companion object {
        const val LOAD_MORE_TRESHOLD = 10
    }

    private var firstVisiblePosition = 0

    private lateinit var style: MessageListViewStyle

    private lateinit var binding: StreamUiMessageListViewBinding

    private var newMessagesTextSingle: String? = null
    private var newMessagesTextPlural: String? = null

    private lateinit var newMessagesBehaviour: NewMessagesBehaviour

    private val buffer: StartStopBuffer<MessageListItemWrapper> = StartStopBuffer()

    private lateinit var adapter: MessageListItemAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var loadingView: View
    private lateinit var loadingViewContainer: ViewGroup
    private lateinit var emptyStateView: View
    private lateinit var emptyStateViewContainer: ViewGroup
    private var lastSeenMessageInChannel: MessageListItem? = null
    private var lastSeenMessageInThread: MessageListItem? = null

    private val defaultChildLayoutParams by lazy {
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
    }

    public var scrollToBottomButtonEnabled: Boolean = true

    private var hasScrolledUp = false

    private var endRegionReachedHandler: () -> Unit = {
        throw IllegalStateException("endRegionReachedHandler must be set.")
    }
    private var lastMessageReadHandler: () -> Unit = {
        throw IllegalStateException("lastMessageReadHandler must be set.")
    }
    private var onMessageEditHandler: (Message) -> Unit = {
        throw IllegalStateException("onMessageEditHandler must be set.")
    }
    private var onMessageDeleteHandler: (Message) -> Unit = {
        throw IllegalStateException("onMessageDeleteHandler must be set.")
    }
    private var onStartThreadHandler: (Message) -> Unit = {
        throw IllegalStateException("onStartThreadHandler must be set.")
    }
    private var onStartThreadListener: (Message) -> Unit = {
        /* Empty */
    }
    private var onMessageFlagHandler: (Message) -> Unit = {
        throw IllegalStateException("onMessageFlagHandler must be set.")
    }
    private var onSendGiphyHandler: (Message, GiphyAction) -> Unit = { _, _ ->
        throw IllegalStateException("onSendGiphyHandler must be set.")
    }
    private var onMessageRetryHandler: (Message) -> Unit = {
        throw IllegalStateException("onMessageRetryHandler must be set.")
    }

    private lateinit var messageOptionsConfiguration: MessageOptionsView.Configuration

    private lateinit var loadMoreListener: EndlessScrollListener

    private lateinit var channel: Channel
    private lateinit var currentUser: User

    /**
     * If you are allowed to scroll up or not
     */
    private var lockScrollUp = true

    private val logger = ChatLogger.get("MessageListView")

    private val DEFAULT_MESSAGE_CLICK_LISTENER =
        MessageClickListener { message ->
            if (message.replyCount > 0) {
                onStartThreadHandler.invoke(message)
                onStartThreadListener.invoke(message)
            }
        }
    private val DEFAULT_MESSAGE_LONG_CLICK_LISTENER =
        MessageLongClickListener { message ->
            context.getFragmentManager()?.let { framentManager ->
                // TODO: pass a real MessageItem instead of mock
                val mockMessageItem = MessageItem(
                    message,
                    positions = listOf(MessageListItem.Position.BOTTOM),
                    isMine = false
                )

                MessageOptionsOverlayDialogFragment
                    .newInstance(mockMessageItem, messageOptionsConfiguration)
                    .show(framentManager, ReactionsOverlayDialogFragment.TAG)
            }
        }

    private val DEFAULT_MESSAGE_RETRY_LISTENER =
        MessageRetryListener { message ->
            onMessageRetryHandler.invoke(message)
        }
    private val DEFAULT_ATTACHMENT_CLICK_LISTENER =
        AttachmentClickListener { message, attachment ->
            ChatUI.instance()
                .navigator
                .navigate(AttachmentDestination(message, attachment, context))
        }
    private val DEFAULT_REACTION_VIEW_CLICK_LISTENER =
        ReactionViewClickListener { message: Message ->
            context.getFragmentManager()?.let {
                // TODO: pass a real MessageItem instead of mock
                val mockMessageItem = MessageItem(
                    message.copy(text = "MOCKED TEXT: ${message.text}").apply {
                        latestReactions.forEach { it.type = ReactionType.LOVE.type }
                        ownReactions.forEach { it.type = ReactionType.LOVE.type }
                    },
                    positions = listOf(MessageListItem.Position.BOTTOM),
                    isMine = false
                )

                ReactionsOverlayDialogFragment.newInstance(mockMessageItem)
                    .apply { setReactionClickListener {} }
                    .show(it, ReactionsOverlayDialogFragment.TAG)
            }
        }
    private val DEFAULT_USER_CLICK_LISTENER = UserClickListener { /* Empty */ }
    private val DEFAULT_READ_STATE_CLICK_LISTENER =
        ReadStateClickListener { reads: List<ChannelUserRead> ->
            // TODO implement
        }
    private val DEFAULT_GIPHY_SEND_LISTENER =
        GiphySendListener { message, action ->
            onSendGiphyHandler.invoke(message, action)
        }

    private val listenerContainer: ListenerContainer = ListenerContainerImpl(
        messageClickListener = DEFAULT_MESSAGE_CLICK_LISTENER,
        messageLongClickListener = DEFAULT_MESSAGE_LONG_CLICK_LISTENER,
        messageRetryListener = DEFAULT_MESSAGE_RETRY_LISTENER,
        attachmentClickListener = DEFAULT_ATTACHMENT_CLICK_LISTENER,
        reactionViewClickListener = DEFAULT_REACTION_VIEW_CLICK_LISTENER,
        userClickListener = DEFAULT_USER_CLICK_LISTENER,
        readStateClickListener = DEFAULT_READ_STATE_CLICK_LISTENER,
        giphySendListener = DEFAULT_GIPHY_SEND_LISTENER
    )

    private lateinit var messageListItemViewHolderFactory: MessageListItemViewHolderFactory
    private lateinit var messageDateFormatter: DateFormatter

    public constructor(context: Context) : super(context) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        parseAttr(context, attrs)
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        parseAttr(context, attrs)
        init(context, attrs)
    }

    private fun init(context: Context, attr: AttributeSet?) {
        binding = StreamUiMessageListViewBinding.inflate(context.inflater, this, true)

        initRecyclerView()
        initUnseenMessagesButton()
        initLoadingView()
        initEmptyStateView()

        if (attr != null) {
            configureAttributes(attr)
        }

        hasScrolledUp = false

        buffer.subscribe(::handleNewWrapper)
        buffer.active()
    }

    private fun initLoadingView() {
        loadingView = binding.defaultLoadingView
        loadingViewContainer = binding.loadingViewContainer
    }

    private fun initEmptyStateView() {
        emptyStateView = binding.defaultEmptyStateView
        emptyStateViewContainer = binding.emptyStateViewContainer
    }

    private fun initRecyclerView() {
        layoutManager = LinearLayoutManager(context).apply {
            stackFromEnd = true
        }

        binding.chatMessagesRV.apply {
            layoutManager = this@MessageListView.layoutManager
            setHasFixedSize(true)
            setItemViewCacheSize(20)
        }
    }

    private fun initUnseenMessagesButton() {
        binding.scrollToBottomButton.setOnClickListener {
            binding.chatMessagesRV.scrollToPosition(lastPosition())
        }
    }

    private fun parseAttr(context: Context, attrs: AttributeSet?) {
        style = MessageListViewStyle(context, attrs)
    }

    private fun configureAttributes(attributeSet: AttributeSet) {
        val tArray = context
            .obtainStyledAttributes(attributeSet, R.styleable.MessageListView)

        tArray.getInteger(
            R.styleable.MessageListView_streamUiLoadMoreThreshold,
            LOAD_MORE_TRESHOLD,
        ).also { loadMoreThreshold ->
            loadMoreListener = EndlessScrollListener(loadMoreThreshold) {
                endRegionReachedHandler()
            }
        }

        with(binding.scrollToBottomButton) {
            setUnreadBadgeEnabled(style.scrollButtonViewStyle.scrollButtonUnreadEnabled)
            setButtonRippleColor(style.scrollButtonViewStyle.scrollButtonRippleColor)
            setButtonIcon(style.scrollButtonViewStyle.scrollButtonIcon)
            setButtonColor(style.scrollButtonViewStyle.scrollButtonColor)
            setUnreadBadgeColor(style.scrollButtonViewStyle.scrollButtonBadgeColor)
        }
        scrollToBottomButtonEnabled = style.scrollButtonViewStyle.scrollButtonEnabled

        newMessagesBehaviour = NewMessagesBehaviour.parseValue(
            tArray.getInt(
                R.styleable.MessageListView_streamUiNewMessagesBehaviour,
                NewMessagesBehaviour.COUNT_UPDATE.value
            )
        )

        tArray.getText(R.styleable.MessageListView_streamUiMessagesEmptyStateLabelText)
            ?.let { emptyStateText ->
                emptyStateView.let {
                    if (it is TextView) {
                        it.text = emptyStateText
                    }
                }
            }

        configureMessageOptions(tArray)
        tArray.recycle()
    }

    private fun lastPosition(): Int {
        return adapter.itemCount - 1
    }

    private fun configureMessageOptions(tArray: TypedArray) {
        val iconsTint = tArray.getColor(
            R.styleable.MessageListView_streamUiMessageOptionIconColor,
            ContextCompat.getColor(context, R.color.stream_ui_grey)
        )

        val replyText = tArray.getString(R.styleable.MessageListView_streamUiReplyOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_reply)
        val replyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiReplyOptionIcon,
            R.drawable.stream_ui_ic_arrow_curve_left
        )

        val threadReplyText =
            tArray.getString(R.styleable.MessageListView_streamUiThreadReplyOptionMessage)
                ?: context.getString(R.string.stream_ui_message_option_thread_reply)
        val threadReplyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiThreadReplyOptionIcon,
            R.drawable.stream_ui_ic_thread_reply
        )

        val copyText = tArray.getString(R.styleable.MessageListView_streamUiCopyOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_copy)
        val copyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiCopyOptionIcon,
            R.drawable.stream_ui_ic_copy
        )

        val muteText = tArray.getString(R.styleable.MessageListView_streamUiMuteOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_mute)
        val muteIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiMuteOptionIcon,
            R.drawable.stream_ui_ic_mute
        )

        val blockText = tArray.getString(R.styleable.MessageListView_streamUiBlockOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_block_user)
        val blockIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiBlockOptionIcon,
            R.drawable.stream_ui_ic_user_block
        )

        val deleteText = tArray.getString(R.styleable.MessageListView_streamUiDeleteOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_delete_user)
        val deleteIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiDeleteOptionIcon,
            R.drawable.stream_ui_ic_delete
        )

        messageOptionsConfiguration = MessageOptionsView.Configuration(
            iconsTint = iconsTint,
            replyText = replyText,
            replyIcon = replyIcon,
            threadReplyText = threadReplyText,
            threadReplyIcon = threadReplyIcon,
            copyText = copyText,
            copyIcon = copyIcon,
            muteText = muteText,
            muteIcon = muteIcon,
            blockText = blockText,
            blockIcon = blockIcon,
            deleteText = deleteText,
            deleteIcon = deleteIcon
        )
    }

    override fun setLoadingMore(loadingMore: Boolean) {
        if (loadingMore) {
            loadMoreListener.disablePagination()
        } else {
            loadMoreListener.enablePagination()
        }
    }

    override fun scrollToMessage(message: Message) {
        val targetListItem = adapter.currentList.firstOrNull { it is MessageItem && it.message.id == message.id }
        targetListItem?.let {
            val position = adapter.currentList.indexOf(it)
            binding.chatMessagesRV.layoutManager?.scrollToPosition(position)
        }
    }

    private fun setMessageListItemAdapter(adapter: MessageListItemAdapter) {
        binding.chatMessagesRV.addOnScrollListener(loadMoreListener)
        binding.chatMessagesRV.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val currentList = adapter.currentList.toList()
                    if (!::layoutManager.isInitialized || currentList.isEmpty()) {
                        return
                    }
                    val currentFirstVisible = layoutManager.findFirstVisibleItemPosition()
                    val currentLastVisible = layoutManager.findLastVisibleItemPosition()

                    hasScrolledUp = currentLastVisible < lastPosition()
                    firstVisiblePosition = currentFirstVisible

                    val realLastVisibleMessage =
                        min(max(currentLastVisible, getLastSeenMessagePosition()), currentList.size)
                    updateLastSeen(currentList[realLastVisibleMessage])

                    val unseenItems = adapter.itemCount - 1 - realLastVisibleMessage

                    if (scrollToBottomButtonEnabled) {
                        binding.scrollToBottomButton.setUnreadCount(unseenItems)
                        binding.scrollToBottomButton.isVisible = hasScrolledUp
                    } else {
                        binding.scrollToBottomButton.isVisible = false
                    }
                }
            }
        )

        /*
         * Lock for 500 milliseconds setMessageListScrollUp in here.
         * Because when keyboard shows up, MessageList is scrolled up and it triggers hiding keyboard.
         */
        addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                lockScrollUp = true
                postDelayed({ lockScrollUp = false }, 500)
            }
        }

        binding.chatMessagesRV.adapter = adapter
    }

    override fun init(channel: Channel, currentUser: User) {
        this.currentUser = currentUser
        this.channel = channel
        initAdapter()
    }

    private fun initAdapter() {
        // Create default ViewHolderFactory if needed
        if (::messageListItemViewHolderFactory.isInitialized.not()) {
            messageListItemViewHolderFactory = MessageListItemViewHolderFactory()
        }

        if (::messageDateFormatter.isInitialized.not()) {
            messageDateFormatter = DateFormatter.from(context)
        }

        messageListItemViewHolderFactory.listenerContainer = this.listenerContainer

        adapter = MessageListItemAdapter(messageListItemViewHolderFactory)
        adapter.setHasStableIds(true)

        setMessageListItemAdapter(adapter)
    }

    /**
     * @param view will be added to the view hierarchy of [ChannelsView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container [ViewGroup].
     */
    public fun setLoadingView(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        loadingViewContainer.removeView(loadingView)
        loadingView = view
        loadingViewContainer.addView(loadingView, layoutParams)
    }

    override fun showLoadingView() {
        loadingViewContainer.isVisible = true
    }

    override fun hideLoadingView() {
        loadingViewContainer.isVisible = false
    }

    /**
     * @param view will be added to the view hierarchy of [ChannelsView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container [ViewGroup].
     */
    public fun setEmptyStateView(view: View, layoutParams: FrameLayout.LayoutParams = defaultChildLayoutParams) {
        emptyStateViewContainer.removeView(emptyStateView)
        emptyStateView = view
        emptyStateViewContainer.addView(emptyStateView, layoutParams)
    }

    override fun showEmptyStateView() {
        emptyStateViewContainer.isVisible = true
    }

    override fun hideEmptyStateView() {
        emptyStateViewContainer.isVisible = false
    }

    public fun setNewMessagesBehaviour(newMessagesBehaviour: NewMessagesBehaviour) {
        this.newMessagesBehaviour = newMessagesBehaviour
    }

    public fun setMessageViewHolderFactory(messageListItemViewHolderFactory: MessageListItemViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set MessageViewHolderFactory first" }
        this.messageListItemViewHolderFactory = messageListItemViewHolderFactory
    }

    public fun setMessageDateFormatter(messageDateFormatter: DateFormatter) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized; please set DateFormatter first" }
        this.messageDateFormatter = messageDateFormatter
    }

    override fun displayNewMessage(listItem: MessageListItemWrapper) {
        buffer.enqueueData(listItem)
    }

    public fun scrollToBottom() {
        layoutManager.scrollToPosition(adapter.itemCount - 1)
    }

    private fun handleNewWrapper(listItem: MessageListItemWrapper) {
        buffer.hold()
        val entities = listItem.items

        val startThreadMode = !adapter.isThread && listItem.isThread

        adapter.isThread = listItem.isThread

        val oldSize = adapter.itemCount

        adapter.submitList(entities) {
            continueMessageAdd(startThreadMode, listItem, entities, oldSize)
        }
    }

    private fun continueMessageAdd(
        startThreadMode: Boolean,
        listItem: MessageListItemWrapper,
        entities: List<MessageListItem>,
        oldSize: Int
    ) {
        val newSize = adapter.itemCount
        val sizeGrewBy = newSize - oldSize

        if (startThreadMode) {
            layoutManager.scrollToPosition(0)
            buffer.active()
            return
        }

        // Scroll to bottom position for typing indicator
        if (listItem.isTyping && scrolledBottom(sizeGrewBy + 2) && !hasScrolledUp) {
            val newPosition = adapter.itemCount - 1
            layoutManager.scrollToPosition(newPosition)
            buffer.active()
            return
        }

        if (!listItem.hasNewMessages) {
            // we only touch scroll for new messages, we ignore
            // read
            // typing
            // message updates
            logger.logI("no Scroll no new message")
            buffer.active()
            return
        }

        if (oldSize == 0 && newSize != 0) {
            val newPosition = adapter.itemCount - 1
            layoutManager.scrollToPosition(newPosition)
            logger.logI(
                String.format("Scroll: First load scrolling down to bottom %d", newPosition)
            )
            lastMessageReadHandler.invoke()
        } else {
            if (newSize == 0) {
                buffer.active()
                return
            }

            // regular new message behaviour
            // we scroll down all the way, unless you've scrolled up
            // if you've scrolled up we set a variable on the viewmodel that there are new messages
            val newPosition = adapter.itemCount - 1
            val layoutSize = layoutManager.itemCount
            logger.logI(
                String.format(
                    "Scroll: Moving down to %d, layout has %d elements",
                    newPosition,
                    layoutSize
                )
            )
            val isMine = (entities.lastOrNull() as? MessageItem)?.isMine ?: false
            // Scroll to bottom when the user wrote the message.
            if (isMine ||
                !hasScrolledUp ||
                newMessagesBehaviour == NewMessagesBehaviour.SCROLL_TO_BOTTOM
            ) {
                layoutManager.scrollToPosition(adapter.itemCount - 1)
            } else {
                val unseenItems = newSize - getLastSeenMessagePosition() - 1
                binding.scrollToBottomButton.setUnreadCount(unseenItems)
            }
            // we want to mark read if there is a new message
            // and this view is currently being displayed...
            // we can't always run it since read and typing events also influence this list..
            // viewModel.markLastMessageRead(); // TODO this is event
            lastMessageReadHandler.invoke()
        }

        buffer.active()
    }

    private fun scrolledBottom(delta: Int): Boolean {
        return getLastSeenMessagePosition() + delta >= lastPosition()
    }

    private fun getLastSeenMessagePosition(): Int {
        val lastMessageId = when (adapter.isThread) {
            true -> lastSeenMessageInThread
            false -> lastSeenMessageInChannel
        }?.getStableId()
        return adapter.currentList.indexOfLast { message ->
            message?.getStableId() == lastMessageId
        }
    }

    private fun updateLastSeen(messageListItem: MessageListItem?) {
        when (adapter.isThread) {
            true -> lastSeenMessageInThread = messageListItem
            false -> lastSeenMessageInChannel = messageListItem
        }.exhaustive
    }

    /**
     * Sets the message click listener to be used by MessageListView.
     *
     * @param messageClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setMessageClickListener(messageClickListener: MessageClickListener?) {
        listenerContainer.messageClickListener =
            messageClickListener ?: DEFAULT_MESSAGE_CLICK_LISTENER
    }

    /**
     * Sets the message retry listener to be used by MessageListView.
     *
     * @param messageRetryListener The listener to use. If null, the default will be used instead.
     */
    public fun setMessageRetryListener(messageRetryListener: MessageRetryListener?) {
        listenerContainer.messageRetryListener =
            messageRetryListener ?: DEFAULT_MESSAGE_RETRY_LISTENER
    }

    /**
     * Sets the attachment click listener to be used by MessageListView.
     *
     * @param attachmentClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setAttachmentClickListener(attachmentClickListener: AttachmentClickListener?) {
        listenerContainer.attachmentClickListener =
            attachmentClickListener ?: DEFAULT_ATTACHMENT_CLICK_LISTENER
    }

    /**
     * Sets the reaction view click listener to be used by MessageListView.
     *
     * @param reactionViewClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setReactionViewClickListener(reactionViewClickListener: ReactionViewClickListener?) {
        listenerContainer.reactionViewClickListener =
            reactionViewClickListener ?: DEFAULT_REACTION_VIEW_CLICK_LISTENER
    }

    /**
     * Sets the user click listener to be used by MessageListView.
     *
     * @param userClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setUserClickListener(userClickListener: UserClickListener?) {
        listenerContainer.userClickListener = userClickListener ?: DEFAULT_USER_CLICK_LISTENER
    }

    /**
     * Sets the read state click listener to be used by MessageListView.
     *
     * @param readStateClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setReadStateClickListener(readStateClickListener: ReadStateClickListener?) {
        listenerContainer.readStateClickListener =
            readStateClickListener ?: DEFAULT_READ_STATE_CLICK_LISTENER
    }

    override fun setEndRegionReachedHandler(endRegionReachedHandler: () -> Unit) {
        this.endRegionReachedHandler = endRegionReachedHandler
    }

    override fun setLastMessageReadHandler(lastMessageReadHandler: () -> Unit) {
        this.lastMessageReadHandler = lastMessageReadHandler
    }

    override fun setOnMessageEditHandler(onMessageEditHandler: (Message) -> Unit) {
        this.onMessageEditHandler = onMessageEditHandler
    }

    override fun setOnMessageDeleteHandler(onMessageDeleteHandler: (Message) -> Unit) {
        this.onMessageDeleteHandler = onMessageDeleteHandler
    }

    override fun setOnStartThreadHandler(onStartThreadHandler: (Message) -> Unit) {
        this.onStartThreadHandler = onStartThreadHandler
    }

    override fun setOnMessageFlagHandler(onMessageFlagHandler: (Message) -> Unit) {
        this.onMessageFlagHandler = onMessageFlagHandler
    }

    override fun setOnSendGiphyHandler(onSendGiphyHandler: (Message, GiphyAction) -> Unit) {
        this.onSendGiphyHandler = onSendGiphyHandler
    }

    override fun setOnMessageRetryHandler(onMessageRetryHandler: (Message) -> Unit) {
        this.onMessageRetryHandler = onMessageRetryHandler
    }

    public fun setOnStartThreadListener(onStartThreadListener: (Message) -> Unit) {
        this.onStartThreadListener = onStartThreadListener
    }

    public fun interface MessageClickListener {
        public fun onMessageClick(message: Message)
    }

    public fun interface MessageRetryListener {
        public fun onRetryMessage(message: Message)
    }

    public fun interface MessageLongClickListener {
        public fun onMessageLongClick(message: Message)
    }

    public fun interface AttachmentClickListener {
        public fun onAttachmentClick(message: Message, attachment: Attachment)
    }

    public fun interface GiphySendListener {
        public fun onGiphySend(message: Message, action: GiphyAction)
    }

    public fun interface UserClickListener {
        public fun onUserClick(user: User)
    }

    public fun interface ReadStateClickListener {
        public fun onReadStateClick(reads: List<ChannelUserRead>)
    }

    public fun interface ReactionViewClickListener {
        public fun onReactionViewClick(message: Message)
    }

    public enum class NewMessagesBehaviour(internal val value: Int) {
        SCROLL_TO_BOTTOM(0), COUNT_UPDATE(1);

        internal companion object {
            fun parseValue(value: Int): NewMessagesBehaviour {
                return values().find { behaviour -> behaviour.value == value }
                    ?: throw IllegalArgumentException("Unknown behaviour type. It must be either SCROLL_TO_BOTTOM (int 0) or COUNT_UPDATE (int 1)")
            }
        }
    }
}
