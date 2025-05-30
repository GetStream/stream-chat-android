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

@file:OptIn(ExperimentalStreamChatApi::class)

package io.getstream.chat.ui.sample.feature.chat.info.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentGroupChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem
import io.getstream.chat.ui.sample.feature.chat.info.group.member.GroupChatInfoMemberOptionsDialogFragment
import io.getstream.chat.ui.sample.feature.chat.info.group.users.GroupChatInfoAddUsersDialogFragment
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import io.getstream.chat.ui.sample.util.extensions.autoScrollToTop
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize

class GroupChatInfoFragment : Fragment() {

    private val args: GroupChatInfoFragmentArgs by navArgs()
    private val viewModel: ChannelInfoViewModel by viewModels {
        ChannelInfoViewModelFactory(context = requireContext(), cid = args.cid)
    }
    private val headerViewModel: MessageListHeaderViewModel by viewModels {
        MessageListViewModelFactory(requireContext(), args.cid)
    }
    private val adapter: GroupChatInfoAdapter = GroupChatInfoAdapter()

    private var _binding: FragmentGroupChatInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentGroupChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.headerView.setBackButtonClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.optionsRecyclerView.adapter = adapter
        binding.optionsRecyclerView.autoScrollToTop()
        headerViewModel.bindView(binding.headerView, viewLifecycleOwner)
        // if (!isAnonymousChannel()) {
        //     binding.addChannelButton.apply {
        //         isVisible = true
        //         setOnClickListener {
        //             GroupChatInfoAddUsersDialogFragment.newInstance(args.cid)
        //                 .show(parentFragmentManager, GroupChatInfoAddUsersDialogFragment.TAG)
        //         }
        //     }
        // }
        bindGroupInfoViewModel()
        parentFragmentManager.setFragmentResultListener(
            GroupChatInfoMemberOptionsDialogFragment.REQUEST_KEY,
            viewLifecycleOwner,
        ) { _, result ->
            val event = GroupChatInfoMemberOptionsDialogFragment.getEventFromResult(result)
            viewModel.onMemberViewEvent(event)
        }
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindGroupInfoViewModel() {
        // subscribeForChannelMutesUpdatedEvents()
        // subscribeForChannelVisibilityEvents()
        setOnClickListeners()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ChannelInfoViewEvent.Error -> showError(event)
                is ChannelInfoViewEvent.Navigation -> onNavigationEvent(event)
                is ChannelInfoViewEvent.Modal -> onModalEvent(event)
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChannelInfoViewState.Loading -> {
                    // TODO: Show loading state if needed
                }

                is ChannelInfoViewState.Content -> {
                    if (state.options.contains(ChannelInfoViewState.Content.Option.AddMember)) {
                        binding.addChannelButton.apply {
                            isVisible = true
                            setOnClickListener {
                                GroupChatInfoAddUsersDialogFragment.newInstance(args.cid)
                                    .show(parentFragmentManager, GroupChatInfoAddUsersDialogFragment.TAG)
                            }
                        }
                    }
                    val members = state.members.map { member ->
                        ChatInfoItem.MemberItem(
                            member = member,
                            isOwner = state.owner.id == member.getUserId(),
                        )
                    }
                    val options = state.options.mapNotNull { option ->
                        when (option) {
                            is ChannelInfoViewState.Content.Option.Separator,
                            -> ChatInfoItem.Separator

                            is ChannelInfoViewState.Content.Option.AddMember,
                            -> null // Not rendered as an option item

                            is ChannelInfoViewState.Content.Option.RenameChannel,
                            -> ChatInfoItem.ChannelName(option.name)

                            is ChannelInfoViewState.Content.Option.MuteChannel,
                            -> ChatInfoItem.Option.Stateful.MuteChannel(isChecked = option.isMuted)

                            is ChannelInfoViewState.Content.Option.HideChannel,
                            -> ChatInfoItem.Option.HideChannel(isHidden = option.isHidden)

                            is ChannelInfoViewState.Content.Option.PinnedMessages,
                            -> ChatInfoItem.Option.PinnedMessages

                            is ChannelInfoViewState.Content.Option.LeaveChannel,
                            -> ChatInfoItem.Option.LeaveGroup

                            is ChannelInfoViewState.Content.Option.DeleteChannel,
                            -> ChatInfoItem.Option.DeleteConversation

                            // Not applicable in this UI
                            is ChannelInfoViewState.Content.Option.UserInfo,
                            -> null
                        }
                    }
                    val optionsWithExtras = buildList {
                        val separatorIndex = options.indexOfFirst { it == ChatInfoItem.Separator }
                        val insertIndex = if (separatorIndex != -1) separatorIndex else options.size

                        addAll(options.subList(0, insertIndex))

                        // These options aren't coming from the ViewModel yet, so we add them manually
                        add(ChatInfoItem.Option.SharedMedia)
                        add(ChatInfoItem.Option.SharedFiles)

                        addAll(options.subList(insertIndex, options.size))
                    }
                    val items = buildList {
                        addAll(members)
                        if (state.members.canExpand && state.members.isCollapsed) {
                            add(ChatInfoItem.MembersSeparator(state.members.collapsedCount))
                        }
                        add(ChatInfoItem.Separator)
                        addAll(optionsWithExtras)
                    }
                    adapter.submitList(items)
                }
            }
        }
    }

    private fun showError(error: ChannelInfoViewEvent.Error) {
        when (error) {
            ChannelInfoViewEvent.RenameChannelError,
            -> R.string.stream_ui_channel_info_rename_group_error

            ChannelInfoViewEvent.MuteChannelError,
            ChannelInfoViewEvent.UnmuteChannelError,
            -> R.string.stream_ui_channel_info_mute_group_error

            ChannelInfoViewEvent.HideChannelError,
            ChannelInfoViewEvent.UnhideChannelError,
            -> R.string.stream_ui_channel_info_hide_group_error

            ChannelInfoViewEvent.LeaveChannelError,
            -> R.string.stream_ui_channel_info_leave_group_error

            ChannelInfoViewEvent.DeleteChannelError,
            -> R.string.stream_ui_channel_info_delete_group_error

            ChannelInfoViewEvent.BanMemberError,
            -> R.string.stream_ui_channel_info_ban_member_error

            ChannelInfoViewEvent.UnbanMemberError,
            -> R.string.stream_ui_channel_info_unban_member_error

            ChannelInfoViewEvent.RemoveMemberError,
            -> R.string.stream_ui_channel_info_remove_member_error
        }.let(::showToast)
    }

    private fun onNavigationEvent(event: ChannelInfoViewEvent.Navigation) {
        when (event) {
            is ChannelInfoViewEvent.NavigateUp ->
                findNavController().popBackStack(R.id.homeFragment, false)

            is ChannelInfoViewEvent.NavigateToPinnedMessages ->
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToPinnedMessageListFragment(args.cid),
                )

            is ChannelInfoViewEvent.NavigateToChannel ->
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionOpenChat(event.cid),
                )

            is ChannelInfoViewEvent.NavigateToDraftChannel ->
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionOpenChatPreview(event.memberId),
                )
        }
    }

    private fun onModalEvent(event: ChannelInfoViewEvent.Modal) {
        when (event) {
            is ChannelInfoViewEvent.MemberInfoModal ->
                GroupChatInfoMemberOptionsDialogFragment.newInstance(
                    cid = event.cid,
                    memberId = event.member.getUserId(),
                ).show(parentFragmentManager, GroupChatInfoMemberOptionsDialogFragment.TAG)

            is ChannelInfoViewEvent.BanMemberModal -> {
                val items = event.timeouts.map { timeout ->
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
                    .setTitle(getString(R.string.stream_ui_channel_info_ban_member_modal_title, event.member.user.name))
                    .setItems(items.toTypedArray()) { _, which ->
                        val timeout = event.timeouts[which]
                        viewModel.onViewAction(
                            ChannelInfoViewAction.BanMemberConfirmationClick(
                                memberId = event.member.getUserId(),
                                timeoutInMinutes = timeout.valueInMinutes,
                            ),
                        )
                    }
                    .show()
            }

            is ChannelInfoViewEvent.DeleteChannelModal ->
                ConfirmationDialogFragment.newDeleteChannelInstance(requireContext(), isGroupChannel = true)
                    .apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                            viewModel.onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)
                        }
                    }
                    .show(parentFragmentManager, ConfirmationDialogFragment.TAG)

            is ChannelInfoViewEvent.HideChannelModal ->
                ConfirmationDialogFragment.newHideChannelInstance(requireContext(), isGroupChannel = true)
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
                    }
                    .show(parentFragmentManager, ConfirmationDialogFragment.TAG)

            is ChannelInfoViewEvent.LeaveChannelModal ->
                ConfirmationDialogFragment.newLeaveChannelInstance(requireContext())
                    .apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                            viewModel.onViewAction(
                                ChannelInfoViewAction.LeaveChannelConfirmationClick(quitMessage = null),
                            )
                        }
                    }
                    .show(parentFragmentManager, ConfirmationDialogFragment.TAG)

            is ChannelInfoViewEvent.RemoveMemberModal ->
                ConfirmationDialogFragment.newRemoveMemberInstance(requireContext(), event.member)
                    .apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                            viewModel.onViewAction(
                                ChannelInfoViewAction.RemoveMemberConfirmationClick(
                                    memberId = event.member.getUserId(),
                                ),
                            )
                        }
                    }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)
        }
    }

    private fun setOnClickListeners() {
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            when (option) {
                is ChatInfoItem.Option.Stateful.MuteChannel ->
                    viewModel.onViewAction(ChannelInfoViewAction.MuteChannelClick)

                else -> throw IllegalStateException("Chat info option $option is not supported!")
            }
        }
        adapter.setChatInfoOptionClickListener { option ->
            when (option) {
                ChatInfoItem.Option.PinnedMessages -> findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToPinnedMessageListFragment(args.cid),
                )

                ChatInfoItem.Option.SharedMedia -> findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToChatInfoSharedMediaFragment(args.cid),
                )

                ChatInfoItem.Option.SharedFiles -> findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToChatInfoSharedFilesFragment(args.cid),
                )

                ChatInfoItem.Option.LeaveGroup ->
                    viewModel.onViewAction(ChannelInfoViewAction.LeaveChannelClick)

                is ChatInfoItem.Option.HideChannel ->
                    viewModel.onViewAction(ChannelInfoViewAction.HideChannelClick)

                ChatInfoItem.Option.DeleteConversation ->
                    viewModel.onViewAction(ChannelInfoViewAction.DeleteChannelClick)

                ChatInfoItem.Option.SharedGroups,
                is ChatInfoItem.Option.Stateful.Block,
                is ChatInfoItem.Option.Stateful.MuteChannel,
                is ChatInfoItem.Option.Stateful.MuteDistinctChannel,
                -> Unit
            }
        }
        adapter.setMemberClickListener { viewModel.onViewAction(ChannelInfoViewAction.MemberClick(it)) }
        adapter.setMembersSeparatorClickListener { viewModel.onViewAction(ChannelInfoViewAction.ExpandMembersClick) }
        adapter.setNameChangedListener { viewModel.onViewAction(ChannelInfoViewAction.RenameChannelClick(name = it)) }
    }
}
