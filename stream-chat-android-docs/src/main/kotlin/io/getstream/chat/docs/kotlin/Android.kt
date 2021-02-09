package io.getstream.chat.docs.kotlin

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import io.getstream.chat.android.ui.MessageList.header.MessageListHeaderView
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.messages.header.bindView
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.SearchResultListView
import io.getstream.chat.android.ui.search.SearchViewModel
import io.getstream.chat.android.ui.search.bindView
import io.getstream.chat.android.ui.textinput.MessageInputView
import io.getstream.chat.android.ui.textinput.bindView

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
}
