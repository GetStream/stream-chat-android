package io.getstream.chat.ui.sample.feature.chat.info.shared

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.ui.channel.list.viewmodel.ChannelListViewModel
import io.getstream.chat.android.ui.channel.list.viewmodel.bindView
import io.getstream.chat.android.ui.channel.list.viewmodel.factory.ChannelListViewModelFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoSharedGroupsBinding
import io.getstream.chat.ui.sample.databinding.SharedGroupsEmptyViewBinding

class ChatInfoSharedGroupsFragment : Fragment() {

    private val args: ChatInfoSharedGroupsFragmentArgs by navArgs()
    private val viewModel: ChannelListViewModel by viewModels {
        ChannelListViewModelFactory(
            filter = Filters.and(
                Filters.eq("type", "messaging"),
                Filters.`in`(
                    "members",
                    listOf(args.memberId).let { members ->
                        ChatClient.instance().globalState.user.value?.id?.let(members::plus) ?: members
                    }
                ),
                Filters.or(Filters.notExists("draft"), Filters.eq("draft", false)),
                Filters.greaterThan("member_count", 2),
            ),
        )
    }

    private var _binding: FragmentChatInfoSharedGroupsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatInfoSharedGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initToolbar(binding.toolbar)
        binding.channelsView.apply {
            setShouldDrawItemSeparatorOnLastItem(true)
            setViewHolderFactory(ChatInfoSharedGroupsViewHolderFactory())

            setChannelItemClickListener {
                findNavController().navigateSafely(ChatInfoSharedGroupsFragmentDirections.actionOpenChat(it.cid, null))
            }

            SharedGroupsEmptyViewBinding.inflate(layoutInflater).root.apply {
                text = getString(R.string.chat_info_option_shared_groups_empty_title, args.memberName)
                setEmptyStateView(this)
            }

            viewModel.bindView(this, viewLifecycleOwner)
        }
    }
}
