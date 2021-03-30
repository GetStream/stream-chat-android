package io.getstream.chat.android.ui.message.list

import android.animation.LayoutTransition
import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.enums.GiphyAction
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.ListenerDelegate
import com.getstream.sdk.chat.utils.StartStopBuffer
import com.getstream.sdk.chat.utils.extensions.activity
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.getstream.sdk.chat.utils.extensions.inflater
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.getstream.sdk.chat.view.EndlessScrollListener
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.extensions.internal.getFragmentManager
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser
import io.getstream.chat.android.ui.common.extensions.internal.isMedia
import io.getstream.chat.android.ui.common.extensions.isInThread
import io.getstream.chat.android.ui.common.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.ui.common.navigation.destinations.WebLinkDestination
import io.getstream.chat.android.ui.databinding.StreamUiMessageListViewBinding
import io.getstream.chat.android.ui.gallery.AttachmentGalleryActivity
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.gallery.toAttachment
import io.getstream.chat.android.ui.message.list.MessageListView.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.AttachmentDownloadHandler
import io.getstream.chat.android.ui.message.list.MessageListView.ConfirmDeleteMessageHandler
import io.getstream.chat.android.ui.message.list.MessageListView.EndRegionReachedHandler
import io.getstream.chat.android.ui.message.list.MessageListView.EnterThreadListener
import io.getstream.chat.android.ui.message.list.MessageListView.GiphySendHandler
import io.getstream.chat.android.ui.message.list.MessageListView.GiphySendListener
import io.getstream.chat.android.ui.message.list.MessageListView.LastMessageReadHandler
import io.getstream.chat.android.ui.message.list.MessageListView.LinkClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.MessageClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.MessageDeleteHandler
import io.getstream.chat.android.ui.message.list.MessageListView.MessageEditHandler
import io.getstream.chat.android.ui.message.list.MessageListView.MessageFlagHandler
import io.getstream.chat.android.ui.message.list.MessageListView.MessageLongClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.MessageReactionHandler
import io.getstream.chat.android.ui.message.list.MessageListView.MessageReplyHandler
import io.getstream.chat.android.ui.message.list.MessageListView.MessageRetryHandler
import io.getstream.chat.android.ui.message.list.MessageListView.MessageRetryListener
import io.getstream.chat.android.ui.message.list.MessageListView.ReactionViewClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.ThreadClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.ThreadStartHandler
import io.getstream.chat.android.ui.message.list.MessageListView.UserBlockHandler
import io.getstream.chat.android.ui.message.list.MessageListView.UserClickListener
import io.getstream.chat.android.ui.message.list.MessageListView.UserMuteHandler
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemAdapter
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemDecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.internal.HiddenMessageListItemPredicate
import io.getstream.chat.android.ui.message.list.internal.MessageListScrollHelper
import io.getstream.chat.android.ui.message.list.options.message.internal.MessageOptionsDialogFragment
import io.getstream.chat.android.ui.message.list.options.message.internal.MessageOptionsView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        private const val LOAD_MORE_THRESHOLD = 10
    }

    private lateinit var messageListViewStyle: MessageListViewStyle

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

    private var endRegionReachedHandler = EndRegionReachedHandler {
        throw IllegalStateException("endRegionReachedHandler must be set.")
    }
    private var lastMessageReadHandler = LastMessageReadHandler {
        throw IllegalStateException("lastMessageReadHandler must be set.")
    }
    private var messageEditHandler = MessageEditHandler {
        throw IllegalStateException("onMessageEditHandler must be set.")
    }
    private var messageDeleteHandler = MessageDeleteHandler {
        throw IllegalStateException("onMessageDeleteHandler must be set.")
    }
    private var threadStartHandler = ThreadStartHandler {
        throw IllegalStateException("onStartThreadHandler must be set.")
    }
    private var messageFlagHandler = MessageFlagHandler {
        throw IllegalStateException("onMessageFlagHandler must be set.")
    }
    private var giphySendHandler = GiphySendHandler { _, _ ->
        throw IllegalStateException("onSendGiphyHandler must be set.")
    }
    private var messageRetryHandler = MessageRetryHandler {
        throw IllegalStateException("onMessageRetryHandler must be set.")
    }
    private var messageReactionHandler = MessageReactionHandler { _, _ ->
        throw IllegalStateException("onMessageReactionHandler must be set.")
    }
    private var userMuteHandler = UserMuteHandler {
        throw IllegalStateException("onMuteUserHandler must be set.")
    }
    private var userBlockHandler = UserBlockHandler { _, _ ->
        throw IllegalStateException("onBlockUserHandler must be set.")
    }
    private var messageReplyHandler = MessageReplyHandler { _, _ ->
        throw IllegalStateException("onReplyMessageHandler must be set")
    }
    private var attachmentDownloadHandler = AttachmentDownloadHandler {
        throw IllegalStateException("onAttachmentDownloadHandler must be set")
    }

    private var confirmDeleteMessageHandler = ConfirmDeleteMessageHandler { _, confirmCallback ->
        AlertDialog.Builder(context)
            .setTitle(R.string.stream_ui_message_option_delete_confirmation_title)
            .setMessage(R.string.stream_ui_message_option_delete_confirmation_message)
            .setPositiveButton(R.string.stream_ui_message_option_delete_positive_button) { dialog, _ ->
                dialog.dismiss()
                confirmCallback()
            }
            .setNegativeButton(R.string.stream_ui_message_option_delete_negative_button) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private var _attachmentReplyOptionHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentReplyOptionHandler {
            throw IllegalStateException("onAttachmentReplyOptionClickHandler must be set")
        }
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentReplyOptionHandler { realListener().onClick(it) }
    }

    private var _attachmentShowInChatOptionClickHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentShowInChatOptionHandler {
            throw IllegalStateException("onAttachmentShowInChatOptionClickHandler must be set")
        }
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentShowInChatOptionHandler { realListener().onClick(it) }
    }

    private var _attachmentDownloadOptionHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentDownloadOptionHandler { attachmentData ->
            DEFAULT_ATTACHMENT_DOWNLOAD_CLICK_LISTENER.onAttachmentDownloadClick(attachmentData.toAttachment())
        }
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentDownloadOptionHandler { realListener().onClick(it) }
    }

    private var _attachmentDeleteOptionHandler by ListenerDelegate(
        initialValue = AttachmentGalleryActivity.AttachmentDeleteOptionHandler {
            throw IllegalStateException("onAttachmentDeleteOptionClickHandler must be set")
        }
    ) { realListener ->
        AttachmentGalleryActivity.AttachmentDeleteOptionHandler { attachmentData ->
            realListener().onClick(attachmentData)
        }
    }

    private var messageListItemPredicate: MessageListItemPredicate = HiddenMessageListItemPredicate

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
                threadStartHandler.onStartThread(message)
            }
        }
    private val DEFAULT_MESSAGE_LONG_CLICK_LISTENER =
        MessageLongClickListener { message ->
            context.getFragmentManager()?.let { fragmentManager ->
                MessageOptionsDialogFragment
                    .newMessageOptionsInstance(
                        message,
                        messageOptionsConfiguration.copy(
                            threadsEnabled = !adapter.isThread && !message.isInThread() && messageOptionsConfiguration.threadsEnabled,
                        ),
                        messageListViewStyle.itemStyle,
                        channel.config.isReactionsEnabled && messageListViewStyle.reactionsEnabled
                    )
                    .apply {
                        setReactionClickHandler { message, reactionType ->
                            messageReactionHandler.onMessageReaction(message, reactionType)
                        }
                        setConfirmDeleteMessageClickHandler { message, callback ->
                            confirmDeleteMessageHandler.onConfirmDeleteMessage(
                                message,
                                callback::onConfirmDeleteMessage
                            )
                        }
                        setMessageOptionsHandlers(
                            MessageOptionsDialogFragment.MessageOptionsHandlers(
                                threadReplyHandler = threadStartHandler,
                                retryHandler = messageRetryHandler,
                                editClickHandler = messageEditHandler,
                                flagClickHandler = messageFlagHandler,
                                muteClickHandler = userMuteHandler,
                                blockClickHandler = userBlockHandler,
                                deleteClickHandler = messageDeleteHandler,
                                replyClickHandler = messageReplyHandler,
                            )
                        )
                    }
                    .show(fragmentManager, MessageOptionsDialogFragment.TAG)
            }
        }
    private val DEFAULT_MESSAGE_RETRY_LISTENER =
        MessageRetryListener { message ->
            messageRetryHandler.onMessageRetry(message)
        }
    private val DEFAULT_THREAD_CLICK_LISTENER =
        ThreadClickListener { message ->
            if (message.replyCount > 0) {
                threadStartHandler.onStartThread(message)
            }
        }

    private val attachmentGalleryDestination =
        AttachmentGalleryDestination(
            context,
            _attachmentReplyOptionHandler,
            _attachmentShowInChatOptionClickHandler,
            _attachmentDownloadOptionHandler,
            _attachmentDeleteOptionHandler,
        )

    private val DEFAULT_ATTACHMENT_CLICK_LISTENER =
        AttachmentClickListener { message, attachment ->
            val destination = when {
                message.attachments.all(Attachment::isMedia) -> {
                    val filteredAttachments = message.attachments
                        .filter { it.type == ModelType.attach_image && !it.imagePreviewUrl.isNullOrEmpty() }
                    val attachmentGalleryItems = filteredAttachments.map {
                        AttachmentGalleryItem(
                            attachment = it,
                            user = message.user,
                            createdAt = message.getCreatedAtOrThrow(),
                            messageId = message.id,
                            cid = message.cid,
                            isMine = message.user.isCurrentUser()
                        )
                    }
                    val attachmentIndex = filteredAttachments.indexOf(attachment)

                    attachmentGalleryDestination.setData(attachmentGalleryItems, attachmentIndex)
                    attachmentGalleryDestination
                }
                else -> AttachmentDestination(message, attachment, context)
            }
            ChatUI.navigator.navigate(destination)
        }

    private val DEFAULT_ATTACHMENT_DOWNLOAD_CLICK_LISTENER =
        AttachmentDownloadClickListener { attachment ->
            attachmentDownloadHandler.onAttachmentDownload(attachment)
            Toast.makeText(
                context,
                context.getString(R.string.stream_ui_attachment_downloading_started),
                Toast.LENGTH_SHORT
            ).show()
        }
    private val DEFAULT_REACTION_VIEW_CLICK_LISTENER =
        ReactionViewClickListener { message: Message ->
            context.getFragmentManager()?.let {
                MessageOptionsDialogFragment.newReactionOptionsInstance(
                    message,
                    messageListViewStyle.itemStyle,
                    channel.config.isReactionsEnabled && messageListViewStyle.reactionsEnabled
                ).apply {
                    setReactionClickHandler { message, reactionType ->
                        messageReactionHandler.onMessageReaction(message, reactionType)
                    }
                }
                    .show(it, MessageOptionsDialogFragment.TAG)
            }
        }
    private val DEFAULT_USER_CLICK_LISTENER = UserClickListener { /* Empty */ }
    private val DEFAULT_GIPHY_SEND_LISTENER =
        GiphySendListener { message, action ->
            giphySendHandler.onSendGiphy(message, action)
        }
    private val DEFAULT_LINK_CLICK_LISTENER = LinkClickListener { url ->
        ChatUI.navigator.navigate(WebLinkDestination(url, context, ChatUI.urlSigner))
    }
    private val DEFAULT_ENTER_THREAD_LISTENER = EnterThreadListener {
        // Empty
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
    private var enterThreadListener = DEFAULT_ENTER_THREAD_LISTENER

    private lateinit var messageListItemViewHolderFactory: MessageListItemViewHolderFactory
    private lateinit var messageDateFormatter: DateFormatter
    private lateinit var attachmentViewFactory: AttachmentViewFactory

    public constructor(context: Context) : super(context) {
        init(context, null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }

    private fun init(context: Context, attr: AttributeSet?) {
        messageListViewStyle = MessageListViewStyle(context, attr)

        binding = StreamUiMessageListViewBinding.inflate(context.inflater, this)

        initRecyclerView()
        initScrollHelper()
        initLoadingView()
        initEmptyStateView()

        configureAttributes(attr)
        layoutTransition = LayoutTransition()

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
            lastMessageReadHandler.onLastMessageRead()
        }
    }

    private fun configureAttributes(attributeSet: AttributeSet?) {
        val tArray = context
            .obtainStyledAttributes(attributeSet, R.styleable.MessageListView)

        tArray.getInteger(
            R.styleable.MessageListView_streamUiLoadMoreThreshold,
            LOAD_MORE_THRESHOLD,
        ).also { loadMoreThreshold ->
            loadMoreListener = EndlessScrollListener(loadMoreThreshold) {
                endRegionReachedHandler.onEndRegionReached()
            }
        }

        binding.scrollToBottomButton.setScrollButtonViewStyle(messageListViewStyle.scrollButtonViewStyle)
        scrollHelper.scrollToBottomButtonEnabled = messageListViewStyle.scrollButtonViewStyle.scrollButtonEnabled

        NewMessagesBehaviour.parseValue(
            tArray.getInt(
                R.styleable.MessageListView_streamUiNewMessagesBehaviour,
                NewMessagesBehaviour.COUNT_UPDATE.value
            )
        ).also {
            scrollHelper.alwaysScrollToBottom = it == NewMessagesBehaviour.SCROLL_TO_BOTTOM
        }

        configureMessageOptions(tArray)
        tArray.recycle()
        if (background == null) {
            setBackgroundColor(messageListViewStyle.backgroundColor)
        }
    }

    private fun configureMessageOptions(tArray: TypedArray) {
        val iconsTint = tArray.getColor(
            R.styleable.MessageListView_streamUiMessageOptionIconColor,
            ContextCompat.getColor(context, R.color.stream_ui_grey)
        )

        val replyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiReplyOptionIcon,
            R.drawable.stream_ui_ic_arrow_curve_left
        )

        val replyEnabled = tArray.getBoolean(R.styleable.MessageListView_streamUiReplyEnabled, true)

        val threadReplyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiThreadReplyOptionIcon,
            R.drawable.stream_ui_ic_thread_reply
        )

        val retryIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiRetryOptionIcon,
            R.drawable.stream_ui_ic_send
        )

        val copyIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiCopyOptionIcon,
            R.drawable.stream_ui_ic_copy
        )

        val editIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiEditOptionIcon,
            R.drawable.stream_ui_ic_edit
        )

        val flagIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiFlagOptionIcon,
            R.drawable.stream_ui_ic_flag
        )

        val muteIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiMuteOptionIcon,
            R.drawable.stream_ui_ic_mute
        )

        val blockIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiBlockOptionIcon,
            R.drawable.stream_ui_ic_user_block
        )

        val deleteIcon = tArray.getResourceId(
            R.styleable.MessageListView_streamUiDeleteOptionIcon,
            R.drawable.stream_ui_ic_delete
        )

        val copyTextEnabled = tArray.getBoolean(R.styleable.MessageListView_streamUiCopyMessageActionEnabled, true)

        val deleteConfirmationEnabled =
            tArray.getBoolean(R.styleable.MessageListView_streamUiDeleteConfirmationEnabled, true)

        val deleteMessageEnabled =
            tArray.getBoolean(R.styleable.MessageListView_streamUiDeleteMessageEnabled, true)

        val editMessageEnabled = tArray.getBoolean(R.styleable.MessageListView_streamUiEditMessageEnabled, true)

        val threadsEnabled = tArray.getBoolean(R.styleable.MessageListView_streamUiThreadsEnabled, true)

        messageOptionsConfiguration = MessageOptionsView.Configuration(
            iconsTint = iconsTint,
            replyIcon = replyIcon,
            threadReplyIcon = threadReplyIcon,
            retryIcon = retryIcon,
            copyIcon = copyIcon,
            editMessageEnabled = editMessageEnabled,
            editIcon = editIcon,
            flagIcon = flagIcon,
            muteIcon = muteIcon,
            blockIcon = blockIcon,
            deleteIcon = deleteIcon,
            replyEnabled = replyEnabled,
            threadsEnabled = threadsEnabled,
            copyTextEnabled = copyTextEnabled,
            deleteConfirmationEnabled = deleteConfirmationEnabled,
            deleteMessageEnabled = deleteMessageEnabled
        )
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        activity?.activityResultRegistry?.let { registry ->
            attachmentGalleryDestination.register(registry)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        attachmentGalleryDestination.unregister()
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

        messageOptionsConfiguration = messageOptionsConfiguration.copy(
            replyEnabled = messageOptionsConfiguration.replyEnabled && channel.config.isRepliesEnabled,
            threadsEnabled = messageOptionsConfiguration.threadsEnabled && channel.config.isRepliesEnabled,
        )
    }

    private fun initAdapter() {
        // Create default DateFormatter if needed
        if (::messageDateFormatter.isInitialized.not()) {
            messageDateFormatter = DateFormatter.from(context)
        }

        if (::attachmentViewFactory.isInitialized.not()) {
            attachmentViewFactory = AttachmentViewFactory()
        }

        // Create default ViewHolderFactory if needed
        if (::messageListItemViewHolderFactory.isInitialized.not()) {
            messageListItemViewHolderFactory = MessageListItemViewHolderFactory()
        }

        messageListItemViewHolderFactory.decoratorProvider = MessageListItemDecoratorProvider(
            currentUser = currentUser,
            dateFormatter = messageDateFormatter,
            isDirectMessage = channel.isDirectMessaging(),
            messageListViewStyle.itemStyle,
        )

        messageListItemViewHolderFactory.setListenerContainer(this.listenerContainer)
        messageListItemViewHolderFactory.setAttachmentViewFactory(this.attachmentViewFactory)
        messageListItemViewHolderFactory.setMessageListItemStyle(this.messageListViewStyle.itemStyle)

        adapter = MessageListItemAdapter(messageListItemViewHolderFactory)
        adapter.setHasStableIds(true)

        setMessageListItemAdapter(adapter)
    }

    /**
     * @param view will be added to the view hierarchy of [MessageListView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container [ViewGroup].
     */
    @JvmOverloads
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
     * @param view will be added to the view hierarchy of [MessageListView] and managed by it.
     * The view should not be added to another [ViewGroup] instance elsewhere.
     * @param layoutParams defines how the view will be situated inside its container [ViewGroup].
     */
    @JvmOverloads
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

    /**
     * Enables or disables the message editing feature.
     *
     * @param enabled True if editing a message is enabled, false otherwise.
     */
    public fun setEditMessageEnabled(enabled: Boolean) {
        updateMessageOptionsConfiguration { copy(editMessageEnabled = enabled) }
    }

    /**
     * Enables or disables the message deleting feature.
     *
     * @param enabled True if deleting a message is enabled, false otherwise.
     */
    public fun setDeleteMessageEnabled(enabled: Boolean) {
        updateMessageOptionsConfiguration { copy(deleteMessageEnabled = enabled) }
    }

    public fun setMessageViewHolderFactory(messageListItemViewHolderFactory: MessageListItemViewHolderFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set MessageViewHolderFactory first" }
        this.messageListItemViewHolderFactory = messageListItemViewHolderFactory
    }

    public fun setMessageDateFormatter(messageDateFormatter: DateFormatter) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized; please set DateFormatter first" }
        this.messageDateFormatter = messageDateFormatter
    }

    public fun displayNewMessages(listItem: MessageListItemWrapper) {
        buffer.enqueueData(listItem)
    }

    public fun setMessageListItemPredicate(messageListItemPredicate: MessageListItemPredicate) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set MessageListItemPredicate first" }
        this.messageListItemPredicate = messageListItemPredicate
    }

    public fun setAttachmentViewFactory(attachmentViewFactory: AttachmentViewFactory) {
        check(::adapter.isInitialized.not()) { "Adapter was already initialized, please set AttachmentViewFactory first" }
        this.attachmentViewFactory = attachmentViewFactory
    }

    private fun handleNewWrapper(listItem: MessageListItemWrapper) {
        CoroutineScope(DispatcherProvider.IO).launch {
            val filteredList = listItem.items.filter(messageListItemPredicate::predicate)
            withContext(DispatcherProvider.Main) {
                buffer.hold()

                val isThreadStart = !adapter.isThread && listItem.isThread
                val isOldListEmpty = adapter.currentList.isEmpty()
                if (isThreadStart) {
                    listItem.items
                        .asSequence()
                        .filterIsInstance(MessageListItem.MessageItem::class.java)
                        .firstOrNull { it.message.parentId == null }
                        ?.let { enterThreadListener.onThreadEntered(it.message) }
                }
                adapter.isThread = listItem.isThread
                adapter.submitList(filteredList) {
                    scrollHelper.onMessageListChanged(
                        isThreadStart = isThreadStart,
                        hasNewMessages = listItem.hasNewMessages,
                        isInitialList = isOldListEmpty && filteredList.isNotEmpty()
                    )
                    buffer.active()
                }
            }
        }
    }

    private fun updateMessageOptionsConfiguration(
        reducer: MessageOptionsView.Configuration.() -> MessageOptionsView.Configuration,
    ) {
        check(::messageOptionsConfiguration.isInitialized) {
            "Message options configuration needs to be initialized first"
        }
        messageOptionsConfiguration = reducer(messageOptionsConfiguration)
    }

    //region Listener setters
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

    /**
     * Sets the thread click listener to be used by MessageListView.
     *
     * @param enterThreadListener The listener to use. If null, the default will be used instead.
     */
    public fun setEnterThreadListener(enterThreadListener: EnterThreadListener?) {
        this.enterThreadListener = enterThreadListener ?: DEFAULT_ENTER_THREAD_LISTENER
    }
    //endregion

    //region Handler setters
    public fun setEndRegionReachedHandler(endRegionReachedHandler: EndRegionReachedHandler) {
        this.endRegionReachedHandler = endRegionReachedHandler
    }

    public fun setLastMessageReadHandler(lastMessageReadHandler: LastMessageReadHandler) {
        this.lastMessageReadHandler = lastMessageReadHandler
    }

    public fun setMessageEditHandler(messageEditHandler: MessageEditHandler) {
        this.messageEditHandler = messageEditHandler
    }

    public fun setMessageDeleteHandler(messageDeleteHandler: MessageDeleteHandler) {
        this.messageDeleteHandler = messageDeleteHandler
    }

    public fun setThreadStartHandler(threadStartHandler: ThreadStartHandler) {
        this.threadStartHandler = threadStartHandler
    }

    public fun setMessageFlagHandler(messageFlagHandler: MessageFlagHandler) {
        this.messageFlagHandler = messageFlagHandler
    }

    public fun setGiphySendHandler(giphySendHandler: GiphySendHandler) {
        this.giphySendHandler = giphySendHandler
    }

    public fun setMessageRetryHandler(messageRetryHandler: MessageRetryHandler) {
        this.messageRetryHandler = messageRetryHandler
    }

    public fun setMessageReactionHandler(messageReactionHandler: MessageReactionHandler) {
        this.messageReactionHandler = messageReactionHandler
    }

    public fun setUserMuteHandler(userMuteHandler: UserMuteHandler) {
        this.userMuteHandler = userMuteHandler
    }

    public fun setUserBlockHandler(userBlockHandler: UserBlockHandler) {
        this.userBlockHandler = userBlockHandler
    }

    public fun setMessageReplyHandler(messageReplyHandler: MessageReplyHandler) {
        this.messageReplyHandler = messageReplyHandler
    }

    public fun setAttachmentDownloadHandler(attachmentDownloadHandler: AttachmentDownloadHandler) {
        this.attachmentDownloadHandler = attachmentDownloadHandler
    }

    public fun setConfirmDeleteMessageHandler(confirmDeleteMessageHandler: ConfirmDeleteMessageHandler) {
        this.confirmDeleteMessageHandler = confirmDeleteMessageHandler
    }

    public fun setAttachmentReplyOptionClickHandler(handler: AttachmentGalleryActivity.AttachmentReplyOptionHandler) {
        this._attachmentReplyOptionHandler = handler
    }

    public fun setAttachmentShowInChatOptionClickHandler(handler: AttachmentGalleryActivity.AttachmentShowInChatOptionHandler) {
        this._attachmentShowInChatOptionClickHandler = handler
    }

    public fun setDownloadOptionHandler(handler: AttachmentGalleryActivity.AttachmentDownloadOptionHandler) {
        this._attachmentDownloadOptionHandler = handler
    }

    public fun setAttachmentDeleteOptionClickHandler(handler: AttachmentGalleryActivity.AttachmentDeleteOptionHandler) {
        this._attachmentDeleteOptionHandler = handler
    }

    public fun setReactionsEnabled(enabled: Boolean) {
        messageListViewStyle = messageListViewStyle.copy(
            reactionsEnabled = enabled,
            itemStyle = messageListViewStyle.itemStyle.copy(reactionsEnabled = enabled)
        )
    }

    public fun setRepliesEnabled(enabled: Boolean) {
        messageOptionsConfiguration = messageOptionsConfiguration.copy(replyEnabled = enabled)
    }

    public fun setThreadsEnabled(enabled: Boolean) {
        updateMessageOptionsConfiguration { copy(threadsEnabled = enabled) }
    }

    //endregion

    //region Listener declarations
    public fun interface EnterThreadListener {
        public fun onThreadEntered(message: Message)
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
    //endregion

    //region Handler declarations
    public fun interface EndRegionReachedHandler {
        public fun onEndRegionReached()
    }

    public fun interface LastMessageReadHandler {
        public fun onLastMessageRead()
    }

    public fun interface MessageEditHandler {
        public fun onMessageEdit(message: Message)
    }

    public fun interface MessageDeleteHandler {
        public fun onMessageDelete(message: Message)
    }

    public fun interface ConfirmDeleteMessageHandler {
        public fun onConfirmDeleteMessage(
            message: Message,
            confirmCallback: () -> Unit,
        )
    }

    public fun interface MessageFlagHandler {
        public fun onMessageFlag(message: Message)
    }

    public fun interface MessageRetryHandler {
        public fun onMessageRetry(message: Message)
    }

    public fun interface MessageReactionHandler {
        public fun onMessageReaction(message: Message, reactionType: String)
    }

    public fun interface MessageReplyHandler {
        public fun onMessageReply(cid: String, message: Message)
    }

    public fun interface ThreadStartHandler {
        public fun onStartThread(message: Message)
    }

    public fun interface GiphySendHandler {
        public fun onSendGiphy(message: Message, action: GiphyAction)
    }

    public fun interface UserMuteHandler {
        public fun onUserMute(user: User)
    }

    public fun interface UserBlockHandler {
        public fun onUserBlock(user: User, cid: String)
    }

    public fun interface AttachmentDownloadHandler {
        public fun onAttachmentDownload(attachment: Attachment)
    }
    //endregion

    /**
     * Predicate object with a filter condition for MessageListItem. Used to filter a list of MessageListItem
     * before applying it to MessageListView.
     */
    public fun interface MessageListItemPredicate {
        public fun predicate(item: MessageListItem): Boolean
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
