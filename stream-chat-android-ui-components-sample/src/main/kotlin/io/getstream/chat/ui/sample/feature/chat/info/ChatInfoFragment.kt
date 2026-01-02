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
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModel
import io.getstream.chat.android.ui.viewmodel.channel.ChannelInfoViewModelFactory
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoBinding

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
                is ChannelInfoViewEvent.Error -> showError(event, isGroupChannel = false)
                is ChannelInfoViewEvent.Navigation -> onNavigationEvent(event)
                is ChannelInfoViewEvent.Modal -> showModal(event, viewModel, isGroupChannel = false)
            }
        }
    }

    private fun onNavigationEvent(event: ChannelInfoViewEvent.Navigation) {
        when (event) {
            is ChannelInfoViewEvent.NavigateUp ->
                findNavController().popBackStack(R.id.homeFragment, false)

            is ChannelInfoViewEvent.NavigateToPinnedMessages ->
                findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToPinnedMessageListFragment(args.cid),
                )

            is ChannelInfoViewEvent.NavigateToMediaAttachments ->
                findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedMediaFragment(args.cid),
                )

            is ChannelInfoViewEvent.NavigateToFilesAttachments ->
                findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedFilesFragment(args.cid),
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
        addAll(options.toChannelInfoItems(isGroupChannel = false))
    }

    private fun setOnClickListeners(member: Member) {
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

                ChatInfoItem.Option.SharedGroups -> findNavController().navigateSafely(
                    ChatInfoFragmentDirections.actionChatInfoFragmentToChatInfoSharedGroupsFragment(
                        member.getUserId(),
                        member.user.name,
                    ),
                )

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

                // Already handled
                is ChatInfoItem.Option.Stateful.MuteChannel -> Unit
            }
        }
    }
}
