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
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.subscribeFor
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentGroupChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.ChatViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem
import io.getstream.chat.ui.sample.feature.chat.info.group.member.GroupChatInfoMemberOptionsDialogFragment
import io.getstream.chat.ui.sample.feature.chat.info.group.users.GroupChatInfoAddUsersDialogFragment
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment
import io.getstream.chat.ui.sample.util.extensions.autoScrollToTop
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize
import io.getstream.log.taggedLogger

class GroupChatInfoFragment : Fragment() {

    private val logger by taggedLogger("GroupChatInfo-View")

    private val args: GroupChatInfoFragmentArgs by navArgs()
    private val viewModel: GroupChatInfoViewModel by viewModels { ChatViewModelFactory(args.cid) }
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
            requireActivity().onBackPressed()
        }
        binding.optionsRecyclerView.adapter = adapter
        binding.optionsRecyclerView.autoScrollToTop()
        headerViewModel.bindView(binding.headerView, viewLifecycleOwner)
        if (!isAnonymousChannel()) {
            binding.addChannelButton.apply {
                isVisible = true
                setOnClickListener {
                    GroupChatInfoAddUsersDialogFragment.newInstance(args.cid)
                        .show(parentFragmentManager, GroupChatInfoAddUsersDialogFragment.TAG)
                }
            }
        }
        bindGroupInfoViewModel()
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    // Distinct channel == channel created without id (based on members).
    // There is no possibility to modify distinct channel members.
    private fun isAnonymousChannel(): Boolean = args.cid.contains("!members")

    private fun bindGroupInfoViewModel() {
        subscribeForChannelMutesUpdatedEvents()
        subscribeForChannelVisibilityEvents()
        setOnClickListeners()

        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is GroupChatInfoViewModel.UiEvent.ShowMemberOptions ->
                        GroupChatInfoMemberOptionsDialogFragment.newInstance(
                            args.cid,
                            it.channelName,
                            it.member.user,
                            viewModel.state.value!!.ownCapabilities,
                        )
                            .show(parentFragmentManager, GroupChatInfoMemberOptionsDialogFragment.TAG)
                    GroupChatInfoViewModel.UiEvent.RedirectToHome -> findNavController().popBackStack(
                        R.id.homeFragment,
                        false,
                    )
                }
            },
        )
        viewModel.state.observe(viewLifecycleOwner) { state ->
            val members = if (state.shouldExpandMembers != false) {
                state.members.map { ChatInfoItem.MemberItem(it, state.createdBy) }
            } else {
                state.members.take(GroupChatInfoViewModel.COLLAPSED_MEMBERS_COUNT)
                    .map { ChatInfoItem.MemberItem(it, state.createdBy) } + ChatInfoItem.MembersSeparator(state.membersToShowCount)
            }
            adapter.submitList(
                members +
                    listOf(
                        ChatInfoItem.Separator,
                        ChatInfoItem.ChannelName(state.channelName),
                        ChatInfoItem.Option.Stateful.MuteChannel(isChecked = state.channelMuted),
                        ChatInfoItem.Option.HideChannel(isHidden = state.channelHidden),
                        ChatInfoItem.Option.PinnedMessages,
                        ChatInfoItem.Option.SharedMedia,
                        ChatInfoItem.Option.SharedFiles,
                        ChatInfoItem.Option.LeaveGroup,
                    ),
            )
        }
        viewModel.errorEvents.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is GroupChatInfoViewModel.ErrorEvent.ChangeGroupNameError -> R.string.chat_group_info_error_change_name
                    is GroupChatInfoViewModel.ErrorEvent.MuteChannelError -> R.string.chat_group_info_error_mute_channel
                    is GroupChatInfoViewModel.ErrorEvent.HideChannelError -> R.string.chat_group_info_error_hide_channel
                    is GroupChatInfoViewModel.ErrorEvent.LeaveChannelError -> R.string.chat_group_info_error_leave_channel
                }.let(::showToast)
            },
        )
    }

    private fun setOnClickListeners() {
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            logger.d { "[onStatefulOptionChanged] option: $option, isChecked: $isChecked" }

            when (option) {
                is ChatInfoItem.Option.Stateful.MuteChannel -> viewModel.onAction(
                    GroupChatInfoViewModel.Action.MuteChannelClicked(isChecked),
                )
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
                ChatInfoItem.Option.LeaveGroup -> {
                    val channelName = viewModel.state.value!!.channelName
                    ConfirmationDialogFragment.newLeaveChannelInstance(requireContext(), channelName)
                        .apply {
                            confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                                viewModel.onAction(GroupChatInfoViewModel.Action.LeaveChannelClicked)
                            }
                        }
                        .show(parentFragmentManager, ConfirmationDialogFragment.TAG)
                }
                is ChatInfoItem.Option.HideChannel -> prepareHideChannelClickedAction {
                    viewModel.onAction(it)
                }
                else -> throw IllegalStateException("Group chat info option $option is not supported!")
            }
        }
        adapter.setMemberClickListener { viewModel.onAction(GroupChatInfoViewModel.Action.MemberClicked(it)) }
        adapter.setMembersSeparatorClickListener { viewModel.onAction(GroupChatInfoViewModel.Action.MembersSeparatorClicked) }
        adapter.setNameChangedListener { viewModel.onAction(GroupChatInfoViewModel.Action.NameChanged(it)) }
    }

    private fun subscribeForChannelMutesUpdatedEvents() {
        ChatClient.instance().subscribeFor<NotificationChannelMutesUpdatedEvent>(viewLifecycleOwner) {
            viewModel.onAction(GroupChatInfoViewModel.Action.ChannelMutesUpdated(it.me.channelMutes))
        }
    }

    private fun subscribeForChannelVisibilityEvents() {
        ChatClient.instance().subscribeFor<ChannelHiddenEvent>(viewLifecycleOwner) {
            viewModel.onAction(
                GroupChatInfoViewModel.Action.ChannelHiddenUpdated(
                    cid = it.cid,
                    hidden = true,
                    clearHistory = it.clearHistory,
                ),
            )
        }
        ChatClient.instance().subscribeFor<ChannelVisibleEvent>(viewLifecycleOwner) {
            viewModel.onAction(
                GroupChatInfoViewModel.Action.ChannelHiddenUpdated(
                    cid = it.cid,
                    hidden = false,
                ),
            )
        }
    }

    private fun prepareHideChannelClickedAction(
        onReady: (GroupChatInfoViewModel.Action.HideChannelClicked) -> Unit,
    ) {
        val curValue = viewModel.state.value!!.channelHidden
        val newValue = curValue.not()
        val action = GroupChatInfoViewModel.Action.HideChannelClicked(newValue)
        if (newValue) {
            val channelName = viewModel.state.value!!.channelName
            ConfirmationDialogFragment.newHideChannelInstance(requireContext(), channelName)
                .apply {
                    confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                        onReady(action.copy(clearHistory = true))
                    }
                    cancelClickListener = ConfirmationDialogFragment.CancelClickListener {
                        onReady(action)
                    }
                }
                .show(parentFragmentManager, ConfirmationDialogFragment.TAG)
        } else {
            onReady(action)
        }
    }
}
