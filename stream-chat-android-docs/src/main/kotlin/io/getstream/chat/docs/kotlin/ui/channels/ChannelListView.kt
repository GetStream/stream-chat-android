// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.channels

import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListItem
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.android.ui.common.style.TextStyle

/**
 * [Channel List](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list/)
 */
private class ChannelListViewSnippets() : Fragment() {

    private lateinit var channelListView: ChannelListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list/#usage)
     */
    fun usage() {
        // Instantiate the ViewModel
        val viewModel: ChannelListViewModel by viewModels {
            ChannelListViewModelFactory(
                filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()!!.id)),
                ),
                sort = ChannelListViewModel.DEFAULT_SORT,
                limit = 30,
            )
        }
        // Bind the ViewModel with ChannelListView
        viewModel.bindView(channelListView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list/#handling-actions)
     */
    fun handlingActions() {
        channelListView.setChannelItemClickListener { channel ->
            // Handle channel click
        }
        channelListView.setChannelInfoClickListener { channel ->
            // Handle channel info click
        }
        channelListView.setUserClickListener { user ->
            // Handle member click
        }
    }

    /**
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list/#customization)
     */
    fun customization() {
        TransformStyle.channelListStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                optionsEnabled = false,
                foregroundLayoutColor = Color.LTGRAY,
                indicatorReadIcon = ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_ic_clock)!!,
                channelTitleText = TextStyle(
                    color = Color.WHITE,
                    size = resources.getDimensionPixelSize(R.dimen.stream_ui_text_large),
                ),
                lastMessageText = TextStyle(
                    size = resources.getDimensionPixelSize(R.dimen.stream_ui_text_small),
                ),
                unreadMessageCounterBackgroundColor = Color.BLUE,
            )
        }
    }

    /**
     * [Creating a Custom ViewHolder Factory](https://getstream.io/chat/docs/sdk/android/ui/components/channel-list/#creating-a-custom-viewholder-factory)
     */
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
}
