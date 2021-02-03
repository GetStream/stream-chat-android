package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.getstream.sdk.chat.ChatUI
import com.getstream.sdk.chat.utils.Utils
import io.getstream.chat.android.livedata.ChatDomain
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
            ChatUI.instance()
                .navigator
                .navigate(attachmentGalleryDestination)
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
        viewModel.state.observe(viewLifecycleOwner) { state ->
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
                                isMine = it.message.user.id == ChatDomain.instance().currentUser.id
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
            toolbar.elevation = ACTION_BAR_ELEVATION
            progressBar.isVisible = true
            mediaAttachmentGridView.isVisible = false
            emptyStateView.isVisible = false
            mediaAttachmentGridView.disablePagination()
        }
    }

    private fun showResults(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        with(binding) {
            toolbar.elevation = 0f
            progressBar.isVisible = false
            mediaAttachmentGridView.isVisible = true
            emptyStateView.isVisible = false
            mediaAttachmentGridView.enablePagination()
            mediaAttachmentGridView.setAttachments(attachmentGalleryItems)
        }
    }

    private fun showEmptyState() {
        with(binding) {
            toolbar.elevation = ACTION_BAR_ELEVATION
            progressBar.isVisible = false
            emptyStateView.isVisible = true
            mediaAttachmentGridView.isVisible = false
            mediaAttachmentGridView.disablePagination()
        }
    }

    companion object {
        private val ACTION_BAR_ELEVATION = Utils.dpToPx(4).toFloat()
    }
}
