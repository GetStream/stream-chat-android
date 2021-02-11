package io.getstream.chat.docs.kotlin

import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelsViewModelFactory
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelsView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.android.ui.message.input.MessageInputView
import io.getstream.chat.android.ui.message.input.bindView
import io.getstream.chat.android.ui.message.list.header.MessageListHeaderView
import io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel
import io.getstream.chat.android.ui.message.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.search.SearchInputView
import io.getstream.chat.android.ui.search.list.SearchResultListView
import io.getstream.chat.android.ui.search.list.viewmodel.SearchViewModel
import io.getstream.chat.android.ui.search.list.viewmodel.bindView

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
     * @see <a href="https://getstream.io/chat/docs/node/channels_view_new/">Channels View</a>
     */
    class Channels(private val channelsView: ChannelsView) : Fragment() {

        fun bindingWithViewModel() {
            // Get ViewModel
            val viewModel: ChannelsViewModel by viewModels {
                ChannelsViewModelFactory(
                    filter = Filters.and(
                        Filters.eq("type", "messaging"),
                        Filters.`in`("members", listOf(ChatDomain.instance().currentUser.id)),
                    ),
                    sort = ChannelsViewModel.DEFAULT_SORT,
                    limit = 30,
                )
            }
            // Bind it with ChannelsView
            viewModel.bindView(channelsView, viewLifecycleOwner)
        }

        fun handlingChannelActions() {
            channelsView.setChannelInfoClickListener { channel ->
                // Handle Channel Info Click
            }

            channelsView.setUserClickListener { user ->
                // Handle Member Click
            }
        }

        fun handlingUserInteractions() {
            channelsView.setChannelItemClickListener { channel ->
                // Handle Channel Click
            }

            channelsView.setChannelLongClickListener { channel ->
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
            channelsView.setEmptyStateView(loadingView, layoutParams)

            // Create empty state view and use default layout params
            val emptyStateView = TextView(context).apply {
                text = "No channels available"
            }
            channelsView.setEmptyStateView(emptyStateView)

            // Set custom item separator drawable
            channelsView.setItemSeparator(R.drawable.stream_ui_divider)

            // Add separator to the last item
            channelsView.setShouldDrawItemSeparatorOnLastItem(true)
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
            channelsView.setViewHolderFactory(customFactory)
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
            val viewModel: MessageListHeaderViewModel by viewModels()
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
