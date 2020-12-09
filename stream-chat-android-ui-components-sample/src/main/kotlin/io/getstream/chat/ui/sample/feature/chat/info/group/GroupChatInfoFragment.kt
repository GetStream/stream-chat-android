package io.getstream.chat.ui.sample.feature.chat.info.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.viewmodel.ChannelHeaderViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelViewModelFactory
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.ui.messages.header.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.FragmentGroupChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.ChatViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem

class GroupChatInfoFragment : Fragment() {

    private val args: GroupChatInfoFragmentArgs by navArgs()
    private val viewModel: GroupChatInfoViewModel by viewModels { ChatViewModelFactory(args.cid) }
    private val headerViewModel: ChannelHeaderViewModel by viewModels { ChannelViewModelFactory(args.cid) }
    private val adapter: GroupChatInfoAdapter = GroupChatInfoAdapter()

    private var _binding: FragmentGroupChatInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGroupChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.optionsRecyclerView.adapter = adapter
        headerViewModel.bindView(binding.headerView, viewLifecycleOwner)
        bindGroupInfoViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindGroupInfoViewModel() {
        subscribeForChannelMutesUpdatedEvents()
        setOnClickListeners()

        viewModel.channelLeftState.observe(viewLifecycleOwner) { wasLeft ->
            if (wasLeft) {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            val members = if (state.shouldExpandMembers != false) {
                state.members.map { ChatInfoItem.MemberItem(it) }
            } else {
                state.members.take(GroupChatInfoViewModel.COLLAPSED_MEMBERS_COUNT)
                    .map { ChatInfoItem.MemberItem(it) } + ChatInfoItem.MembersSeparator(state.membersToShowCount)
            }
            adapter.submitList(
                members +
                    listOf(
                        ChatInfoItem.Separator,
                        ChatInfoItem.ChannelName(state.channelName),
                        ChatInfoItem.Option.Stateful.Mute(isChecked = state.channelMuted),
                        ChatInfoItem.Option.SharedMedia,
                        ChatInfoItem.Option.SharedFiles,
                        ChatInfoItem.Option.LeaveGroup,
                    )
            )
        }
    }

    private fun setOnClickListeners() {
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            viewModel.onEvent(
                when (option) {
                    is ChatInfoItem.Option.Stateful.Mute -> GroupChatInfoViewModel.Event.MuteChannelClicked(
                        isChecked
                    )
                    else -> throw IllegalStateException("Chat info option $option is not supported!")
                }
            )
        }
        adapter.setChatInfoOptionClickListener { option ->
            when (option) {
                ChatInfoItem.Option.SharedMedia -> Unit // TODO: Not supported yet
                ChatInfoItem.Option.SharedFiles -> Unit // TODO: Not supported yet
                ChatInfoItem.Option.LeaveGroup -> viewModel.onEvent(GroupChatInfoViewModel.Event.LeaveChannel)
                else -> throw IllegalStateException("Group chat info option $option is not supported!")
            }
        }
        adapter.setMembersSeparatorClickListener { viewModel.onEvent(GroupChatInfoViewModel.Event.MembersSeparatorClick) }
        adapter.setNameChangedListener { viewModel.onEvent(GroupChatInfoViewModel.Event.NameChanged(it)) }
    }

    private fun subscribeForChannelMutesUpdatedEvents() {
        ChatClient.instance().subscribeFor<NotificationChannelMutesUpdatedEvent>(viewLifecycleOwner) {
            viewModel.onEvent(GroupChatInfoViewModel.Event.ChannelMutesUpdated(it.me.channelMutes))
        }
    }
}
