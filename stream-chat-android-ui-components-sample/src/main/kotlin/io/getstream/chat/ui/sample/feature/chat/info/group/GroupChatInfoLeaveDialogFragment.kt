package io.getstream.chat.ui.sample.feature.chat.info.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.GroupChatInfoLeaveDialogFragmentBinding

internal class GroupChatInfoLeaveDialogFragment : BottomSheetDialogFragment() {

    var leaveChannelListener: LeaveChannelListener? = null

    private val channelName: String by lazy { requireArguments().getString(ARG_CHANNEL_NAME)!! }
    private var _binding: GroupChatInfoLeaveDialogFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onDetach() {
        super.onDetach()
        leaveChannelListener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = GroupChatInfoLeaveDialogFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.descriptionTextView.text = getString(R.string.chat_group_info_leave_confirm, channelName)
        binding.cancelButton.setOnClickListener { dismiss() }
        binding.confirmButton.setOnClickListener {
            leaveChannelListener?.onLeaveChannel()
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun interface LeaveChannelListener {
        fun onLeaveChannel()
    }

    companion object {
        const val TAG = "GroupChatInfoLeave"
        private const val ARG_CHANNEL_NAME = "channel_name"

        fun newInstance(channelName: String): GroupChatInfoLeaveDialogFragment {
            return GroupChatInfoLeaveDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHANNEL_NAME, channelName)
                }
            }
        }
    }
}
