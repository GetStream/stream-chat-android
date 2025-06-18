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

package io.getstream.chat.ui.sample.feature.chat.preview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.state.utils.EventObserver
import io.getstream.chat.android.ui.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListHeaderViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.ui.viewmodel.messages.MessageListViewModelFactory
import io.getstream.chat.android.ui.viewmodel.messages.bindView
import io.getstream.chat.ui.sample.common.navigateSafely
import io.getstream.chat.ui.sample.databinding.FragmentChatPreviewBinding
import io.getstream.chat.ui.sample.util.extensions.useAdjustResize

class ChatPreviewFragment : Fragment() {

    private val args: ChatPreviewFragmentArgs by navArgs()
    private val viewModel: ChatPreviewViewModel by viewModels { ChatPreviewViewModelFactory(args.memberId) }

    private var _binding: FragmentChatPreviewBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatPreviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.messagesHeaderView.setBackButtonClickListener {
            findNavController().navigateUp()
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.cid != null) {
                binding.progressBar.isVisible = false
                initializeChatPreview(state.cid)
            }
        }
        viewModel.events.observe(
            viewLifecycleOwner,
            EventObserver {
                when (it) {
                    is ChatPreviewViewModel.UiEvent.NavigateToChat -> findNavController().navigateSafely(
                        ChatPreviewFragmentDirections.actionOpenChat(cid = it.cid),
                    )
                }
            },
        )
    }

    override fun onResume() {
        super.onResume()
        useAdjustResize()
    }

    private fun initializeChatPreview(cid: String) {
        val factory = MessageListViewModelFactory(requireContext(), cid)
        val messageListHeaderViewModel = factory.create(MessageListHeaderViewModel::class.java)
        val messageListViewModel = factory.create(MessageListViewModel::class.java)
        val messageComposerViewModel = factory.create(MessageComposerViewModel::class.java)

        binding.messagesHeaderView.apply {
            messageListHeaderViewModel.bindView(this, viewLifecycleOwner)

            setAvatarClickListener {
                navigateToChatInfo(cid)
            }
            setTitleClickListener {
                navigateToChatInfo(cid)
            }
            setSubtitleClickListener {
                navigateToChatInfo(cid)
            }
        }

        binding.messageListView.apply {
            isVisible = true
            messageListViewModel.bindView(this, viewLifecycleOwner)
        }

        binding.messageComposerView.apply {
            isVisible = true
            messageComposerViewModel.bindView(this, viewLifecycleOwner)
            sendMessageButtonClickListener = {
                viewModel.onAction(ChatPreviewViewModel.Action.MessageSent)
            }
        }
    }

    private fun navigateToChatInfo(cid: String) {
        findNavController().navigateSafely(
            ChatPreviewFragmentDirections.actionOpenChatInfo(cid = cid),
        )
    }
}
