package io.getstream.chat.android.ui.messages.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.StartStopBuffer
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.view.EndlessScrollListener
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.chat.navigation.GalleryImageAttachmentDestination
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiMessageListViewBinding
import io.getstream.chat.android.ui.messages.adapter.MessageListItemAdapter
import io.getstream.chat.android.ui.messages.adapter.MessageListItemDecoratorProvider
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.ThreadClickListener
import io.getstream.chat.android.ui.messages.view.MessageListView.UserClickListener
import io.getstream.chat.android.ui.options.MessageOptionsDialogFragment
import io.getstream.chat.android.ui.options.MessageOptionsView
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.utils.extensions.isInThread

/**
 * MessageListView renders a list of messages and extends the [RecyclerView]
 * The most common customizations are
 * - Disabling Reactions
 * - Disabling Threads
 * - Customizing the click and longCLick (via the adapter)
 * - The list_item_message template to use (perhaps, multiple ones...?)
 */
public class MessageListView : ConstraintLayout {

    private companion object {
        const val LOAD_MORE_THRESHOLD = 10
    }

    private lateinit var style: MessageListViewStyle

    private lateinit var binding: StreamUiMessageListViewBinding

    private val buffer: StartStopBuffer<MessageListItemWrapper> = StartStopBuffer()

    private lateinit var adapter: MessageListItemAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var loadingView: View
    private lateinit var loadingViewContainer: ViewGroup
    private lateinit var emptyStateView: View
    private lateinit var emptyStateViewContainer: ViewGroup
    private lateinit var scrollHelper: MessageListScrollHelper

    private val defaultChildLayoutParams by lazy {
        FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.CENTER
        )
    }

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
    private var onMessageReactionHandler: (Message, String) -> Unit = { _, _ ->
        throw IllegalStateException("onMessageReactionHandler must be set.")
    }
    private var onMuteUserHandler: (User) -> Unit = {
        throw IllegalStateException("onMuteUserHandler must be set.")
    }
    private var onBlockUserHandler: (User) -> Unit = {
        throw IllegalStateException("onBlockUserHandler must be set.")
    }
    private var onReplyMessageHandler: (cid: String, Message) -> Unit = { _, _ ->
        throw IllegalStateException("onReplyMessageHandler must be set")
    }
    private var onAttachmentDownloadHandler: (Attachment) -> Unit = {
        throw IllegalStateException("onAttachmentDownloadHandler must be set")
    }

    private lateinit var messageOptionsConfiguration: MessageOptionsView.Configuration

    private lateinit var loadMoreListener: EndlessScrollListener

    private lateinit var channel: Channel
    private lateinit var currentUser: User

    /**
     * If you are allowed to scroll up or not
     */
    private var lockScrollUp = true

    private val DEFAULT_MESSAGE_CLICK_LISTENER =
        MessageClickListener { message ->
            if (message.replyCount > 0) {
                onStartThreadHandler.invoke(message)
                onStartThreadListener.invoke(message)
            }
        }
    private val DEFAULT_MESSAGE_LONG_CLICK_LISTENER =
        MessageLongClickListener { message ->
            context.getFragmentManager()?.let { fragmentManager ->
                MessageOptionsDialogFragment
                    .newMessageOptionsInstance(
                        message,
                        messageOptionsConfiguration.copy(
                            threadEnabled = !adapter.isThread && !message.isInThread(),
                        )
                    )
                    .apply {
                        setReactionClickHandler(onMessageReactionHandler)
                        setMessageOptionsHandlers(
                            MessageOptionsDialogFragment.MessageOptionsHandlers(
                                threadReplyHandler = onStartThreadHandler,
                                retryHandler = onMessageRetryHandler,
                                editClickHandler = onMessageEditHandler,
                                flagClickHandler = onMessageFlagHandler,
                                muteClickHandler = onMuteUserHandler,
                                blockClickHandler = onBlockUserHandler,
                                deleteClickHandler = onMessageDeleteHandler,
                                replyClickHandler = onReplyMessageHandler,
                            )
                        )
                    }
                    .show(fragmentManager, MessageOptionsDialogFragment.TAG)
            }
        }
    private val DEFAULT_MESSAGE_RETRY_LISTENER =
        MessageRetryListener { message ->
            onMessageRetryHandler.invoke(message)
        }
    private val DEFAULT_THREAD_CLICK_LISTENER =
        ThreadClickListener { message ->
            if (message.replyCount > 0) {
                onStartThreadHandler.invoke(message)
                onStartThreadListener.invoke(message)
            }
        }
    private val DEFAULT_ATTACHMENT_CLICK_LISTENER =
        AttachmentClickListener { message, attachment ->
            ChatUI.instance()
                .navigator
                .navigate(GalleryImageAttachmentDestination(message, attachment, context))
        }
    private val DEFAULT_ATTACHMENT_DOWNLOAD_CLICK_LISTENER =
        AttachmentDownloadClickListener { attachment ->
            onAttachmentDownloadHandler.invoke(attachment)
            Toast.makeText(
                context,
                context.getString(R.string.stream_ui_attachment_downloading_started),
                Toast.LENGTH_SHORT
            ).show()
        }
    private val DEFAULT_REACTION_VIEW_CLICK_LISTENER =
        ReactionViewClickListener { message: Message ->
            context.getFragmentManager()?.let {
                MessageOptionsDialogFragment.newReactionOptionsInstance(message)
                    .apply {
                        setReactionClickHandler { message, reactionType ->
                            onMessageReactionHandler(message, reactionType)
                        }
                    }
                    .show(it, MessageOptionsDialogFragment.TAG)
            }
        }
    private val DEFAULT_USER_CLICK_LISTENER = UserClickListener { /* Empty */ }
    private val DEFAULT_GIPHY_SEND_LISTENER =
        GiphySendListener { message, action ->
            onSendGiphyHandler.invoke(message, action)
        }
    private val DEFAULT_LINK_CLICK_LISTENER = LinkClickListener { url ->
        ChatUI.instance().navigator.navigate(WebLinkDestination(url, context))
    }

    private val listenerContainer = MessageListListenerContainerImpl(
        messageClickListener = DEFAULT_MESSAGE_CLICK_LISTENER,
        messageLongClickListener = DEFAULT_MESSAGE_LONG_CLICK_LISTENER,
        messageRetryListener = DEFAULT_MESSAGE_RETRY_LISTENER,
        threadClickListener = DEFAULT_THREAD_CLICK_LISTENER,
        attachmentClickListener = DEFAULT_ATTACHMENT_CLICK_LISTENER,
        attachmentDownloadClickListener = DEFAULT_ATTACHMENT_DOWNLOAD_CLICK_LISTENER,
        reactionViewClickListener = DEFAULT_REACTION_VIEW_CLICK_LISTENER,
        userClickListener = DEFAULT_USER_CLICK_LISTENER,
        giphySendListener = DEFAULT_GIPHY_SEND_LISTENER,
        linkClickListener = DEFAULT_LINK_CLICK_LISTENER,
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
        initScrollHelper()
        initLoadingView()
        initEmptyStateView()

        if (attr != null) {
            configureAttributes(attr)
        }

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

    private fun initScrollHelper() {
        scrollHelper = MessageListScrollHelper(
            recyclerView = binding.chatMessagesRV,
            scrollButtonView = binding.scrollToBottomButton,
        ) {
            lastMessageReadHandler.invoke()
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
            LOAD_MORE_THRESHOLD,
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
        scrollHelper.scrollToBottomButtonEnabled = style.scrollButtonViewStyle.scrollButtonEnabled

        NewMessagesBehaviour.parseValue(
            tArray.getInt(
                R.styleable.MessageListView_streamUiNewMessagesBehaviour,
                NewMessagesBehaviour.COUNT_UPDATE.value
            )
        ).also {
            scrollHelper.alwaysScrollToBottom = it == NewMessagesBehaviour.SCROLL_TO_BOTTOM
        }

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

        val retryText =
            tArray.getString(R.styleable.MessageListView_streamUiRetryOptionMessage)
                ?: context.getString(R.string.stream_ui_message_option_retry)
        val retryIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiRetryOptionIcon,
            R.drawable.stream_ui_ic_send
        )

        val copyText = tArray.getString(R.styleable.MessageListView_streamUiCopyOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_copy)
        val copyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiCopyOptionIcon,
            R.drawable.stream_ui_ic_copy
        )

        val editText = tArray.getString(R.styleable.MessageListView_streamUiEditOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_edit)
        val editIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiEditOptionIcon,
            R.drawable.stream_ui_ic_edit
        )

        val flagText = tArray.getString(R.styleable.MessageListView_streamUiFlagOptionMessage)
            ?: context.getString(R.string.stream_ui_message_option_flag)
        val flagIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiFlagOptionIcon,
            R.drawable.stream_ui_ic_flag
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

        val copyTextEnabled = tArray.getBoolean(R.styleable.MessageListView_streamUiCopyMessageActionEnabled, true)

        val deleteConfirmationEnabled =
            tArray.getBoolean(R.styleable.MessageListView_streamUiDeleteConfirmationEnabled, true)

        val deleteDialogTitle =
            tArray.getString(R.styleable.MessageListView_streamUiDeleteConfirmationTitle)
                ?: resources.getString(R.string.stream_ui_message_option_delete_confirmation_title)

        val deleteDialogMessage =
            tArray.getString(R.styleable.MessageListView_streamUiDeleteConfirmationTitle)
                ?: resources.getString(R.string.stream_ui_message_option_delete_confirmation_message)

        val deleteDialogPositiveButton =
            tArray.getString(R.styleable.MessageListView_streamUiDeleteConfirmationTitle)
                ?: resources.getString(R.string.stream_ui_message_option_delete_positive_button)

        val deleteDialogNegativeButton =
            tArray.getString(R.styleable.MessageListView_streamUiDeleteConfirmationTitle)
                ?: resources.getString(R.string.stream_ui_message_option_delete_negative_button)

        messageOptionsConfiguration = MessageOptionsView.Configuration(
            iconsTint = iconsTint,
            replyText = replyText,
            replyIcon = replyIcon,
            threadReplyText = threadReplyText,
            threadReplyIcon = threadReplyIcon,
            retryText = retryText,
            retryIcon = retryIcon,
            copyText = copyText,
            copyIcon = copyIcon,
            editText = editText,
            editIcon = editIcon,
            flagText = flagText,
            flagIcon = flagIcon,
            muteText = muteText,
            muteIcon = muteIcon,
            blockText = blockText,
            blockIcon = blockIcon,
            deleteText = deleteText,
            deleteIcon = deleteIcon,
            copyTextEnabled = copyTextEnabled,
            deleteConfirmationEnabled = deleteConfirmationEnabled,
            deleteConfirmationTitle = deleteDialogTitle,
            deleteConfirmationMessage = deleteDialogMessage,
            deleteConfirmationPositiveButton = deleteDialogPositiveButton,
            deleteConfirmationNegativeButton = deleteDialogNegativeButton,
        )
    }

    public fun setLoadingMore(loadingMore: Boolean) {
        if (loadingMore) {
            loadMoreListener.disablePagination()
        } else {
            loadMoreListener.enablePagination()
        }
    }

    public fun scrollToMessage(message: Message) {
        scrollHelper.scrollToMessage(message)
    }

    private fun setMessageListItemAdapter(adapter: MessageListItemAdapter) {
        binding.chatMessagesRV.addOnScrollListener(loadMoreListener)
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
        // Create default DateFormatter if needed
        if (::messageDateFormatter.isInitialized.not()) {
            messageDateFormatter = DateFormatter.from(context)
        }

        // Create default ViewHolderFactory if needed
        if (::messageListItemViewHolderFactory.isInitialized.not()) {
            messageListItemViewHolderFactory = MessageListItemViewHolderFactory()
        }

        messageListItemViewHolderFactory.decoratorProvider = MessageListItemDecoratorProvider(
            currentUser = currentUser,
            dateFormatter = messageDateFormatter,
            isDirectMessage = channel.isDirectMessaging()
        )
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

    public fun showLoadingView() {
        loadingViewContainer.isVisible = true
    }

    public fun hideLoadingView() {
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

    public fun showEmptyStateView() {
        emptyStateViewContainer.isVisible = true
    }

    public fun hideEmptyStateView() {
        emptyStateViewContainer.isVisible = false
    }

    public fun setNewMessagesBehaviour(newMessagesBehaviour: NewMessagesBehaviour) {
        scrollHelper.alwaysScrollToBottom = newMessagesBehaviour == NewMessagesBehaviour.SCROLL_TO_BOTTOM
    }

    public fun setScrollToBottomButtonEnabled(scrollToBottomButtonEnabled: Boolean) {
        scrollHelper.scrollToBottomButtonEnabled = scrollToBottomButtonEnabled
    }

    public fun setMessageViewHolderFactory(messageListItemViewHolderFactory: MessageListItemViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set MessageViewHolderFactory first" }
        this.messageListItemViewHolderFactory = messageListItemViewHolderFactory
    }

    public fun setMessageDateFormatter(messageDateFormatter: DateFormatter) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized; please set DateFormatter first" }
        this.messageDateFormatter = messageDateFormatter
    }

    public fun displayNewMessage(listItem: MessageListItemWrapper) {
        buffer.enqueueData(listItem)
    }

    private fun handleNewWrapper(listItem: MessageListItemWrapper) {
        buffer.hold()

        val isThreadStart = !adapter.isThread && listItem.isThread
        val isOldListEmpty = adapter.currentList.isEmpty()

        adapter.isThread = listItem.isThread
        adapter.submitList(listItem.items) {
            scrollHelper.onMessageListChanged(
                isThreadStart = isThreadStart,
                hasNewMessages = listItem.hasNewMessages,
                isInitialList = isOldListEmpty && listItem.items.isNotEmpty()
            )
            buffer.active()
        }
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
     * Sets the thread click listener to be used by MessageListView.
     *
     * @param threadClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setThreadClickListener(threadClickListener: ThreadClickListener?) {
        listenerContainer.threadClickListener =
            threadClickListener ?: DEFAULT_THREAD_CLICK_LISTENER
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
     * Sets the attachment download click listener to be used by MessageListView.
     *
     * @param attachmentDownloadClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setAttachmentDownloadClickListener(attachmentDownloadClickListener: AttachmentDownloadClickListener?) {
        listenerContainer.attachmentDownloadClickListener =
            attachmentDownloadClickListener ?: DEFAULT_ATTACHMENT_DOWNLOAD_CLICK_LISTENER
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
     * Sets the link click listener to be used by MessageListView.
     *
     * @param linkClickListener The listener to use. If null, the default will be used instead.
     */
    public fun setLinkClickListener(linkClickListener: LinkClickListener?) {
        listenerContainer.linkClickListener = linkClickListener ?: DEFAULT_LINK_CLICK_LISTENER
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

    public fun setOnMessageReactionHandler(onMessageReactionHandler: (Message, String) -> Unit) {
        this.onMessageReactionHandler = onMessageReactionHandler
    }

    public fun setOnMuteUserHandler(onUserMuteHandler: (User) -> Unit) {
        this.onMuteUserHandler = onUserMuteHandler
    }

    public fun setOnBlockUserHandler(onUserBlockHandler: (User, Channel) -> Unit) {
        val blockUserForThisChannel: (User) -> Unit = { user ->
            onUserBlockHandler(user, channel)
        }

        this.onBlockUserHandler = blockUserForThisChannel
    }

    public fun setOnReplyMessageHandler(onReplyMessageHandler: (cid: String, Message) -> Unit) {
        this.onReplyMessageHandler = onReplyMessageHandler
    }

    public fun setOnAttachmentDownloadHandler(onAttachmentDownloadHandler: (Attachment) -> Unit) {
        this.onAttachmentDownloadHandler = onAttachmentDownloadHandler
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

    public fun interface ThreadClickListener {
        public fun onThreadClick(message: Message)
    }

    public fun interface AttachmentClickListener {
        public fun onAttachmentClick(message: Message, attachment: Attachment)
    }

    public fun interface AttachmentDownloadClickListener {
        public fun onAttachmentDownloadClick(attachment: Attachment)
    }

    public fun interface GiphySendListener {
        public fun onGiphySend(message: Message, action: GiphyAction)
    }

    public fun interface LinkClickListener {
        public fun onLinkClick(url: String)
    }

    public fun interface UserClickListener {
        public fun onUserClick(user: User)
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
