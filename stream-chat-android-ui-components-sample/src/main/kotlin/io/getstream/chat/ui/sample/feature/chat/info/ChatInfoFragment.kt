package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoBinding
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment

class ChatInfoFragment : Fragment() {

    private val args: ChatInfoFragmentArgs by navArgs()
    private val viewModel: ChatInfoViewModel by viewModels { ChatInfoViewModelFactory(args.cid, args.userData) }
    private val adapter: ChatInfoAdapter = ChatInfoAdapter()

    private var _binding: FragmentChatInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        binding.optionsRecyclerView.itemAnimator = null
        binding.optionsRecyclerView.adapter = adapter
        bindChatInfoViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindChatInfoViewModel() {
        subscribeForChannelMutesUpdatedEvents()
        setOnClickListeners()

        viewModel.channelDeletedState.observe(viewLifecycleOwner) { isDeleted ->
            if (isDeleted) {
                findNavController().popBackStack(R.id.homeFragment, false)
            }
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.loading) {
                binding.optionsRecyclerView.isVisible = false
                binding.progressBar.isVisible = true
                return@observe
            }
            adapter.submitList(buildChatInfoItems(state))
            binding.optionsRecyclerView.isVisible = true
            binding.progressBar.isVisible = false
        }
    }

    private fun buildChatInfoItems(state: ChatInfoViewModel.State): List<ChatInfoItem> {
        return mutableListOf<ChatInfoItem>().apply {
            if (state.member != null) {
                add(ChatInfoItem.MemberItem(state.member))
                add(ChatInfoItem.Separator)
            }

            if (state.channelExists) {
                add(ChatInfoItem.Option.Stateful.Notifications(isChecked = state.notificationsEnabled))

                if (state.member != null) {
                    add(ChatInfoItem.Option.Stateful.MuteUser(isChecked = state.isMemberMuted))
                    add(ChatInfoItem.Option.Stateful.Block(isChecked = state.isMemberBlocked))
                }
            }

            add(ChatInfoItem.Option.SharedMedia)
            add(ChatInfoItem.Option.SharedFiles)

            if (state.member != null) {
                add(ChatInfoItem.Option.SharedGroups)
            }

            if (state.canDeleteChannel) {
                add(ChatInfoItem.Separator)
                add(ChatInfoItem.Option.DeleteConversation)
            }
        }
    }

    private fun setOnClickListeners() {
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            viewModel.onAction(
                when (option) {
                    is ChatInfoItem.Option.Stateful.Notifications -> ChatInfoViewModel.Action.OptionNotificationClicked(
                        isChecked
                    )
                    is ChatInfoItem.Option.Stateful.MuteUser -> ChatInfoViewModel.Action.OptionMuteUserClicked(isChecked)
                    is ChatInfoItem.Option.Stateful.Block -> ChatInfoViewModel.Action.OptionBlockUserClicked(isChecked)
                    else -> throw IllegalStateException("Chat info option $option is not supported!")
                }
            )
        }
        adapter.setChatInfoOptionClickListener { option ->
            when (option) {
                ChatInfoItem.Option.SharedMedia -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedMediaFragment(args.cid)
                )
                ChatInfoItem.Option.SharedFiles -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedFilesFragment(args.cid)
                )
                ChatInfoItem.Option.SharedGroups -> {
                    // Option shouldn't be visible when member is not set
                    val member = viewModel.state.value!!.member ?: return@setChatInfoOptionClickListener
                    findNavController().navigateSafely(
                        ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedGroupsFragment(
                            member.getUserId(),
                            member.user.name,
                        )
                    )
                }
                ChatInfoItem.Option.DeleteConversation -> {
                    ConfirmationDialogFragment.newDeleteChannelInstance(requireContext()).apply {
                        confirmClickListener =
                            ConfirmationDialogFragment.ConfirmClickListener {
                                viewModel.onAction(ChatInfoViewModel.Action.ChannelDeleted)
                            }
                    }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)
                }
                else -> throw IllegalStateException("Chat info option $option is not supported!")
            }
        }
    }

    private fun subscribeForChannelMutesUpdatedEvents() {
        ChatClient.instance().subscribeFor<NotificationChannelMutesUpdatedEvent>(viewLifecycleOwner) {
            viewModel.onAction(ChatInfoViewModel.Action.ChannelMutesUpdated(it.me.channelMutes))
        }
    }
}
