package io.getstream.chat.docs.cookbook.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.StyleTransformer
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.channel.list.ChannelListView
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.BaseChannelListItemViewHolder
import io.getstream.chat.android.ui.channel.list.adapter.viewholder.ChannelListItemViewHolderFactory
import io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
import io.getstream.chat.android.ui.channel.list.header.viewmodel.ChannelListHeaderViewModel
import io.getstream.chat.android.ui.channel.list.header.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.android.ui.common.extensions.getDisplayName
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.docs.R
import io.getstream.chat.docs.databinding.CustomChannelListItemBinding

/**
 * @see <a href="https://github.com/GetStream/stream-chat-android/wiki/UI-Cookbook#channel-list">Channel List</a>
 */
class ChannelList : Fragment() {

    lateinit var channelListHeaderView: ChannelListHeaderView
    lateinit var channelListView: ChannelListView

    fun bindingViewModels() {
        // Step 1: Create ViewModels
        val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()
        val channelListFactory: ChannelListViewModelFactory = ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`("members", ChatDomain.instance().user.value?.let(::listOf) ?: emptyList()),
            ),
            sort = QuerySort.desc(Channel::lastUpdated),
            limit = 30
        )
        val channelListViewModel: ChannelListViewModel by viewModels { channelListFactory }

        // Step 2: Bind views with view models
        channelListHeaderViewModel.bindView(channelListHeaderView, viewLifecycleOwner)
        channelListViewModel.bindView(channelListView, viewLifecycleOwner)
    }

    fun handlingChannelActions() {
        channelListHeaderView.setOnActionButtonClickListener {
            // Handle Action Button Click
        }
        channelListHeaderView.setOnUserAvatarClickListener {
            // Handle User Avatar Click
        }
        channelListView.setChannelItemClickListener { channel ->
            // Handle Channel Click
        }
        channelListView.setChannelInfoClickListener { channel ->
            // Handle Channel Info Click
        }
        channelListView.setUserClickListener { user ->
            // Handle Member Click
        }
    }

    fun changingChannelListViewStyle() {
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

    fun changingChannelListViewComponents() {
        // Inflate loading view
        val loadingView = LayoutInflater.from(context).inflate(R.layout.channel_list_loading_view, channelListView)
        // Set loading view
        channelListView.setLoadingView(loadingView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
    }

    fun creatingCustomViewModelFactory() {
        channelListView.setViewHolderFactory(CustomChannelListItemViewHolderFactory())
    }

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

            binding.apply {
                avatarView.setChannelData(channel)
                nameTextView.text = channel.getDisplayName(itemView.context)
                membersCountTextView.text = itemView.context.resources.getQuantityString(
                    R.plurals.members_count,
                    channel.members.size,
                    channel.members.size
                )
            }
        }
    }
}
