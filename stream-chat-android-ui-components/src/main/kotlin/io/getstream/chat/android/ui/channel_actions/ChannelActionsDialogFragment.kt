package io.getstream.chat.android.ui.channel_actions

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelActionsBinding

internal class ChannelActionsDialogFragment : BottomSheetDialogFragment() {
    private val membersAdapter: ChannelMembersAdapter = ChannelMembersAdapter {
        listener?.onMemberSelected(it)
    }

    private val cid: String by lazy { requireArguments().getString(ARG_CID)!! }
    private val isGroup: Boolean by lazy { requireArguments().getBoolean(ARG_IS_GROUP, false) }

    private var listener: OnChannelActionSelectedListener? = null

    private val channelActionsViewModel: ChannelActionsViewModel by viewModels {
        ChannelActionsViewModelFactory(cid, isGroup)
    }

    private var _binding: StreamUiFragmentChannelActionsBinding? = null
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = (parentFragment ?: requireActivity()) as? OnChannelActionSelectedListener
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StreamUiFragmentChannelActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        channelActionsViewModel.members.observe(viewLifecycleOwner) {
            membersAdapter.submitList(it)
            bindMemberNames(it)
            bindMembersInfo(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.StreamUiChannelActionsBottomSheetDialog

    private fun setupViews() {
        with(binding) {
            if (isGroup) {
                leaveGroupButton.isVisible = true
                deleteChannelButton.isVisible = true
                deleteContactButton.isVisible = false
                leaveGroupButton.setOnClickListener {
                    listener?.onLeaveGroupClicked(cid)
                }
                deleteChannelButton.setOnClickListener {
                    listener?.onDeleteChannelClicked(cid)
                }
            } else {
                leaveGroupButton.isVisible = false
                deleteChannelButton.isVisible = false
                deleteContactButton.isVisible = true
                deleteContactButton.setOnClickListener {
                    listener?.onDeleteContactClicked(cid)
                }
            }
            recyclerView.adapter = membersAdapter
        }
    }

    private fun bindMemberNames(members: List<Member>) {
        binding.channelMembersTextView.text = if (isGroup) {
            members.joinToString { it.user.name }
        } else {
            members.getOrNull(0)?.user?.name ?: ""
        }
    }

    private fun bindMembersInfo(members: List<Member>) {
        binding.membersInfoTextView.text = if (isGroup) {
            requireContext().resources.getQuantityString(
                R.plurals.stream_ui_channel_actions_members_count,
                members.size,
                members.size,
                members.count { it.user.online }
            )
        } else {
            members
                .getOrNull(0)
                ?.user
                ?.lastActive
                ?.time
                ?.let { DateUtils.getRelativeTimeSpanString(it) }
                ?.let { requireContext().getString(R.string.stream_ui_channel_actions_last_seen, it) }
                ?: ""
        }
    }

    interface OnChannelActionSelectedListener {
        fun onDeleteChannelClicked(cid: String)

        fun onDeleteContactClicked(cid: String)

        fun onLeaveGroupClicked(cid: String)

        fun onMemberSelected(member: Member)
    }

    companion object {
        private const val ARG_CID = "cid"
        private const val ARG_IS_GROUP = "is_group"

        fun newGroupChatInstance(cid: String): ChannelActionsDialogFragment {
            return newInstance(cid, true)
        }

        fun newDirectMessageInstance(cid: String): ChannelActionsDialogFragment {
            return newInstance(cid, false)
        }

        private fun newInstance(cid: String, isGroup: Boolean): ChannelActionsDialogFragment {
            return ChannelActionsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CID, cid)
                    putBoolean(ARG_IS_GROUP, isGroup)
                }
            }
        }
    }
}
