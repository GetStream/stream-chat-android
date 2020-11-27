package io.getstream.chat.ui.sample.feature.channel.add.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.databinding.FragmentAddGroupChannelBinding
import io.getstream.chat.ui.sample.feature.channel.add.AddChannelView
import io.getstream.chat.ui.sample.feature.channel.add.AddChannelViewModel

class AddGroupChannelFragment : Fragment() {

    private var _binding: FragmentAddGroupChannelBinding? = null
    private val binding get() = _binding!!
    private val addChannelViewModel: AddChannelViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddGroupChannelBinding.inflate(inflater, container, false)
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
                    is AddChannelViewModel.State.ShowChannel,
                    AddChannelViewModel.State.HideChannel,
                    is AddChannelViewModel.State.NavigateToChannel -> Unit
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
            setSearchInputChangedListener {
                addChannelViewModel.onEvent(AddChannelViewModel.Event.SearchInputChanged(it))
            }
        }
    }
}
