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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.utils.extensions.isDirectMessaging
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.common.extensions.getLastSeenText
import io.getstream.chat.android.ui.common.extensions.internal.setStartDrawable
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelActionsBinding

/**
 * A bottom sheet with the list of actions users can take with the selected channel.
 */
internal class ChannelActionsDialogFragment : BottomSheetDialogFragment() {

    private var _binding: StreamUiFragmentChannelActionsBinding? = null
    private val binding get() = _binding!!

    /**
     * Style for the dialog.
     */
    private lateinit var style: ChannelActionsDialogViewStyle

    /**
     * The selected channel.
     */
    private lateinit var channel: Channel

    /**
     * A listener that handles clicks on channel action items.
     */
    private var channelActionListener: ChannelActionListener? = null

    /**
     * The full channel id, i.e. "messaging:123".
     */
    private val cid get() = channel.cid

    /**
     * If the channel is not one-to-one conversation.
     */
    private val isGroup get() = !channel.isDirectMessaging()

    /**
     * [ViewModel] for the dialog.
     */
    private val channelActionsViewModel: ChannelActionsViewModel by viewModels {
        ChannelActionsViewModelFactory(cid, isGroup)
    }

    /**
     * An [RecyclerView.Adapter] for the list of channel members.
     */
    private val membersAdapter: ChannelMembersAdapter = ChannelMembersAdapter {
        channelActionListener?.onMemberSelected(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return StreamUiFragmentChannelActionsBinding.inflate(requireContext().streamThemeInflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isInitialized = ::channel.isInitialized && ::style.isInitialized
        if (savedInstanceState == null && isInitialized) {
            setupDialog()
        } else {
            // The process has been killed
            dismiss()
        }
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        binding.recyclerView.adapter = membersAdapter
        binding.channelActionsContainer.background = style.background

        setupViewInfoAction()
        setupLeaveGroupButton()
        setupCancelButton()
        setupDeleteConversationButton()

        channelActionsViewModel.state.observe(viewLifecycleOwner) { state ->
            membersAdapter.submitList(state.members)
            bindMemberNames(state.members)
            bindMembersInfo(state.members)
            bindDeleteConversationButton(state.canDeleteChannel)
            bindLeaveGroupButton(state.canLeaveChannel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    /**
     * Initializes the dialog with the selected channel.
     *
     * @param channel The selected channel.
     */
    fun setChannel(channel: Channel) {
        this.channel = channel
    }

    /**
     * Initializes the dialog with the style.
     *
     * @param style Style for the dialog.
     */
    fun setStyle(style: ChannelActionsDialogViewStyle) {
        this.style = style
    }

    /**
     * Sets a click listener for channel action.
     *
     * @param channelActionListener The listener to set.
     */
    fun setChannelActionListener(channelActionListener: ChannelActionListener) {
        this.channelActionListener = channelActionListener
    }

    /**
     * Initializes the "View Info" action button.
     */
    private fun setupViewInfoAction() {
        binding.viewInfoButton.apply {
            if (style.viewInfoEnabled) {
                setStartDrawable(style.viewInfoIcon)
                setTextStyle(style.itemTextStyle)
                setOnClickListener {
                    channelActionListener?.onChannelInfoSelected(cid)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    /**
     * Initializes the "Leave Group" action button.
     */
    private fun setupLeaveGroupButton() {
        binding.leaveGroupButton.apply {
            if (style.leaveGroupEnabled) {
                setStartDrawable(style.leaveGroupIcon)
                setTextStyle(style.itemTextStyle)
                setOnClickListener {
                    channelActionListener?.onLeaveChannelClicked(cid)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    /**
     * Initializes the "Delete Conversation" action button.
     */
    private fun setupDeleteConversationButton() {
        binding.deleteButton.apply {
            if (style.deleteConversationEnabled) {
                setStartDrawable(style.deleteConversationIcon)
                setTextStyle(style.warningItemTextStyle)
                setOnClickListener {
                    channelActionListener?.onDeleteConversationClicked(cid)
                    dismiss()
                }
            } else {
                isVisible = false
            }
        }
    }

    /**
     * Initializes the "Cancel" action button.
     */
    private fun setupCancelButton() {
        binding.cancelButton.apply {
            if (style.cancelEnabled) {
                setStartDrawable(style.cancelIcon)
                setTextStyle(style.itemTextStyle)
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

    /**
     * Updates the title with the member names.
     */
    private fun bindMemberNames(members: List<Member>) {
        style.memberNamesTextStyle.apply(binding.channelMembersTextView)
        binding.channelMembersTextView.text = if (isGroup) {
            members.joinToString { it.user.name }
        } else {
            members.getOrNull(0)?.user?.name ?: ""
        }
    }

    /**
     * Updates the subtitle with the status text.
     */
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

    /**
     * A listener for channel action clicks. Also, allows to listen for channel member clicks.
     */
    interface ChannelActionListener {
        fun onMemberSelected(member: Member)
        fun onDeleteConversationClicked(cid: String)
        fun onLeaveChannelClicked(cid: String)
        fun onChannelInfoSelected(cid: String)
    }

    companion object {
        /**
         * Creates a new instance of [ChannelActionsDialogFragment].
         *
         * @param channel The selected channel.
         * @param style The style for the dialog.
         */
        fun newInstance(
            channel: Channel,
            style: ChannelActionsDialogViewStyle,
        ): ChannelActionsDialogFragment {
            return ChannelActionsDialogFragment().apply {
                setChannel(channel)
                setStyle(style)
            }
        }
    }
}
