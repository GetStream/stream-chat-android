/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.chat.info

import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.feature.chat.info.group.member.GroupChatInfoMemberOptionsDialogFragment
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment

internal fun List<ChannelInfoViewState.Content.Option>.toChannelInfoItems(
    isGroupChannel: Boolean,
): List<ChatInfoItem> = buildList {
    this@toChannelInfoItems.forEach { option ->
        when (option) {
            // Not rendered as an option item
            is ChannelInfoViewState.Content.Option.AddMember -> Unit

            // Not applicable in this UI
            is ChannelInfoViewState.Content.Option.UserInfo -> Unit

            is ChannelInfoViewState.Content.Option.Separator -> add(ChatInfoItem.Separator)

            is ChannelInfoViewState.Content.Option.RenameChannel ->
                add(ChatInfoItem.ChannelName(option.name))

            is ChannelInfoViewState.Content.Option.MuteChannel -> add(
                ChatInfoItem.Option.Stateful.MuteChannel(
                    textResId = if (isGroupChannel) {
                        R.string.stream_ui_channel_info_option_mute_group
                    } else {
                        R.string.stream_ui_channel_info_option_mute_conversation
                    },
                    isChecked = option.isMuted,
                ),
            )

            is ChannelInfoViewState.Content.Option.HideChannel -> add(
                ChatInfoItem.Option.HideChannel(
                    textResId = if (isGroupChannel) {
                        R.string.stream_ui_channel_info_option_hide_group
                    } else {
                        R.string.stream_ui_channel_info_option_hide_conversation
                    },
                    isChecked = option.isHidden,
                ),
            )

            is ChannelInfoViewState.Content.Option.PinnedMessages -> {
                add(ChatInfoItem.Option.PinnedMessages)
                // These options aren't coming from the ViewModel yet, so we add them manually
                add(ChatInfoItem.Option.SharedMedia)
                add(ChatInfoItem.Option.SharedFiles)
                if (!isGroupChannel) {
                    add(ChatInfoItem.Option.SharedGroups)
                }
            }

            is ChannelInfoViewState.Content.Option.LeaveChannel -> add(
                ChatInfoItem.Option.LeaveChannel(
                    textResId = if (isGroupChannel) {
                        R.string.stream_ui_channel_info_option_leave_group
                    } else {
                        R.string.stream_ui_channel_info_option_leave_conversation
                    },
                ),
            )

            is ChannelInfoViewState.Content.Option.DeleteChannel -> add(
                ChatInfoItem.Option.DeleteChannel(
                    textResId = if (isGroupChannel) {
                        R.string.stream_ui_channel_info_option_delete_group
                    } else {
                        R.string.stream_ui_channel_info_option_delete_conversation
                    },
                ),
            )
        }
    }
}

@Suppress("LongMethod")
internal fun Fragment.showModal(
    modal: ChannelInfoViewEvent.Modal,
    viewModel: ChannelInfoViewModel,
    isGroupChannel: Boolean,
    onDismiss: ((modal: ChannelInfoViewEvent.Modal) -> Unit)? = null,
) {
    when (modal) {
        is ChannelInfoViewEvent.HideChannelModal ->
            ConfirmationDialogFragment.newHideChannelInstance(requireContext(), isGroupChannel)
                .apply {
                    confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                        viewModel.onViewAction(
                            ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory = true),
                        )
                    }
                    cancelClickListener = ConfirmationDialogFragment.CancelClickListener {
                        viewModel.onViewAction(
                            ChannelInfoViewAction.HideChannelConfirmationClick(clearHistory = false),
                        )
                    }
                    onDismissListener = { onDismiss?.invoke(modal) }
                }
                .show(parentFragmentManager, ConfirmationDialogFragment.TAG)

        is ChannelInfoViewEvent.LeaveChannelModal ->
            ConfirmationDialogFragment.newLeaveChannelInstance(requireContext(), isGroupChannel)
                .apply {
                    confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                        viewModel.onViewAction(
                            ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage = null),
                        )
                    }
                }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)

        is ChannelInfoViewEvent.DeleteChannelModal ->
            ConfirmationDialogFragment.newDeleteChannelInstance(requireContext(), isGroupChannel)
                .apply {
                    confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                        viewModel.onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)
                    }
                }
                .show(parentFragmentManager, ConfirmationDialogFragment.TAG)

        is ChannelInfoViewEvent.MemberInfoModal ->
            GroupChatInfoMemberOptionsDialogFragment.newInstance(
                cid = modal.cid,
                memberId = modal.member.getUserId(),
            ).show(parentFragmentManager, GroupChatInfoMemberOptionsDialogFragment.TAG)

        is ChannelInfoViewEvent.BanMemberModal -> {
            val items = modal.timeouts.map { timeout ->
                getString(
                    when (timeout) {
                        ChannelInfoViewEvent.BanMemberModal.Timeout.OneHour ->
                            R.string.stream_ui_channel_info_ban_member_modal_timeout_one_hour

                        ChannelInfoViewEvent.BanMemberModal.Timeout.OneDay ->
                            R.string.stream_ui_channel_info_ban_member_modal_timeout_one_day

                        ChannelInfoViewEvent.BanMemberModal.Timeout.OneWeek ->
                            R.string.stream_ui_channel_info_ban_member_modal_timeout_one_week

                        ChannelInfoViewEvent.BanMemberModal.Timeout.NoTimeout ->
                            R.string.stream_ui_channel_info_ban_member_modal_no_timeout
                    },
                )
            }
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.stream_ui_channel_info_ban_member_modal_title, modal.member.user.name))
                .setItems(items.toTypedArray()) { _, which ->
                    val timeout = modal.timeouts[which]
                    viewModel.onViewAction(
                        ChannelInfoViewAction.BanMemberConfirmationClick(
                            memberId = modal.member.getUserId(),
                            timeoutInMinutes = timeout.valueInMinutes,
                        ),
                    )
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }

        is ChannelInfoViewEvent.RemoveMemberModal ->
            ConfirmationDialogFragment.newRemoveMemberInstance(requireContext(), modal.member)
                .apply {
                    confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                        viewModel.onViewAction(
                            ChannelInfoViewAction.RemoveMemberConfirmationClick(
                                memberId = modal.member.getUserId(),
                            ),
                        )
                    }
                }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)
    }
}

internal fun Fragment.showError(error: ChannelInfoViewEvent.Error, isGroupChannel: Boolean) {
    when (error) {
        ChannelInfoViewEvent.RenameChannelError,
        -> R.string.stream_ui_channel_info_rename_group_error

        ChannelInfoViewEvent.MuteChannelError,
        ChannelInfoViewEvent.UnmuteChannelError,
        -> if (isGroupChannel) {
            R.string.stream_ui_channel_info_mute_conversation_error
        } else {
            R.string.stream_ui_channel_info_mute_group_error
        }

        ChannelInfoViewEvent.HideChannelError,
        ChannelInfoViewEvent.UnhideChannelError,
        -> if (isGroupChannel) {
            R.string.stream_ui_channel_info_hide_conversation_error
        } else {
            R.string.stream_ui_channel_info_hide_group_error
        }

        ChannelInfoViewEvent.LeaveChannelError,
        -> if (isGroupChannel) {
            R.string.stream_ui_channel_info_leave_conversation_error
        } else {
            R.string.stream_ui_channel_info_leave_group_error
        }

        ChannelInfoViewEvent.DeleteChannelError,
        -> if (isGroupChannel) {
            R.string.stream_ui_channel_info_delete_conversation_error
        } else {
            R.string.stream_ui_channel_info_delete_group_error
        }

        ChannelInfoViewEvent.BanMemberError,
        -> R.string.stream_ui_channel_info_ban_member_error

        ChannelInfoViewEvent.UnbanMemberError,
        -> R.string.stream_ui_channel_info_unban_member_error

        ChannelInfoViewEvent.RemoveMemberError,
        -> R.string.stream_ui_channel_info_remove_member_error
    }.let(::showToast)
}
