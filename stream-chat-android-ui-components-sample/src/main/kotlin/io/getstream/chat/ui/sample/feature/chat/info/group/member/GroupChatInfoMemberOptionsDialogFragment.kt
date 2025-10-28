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

package io.getstream.chat.ui.sample.feature.chat.info.group.member

import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoMemberViewModel
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoMemberViewModelFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupMemberOptionsFragmentBinding
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Date

class GroupChatInfoMemberOptionsDialogFragment : BottomSheetDialogFragment() {

    private val cid: String by lazy { requireArguments().getString(ARG_CID)!! }
    private val memberId: String by lazy { requireArguments().getString(ARG_MEMBER_ID)!! }

    private val viewModel: ChannelInfoMemberViewModel by viewModels { ChannelInfoMemberViewModelFactory(cid, memberId) }
    private var _binding: ChatInfoGroupMemberOptionsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = ChatInfoGroupMemberOptionsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
    }

    private fun formatBanExpiry(banExpires: Date?): String {
        if (banExpires == null) return getString(R.string.stream_ui_channel_info_member_modal_ban_no_expiration)
        val currentTime = System.currentTimeMillis()
        val diffInMillis = banExpires.time - currentTime

        return if (diffInMillis <= 0) {
            getString(R.string.stream_ui_channel_info_member_modal_ban_expired)
        } else {
            getString(
                R.string.stream_ui_channel_info_member_modal_ban_expires_at,
                DateUtils.getRelativeTimeSpanString(
                    banExpires.time,
                    currentTime,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE,
                ).toString().lowercase(),
            )
        }
    }

    private fun initViewModel() {
        viewModel.state.observe(viewLifecycleOwner, ::bindState)
        viewModel.events.observe(viewLifecycleOwner) { event ->
            val result = event.asResult()
            parentFragmentManager.setFragmentResult(REQUEST_KEY, result)
            dismiss()
        }
    }

    @Suppress("NestedBlockDepth")
    private fun bindState(state: ChannelInfoMemberViewState) {
        binding.apply {
            optionCancel.setOnOptionClickListener(::dismiss)
            optionViewInfo.isVisible = false
            optionMessage.isVisible = false
            optionBan.isVisible = false
            optionRemove.isVisible = false
            when (state) {
                // Not applicable in this UI
                is ChannelInfoMemberViewState.Loading -> Unit

                is ChannelInfoMemberViewState.Content -> {
                    val member = state.member
                    val user = member.user
                    userNameTextView.text = user.name
                    lastSeenTextView.text = user.getLastSeenText(requireContext())
                    banExpiresTextView.isVisible = member.banned
                    banExpiresTextView.text = formatBanExpiry(member.banExpires)
                    userAvatarView.setUser(user)

                    state.options.forEach { option ->
                        when (option) {
                            is ChannelInfoMemberViewState.Content.Option.MessageMember -> {
                                optionMessage.isVisible = true
                                optionMessage.setOnClickListener {
                                    viewModel.onViewAction(ChannelInfoMemberViewAction.MessageMemberClick)
                                }
                            }

                            is ChannelInfoMemberViewState.Content.Option.BanMember -> {
                                optionBan.isVisible = true
                                optionBan.setOnOptionText(
                                    getString(R.string.stream_ui_channel_info_member_modal_option_ban_member),
                                )
                                optionBan.setOnClickListener {
                                    viewModel.onViewAction(ChannelInfoMemberViewAction.BanMemberClick)
                                }
                            }

                            is ChannelInfoMemberViewState.Content.Option.UnbanMember -> {
                                optionBan.isVisible = true
                                optionBan.setOnOptionText(
                                    getString(R.string.stream_ui_channel_info_member_modal_option_unban_member),
                                )
                                optionBan.setOnClickListener {
                                    viewModel.onViewAction(ChannelInfoMemberViewAction.UnbanMemberClick)
                                }
                            }

                            is ChannelInfoMemberViewState.Content.Option.RemoveMember -> {
                                optionRemove.isVisible = true
                                optionRemove.setOnClickListener {
                                    viewModel.onViewAction(ChannelInfoMemberViewAction.RemoveMemberClick)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GroupChatInfoMemberOptionsDialogFragment"
        const val REQUEST_KEY = "${TAG}_requestKey"
        private const val ARG_CID = "cid"
        private const val ARG_MEMBER_ID = "memberId"
        private const val RESULT = "event"

        fun newInstance(cid: String, memberId: String): GroupChatInfoMemberOptionsDialogFragment = GroupChatInfoMemberOptionsDialogFragment().apply {
            arguments = bundleOf(
                ARG_CID to cid,
                ARG_MEMBER_ID to memberId,
            )
        }

        fun getEventFromResult(result: Bundle): ChannelInfoMemberViewEvent = when (val option = result.get(RESULT) as FragmentResult) {
            is FragmentResult.MessageMember ->
                ChannelInfoMemberViewEvent.MessageMember(option.memberId, option.distinctCid)

            is FragmentResult.BanMember -> ChannelInfoMemberViewEvent.BanMember(option.member)
            is FragmentResult.UnbanMember -> ChannelInfoMemberViewEvent.UnbanMember(option.member)
            is FragmentResult.RemoveMember -> ChannelInfoMemberViewEvent.RemoveMember(option.member)
        }

        private fun ChannelInfoMemberViewEvent.asResult() = bundleOf(
            RESULT to when (this) {
                is ChannelInfoMemberViewEvent.MessageMember -> FragmentResult.MessageMember(memberId, distinctCid)
                is ChannelInfoMemberViewEvent.BanMember -> FragmentResult.BanMember(member)
                is ChannelInfoMemberViewEvent.UnbanMember -> FragmentResult.UnbanMember(member)
                is ChannelInfoMemberViewEvent.RemoveMember -> FragmentResult.RemoveMember(member)
            },
        )
    }
}

@Parcelize
private sealed interface FragmentResult : Parcelable {
    data class MessageMember(val memberId: String, val distinctCid: String?) : FragmentResult
    data class BanMember(val member: @RawValue Member) : FragmentResult
    data class UnbanMember(val member: @RawValue Member) : FragmentResult
    data class RemoveMember(val member: @RawValue Member) : FragmentResult
}
