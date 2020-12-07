package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.getFragmentManager
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
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
        viewModel.bindView(this, viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun showOptions(options: List<ChatInfoItem>) {
        adapter.submitList(options)
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
            when (option) {
                ChatInfoItem.Option.SharedMedia -> Unit // Not supported yet
                ChatInfoItem.Option.SharedFiles -> Unit // Not supported yet
                ChatInfoItem.Option.SharedGroups -> {
                    val memberId = viewModel.state.value!!.member.getUserId()
                    findNavController().navigateSafely(
                        ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedGroupsFragment(
                            memberId
                        )
                    )
                }
                ChatInfoItem.Option.DeleteConversation -> {
                    context.getFragmentManager()?.let {
                        ChatInfoDeleteChannelDialogFragment.newInstance()
                            .apply {
                                setDeleteChannelListener {
                                    viewModel.onEvent(ChatInfoViewModel.Event.DeleteChannel)
                                }
                            }
                            .show(it, ChatInfoDeleteChannelDialogFragment.TAG)
                    }
                }
                is ChatInfoItem.Option.Stateful -> Unit
            }
        }
    }
}
