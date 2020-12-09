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
import io.getstream.chat.android.ui.messages.header.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.FragmentGroupChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.ChatViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoAdapter
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem
import io.getstream.chat.ui.sample.feature.chat.info.OptionType

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
        headerViewModel.bindView(binding.headerView, viewLifecycleOwner)
        viewModel.bindView(this, viewLifecycleOwner)
        initAdapter()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun showOptions(options: List<ChatInfoItem>) {
        adapter.submitList(options)
    }

    fun setMembersSeparatorClickListener(listener: GroupChatInfoAdapter.MembersSeparatorClickListener) {
        adapter.setMembersSeparatorClickListener(listener)
    }

    fun setNameChangedListener(listener: GroupChatInfoAdapter.NameChangedListener) {
        adapter.setNameChangedListener(listener)
    }

    fun setChatInfoStatefulOptionChangedListener(listener: ChatInfoAdapter.ChatInfoStatefulOptionChangedListener?) {
        adapter.setChatInfoStatefulOptionChangedListener(listener)
    }

    fun navigateUpToHome() {
        findNavController().popBackStack(R.id.homeFragment, false)
    }

    private fun initAdapter() {
        binding.optionsRecyclerView.adapter = adapter
        adapter.setChatInfoOptionClickListener { option ->
            when (option.optionType) {
                OptionType.SHARED_MEDIA -> Unit // Not supported yet
                OptionType.SHARED_FILES -> Unit // Not supported yet
                OptionType.LEAVE_GROUP -> viewModel.onEvent(GroupChatInfoViewModel.Event.LeaveChannel)
                else -> throw IllegalStateException("Group chat info option ${option.optionType} is not supported!")
            }
        }
    }
}
