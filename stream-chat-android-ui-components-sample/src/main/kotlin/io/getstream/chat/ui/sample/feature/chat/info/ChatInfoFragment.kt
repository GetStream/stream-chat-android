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

package io.getstream.chat.ui.sample.feature.chat.info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoBinding
import io.getstream.chat.ui.sample.feature.common.ConfirmationDialogFragment

class ChatInfoFragment : Fragment() {

    private val args: ChatInfoFragmentArgs by navArgs()
    private val viewModel: ChannelInfoViewModel by viewModels {
        ChannelInfoViewModelFactory(context = requireContext(), cid = args.cid)
    }
    private val adapter: ChatInfoAdapter = ChatInfoAdapter()

    private var _binding: FragmentChatInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        binding.optionsRecyclerView.itemAnimator = null
        binding.optionsRecyclerView.adapter = adapter
        bindChatInfoViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindChatInfoViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ChannelInfoViewState.Loading -> {
                    binding.optionsRecyclerView.isVisible = false
                    binding.progressBar.isVisible = true
                }

                is ChannelInfoViewState.Content -> {
                    val member = state.members.first()
                    setOnClickListeners(member)
                    adapter.submitList(buildChatInfoItems(member, state.options))
                    binding.optionsRecyclerView.isVisible = true
                    binding.progressBar.isVisible = false
                }
            }
        }
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ChannelInfoViewEvent.Error -> showError(event)
                is ChannelInfoViewEvent.Navigation -> onNavigationEvent(event)
                is ChannelInfoViewEvent.Modal -> Unit
            }
        }
    }

    private fun showError(error: ChannelInfoViewEvent.Error) {
        when (error) {
            ChannelInfoViewEvent.RenameChannelError,
            -> R.string.stream_ui_channel_info_rename_group_error

            ChannelInfoViewEvent.MuteChannelError,
            ChannelInfoViewEvent.UnmuteChannelError,
            -> R.string.stream_ui_channel_info_mute_conversation_error

            ChannelInfoViewEvent.HideChannelError,
            ChannelInfoViewEvent.UnhideChannelError,
            -> R.string.stream_ui_channel_info_hide_conversation_error

            ChannelInfoViewEvent.LeaveChannelError,
            -> R.string.stream_ui_channel_info_leave_conversation_error

            ChannelInfoViewEvent.DeleteChannelError,
            -> R.string.stream_ui_channel_info_delete_conversation_error

            ChannelInfoViewEvent.RemoveMemberError,
            -> R.string.stream_ui_channel_info_remove_member_error

            ChannelInfoViewEvent.BanMemberError,
            -> R.string.stream_ui_channel_info_ban_member_error

            ChannelInfoViewEvent.UnbanMemberError,
            -> R.string.stream_ui_channel_info_unban_member_error
        }.let(::showToast)
    }

    private fun onNavigationEvent(event: ChannelInfoViewEvent.Navigation) {
        when (event) {
            is ChannelInfoViewEvent.NavigateUp ->
                findNavController().popBackStack(R.id.homeFragment, false)

            is ChannelInfoViewEvent.NavigateToPinnedMessages ->
                findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToPinnedMessageListFragment(args.cid),
                )
            // No need to handle these in ChatInfoFragment,
            // as it is only applicable for group channels.
            is ChannelInfoViewEvent.NavigateToChannel,
            is ChannelInfoViewEvent.NavigateToDraftChannel,
            -> Unit
        }
    }

    private fun buildChatInfoItems(
        member: Member,
        options: List<ChannelInfoViewState.Content.Option>,
    ): List<ChatInfoItem> = buildList {
        add(ChatInfoItem.MemberItem(member = member))
        add(ChatInfoItem.Separator)

        options.forEach { option ->
            when (option) {
                is ChannelInfoViewState.Content.Option.MuteChannel ->
                    add(ChatInfoItem.Option.Stateful.MuteDistinctChannel(isChecked = option.isMuted))

                is ChannelInfoViewState.Content.Option.HideChannel ->
                    add(ChatInfoItem.Option.HideChannel(isHidden = option.isHidden))

                is ChannelInfoViewState.Content.Option.PinnedMessages -> {
                    add(ChatInfoItem.Option.PinnedMessages)
                    // These options aren't coming from the ViewModel yet, so we add them manually
                    add(ChatInfoItem.Option.SharedMedia)
                    add(ChatInfoItem.Option.SharedFiles)
                    add(ChatInfoItem.Option.SharedGroups)
                }

                is ChannelInfoViewState.Content.Option.LeaveChannel ->
                    add(ChatInfoItem.Option.LeaveGroup)

                is ChannelInfoViewState.Content.Option.DeleteChannel ->
                    add(ChatInfoItem.Option.DeleteConversation)

                is ChannelInfoViewState.Content.Option.Separator ->
                    add(ChatInfoItem.Separator)

                // Not applicable in this UI
                is ChannelInfoViewState.Content.Option.AddMember,
                is ChannelInfoViewState.Content.Option.UserInfo,
                is ChannelInfoViewState.Content.Option.RenameChannel,
                -> Unit
            }
        }

        // add(ChatInfoItem.Option.SharedMedia)
        // add(ChatInfoItem.Option.SharedFiles)
        // if (content.member != null) {
        //     add(ChatInfoItem.Option.SharedGroups)
        // }
    }

    private fun setOnClickListeners(member: Member) {
        adapter.setChatInfoStatefulOptionChangedListener { option, isChecked ->
            viewModel.onViewAction(
                when (option) {
                    is ChatInfoItem.Option.Stateful.MuteDistinctChannel ->
                        ChannelInfoViewAction.MuteChannelClick

                    else -> throw IllegalStateException("Chat info option $option is not supported!")
                },
            )
        }
        adapter.setChatInfoOptionClickListener { option ->
            when (option) {
                ChatInfoItem.Option.PinnedMessages -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToPinnedMessageListFragment(args.cid),
                )

                ChatInfoItem.Option.SharedMedia -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedMediaFragment(args.cid),
                )

                ChatInfoItem.Option.SharedFiles -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedFilesFragment(args.cid),
                )

                ChatInfoItem.Option.SharedGroups -> {
                    // Option shouldn't be visible when member is not set
                    // val member = viewModel.state.value!!.member ?: return@setChatInfoOptionClickListener
                    findNavController().navigateSafely(
                        ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedGroupsFragment(
                            member.getUserId(),
                            member.user.name,
                        ),
                    )
                }

                ChatInfoItem.Option.DeleteConversation -> {
                    ConfirmationDialogFragment.newDeleteChannelInstance(
                        context = requireContext(),
                        isGroupChannel = false,
                    ).apply {
                        confirmClickListener = ConfirmationDialogFragment.ConfirmClickListener {
                            viewModel.onViewAction(ChannelInfoViewAction.DeleteChannelConfirmationClick)
                        }
                    }.show(parentFragmentManager, ConfirmationDialogFragment.TAG)
                }

                else -> throw IllegalStateException("Chat info option $option is not supported!")
            }
        }
    }
}
