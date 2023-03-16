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

package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import androidx.navigation.fragment.navArgs
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.gallery.AttachmentGalleryDestination
import io.getstream.chat.android.ui.gallery.AttachmentGalleryItem
import io.getstream.chat.ui.sample.common.initToolbar
import io.getstream.chat.ui.sample.common.showToast
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoSharedMediaBinding
import io.getstream.chat.ui.sample.feature.chat.info.shared.ChatInfoSharedAttachmentsViewModel
import io.getstream.chat.ui.sample.feature.chat.info.shared.ChatInfoSharedAttachmentsViewModelFactory
import io.getstream.chat.ui.sample.feature.chat.info.shared.SharedAttachment

class ChatInfoSharedMediaFragment : Fragment() {

    private val args: ChatInfoSharedMediaFragmentArgs by navArgs()
    private val factory: ChatInfoSharedAttachmentsViewModelFactory by lazy {
        ChatInfoSharedAttachmentsViewModelFactory(
            args.cid!!,
            ChatInfoSharedAttachmentsViewModel.AttachmentsType.MEDIA
        )
    }
    private val viewModel: ChatInfoSharedAttachmentsViewModel by viewModels { factory }

    private val attachmentGalleryDestination by lazy {
        AttachmentGalleryDestination(
            requireContext(),
            attachmentReplyOptionHandler = {
                showToast("Reply")
            },
            attachmentShowInChatOptionHandler = {
                showToast("Show in chat")
            },
            attachmentDownloadOptionHandler = {
                showToast("Download")
            },
            attachmentDeleteOptionClickHandler = {
                showToast("Delete")
            },
        )
    }

    private var _binding: FragmentChatInfoSharedMediaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentChatInfoSharedMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initToolbar(binding.toolbar)

        activity?.activityResultRegistry?.let { registry ->
            attachmentGalleryDestination.register(registry)
        }
        binding.mediaAttachmentGridView.setMediaClickListener {
            val attachmentGalleryItems = binding.mediaAttachmentGridView.getAttachments()
            attachmentGalleryDestination.setData(attachmentGalleryItems, it)
            ChatUI.navigator.navigate(attachmentGalleryDestination)
        }
        binding.mediaAttachmentGridView.setOnLoadMoreListener {
            viewModel.onAction(ChatInfoSharedAttachmentsViewModel.Action.LoadMoreRequested)
        }

        if (args.cid != null) {
            bindViewModel()
        } else {
            showEmptyState()
        }
    }

    override fun onDestroyView() {
        attachmentGalleryDestination.unregister()
        _binding = null
        super.onDestroyView()
    }

    private fun bindViewModel() {
        Transformations.switchMap(viewModel.state) { state ->
            Transformations.map(ChatClient.instance().clientState.user.asLiveData()) { user ->
                user to state
            }
        }.observe(viewLifecycleOwner) { (user, state) ->
            if (state.isLoading) {
                showLoading()
            } else {
                val results = state.results.filterIsInstance<SharedAttachment.AttachmentItem>()
                    .mapNotNull {
                        if (!it.attachment.imageUrl.isNullOrEmpty() && it.attachment.titleLink.isNullOrEmpty()) {
                            AttachmentGalleryItem(
                                attachment = it.attachment,
                                user = it.message.user,
                                createdAt = it.createdAt,
                                messageId = it.message.id,
                                cid = it.message.cid,
                                isMine = it.message.user.id == user?.id,
                                parentMessageId = it.message.parentId,
                            )
                        } else {
                            null
                        }
                    }
                if (results.isEmpty()) {
                    showEmptyState()
                } else {
                    showResults(results)
                }
            }
        }
    }

    private fun showLoading() {
        with(binding) {
            separator.isVisible = true
            progressBar.isVisible = true
            mediaAttachmentGridView.isVisible = false
            emptyStateView.isVisible = false
            mediaAttachmentGridView.disablePagination()
        }
    }

    private fun showResults(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        with(binding) {
            separator.isVisible = false
            progressBar.isVisible = false
            mediaAttachmentGridView.isVisible = true
            emptyStateView.isVisible = false
            mediaAttachmentGridView.enablePagination()
            mediaAttachmentGridView.setAttachments(attachmentGalleryItems)
        }
    }

    private fun showEmptyState() {
        with(binding) {
            separator.isVisible = true
            progressBar.isVisible = false
            emptyStateView.isVisible = true
            mediaAttachmentGridView.isVisible = false
            mediaAttachmentGridView.disablePagination()
        }
    }
}
