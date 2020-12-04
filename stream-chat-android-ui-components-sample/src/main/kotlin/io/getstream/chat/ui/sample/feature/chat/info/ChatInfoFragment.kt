package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.ChatViewModelFactory

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
        initAdapter()
        viewModel.state.observe(viewLifecycleOwner) { state ->
            adapter.submitList(convertStateToListItems(state))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initAdapter() {
        binding.optionsRecyclerView.adapter = adapter
        adapter.setChatInfoOptionClickListener { option ->
            when (option) {
                ChatInfoItem.Option.SharedMedia -> Unit
                ChatInfoItem.Option.SharedFiles -> Unit
                ChatInfoItem.Option.SharedGroups -> Unit
                ChatInfoItem.Option.LeaveChannel -> Unit
                is ChatInfoItem.Option.Stateful -> Unit
            }
        }
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            when (option) {
                is ChatInfoItem.Option.Stateful.Notifications -> Unit
                is ChatInfoItem.Option.Stateful.Mute -> Unit
                is ChatInfoItem.Option.Stateful.Block -> Unit
            }
        }
    }

    private fun convertStateToListItems(state: ChatInfoViewModel.State): List<ChatInfoItem> {
        return listOf(
            ChatInfoItem.MemberItem(state.member),
            ChatInfoItem.Separator,
            ChatInfoItem.Option.Stateful.Notifications(isChecked = state.notificationsEnabled),
            ChatInfoItem.Option.Stateful.Mute(isChecked = state.isMemberMuted),
            ChatInfoItem.Option.Stateful.Block(isChecked = state.isMemberBlocked),
            ChatInfoItem.Option.SharedMedia,
            ChatInfoItem.Option.SharedFiles,
            ChatInfoItem.Option.SharedGroups,
            ChatInfoItem.Separator,
            ChatInfoItem.Option.LeaveChannel
        )
    }
}
