package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getFragmentManager
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.ChatViewModelFactory
import java.lang.IllegalStateException

class ChatInfoFragment : Fragment() {

    private val args: ChatInfoFragmentArgs by navArgs()
    private val factory: ChatViewModelFactory by lazy { ChatViewModelFactory(args.cid) }
    private val viewModel: ChatInfoViewModel by viewModels { factory }
    private val adapter: ChatInfoAdapter = ChatInfoAdapter()

    private var _binding: FragmentChatInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
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
            adapter.submitList(
                listOf(
                    ChatInfoItem.MemberItem(state.member),
                    ChatInfoItem.Separator,
                    ChatInfoItem.Option.Stateful.Notifications(isChecked = state.notificationsEnabled),
                    ChatInfoItem.Option.Stateful.MuteUser(isChecked = state.isMemberMuted),
                    ChatInfoItem.Option.Stateful.Block(isChecked = state.isMemberBlocked),
                    ChatInfoItem.Option.SharedMedia,
                    ChatInfoItem.Option.SharedFiles,
                    ChatInfoItem.Option.SharedGroups,
                    ChatInfoItem.Separator,
                    ChatInfoItem.Option.DeleteConversation,
                )
            )
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
                ChatInfoItem.Option.SharedMedia -> Unit // TODO: Not supported yet
                ChatInfoItem.Option.SharedFiles -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedFilesFragment(args.cid)
                )
                ChatInfoItem.Option.SharedGroups -> {
                    val member = viewModel.state.value!!.member
                    findNavController().navigateSafely(
                        ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedGroupsFragment(
                            member.getUserId(),
                            member.user.name,
                        )
                    )
                }
                ChatInfoItem.Option.DeleteConversation -> {
                    context.getFragmentManager()?.let {
                        ChatInfoDeleteChannelDialogFragment.newInstance()
                            .apply {
                                setDeleteChannelListener {
                                    viewModel.onAction(ChatInfoViewModel.Action.ChannelDeleted)
                                }
                            }
                            .show(it, ChatInfoDeleteChannelDialogFragment.TAG)
                    }
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
