package io.getstream.chat.docs.kotlin

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
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
import io.getstream.chat.android.ui.message.input.bindView
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.message.view.bindView
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.list.SearchResultListView
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter

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
                    sort = ChannelsViewModel.DEFAULT_SORT,
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
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/message_input_view_neo">Message Input View</a>
     */
    class MessageInput(private val messageInputView: MessageInputView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: MessageInputViewModel by viewModels()
            // Bind it with ChannelListHeaderView
            viewModel.bindView(messageInputView, viewLifecycleOwner)
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/message_list_header_view">Message List Header View</a>
     */
    class MessageListHeader(private val messageListHeaderView: MessageListHeaderView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: MessageListHeaderViewModel by viewModels()
            // Bind it with MessageListHeaderView
            viewModel.bindView(messageListHeaderView, viewLifecycleOwner)
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

            ChatUI.instance().navigator.navigate(destination)
        }
    }

    /**
     * @see <a href="https://getstream.io/nessy/docs/chat_docs/android_chat_ux/message_list_view_new">Message List View</a>
     */
    class MessageListViewDocs : Fragment() {
        lateinit var messageListView: MessageListView

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

        fun bindWithViewModel() {
            val viewModel: MessageListViewModel by viewModels()
            viewModel.bindView(messageListView, viewLifecycleOwner)
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
}
