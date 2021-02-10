package io.getstream.chat.docs.kotlin

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewHolderFactory
import io.getstream.chat.android.ui.messages.header.MessageListHeaderView
import io.getstream.chat.android.ui.messages.header.bindView
import io.getstream.chat.android.ui.messages.view.MessageListView
import io.getstream.chat.android.ui.messages.view.bindView
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.SearchResultListView
import io.getstream.chat.android.ui.search.SearchViewModel
import io.getstream.chat.android.ui.search.bindView
import io.getstream.chat.android.ui.textinput.MessageInputView
import io.getstream.chat.android.ui.textinput.bindView
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
     * @see <a href="https://getstream.io/chat/docs/android/messages_header_view">Message List Header View</a>
     */
    class MessageListHeader(private val messageListHeaderView: MessageListHeaderView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: ChannelHeaderViewModel by viewModels()
            // Bind it with MessagesHeaderView
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
