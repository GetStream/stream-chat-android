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
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.utils.extensions.getLastSeenText
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.ChatInfoGroupMemberOptionsFragmentBinding
import io.getstream.chat.ui.sample.feature.chat.info.MemberData
import io.getstream.chat.ui.sample.feature.chat.info.group.GroupChatInfoFragmentDirections
import io.getstream.chat.ui.sample.feature.chat.info.toMember
import io.getstream.chat.ui.sample.feature.chat.info.toMemberData
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import java.util.Date

class GroupChatInfoMemberOptionsDialogFragment : BottomSheetDialogFragment() {

    private val cid: String by lazy {
        requireArguments().getString(ARG_CID)!!
    }
    private val memberData: MemberData by lazy {
        requireArguments().getSerializable(ARG_MEMBER_DATA) as MemberData
    }
    private val channelName: String by lazy {
        requireArguments().getString(ARG_CHANNEL_NAME)!!
    }
    private val member: Member by lazy {
        memberData.toMember()
    }
    private val ownCapabilities: Set<String> by lazy {
        requireArguments().getStringArrayList(ARG_OWN_CAPABILITIES)?.toSet() ?: setOf()
    }

    private val viewModel: GroupChatInfoMemberOptionsViewModel by viewModels {
        GroupChatInfoMemberOptionsViewModelFactory(cid, member.user.id)
    }
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
        binding.apply {
            userNameTextView.text = member.user.name
            lastSeenTextView.text = member.user.getLastSeenText(requireContext())
            banExpiresTextView.isVisible = member.banned
            banExpiresTextView.text = formatBanExpiry(member.banExpires)
            userAvatarView.setUser(member.user)
            optionViewInfo.setOnOptionClickListener {
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionOpenChatInfo(
                        userData = memberData.user,
                        cid = viewModel.state.value!!.directChannelCid,
                    ),
                )
                dismiss()
            }
            optionMessage.setOnClickListener {
                viewModel.onAction(GroupChatInfoMemberOptionsViewModel.Action.MessageClicked)
            }

            if (isAnonymousChannel(cid) || !ownCapabilities.contains(ChannelCapabilities.BAN_CHANNEL_MEMBERS)) {
                optionBan.isVisible = false
            } else {
                optionBan.setOnOptionText(
                    getString(
                        when (member.banned) {
                            true -> R.string.chat_group_info_user_option_unban
                            else -> R.string.chat_group_info_user_option_ban
                        },
                    ),
                )
                optionBan.setOnClickListener {
                    viewModel.onAction(
                        when (member.banned) {
                            true -> GroupChatInfoMemberOptionsViewModel.Action.UnbanMember
                            else -> GroupChatInfoMemberOptionsViewModel.Action.BanMember()
                        },
                    )
                }
            }

            if (isAnonymousChannel(cid) || !ownCapabilities.contains(ChannelCapabilities.UPDATE_CHANNEL_MEMBERS)) {
                optionRemove.isVisible = false
            } else {
                optionRemove.setOnClickListener {
                    ConfirmationDialogFragment.newInstance(
                        iconResId = R.drawable.ic_delete,
                        iconTintResId = R.color.red,
                        title = getString(R.string.chat_group_info_user_remove_title, member.user.name),
                        description = getString(
                            R.string.chat_group_info_user_remove_description,
                            member.user.name,
                            channelName,
                        ),
                        confirmText = getString(R.string.remove),
                        cancelText = getString(R.string.cancel),
                    ).apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                            val action = GroupChatInfoMemberOptionsViewModel.Action.RemoveFromChannel(member.user.name)
                            viewModel.onAction(action)
                        }
                    }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)
                }
            }
            optionCancel.setOnOptionClickListener {
                dismiss()
            }
        }
    }

    private fun isAnonymousChannel(cid: String): Boolean = cid.contains("!members")

    private fun formatBanExpiry(banExpires: Date?): String {
        if (banExpires == null) return getString(R.string.chat_group_info_user_ban_no_expiry)
        val currentTime = System.currentTimeMillis()
        val diffInMillis = banExpires.time - currentTime

        return if (diffInMillis <= 0) {
            getString(R.string.chat_group_info_user_ban_expired)
        } else {
            getString(
                R.string.chat_group_info_user_ban_expires,
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
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (!state.loading) {
                binding.apply {
                    optionMessage.isVisible = true
                    optionViewInfo.isVisible = state.directChannelCid != null
                }
            }
        }
        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    GroupChatInfoMemberOptionsViewModel.UiEvent.Dismiss -> dismiss()
                    is GroupChatInfoMemberOptionsViewModel.UiEvent.RedirectToChat -> {
                        findNavController().navigateSafely(
                            GroupChatInfoFragmentDirections.actionOpenChat(cid = it.cid),
                        )
                        dismiss()
                    }
                    GroupChatInfoMemberOptionsViewModel.UiEvent.RedirectToChatPreview -> {
                        findNavController().navigateSafely(
                            GroupChatInfoFragmentDirections.actionOpenChatPreview(memberData.user),
                        )
                        dismiss()
                    }
                }
            },
        )
        viewModel.errorEvents.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is GroupChatInfoMemberOptionsViewModel.ErrorEvent.RemoveMemberError -> R.string.chat_group_info_error_remove_member
                    is GroupChatInfoMemberOptionsViewModel.ErrorEvent.BanMemberError -> R.string.chat_group_info_error_ban_member
                    is GroupChatInfoMemberOptionsViewModel.ErrorEvent.UnbanMemberError -> R.string.chat_group_info_error_unban_member
                }.let(::showToast)
            },
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "GroupChatInfoMemberOptionsDialogFragment"
        private const val ARG_CID = "cid"
        private const val ARG_CHANNEL_NAME = "channel_name"
        private const val ARG_MEMBER_DATA = "member_data"
        private const val ARG_OWN_CAPABILITIES = "own_capabilities"

        fun newInstance(cid: String, channelName: String, member: Member, ownCapabilities: Set<String>) =
            GroupChatInfoMemberOptionsDialogFragment().apply {
                arguments =
                    bundleOf(
                        ARG_CID to cid,
                        ARG_CHANNEL_NAME to channelName,
                        ARG_MEMBER_DATA to member.toMemberData(),
                        ARG_OWN_CAPABILITIES to ownCapabilities.toList(),
                    )
            }
    }
}
