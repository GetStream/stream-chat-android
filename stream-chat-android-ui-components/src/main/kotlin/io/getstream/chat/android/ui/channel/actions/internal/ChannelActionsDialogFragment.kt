/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
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
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.common.extensions.getLastSeenText
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
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
        _binding = StreamUiFragmentChannelActionsBinding.inflate(requireContext().streamThemeInflater, container, false)
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
                bindLeaveGroupButton(canLeaveChannel)
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

    /**
     * Used to change the visibility of the delete conversation button
     * once the [ChannelActionsViewModel.state] updates.
     */
    private fun bindDeleteConversationButton(canDeleteChannel: Boolean) {
        binding.deleteButton.isVisible = canDeleteChannel && style.deleteConversationEnabled
    }

    /**
     * Used to change the visibility of the leave group button
     * once the [ChannelActionsViewModel.state] updates.
     */
    private fun bindLeaveGroupButton(canLeaveChannel: Boolean) {
        binding.leaveGroupButton.isVisible = canLeaveChannel && style.leaveGroupEnabled
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

    /**
     * Styles the action item.
     *
     * @param textStyle The style iof the text of the item.
     * @param icon The icon to be drawn at the start of the item.
     */
    private fun TextView.configureActionItem(textStyle: TextStyle, icon: Drawable) {
        setStartDrawable(icon)
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
