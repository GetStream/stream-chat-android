package io.getstream.chat.docs.kotlin.ui.channels

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.feature.channels.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.feature.channels.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.channels.bindView
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.CustomChannelListItemBinding

/**
 * [Channel List](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/)
 */
private class ChannelList : Fragment() {

    private lateinit var channelListView: ChannelListView

    /**
     * [Usage](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#usage)
     */
    fun usage() {
        // Instantiate the ViewModel
        val viewModel: ChannelListViewModel by viewModels {
            ChannelListViewModelFactory(
                filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.`in`("members", listOf(ChatClient.instance().getCurrentUser()!!.id)),
                ),
                sort = QuerySortByField.descByName("last_updated"),
                limit = 30,
            )
        }
        // Bind the ViewModel with ChannelListView
        viewModel.bindView(channelListView, viewLifecycleOwner)
    }

    /**
     * [Handling Actions](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#handling-actions)
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
     * [Customization](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#customization)
     */
    fun customization(context: Context) {
        TransformStyle.channelListStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                optionsEnabled = false,
                foregroundLayoutColor = Color.LTGRAY,
                indicatorReadIcon = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_flag)!!,
                channelTitleText = defaultStyle.channelTitleText.copy(
                    color = Color.BLUE,
                    size = context.resources.getDimensionPixelSize(R.dimen.stream_ui_text_large),
                ),
                unreadMessageCounterBackgroundColor = Color.BLUE,
            )
        }
    }

    /**
     * [Creating a Custom ViewHolder Factory](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#creating-a-custom-viewholder-factory)
     */
    class CustomViewHolderFactory {

        class CustomChannelListItemViewHolderFactory : ChannelListItemViewHolderFactory() {
            override fun createChannelViewHolder(parentView: ViewGroup): BaseChannelListItemViewHolder {
                return CustomChannelViewHolder(parentView, listenerContainer.channelClickListener)
            }
        }

        class CustomChannelViewHolder(
            parent: ViewGroup,
            private val channelClickListener: ChannelListView.ChannelClickListener,
            private val binding: CustomChannelListItemBinding = CustomChannelListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
        ) : BaseChannelListItemViewHolder(binding.root) {

            private lateinit var channel: Channel

            init {
                binding.root.setOnClickListener { channelClickListener.onClick(channel) }
            }

            override fun bind(channel: Channel, diff: ChannelListPayloadDiff) {
                this.channel = channel

                binding.channelAvatarView.setChannel(channel)
                binding.channelNameTextView.text = ChatUI.channelNameFormatter.formatChannelName(
                    channel = channel,
                    currentUser = ChatClient.instance().getCurrentUser()
                )
                binding.membersCountTextView.text = itemView.context.resources.getQuantityString(
                    R.plurals.members_count,
                    channel.members.size,
                    channel.members.size
                )
            }
        }

        fun settingCustomViewHolderFactory(channelListView: ChannelListView) {
            // Create custom view holder factory
            val customFactory = CustomChannelListItemViewHolderFactory()

            // Set custom view holder factory
            channelListView.setViewHolderFactory(customFactory)
        }
    }

    /**
     * [Creating a Custom Loading View](https://getstream.io/chat/docs/sdk/android/ui/channel-components/channel-list/#creating-a-custom-loading-view)
     */
    fun customizingLoadingView() {
        // Inflate loading view
        val loadingView = LayoutInflater.from(context).inflate(R.layout.channel_list_loading_view, null)
        // Set loading view
        channelListView.setLoadingView(loadingView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }
}
