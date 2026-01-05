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
import io.getstream.chat.ui.sample.databinding.FragmentGroupChatInfoBinding
import io.getstream.chat.ui.sample.feature.chat.info.ChatInfoItem
import io.getstream.chat.ui.sample.feature.chat.info.group.member.GroupChatInfoMemberOptionsDialogFragment
import io.getstream.chat.ui.sample.feature.chat.info.group.users.GroupChatInfoAddUsersDialogFragment
import io.getstream.chat.ui.sample.feature.chat.info.showError
import io.getstream.chat.ui.sample.feature.chat.info.showModal
import io.getstream.chat.ui.sample.feature.chat.info.toChannelInfoItems
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
        setOnClickListeners()

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ChannelInfoViewEvent.Error -> showError(event, isGroupChannel = true)
                is ChannelInfoViewEvent.Navigation -> onNavigationEvent(event)
                is ChannelInfoViewEvent.Modal -> showModal(event, viewModel, isGroupChannel = true)
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                // Not applicable in this UI
                is ChannelInfoViewState.Loading -> Unit

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
                    val items = buildList {
                        addAll(members)
                        if (state.members.canExpand && state.members.isCollapsed) {
                            add(ChatInfoItem.MembersSeparator(state.members.collapsedCount))
                        }
                        add(ChatInfoItem.Separator)
                        addAll(state.options.toChannelInfoItems(isGroupChannel = true))
                    }
                    adapter.submitList(items)
                }
            }
        }
    }

    private fun onNavigationEvent(event: ChannelInfoViewEvent.Navigation) {
        when (event) {
            is ChannelInfoViewEvent.NavigateUp ->
                findNavController().popBackStack(R.id.homeFragment, false)

            is ChannelInfoViewEvent.NavigateToPinnedMessages ->
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToPinnedMessageListFragment(args.cid),
                )

            is ChannelInfoViewEvent.NavigateToMediaAttachments ->
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToChatInfoSharedMediaFragment(args.cid),
                )

            is ChannelInfoViewEvent.NavigateToFilesAttachments ->
                findNavController().navigateSafely(
                    GroupChatInfoFragmentDirections.actionGroupChatInfoFragmentToChatInfoSharedFilesFragment(args.cid),
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

    private fun setOnClickListeners() {
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            viewModel.onViewAction(
                when (option) {
                    is ChatInfoItem.Option.Stateful.MuteChannel ->
                        if (isChecked) {
                            ChannelInfoViewAction.MuteChannelClick
                        } else {
                            ChannelInfoViewAction.UnmuteChannelClick
                        }
                },
            )
        }
        adapter.setChatInfoOptionClickListener { option ->
            when (option) {
                ChatInfoItem.Option.PinnedMessages ->
                    viewModel.onViewAction(ChannelInfoViewAction.PinnedMessagesClick)

                ChatInfoItem.Option.SharedMedia ->
                    viewModel.onViewAction(ChannelInfoViewAction.MediaAttachmentsClick)

                ChatInfoItem.Option.SharedFiles ->
                    viewModel.onViewAction(ChannelInfoViewAction.FilesAttachmentsClick)

                is ChatInfoItem.Option.LeaveChannel ->
                    viewModel.onViewAction(ChannelInfoViewAction.LeaveChannelClick)

                is ChatInfoItem.Option.DeleteChannel ->
                    viewModel.onViewAction(ChannelInfoViewAction.DeleteChannelClick)

                is ChatInfoItem.Option.HideChannel -> viewModel.onViewAction(
                    if (option.isChecked) {
                        ChannelInfoViewAction.UnhideChannelClick
                    } else {
                        ChannelInfoViewAction.HideChannelClick
                    },
                )

                // Not applicable in this UI
                ChatInfoItem.Option.SharedGroups -> Unit

                // Already handled
                is ChatInfoItem.Option.Stateful.MuteChannel -> Unit
            }
        }
        adapter.setMemberClickListener { viewModel.onViewAction(ChannelInfoViewAction.MemberClick(it)) }
        adapter.setMembersSeparatorClickListener { viewModel.onViewAction(ChannelInfoViewAction.ExpandMembersClick) }
        adapter.setNameChangedListener { viewModel.onViewAction(ChannelInfoViewAction.RenameChannelClick(name = it)) }
    }
}
