/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.channels.actions.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.utils.extensions.isDirectMessaging
import io.getstream.chat.android.ui.databinding.StreamUiFragmentChannelActionsBinding
import io.getstream.chat.android.ui.feature.channels.actions.ChannelActionsDialogViewStyle
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat
import io.getstream.chat.android.ui.utils.extensions.getMembersStatusText
import io.getstream.chat.android.ui.utils.extensions.isCurrentUser
import io.getstream.chat.android.ui.utils.extensions.setStartDrawable
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.viewmodel.channels.internal.ChannelActionsViewModel
import io.getstream.chat.android.ui.viewmodel.channels.internal.ChannelActionsViewModelFactory

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
     * A callback for clicks on channel options.
     */
    private var channelOptionClickListener: ChannelOptionClickListener? = null

    /**
     * A callback for clicks on members.
     */
    private var channelMemberClickListener: ChannelMemberClickListener? = null

    /**
     * The full channel id, i.e. "messaging:123".
     */
    private val cid get() = channel.cid

    /**
     * [ViewModel] for the dialog.
     */
    private val channelActionsViewModel: ChannelActionsViewModel by viewModels {
        ChannelActionsViewModelFactory(cid)
    }

    /**
     * An [RecyclerView.Adapter] for the list of channel members.
     */
    private val membersAdapter: ChannelMembersAdapter = ChannelMembersAdapter {
        channelMemberClickListener?.onMemberClick(it)
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
        binding.channelMembersTextView.setTextStyle(style.memberNamesTextStyle)
        binding.channelMembersInfoTextView.setTextStyle(style.memberInfoTextStyle)

        channelActionsViewModel.channel.observe(viewLifecycleOwner) { channel ->
            this.channel = channel
            bindChannelInfo()
            bindChannelOptions()
        }
    }

    /**
     * Updates the channel name, the member status text and the list of channel members.
     */
    private fun bindChannelInfo() {
        binding.channelMembersTextView.text = ChatUI.channelNameFormatter.formatChannelName(
            channel = channel,
            currentUser = ChatUI.currentUserProvider.getCurrentUser(),
        )
        binding.channelMembersInfoTextView.text = channel.getMembersStatusText(requireContext())

        val channelMembers = channel.members
        val membersToDisplay = if (channel.isDirectMessaging()) {
            channelMembers.filter { !it.user.isCurrentUser() }
        } else {
            channelMembers
        }
        membersAdapter.submitList(membersToDisplay)
    }

    /**
     * Updates the list of channel options.
     */
    private fun bindChannelOptions() {
        binding.optionsContainer.removeAllViews()

        ChannelOptionItemsFactory.defaultFactory(requireContext())
            .createChannelOptionItems(
                selectedChannel = channel,
                ownCapabilities = channel.ownCapabilities,
                style = style,
            ).forEach { action ->
                val channelOptionTextView = requireContext().streamThemeInflater.inflate(
                    R.layout.stream_ui_channel_option_item,
                    binding.optionsContainer,
                    false,
                ) as TextView
                val textStyle = if (action.isDestructive) {
                    style.warningItemTextStyle
                } else {
                    style.itemTextStyle
                }
                channelOptionTextView.text = action.label
                channelOptionTextView.setStartDrawable(
                    requireContext().getDrawableCompat(action.icon)?.apply {
                        mutate()
                        setTint(textStyle.color)
                    },
                )
                channelOptionTextView.setOnClickListener {
                    channelOptionClickListener?.onChannelOptionClick(action)
                    dismiss()
                }
                channelOptionTextView.setTextStyle(textStyle)

                binding.optionsContainer.addView(channelOptionTextView)
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
     * Allows clients to set a click listener for channel option items.
     *
     * @param channelOptionClickListener The callback to be invoked on channel option item click.
     */
    fun setChannelOptionClickListener(channelOptionClickListener: ChannelOptionClickListener) {
        this.channelOptionClickListener = channelOptionClickListener
    }

    /**
     * Allows clients to set a click listener for channel member items.
     *
     * @param channelMemberClickListener The callback to be invoked on channel member item click.
     */
    fun setChannelMemberClickListener(channelMemberClickListener: ChannelMemberClickListener) {
        this.channelMemberClickListener = channelMemberClickListener
    }

    /**
     * A listener for member clicks.
     */
    fun interface ChannelMemberClickListener {
        fun onMemberClick(member: Member)
    }

    /**
     * A listener for channel option clicks.
     */
    fun interface ChannelOptionClickListener {
        fun onChannelOptionClick(channelAction: ChannelAction)
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
