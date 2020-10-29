package com.getstream.sdk.chat.view

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.DefaultBubbleHelper
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory
import com.getstream.sdk.chat.adapter.ListenerContainer
import com.getstream.sdk.chat.adapter.ListenerContainerImpl
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItemAdapter
import com.getstream.sdk.chat.adapter.MessageViewHolderFactory
import com.getstream.sdk.chat.databinding.StreamMessageListViewBinding
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination
import com.getstream.sdk.chat.utils.StartStopBuffer
import com.getstream.sdk.chat.utils.inflater
import com.getstream.sdk.chat.view.dialog.MessageMoreActionDialog
import com.getstream.sdk.chat.view.dialog.ReadUsersDialog
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

/**
 * MessageListView renders a list of messages and extends the RecyclerView
 * The most common customizations are
 * - Disabling Reactions
 * - Disabling Threads
 * - Customizing the click and longCLick (via the adapter)
 * - The list_item_message template to use (perhaps, multiple ones...?)
 */
public class MessageListView : ConstraintLayout {
    private var firstVisiblePosition = 0
    private var lastVisiblePosition = 0

    private lateinit var style: MessageListViewStyle

    private lateinit var binding: StreamMessageListViewBinding

    private var lastViewedPosition = 0
    private var newMessagesTextSingle: String? = null
    private var newMessagesTextPlural: String? = null

    private lateinit var newMessagesBehaviour: NewMessagesBehaviour
    private lateinit var scrollButtonBehaviour: ScrollButtonBehaviour

    private val buffer: StartStopBuffer<MessageListItemWrapper> = StartStopBuffer()

    private lateinit var adapter: MessageListItemAdapter
    private lateinit var layoutManager: LinearLayoutManager

    public var unseenButtonEnabled: Boolean = true

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

    private val loadMoreListener = EndlessScrollListener {
        endRegionReachedHandler()
    }

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
            MessageMoreActionDialog(
                context,
                channel,
                message,
                currentUser,
                style,
                onMessageEditHandler,
                onMessageDeleteHandler,
                { m: Message ->
                    onStartThreadHandler.invoke(m)
                    onStartThreadListener.invoke(m)
                },
                onMessageFlagHandler
            ).show()
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
            MessageMoreActionDialog(
                context,
                channel,
                message,
                currentUser,
                style,
                onMessageEditHandler,
                onMessageDeleteHandler,
                onStartThreadHandler,
                onMessageFlagHandler
            ).show()
        }
    private val DEFAULT_USER_CLICK_LISTENER = UserClickListener { /* Empty */ }
    private val DEFAULT_READ_STATE_CLICK_LISTENER =
        ReadStateClickListener { reads: List<ChannelUserRead> ->
            ReadUsersDialog(context)
                .setReads(reads)
                .setStyle(style)
                .show()
        }
    private val DEFAULT_GIPHY_SEND_LISTENER =
        GiphySendListener { message, action ->
            onSendGiphyHandler.invoke(message, action)
        }

    private val listenerContainer: ListenerContainer = ListenerContainerImpl(
        DEFAULT_MESSAGE_CLICK_LISTENER,
        DEFAULT_MESSAGE_LONG_CLICK_LISTENER,
        DEFAULT_MESSAGE_RETRY_LISTENER,
        DEFAULT_ATTACHMENT_CLICK_LISTENER,
        DEFAULT_REACTION_VIEW_CLICK_LISTENER,
        DEFAULT_USER_CLICK_LISTENER,
        DEFAULT_READ_STATE_CLICK_LISTENER,
        DEFAULT_GIPHY_SEND_LISTENER
    )

    private lateinit var bubbleHelper: BubbleHelper
    private lateinit var attachmentViewHolderFactory: AttachmentViewHolderFactory
    private lateinit var messageViewHolderFactory: MessageViewHolderFactory

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
        binding = StreamMessageListViewBinding.inflate(context.inflater, this, true)

        initRecyclerView()
        initUnseenMessagesButton()
        initUnseenMessagesView()

        if (attr != null) {
            configureAttributes(attr)
        }

        initScrollButtonBehaviour()

        hasScrolledUp = false

        buffer.subscribe(::handleNewWrapper)
        buffer.active()
    }

    private fun initScrollButtonBehaviour() {
        scrollButtonBehaviour = DefaultScrollButtonBehaviour(
            binding.scrollBottomBtn,
            binding.newMessagesTV,
            newMessagesTextSingle,
            newMessagesTextPlural
        )
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
        binding.scrollBottomBtn.setOnClickListener {
            binding.chatMessagesRV.smoothScrollToPosition(lastPosition())
        }
    }

    private fun initUnseenMessagesView() {
        binding.newMessagesTV.visibility = GONE
    }

    private fun parseAttr(context: Context, attrs: AttributeSet?) {
        style = MessageListViewStyle(context, attrs)
    }

    private fun configureAttributes(attributeSet: AttributeSet) {
        val tArray = context
            .obtainStyledAttributes(attributeSet, R.styleable.MessageListView)

        loadMoreListener.loadMoreThreshold = tArray.getInteger(
            R.styleable.MessageListView_streamLoadMoreThreshold,
            context.resources.getInteger(R.integer.stream_load_more_threshold)
        )

        val backgroundRes = tArray.getResourceId(
            R.styleable.MessageListView_streamScrollButtonBackground,
            R.drawable.stream_shape_round
        )

        unseenButtonEnabled = tArray.getBoolean(
            R.styleable.MessageListView_streamDefaultScrollButtonEnabled,
            true
        )

        binding.scrollBottomBtn.setBackgroundResource(backgroundRes)

        if (!unseenButtonEnabled) {
            binding.scrollBottomBtn.visibility = View.GONE
        }

        newMessagesTextSingle =
            tArray.getString(R.styleable.MessageListView_streamNewMessagesTextSingle)
        newMessagesTextPlural =
            tArray.getString(R.styleable.MessageListView_streamNewMessagesTextPlural)
        newMessagesBehaviour = NewMessagesBehaviour.parseValue(
            tArray.getInt(
                R.styleable.MessageListView_streamNewMessagesBehaviour,
                NewMessagesBehaviour.COUNT_UPDATE.value
            )
        )

        val arrowIconRes = tArray.getResourceId(
            R.styleable.MessageListView_streamButtonIcon,
            R.drawable.stream_bottom_arrow
        )

        val scrollButtonArrow = findViewById<ImageView>(R.id.scrollIconIV)
        scrollButtonArrow.setImageResource(arrowIconRes)

        tArray.recycle()
    }

    private fun lastPosition(): Int {
        return adapter.itemCount - 1
    }

    public fun setLoadingMore(loadingMore: Boolean) {
        loadMoreListener.paginationEnabled = !loadingMore
    }

    private fun setMessageListItemAdapter(adapter: MessageListItemAdapter) {
        binding.chatMessagesRV.addOnScrollListener(loadMoreListener)
        binding.chatMessagesRV.addOnScrollListener(
            object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!::layoutManager.isInitialized) {
                        return
                    }

                    val currentFirstVisible = layoutManager.findFirstVisibleItemPosition()
                    val currentLastVisible = layoutManager.findLastVisibleItemPosition()

                    hasScrolledUp = currentLastVisible < lastPosition()
                    lastVisiblePosition = currentLastVisible
                    firstVisiblePosition = currentFirstVisible

                    lastViewedPosition = Math.max(currentLastVisible, lastViewedPosition)

                    val unseenItems = adapter.itemCount - 1 - lastViewedPosition
                    scrollButtonBehaviour.onUnreadMessageCountChanged(unseenItems)

                    if (hasScrolledUp) {
                        scrollButtonBehaviour.userScrolledUp()
                    } else {
                        scrollButtonBehaviour.userScrolledToTheBottom()
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

    public fun init(channel: Channel, currentUser: User) {
        this.currentUser = currentUser
        this.channel = channel
        initAdapter()
    }

    private fun initAdapter() {
        // Create default AttachmentViewHolderFactory if needed
        if (::attachmentViewHolderFactory.isInitialized.not()) {
            attachmentViewHolderFactory = AttachmentViewHolderFactory()
        }
        // Create default ViewHolderFactory if needed
        if (::messageViewHolderFactory.isInitialized.not()) {
            messageViewHolderFactory = MessageViewHolderFactory()
        }
        // Create default BubbleHelper if needed
        if (::bubbleHelper.isInitialized.not()) {
            bubbleHelper = DefaultBubbleHelper.initDefaultBubbleHelper(style, context)
        }

        // Inject Attachment factory
        attachmentViewHolderFactory.listenerContainer = listenerContainer
        attachmentViewHolderFactory.bubbleHelper = bubbleHelper

        // Inject Message factory
        messageViewHolderFactory.listenerContainer = listenerContainer
        messageViewHolderFactory.attachmentViewHolderFactory = attachmentViewHolderFactory
        messageViewHolderFactory.bubbleHelper = bubbleHelper

        adapter = MessageListItemAdapter(channel, messageViewHolderFactory, style)
        adapter.setHasStableIds(true)

        setMessageListItemAdapter(adapter)
    }

    public fun setScrollButtonBehaviour(scrollButtonBehaviour: ScrollButtonBehaviour) {
        this.scrollButtonBehaviour = scrollButtonBehaviour
    }

    public fun setNewMessagesBehaviour(newMessagesBehaviour: NewMessagesBehaviour) {
        this.newMessagesBehaviour = newMessagesBehaviour
    }

    public fun setScrollButtonBackgroundResource(@DrawableRes backgroundRes: Int) {
        binding.scrollBottomBtn.setBackgroundResource(backgroundRes)
    }

    public fun setScrollButtonBackground(drawable: Drawable?) {
        binding.scrollBottomBtn.background = drawable
    }

    public fun setScrollButtonIconResource(@DrawableRes backgroundRes: Int) {
        binding.scrollIconIV.setImageResource(backgroundRes)
    }

    public fun setScrollButtonIcon(drawable: Drawable?) {
        binding.scrollIconIV.setImageDrawable(drawable)
    }

    public fun setAttachmentViewHolderFactory(attachmentViewHolderFactory: AttachmentViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set AttachmentViewHolderFactory first" }
        this.attachmentViewHolderFactory = attachmentViewHolderFactory
    }

    public fun setMessageViewHolderFactory(messageViewHolderFactory: MessageViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set MessageViewHolderFactory first" }
        this.messageViewHolderFactory = messageViewHolderFactory
    }

    /**
     * Use the more explicit setMessageViewHolderFactory method instead.
     */
    @Deprecated(
        message = "Use the more explicit setMessageViewHolderFactory method instead.",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("setMessageViewHolderFactory(messageViewHolderFactory)")
    )
    public fun setViewHolderFactory(messageViewHolderFactory: MessageViewHolderFactory) {
        setMessageViewHolderFactory(messageViewHolderFactory)
    }

    public fun setBubbleHelper(bubbleHelper: BubbleHelper) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set BubbleHelper first" }
        this.bubbleHelper = bubbleHelper
    }

    public fun displayNewMessage(listItem: MessageListItemWrapper) {
        buffer.enqueueData(listItem)
    }

    public fun scrollToBottom() {
        layoutManager.scrollToPosition(adapter.itemCount - 1)
    }

    private fun handleNewWrapper(listItem: MessageListItemWrapper) {
        buffer.hold()
        val entities = listItem.items

        // Adapter initialization for channel and thread swapping
        val backFromThread = adapter.isThread && listItem.isThread

        if (adapter.isThread != listItem.isThread) {
            adapter.isThread = listItem.isThread
        }

        adapter.submitList(entities) {
            continueMessageAdd(backFromThread, listItem, entities, adapter.itemCount)
        }
    }

    private fun continueMessageAdd(
        backFromThread: Boolean,
        listItem: MessageListItemWrapper,
        entities: List<MessageListItem>,
        oldSize: Int
    ) {
        val newSize = adapter.itemCount
        val sizeGrewBy = newSize - oldSize

        // Scroll to origin position on return from thread
        if (backFromThread) {
            // TODO review this, supposed to be the thread parent position
            layoutManager.scrollToPosition(0)
            lastMessageReadHandler.invoke()
            buffer.active()
            return
        }

        // Scroll to bottom position for typing indicator
        if (listItem.isTyping && scrolledBottom(sizeGrewBy + 2)) {
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
        } else if (listItem.loadingMore) {
            // the load more behaviour is different, scroll positions starts out at 0
            // to stay at the relative 0 we should go to 0 + size of new messages...

            // TODO ???
            val newPosition = layoutManager.findLastCompletelyVisibleItemPosition() + sizeGrewBy
            layoutManager.scrollToPosition(newPosition)
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
                val unseenItems = newSize - 1 - lastViewedPosition
                scrollButtonBehaviour.onUnreadMessageCountChanged(unseenItems)
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
        return lastVisiblePosition + delta >= lastPosition()
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
     * Sets the message long click listener to be used by MessageListView.
     *
     * @param messageLongClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setMessageLongClickListener(messageLongClickListener: MessageLongClickListener?) {
        listenerContainer.messageLongClickListener =
            messageLongClickListener ?: DEFAULT_MESSAGE_LONG_CLICK_LISTENER
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

    public fun setEndRegionReachedHandler(endRegionReachedHandler: () -> Unit) {
        this.endRegionReachedHandler = endRegionReachedHandler
    }

    public fun setLastMessageReadHandler(lastMessageReadHandler: () -> Unit) {
        this.lastMessageReadHandler = lastMessageReadHandler
    }

    public fun setOnMessageEditHandler(onMessageEditHandler: (Message) -> Unit) {
        this.onMessageEditHandler = onMessageEditHandler
    }

    public fun setOnMessageDeleteHandler(onMessageDeleteHandler: (Message) -> Unit) {
        this.onMessageDeleteHandler = onMessageDeleteHandler
    }

    public fun setOnStartThreadHandler(onStartThreadHandler: (Message) -> Unit) {
        this.onStartThreadHandler = onStartThreadHandler
    }

    public fun setOnMessageFlagHandler(onMessageFlagHandler: (Message) -> Unit) {
        this.onMessageFlagHandler = onMessageFlagHandler
    }

    public fun setOnSendGiphyHandler(onSendGiphyHandler: (Message, GiphyAction) -> Unit) {
        this.onSendGiphyHandler = onSendGiphyHandler
    }

    public fun setOnMessageRetryHandler(onMessageRetryHandler: (Message) -> Unit) {
        this.onMessageRetryHandler = onMessageRetryHandler
    }

    public fun setOnStartThreadListener(onStartThreadListener: (Message) -> Unit) {
        this.onStartThreadListener = onStartThreadListener
    }

    public fun interface HeaderAvatarGroupClickListener {
        public fun onHeaderAvatarGroupClick(channel: Channel)
    }

    public fun interface HeaderOptionsClickListener {
        public fun onHeaderOptionsClick(channel: Channel)
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

    public interface BubbleHelper {
        public fun getDrawableForMessage(
            message: Message,
            mine: Boolean,
            positions: List<MessageListItem.Position>
        ): Drawable

        public fun getDrawableForAttachment(
            message: Message,
            mine: Boolean,
            positions: List<MessageListItem.Position>,
            attachment: Attachment
        ): Drawable

        public fun getDrawableForAttachmentDescription(
            message: Message,
            mine: Boolean,
            positions: List<MessageListItem.Position>
        ): Drawable
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

    public interface ScrollButtonBehaviour {
        public fun userScrolledUp()
        public fun userScrolledToTheBottom()
        public fun onUnreadMessageCountChanged(count: Int)
    }

    internal class DefaultScrollButtonBehaviour(
        private val unseenBottomBtn: ViewGroup,
        private val newMessagesTextTV: TextView,
        private val newMessagesTextSingle: String?,
        private val newMessagesTextPlural: String?,
        private var isButtonEnabled: Boolean = true
    ) : ScrollButtonBehaviour {
        override fun userScrolledUp() {
            if (!unseenBottomBtn.isShown) {
                unseenBottomBtn.visibility = VISIBLE
            }
        }

        override fun userScrolledToTheBottom() {
            if (unseenBottomBtn.isShown) {
                unseenBottomBtn.visibility = GONE
            }
        }

        override fun onUnreadMessageCountChanged(count: Int) {
            if (count <= 0) {
                newMessagesTextTV.visibility = GONE
            } else if (isButtonEnabled) {
                newMessagesTextTV.visibility = VISIBLE
                newMessagesTextTV.text = formatNewMessagesText(count)
            }
        }

        private fun formatNewMessagesText(unseenItems: Int): String {
            fun createText(formatString: String?) = when (formatString) {
                null -> unseenItems.toString()
                else -> String.format(formatString, unseenItems)
            }

            return when (unseenItems) {
                1 -> createText(newMessagesTextSingle)
                else -> createText(newMessagesTextPlural)
            }
        }
    }
}
