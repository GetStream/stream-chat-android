package io.getstream.chat.android.ui.channel.actions.internal

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.common.extensions.getLastSeenText
import io.getstream.chat.android.ui.common.extensions.internal.setLeftDrawable
import io.getstream.chat.android.ui.common.style.TextStyle
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelActionsBinding

internal class ChannelActionsDialogFragment : BottomSheetDialogFragment() {
    var channelActionListener: ChannelActionListener? = null

    private val cid: String by lazy { requireArguments().getString(ARG_CID)!! }
    private val isGroup: Boolean by lazy { requireArguments().getBoolean(ARG_IS_GROUP, false) }

    private val membersAdapter: ChannelMembersAdapter = ChannelMembersAdapter {
        channelActionListener?.onMemberSelected(it)
    }

    private val channelActionsViewModel: ChannelActionsViewModel by viewModels {
        ChannelActionsViewModelFactory(cid, isGroup)
    }

    private lateinit var style: ChannelActionsDialogViewStyle

    private var _binding: StreamUiFragmentChannelActionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentChannelActionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        consumeStyleArg()

        binding.channelActionsContainer.background = style.background
        binding.recyclerView.adapter = membersAdapter
        configureViewInfoAction()
        configureLeaveGroupButton()
        configureCancelButton()
        configureDeleteConversationButton()

        channelActionsViewModel.state.observe(viewLifecycleOwner) { state ->
            with(state) {
                membersAdapter.submitList(members)
                bindMemberNames(members)
                bindMembersInfo(members)
                bindDeleteConversationButton(canDeleteChannel)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    private fun configureViewInfoAction() {
        binding.viewInfoButton.apply {
            if (style.viewInfoEnabled) {
                configureActionItem(style.itemTextStyle, style.viewInfoIcon)
                setOnClickListener {
                    channelActionListener?.onChannelInfoSelected(cid)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    private fun configureLeaveGroupButton() {
        binding.leaveGroupButton.apply {
            if (style.leaveGroupEnabled) {
                isVisible = isGroup
                configureActionItem(style.itemTextStyle, style.leaveGroupIcon)
                setOnClickListener {
                    channelActionListener?.onLeaveChannelClicked(cid)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    private fun configureDeleteConversationButton() {
        binding.deleteButton.apply {
            if (style.deleteConversationEnabled) {
                configureActionItem(style.warningItemTextStyle, style.deleteConversationIcon)
                setOnClickListener {
                    channelActionListener?.onDeleteConversationClicked(cid)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    private fun configureCancelButton() {
        binding.cancelButton.apply {
            if (style.cancelEnabled) {
                configureActionItem(style.itemTextStyle, style.cancelIcon)
                setOnClickListener {
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    private fun bindDeleteConversationButton(canDeleteChannel: Boolean) {
        binding.deleteButton.isVisible = canDeleteChannel && style.deleteConversationEnabled
    }

    private fun bindMemberNames(members: List<Member>) {
        style.memberNamesTextStyle.apply(binding.channelMembersTextView)
        binding.channelMembersTextView.text = if (isGroup) {
            members.joinToString { it.user.name }
        } else {
            members.getOrNull(0)?.user?.name ?: ""
        }
    }

    private fun bindMembersInfo(members: List<Member>) {
        style.memberInfoTextStyle.apply(binding.membersInfoTextView)
        binding.membersInfoTextView.text = if (isGroup) {
            requireContext().resources.getQuantityString(
                R.plurals.stream_ui_channel_list_member_info,
                members.size,
                members.size,
                members.count { it.user.online }
            )
        } else {
            members
                .getOrNull(0)
                ?.user
                ?.getLastSeenText(requireContext())
                ?: ""
        }
    }

    private fun consumeStyleArg() {
        styleArg?.let {
            style = it
            styleArg = null
        } ?: dismiss()
    }

    private fun TextView.configureActionItem(textStyle: TextStyle, icon: Drawable) {
        setLeftDrawable(icon)
        textStyle.apply(this)
    }

    interface ChannelActionListener {
        fun onDeleteConversationClicked(cid: String)

        fun onLeaveChannelClicked(cid: String)

        fun onMemberSelected(member: Member)

        fun onChannelInfoSelected(cid: String)
    }

    companion object {
        private const val ARG_CID = "cid"
        private const val ARG_IS_GROUP = "is_group"

        var styleArg: ChannelActionsDialogViewStyle? = null

        fun newInstance(
            cid: String,
            isGroup: Boolean,
            style: ChannelActionsDialogViewStyle,
        ): ChannelActionsDialogFragment {
            return ChannelActionsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CID, cid)
                    putBoolean(ARG_IS_GROUP, isGroup)
                    // pass style via static field
                    styleArg = style
                }
            }
        }
    }
}
