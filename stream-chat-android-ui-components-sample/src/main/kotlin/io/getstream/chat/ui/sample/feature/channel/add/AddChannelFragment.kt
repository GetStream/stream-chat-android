package io.getstream.chat.ui.sample.feature.channel.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.getstream.sdk.chat.viewmodel.MessageInputViewModel
import com.getstream.sdk.chat.viewmodel.factory.ChannelViewModelFactory
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel
import com.getstream.sdk.chat.viewmodel.messages.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentAddChannelBinding

class AddChannelFragment : Fragment() {

    private var _binding: FragmentAddChannelBinding? = null
    private val binding get() = _binding!!
    private val addChannelViewModel: AddChannelViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddChannelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        bindAddChannelView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initializeChannel(cid: String) {
        val factory = ChannelViewModelFactory(cid)
        val messageListViewModel = factory.create(MessageListViewModel::class.java)
        val messageInputViewModel = factory.create(MessageInputViewModel::class.java)
        binding.addChannelView.apply {
            messageListViewModel.bindView(messageListView, viewLifecycleOwner)
            // Will be bound when bindView available
            // messageInputViewModel.bindView(messageInputView, viewLifecycleOwner)
            showMessageListView()
            hideUsersRecyclerView()
        }
    }

    private fun cleanChannel() {
        hideMessageList()
    }

    private fun hideMessageList() {
        binding.addChannelView.hideMessageListView()
        binding.addChannelView.showUsersRecyclerView()
    }

    private fun bindAddChannelView() {
        addChannelViewModel.apply {
            state.observe(viewLifecycleOwner) { state ->
                when (state) {
                    AddChannelViewModel.State.Loading -> {
                        binding.addChannelView.showLoadingView()
                        binding.addChannelView.hideUsersRecyclerView()
                        binding.addChannelView.hideEmptyStateView()
                    }
                    AddChannelViewModel.State.Empty -> {
                        binding.addChannelView.hideUsersRecyclerView()
                        binding.addChannelView.hideLoadingView()
                        binding.addChannelView.showEmptyStateView()
                    }
                    is AddChannelViewModel.State.Result -> {
                        binding.addChannelView.setUsers(state.users)
                        binding.addChannelView.hideLoadingView()
                        binding.addChannelView.hideEmptyStateView()
                        binding.addChannelView.showUsersRecyclerView()
                    }
                    is AddChannelViewModel.State.ResultMoreUsers -> {
                        binding.addChannelView.addMoreUsers(state.users)
                    }
                    is AddChannelViewModel.State.ShowChannel -> initializeChannel(state.cid)
                    AddChannelViewModel.State.HideChannel -> cleanChannel()
                    is AddChannelViewModel.State.NavigateToChannel -> findNavController().navigateSafely(
                        AddChannelFragmentDirections.actionOpenChat(state.cid)
                    )
                }
            }
            paginationState.observe(viewLifecycleOwner) { state ->
                binding.addChannelView.setPaginationEnabled(!state.endReached && !state.loadingMore)
            }
        }
        binding.addChannelView.apply {
            endReachedListener = AddChannelView.EndReachedListener {
                addChannelViewModel.onEvent(AddChannelViewModel.Event.ReachedEndOfList)
            }
            setAddMemberButtonClickedListener {
                hideMessageList()
            }
            setMembersChangedListener {
                addChannelViewModel.onEvent(AddChannelViewModel.Event.MembersChanged(it))
            }
            setSearchInputChangedListener {
                addChannelViewModel.onEvent(AddChannelViewModel.Event.SearchInputChanged(it))
            }
            setOnCreateGroupButtonListener {
                findNavController().navigateSafely(R.id.action_addChannelFragment_to_addGroupChannelFragment)
            }
        }
    }
}
