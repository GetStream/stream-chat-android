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

package io.getstream.chat.ui.sample.feature.chat.info.shared.files

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.navigation.destinations.AttachmentDestination
import io.getstream.chat.android.ui.widgets.EndlessScrollListener
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoSharedFilesBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.ChatInfoSharedAttachmentsViewModel
import io.getstream.chat.ui.sample.feature.chat.info.shared.ChatInfoSharedAttachmentsViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment

class ChatInfoSharedFilesFragment : Fragment() {

    private val args: ChatInfoSharedFilesFragmentArgs by navArgs()
    private val viewModel: ChatInfoSharedAttachmentsViewModel by viewModels {
        ChatInfoSharedAttachmentsViewModelFactory(
            args.cid!!,
            ChatInfoSharedAttachmentsViewModel.AttachmentsType.FILES
        )
    }
    private val adapter: ChatInfoSharedFilesAdapter = ChatInfoSharedFilesAdapter()
    private val scrollListener = EndlessScrollListener(LOAD_MORE_THRESHOLD) {
        viewModel.onAction(ChatInfoSharedAttachmentsViewModel.Action.LoadMoreRequested)
    }

    private var _binding: FragmentChatInfoSharedFilesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatInfoSharedFilesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)
        binding.filesRecyclerView.adapter = adapter
        binding.filesRecyclerView.addOnScrollListener(scrollListener)
        adapter.setAttachmentClickListener { attachmentItem ->
            AttachmentDestination(
                attachmentItem.message,
                attachmentItem.attachment,
                requireContext(),
            ).apply {
                ChatUI.navigator.navigate(this)
            }
        }
        if (args.cid != null) {
            bindViewModel()
        } else {
            showEmptyState()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (state.isLoading) {
                showLoading()
            } else {
                if (state.results.isEmpty()) {
                    showEmptyState()
                } else {
                    showResults(state.results)
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.filesRecyclerView.isVisible = false
        binding.emptyStateView.isVisible = false
        scrollListener.disablePagination()
    }

    private fun showResults(attachments: List<SharedAttachment>) {
        binding.progressBar.isVisible = false
        binding.filesRecyclerView.isVisible = true
        binding.emptyStateView.isVisible = false
        scrollListener.enablePagination()
        adapter.submitList(attachments)
    }

    private fun showEmptyState() {
        binding.progressBar.isVisible = false
        binding.filesRecyclerView.isVisible = false
        binding.emptyStateView.isVisible = true
        scrollListener.disablePagination()
    }

    companion object {
        private const val LOAD_MORE_THRESHOLD = 10
    }
}
