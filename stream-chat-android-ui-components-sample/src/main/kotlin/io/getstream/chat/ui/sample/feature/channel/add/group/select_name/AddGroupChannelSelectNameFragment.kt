package io.getstream.chat.ui.sample.feature.channel.add.group.select_name

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentAddGroupChannelSelectNameBinding
import io.getstream.chat.ui.sample.feature.channel.add.group.AddGroupChannelMembersSharedViewModel

class AddGroupChannelSelectNameFragment : Fragment() {

    private var _binding: FragmentAddGroupChannelSelectNameBinding? = null
    private val binding get() = _binding!!
    private val viewModelSelect: AddGroupChannelSelectNameViewModel by viewModels()
    private val sharedMembersViewModel: AddGroupChannelMembersSharedViewModel by activityViewModels()
    private val adapter = AddGroupChannelSelectNameMembersAdapter()
    private var isCreateButtonEnabled: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGroupChannelSelectNameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_add_group_channel_select_name, menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.createGroupChannelButton).actionView.findViewById<View>(R.id.addChannelButton).apply {
            isEnabled = isCreateButtonEnabled
            setOnClickListener {
                sharedMembersViewModel.members.value?.let { members ->
                    viewModelSelect.onEvent(
                        AddGroupChannelSelectNameViewModel.Event.CreateChannel(
                            name = binding.nameEditText.text.toString(),
                            members = members
                        )
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        binding.membersRecyclerView.adapter = adapter
        adapter.deleteMemberClickListener = AddGroupChannelSelectNameMembersAdapter.DeleteMemberClickListener {
            sharedMembersViewModel.removeMember(it)
        }
        sharedMembersViewModel.members.observe(viewLifecycleOwner) { members ->
            if (members.isEmpty()) {
                findNavController().navigateUp()
            } else {
                binding.membersTitleTextView.text = requireContext().resources.getQuantityString(
                    R.plurals.members_count_title,
                    members.size,
                    members.size
                )
                adapter.submitList(members)
            }
        }
        binding.nameEditText.doAfterTextChanged {
            val shouldEnabledCreateButton = it?.toString()?.isNotEmpty() ?: false
            if (shouldEnabledCreateButton != isCreateButtonEnabled) {
                isCreateButtonEnabled = shouldEnabledCreateButton
                requireActivity().invalidateOptionsMenu()
            }
        }
        viewModelSelect.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                AddGroupChannelSelectNameViewModel.State.Loading -> {
                    binding.membersRecyclerView.isVisible = false
                    binding.progressBar.isVisible = true
                }
                is AddGroupChannelSelectNameViewModel.State.NavigateToChannel -> {
                    sharedMembersViewModel.members.removeObservers(viewLifecycleOwner)
                    sharedMembersViewModel.setMembers(emptyList())
                    findNavController().navigateSafely(
                        AddGroupChannelSelectNameFragmentDirections.actionOpenChat(state.cid, null)
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
