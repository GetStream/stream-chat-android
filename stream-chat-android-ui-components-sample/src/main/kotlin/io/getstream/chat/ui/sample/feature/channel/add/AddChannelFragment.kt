package io.getstream.chat.ui.sample.feature.channel.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.message.input.bindView
import io.getstream.chat.android.ui.message.list.viewmodel.factory.MessageListViewModelFactory
import io.getstream.chat.android.ui.message.view.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentAddChannelBinding
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize

class AddChannelFragment : Fragment() {

    private var _binding: FragmentAddChannelBinding? = null
    private val binding get() = _binding!!
    private val addChannelViewModel: AddChannelViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        bindAddChannelView()
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeChannel(cid: String) {
        val factory = MessageListViewModelFactory(cid)
        val messageListViewModel = factory.create(MessageListViewModel::class.java)
        val messageInputViewModel = factory.create(MessageInputViewModel::class.java)
        binding.addChannelView.apply {
            messageListViewModel.bindView(messageListView, viewLifecycleOwner)
            messageInputViewModel.bindView(messageInputView, viewLifecycleOwner)
        }
    }

    private fun bindAddChannelView() {
        addChannelViewModel.apply {
            bindView(binding.addChannelView, viewLifecycleOwner)
            state.observe(viewLifecycleOwner) { state ->
                // Handle unique states
                when (state) {
                    is AddChannelViewModel.State.InitializeChannel -> initializeChannel(state.cid)
                    is AddChannelViewModel.State.NavigateToChannel -> findNavController().navigateSafely(
                        AddChannelFragmentDirections.actionOpenChat(state.cid, null)
                    )
                    AddChannelViewModel.State.Loading,
                    is AddChannelViewModel.State.Result,
                    is AddChannelViewModel.State.ResultMoreUsers -> Unit
                }
            }
        }
        binding.addChannelView.apply {
            setMembersChangedListener {
                addChannelViewModel.onEvent(AddChannelViewModel.Event.MembersChanged(it))
            }
            setOnCreateGroupButtonListener {
                findNavController().navigateSafely(R.id.action_addChannelFragment_to_addGroupChannelFragment)
            }
            messageInputView.setOnSendButtonClickListener {
                addChannelViewModel.onEvent(AddChannelViewModel.Event.MessageSent)
            }
        }
    }
}
