package io.getstream.chat.docs.kotlin

import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.utils.RetryPolicy
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.list.SearchResultListView
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView
import io.getstream.chat.android.ui.suggestion.list.SuggestionListView
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Date

class Android {

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_header_view">Channel List Header View</a>
     */
    class ChannelListHeader(private val channelListHeaderView: ChannelListHeaderView) : Fragment() {
        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: ChannelListHeaderViewModel by viewModels()
            // Bind it with ChannelListHeaderView
            viewModel.bindView(channelListHeaderView, viewLifecycleOwner)
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/channel_list_view/">Channel List View</a>
     */
    class ChannelList(private val channelListView: ChannelListView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: ChannelListViewModel by viewModels {
                ChannelListViewModelFactory(
                    filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id)),
                    ),
                    sort = ChannelListViewModel.DEFAULT_SORT,
                    limit = 30,
                )
            }
            // Bind it with ChannelListView
            viewModel.bindView(channelListView, viewLifecycleOwner)
        }

        fun handlingChannelActions() {
            channelListView.setChannelInfoClickListener { channel ->
                // Handle Channel Info Click
            }

            channelListView.setUserClickListener { user ->
                // Handle Member Click
            }
        }

        fun handlingUserInteractions() {
            channelListView.setChannelItemClickListener { channel ->
                // Handle Channel Click
            }

            channelListView.setChannelLongClickListener { channel ->
                // Handle Channel Click
                true
            }
        }

        fun customizingDefaultViews() {
            // Create loading view and layout params
            val loadingView = ProgressBar(context)
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            )
            channelListView.setEmptyStateView(loadingView, layoutParams)

            // Create empty state view and use default layout params
            val emptyStateView = TextView(context).apply {
                text = "No channels available"
            }
            channelListView.setEmptyStateView(emptyStateView)

            // Set custom item separator drawable
            channelListView.setItemSeparator(R.drawable.stream_ui_divider)

            // Add separator to the last item
            channelListView.setShouldDrawItemSeparatorOnLastItem(true)
        }

        fun customViewHolderFactory() {
            class CustomChannelListItemViewHolderFactory : ChannelListItemViewHolderFactory() {
                override fun getItemViewType(item: ChannelListItem): Int {
                    // Override together with createViewHolder() to introduce different view holder types
                    return super.getItemViewType(item)
                }

                override fun createViewHolder(parentView: ViewGroup, viewType: Int): BaseChannelListItemViewHolder {
                    // Override to create custom create view holder logic
                    return super.createViewHolder(parentView, viewType)
                }

                override fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
                    // Create custom channel view holder
                    return super.createChannelViewHolder(parentView)
                }

                override fun createLoadingMoreViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
                    // Create custom loading more view holder
                    return super.createLoadingMoreViewHolder(parentView)
                }
            }

            // Create custom view holder factory
            val customFactory = CustomChannelListItemViewHolderFactory()

            // Set custom view holder factory
            channelListView.setViewHolderFactory(customFactory)
        }

        fun otherCustomizations() {
            TransformStyle.channelListStyleTransformer = StyleTransformer { defaultViewStyle ->
                // Modify default view style
                defaultViewStyle.copy(optionsEnabled = false)
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/message_input_view_neo">Message Input View</a>
     */
    class MessageInput(private val messageInputView: MessageInputView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
            val viewModel: MessageInputViewModel by viewModels { factory }
            // Bind it with MessageInputView
            viewModel.bindView(messageInputView, viewLifecycleOwner)
        }

        fun handlingUserInteractions() {
            messageInputView.setOnSendButtonClickListener {
                // Handle send button click
            }
            messageInputView.setTypingListener(
                object : MessageInputView.TypingListener {
                    override fun onKeystroke() {
                        // Handle keystroke case
                    }

                    override fun onStopTyping() {
                        // Handle stop typing case
                    }
                }
            )
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/message_list_header_view">Message List Header View</a>
     */
    class MessageListHeader(private val messageListHeaderView: MessageListHeaderView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
            val viewModel: MessageListHeaderViewModel by viewModels { factory }
            // Bind it with MessageListHeaderView
            viewModel.bindView(messageListHeaderView, viewLifecycleOwner)
        }

        fun handlingUserInteractions() {
            messageListHeaderView.setAvatarClickListener {
                // Handle avatar click
            }
            messageListHeaderView.setTitleClickListener {
                // Handle title click
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/search_input_view">Search Input View</a>
     */
    class SearchInput : Fragment() {
        lateinit var searchInputView: SearchInputView

        fun listeningForSearchQueryChanges() {
            searchInputView.setContinuousInputChangedListener {
                // Search query changed
            }
            searchInputView.setDebouncedInputChangedListener {
                // Search query changed and has been stable for a short while
            }
            searchInputView.setSearchStartedListener {
                // Search is triggered
            }

            // Update the current search query programmatically
            searchInputView.setQuery("query")
            // Clear the current search query programmatically
            searchInputView.clear()
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/search_result_list_view">Search Result List View</a>
     */
    class SearchResultList : Fragment() {
        lateinit var searchInputView: SearchInputView
        lateinit var searchResultListView: SearchResultListView

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: SearchViewModel by viewModels()
            // Bind it with SearchResultListView
            viewModel.bindView(searchResultListView, viewLifecycleOwner)
            // Notify ViewModel when search is triggered
            searchInputView.setSearchStartedListener {
                viewModel.setQuery(it)
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/attachmentgallery">Attachment Gallery Activity</a>
     */
    class AttachmentGalleryActivity : Fragment() {

        fun navigateTo() {
            val destination = AttachmentGalleryDestination(
                requireContext(),
                attachmentReplyOptionHandler = { resultItem ->
                    // Handle reply
                },
                attachmentShowInChatOptionHandler = { resultItem ->
                    // Handle show image in chat
                },
                attachmentDownloadOptionHandler = { resultItem ->
                    // Handle download image
                },
                attachmentDeleteOptionClickHandler = { resultItem ->
                    // Handle delete image
                },
            )

            activity?.activityResultRegistry?.let(destination::register)
            val attachmentGalleryItems: List<AttachmentGalleryItem> = listOf()
            destination.setData(attachmentGalleryItems, 0)

            ChatUI.navigator.navigate(destination)
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/message_list_view_new">Message List View</a>
     */
    class MessageListViewDocs : Fragment() {
        private lateinit var messageListView: MessageListView
        private val viewModel: MessageListViewModel by viewModels()

        fun emptyState() {
            // When there's no results, show empty state
            messageListView.showEmptyStateView()
        }

        fun loadingView() {
            // When loading information, show loading view
            messageListView.showLoadingView()
        }

        fun viewHolderFactory() {
            val newViewHolderFactory: MessageListItemViewHolderFactory = MessageListItemViewHolderFactoryExtended()
            messageListView.setMessageViewHolderFactory(newViewHolderFactory)
        }

        fun messageClick() {
            messageListView.setMessageClickListener { message ->
                // Handle message click
            }
        }

        fun messageLongClick() {
            messageListView.setMessageLongClickListener { message ->
                // Handle message long click
            }
        }

        fun dateFormatter() {
            messageListView.setMessageDateFormatter(
                object : DateFormatter {
                    override fun formatDate(localDateTime: LocalDateTime?): String {
                        // Provide a way to format Date
                        return DateTimeFormatter.ofPattern("dd/MM/yyyy").format(localDateTime)
                    }

                    override fun formatTime(localTime: LocalTime?): String {
                        // Provide a way to format Time.
                        return DateTimeFormatter.ofPattern("HH:mm").format(localTime)
                    }
                }
            )
        }

        fun customMessagesFilter() {
            messageListView.setMessageListItemPredicate { messageList ->
                // Boolean logic here
                true
            }
        }

        fun usingTransformStyle() {
            TransformStyle.messageListStyleTransformer = StyleTransformer { defaultMessageListViewStyle ->
                // Modify default MessageListView style
                defaultMessageListViewStyle.copy()
            }

            TransformStyle.messageListItemStyleTransformer = StyleTransformer { defaultMessageListItemStyle ->
                // Modify default MessageListItem style
                defaultMessageListItemStyle.copy()
            }
        }

        fun setNewMessageBehaviour() {
            messageListView.setNewMessagesBehaviour(MessageListView.NewMessagesBehaviour.COUNT_UPDATE)
        }

        fun setEndRegionReachedHandler() {
            messageListView.setEndRegionReachedHandler {
                // Handle pagination and include new logic

                // Option to log the event and use the viewModel
                viewModel.onEvent(MessageListViewModel.Event.EndRegionReached)
                Log.e("LogTag", "On load more")
            }
        }

        fun bindWithViewModel() {
            // Get ViewModel
            val factory: MessageListViewModelFactory = MessageListViewModelFactory(cid = "channelType:channelId")
            val viewModel: MessageListViewModel by viewModels { factory }
            // Bind it with MessageListView
            viewModel.bindView(messageListView, viewLifecycleOwner)
        }

        fun handlingUserInteractions() {
            messageListView.setMessageClickListener { message ->
                // Handle click on message
            }
            messageListView.setMessageLongClickListener { message ->
                // Handle long click on message
            }
            messageListView.setAttachmentClickListener { message, attachment ->
                // Handle long click on attachment
            }
            messageListView.setUserClickListener { user ->
                // Handle click on user avatar
            }
        }

        fun handlers() {
            messageListView.setMessageEditHandler { message ->
                // Handle edit message
            }
            messageListView.setMessageDeleteHandler { message ->
                // Handle delete message
            }
            messageListView.setAttachmentDownloadHandler { attachment ->
                // Handle attachment download
            }
        }

        fun displayNewMessage() {
            val messageItem = MessageListItem.MessageItem(
                message = Message(text = "Lorem ipsum dolor"),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            )

            val messageItemListWrapper = MessageListItemWrapper(listOf(messageItem))
            messageListView.displayNewMessages(messageItemListWrapper)
        }
    }

    class MessageListItemViewHolderFactoryExtended : MessageListItemViewHolderFactory() {
        override fun createViewHolder(
            parentView: ViewGroup,
            viewType: Int,
        ): BaseMessageItemViewHolder<out MessageListItem> {
            // Create a new type of view holder here, if needed
            return super.createViewHolder(parentView, viewType)
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/combining_view_models">Combining Views and View Models</a>
     */
    class CombiningViewsAndViewModels : Fragment() {
        lateinit var messageListView: MessageListView
        lateinit var messageListHeaderView: MessageListHeaderView
        lateinit var messageListViewModel: MessageListViewModel
        lateinit var messageListHeaderViewModel: MessageListHeaderViewModel
        lateinit var messageInputViewModel: MessageInputViewModel

        fun handlingThreads() {
            messageListViewModel.mode.observe(viewLifecycleOwner) { mode ->
                when (mode) {
                    is MessageListViewModel.Mode.Thread -> {
                        // Handle entering thread mode
                        messageListHeaderViewModel.setActiveThread(mode.parentMessage)
                        messageInputViewModel.setActiveThread(mode.parentMessage)
                    }
                    MessageListViewModel.Mode.Normal -> {
                        // Handle leaving thread mode
                        messageListHeaderViewModel.resetThread()
                        messageInputViewModel.resetThread()
                    }
                }
            }
        }

        fun editingMessage() {
            messageListView.setMessageEditHandler { message ->
                messageInputViewModel.editMessage.postValue(message)
            }
        }

        fun handlingBackButtonClicks() {
            messageListHeaderView.setBackButtonClickListener {
                messageListViewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/suggestion_list_view">Suggestion List View</a>
     */
    class SuggestionList : Fragment() {
        lateinit var messageInputView: MessageInputView
        lateinit var suggestionListView: SuggestionListView

        fun connectingSuggestionListViewWithMessageInputView() {
            messageInputView.setSuggestionListView(suggestionListView)
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/android_offline/?language=kotlin">Android Offline</a>
     */
    class AndroidOffline : Fragment() {

        fun initializeChatDomain() {
            val chatClient = ChatClient.Builder("apiKey", requireContext()).build()
            val chatDomain = ChatDomain.Builder(requireContext(), chatClient)
                .offlineEnabled()
                .userPresenceEnabled()
                .build()
        }

        fun getChatDomainInstance() {
            val chatDomain = ChatDomain.instance()
        }

        fun customizeRetryPolicy() {
            val chatDomain = ChatDomain.instance()

            chatDomain.retryPolicy = object : RetryPolicy {
                override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
                    return attempt < 3
                }

                override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int {
                    return 1000 * attempt
                }
            }
        }

        fun watchChannel() {
            val chatDomain = ChatDomain.instance()

            chatDomain.watchChannel(cid = "messaging:123", messageLimit = 0)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val channelController = result.data()

                        // LiveData objects to observe
                        channelController.messages
                        channelController.reads
                        channelController.typing
                    }
                }
        }

        fun loadMoreMessages() {
            val chatDomain = ChatDomain.instance()

            chatDomain.loadOlderMessages("messaging:123", 10)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val channel = result.data()
                    }
                }
        }

        fun sendMessage() {
            val chatDomain = ChatDomain.instance()
            val message = Message(text = "Hello world")

            chatDomain.sendMessage(message)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val message = result.data()
                    }
                }
        }

        fun queryChannels() {
            val chatDomain = ChatDomain.instance()
            val members = listOf("thierry")
            val filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", members),
            )
            val sort = QuerySort<Channel>()

            chatDomain.queryChannels(filter, sort)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val queryChannelsController = result.data()

                        // LiveData objects to observe
                        queryChannelsController.channels
                        queryChannelsController.loading
                        queryChannelsController.endOfChannels
                    }
                }
        }

        fun loadMoreFromChannel() {
            val chatDomain = ChatDomain.instance()
            val members = listOf("thierry")
            val filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", members),
            )
            val sort = QuerySort<Channel>()

            chatDomain.queryChannelsLoadMore(filter, sort)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val channels: List<Channel> = result.data()
                    }
                }
        }

        fun unreadCount() {
            val chatDomain = ChatDomain.instance()

            // LiveData objects to observe
            val totalUnreadCount = chatDomain.totalUnreadCount
            val unreadChannelCount = chatDomain.channelUnreadCount
        }

        fun messagesFromThread() {
            val chatDomain = ChatDomain.instance()

            chatDomain.getThread(cid = "cid", parentId = "parentId").enqueue { result ->
                if (result.isSuccess) {
                    val threadController = result.data()

                    // LiveData objects to observe
                    threadController.messages
                    threadController.loadingOlderMessages
                    threadController.endOfOlderMessages
                }
            }
        }

        fun loadMoreFromThread() {
            val chatDomain = ChatDomain.instance()

            chatDomain.threadLoadMore(cid = "cid", parentId = "parentId", messageLimit = 1)
                .enqueue { result ->
                    if (result.isSuccess) {
                        val messages: List<Message> = result.data()
                    }
                }
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/events/event_listening?language=kotlin">Listening for events</a>
     */
    class SyncHistory() : Fragment() {

        fun getSyncHistory(chatClient: ChatClient) {
            val cidList: List<String> = listOf("messaging:123")
            val lastSeenExample = Date()

            chatClient.getSyncHistory(cidList, lastSeenExample).enqueue { result ->
                if (result.isSuccess) {
                    val events: List<ChatEvent> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/unread_channel/?language=kotlin">Channels</a>
     */
    class UnreadCount : Fragment() {

        fun unreadCountInfo() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest()

            val channel = ChatClient.instance().queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest
            )
                .execute()
                .data()

            // readState is the list of read states for each user on the channel
            val readState: List<ChannelUserRead> = channel.read
        }

        fun unreadCountInfoChatDomain() {
            // Get channel
            val channel = ChatDomain.instance()
                .watchChannel(cid = "messaging:123", messageLimit = 0)
                .execute()
                .data()
                .toChannel()

            // readState is the list of read states for each user on the channel
            val readState: List<ChannelUserRead> = channel.read
        }

        fun unreadCountForCurrentUser() {
            // Get channel
            val queryChannelRequest = QueryChannelRequest()

            val channel = ChatClient.instance().queryChannel(
                channelType = "channel-type",
                channelId = "channel-id",
                request = queryChannelRequest
            )
                .execute()
                .data()

            // Unread count for current user
            val unreadCount: Int? = channel.unreadCount
        }

        fun unreadCountForCurrentUserChatDomain() {
            // Get channel controller
            val channelController = ChatDomain.instance()
                .watchChannel(cid = "messaging:123", messageLimit = 0)
                .execute()
                .data()

            // Unread count for current user
            val unreadCount: LiveData<Int?> = channelController.unreadCount
        }

        fun markAllRead() {
            ChatClient.instance().markAllRead().enqueue { result ->
                if (result.isSuccess) {
                    // Handle success
                } else {
                    // Handle failure
                }
            }
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/message_input_view?language=kotlin">Message Input View</a>
     */
    class TransformStyleMessageInput() : Fragment() {

        fun messageInputCustomisation() {
            TransformStyle.messageInputStyleTransformer = StyleTransformer { viewStyle ->
                viewStyle.copy(
                    messageInputTextColor = ContextCompat.getColor(requireContext(), R.color.stream_ui_white)
                )
            }
        }
    }
}
