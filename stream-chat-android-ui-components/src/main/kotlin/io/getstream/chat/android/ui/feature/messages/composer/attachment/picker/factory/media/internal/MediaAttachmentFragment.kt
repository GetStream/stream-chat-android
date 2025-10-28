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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.media.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.permissions.VisualMediaAccess
import io.getstream.chat.android.ui.common.permissions.resolveVisualMediaAccessState
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentMediaBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.GridSpacingItemDecoration
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the tab of the attachment picker with the media grid.
 */
internal class MediaAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentMediaBinding? = null
    private val binding get() = _binding!!

    private val storageHelper: StorageHelper = StorageHelper()
    private val permissionChecker: PermissionChecker = PermissionChecker()
    private val attachmentFilter: AttachmentFilter = AttachmentFilter()

    private val gridLayoutManager = GridLayoutManager(context, SPAN_COUNT, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration =
        GridSpacingItemDecoration(SPAN_COUNT, SPACING, false)

    /**
     * Style for the attachment picker dialog.
     */
    private lateinit var style: AttachmentsPickerDialogStyle

    private val mediaAttachmentsAdapter: MediaAttachmentAdapter by lazy {
        MediaAttachmentAdapter(style = style, ::updateMediaAttachment, ::requestPermissions)
    }

    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()

    /**
     * A listener invoked when attachments are selected in the attachment tab.
     */
    private var attachmentsPickerTabListener: AttachmentsPickerTabListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding =
            StreamUiFragmentAttachmentMediaBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (::style.isInitialized) {
            setupViews()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::style.isInitialized) {
            checkPermissions()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        attachmentsPickerTabListener = null
    }

    /**
     * Sets the listener invoked when attachments are selected in the attachment tab.
     */
    fun setAttachmentsPickerTabListener(attachmentsPickerTabListener: AttachmentsPickerTabListener) {
        this.attachmentsPickerTabListener = attachmentsPickerTabListener
    }

    /**
     * Initializes the dialog with the style.
     *
     * @param style Style for the dialog.
     */
    fun setStyle(style: AttachmentsPickerDialogStyle) {
        this.style = style
    }

    private fun setupViews() {
        binding.mediaFileRecyclerView.apply {
            layoutManager = gridLayoutManager
            addItemDecoration(gridSpacingItemDecoration)
            adapter = mediaAttachmentsAdapter
        }
        binding.grantPermissionsInclude.apply {
            grantPermissionsImageView.setImageDrawable(style.allowAccessToMediaIconDrawable)
            grantPermissionsTextView.text = style.allowAccessToMediaButtonText
            grantPermissionsTextView.setTextStyle(style.allowAccessButtonTextStyle)
            grantPermissionsTextView.setOnClickListener {
                requestPermissions()
            }
        }
    }

    private fun checkPermissions() {
        val visualMediaAccess = resolveVisualMediaAccessState(requireContext())
        handleVisualMediaAccessState(visualMediaAccess)
    }

    private fun requestPermissions() {
        permissionChecker.checkVisualMediaPermissions(binding.root) { _ ->
            val visualMediaAccess = resolveVisualMediaAccessState(requireContext())
            handleVisualMediaAccessState(visualMediaAccess)
        }
    }

    private fun handleVisualMediaAccessState(access: VisualMediaAccess) {
        when (access) {
            VisualMediaAccess.FULL -> onPermissionGranted()
            VisualMediaAccess.PARTIAL -> onPermissionPartiallyGranted()
            VisualMediaAccess.DENIED -> onPermissionDenied()
        }
    }

    private fun onPermissionGranted() {
        _binding?.run {
            grantPermissionsInclude.grantPermissionsContainer.isVisible = false
            populateAttachments(allowAddMore = false)
        }
    }

    private fun onPermissionPartiallyGranted() {
        _binding?.run {
            grantPermissionsInclude.grantPermissionsContainer.isVisible = false
            populateAttachments(allowAddMore = true)
        }
    }

    private fun onPermissionDenied() {
        _binding?.run {
            grantPermissionsInclude.grantPermissionsContainer.isVisible = true
        }
    }

    private fun updateMediaAttachment(attachmentMetaData: AttachmentMetaData) {
        if (attachmentMetaData.isSelected) {
            attachmentMetaData.isSelected = false
            selectedAttachments = selectedAttachments - attachmentMetaData
            mediaAttachmentsAdapter.deselectAttachment(attachmentMetaData)
        } else {
            attachmentMetaData.isSelected = true
            selectedAttachments = selectedAttachments + attachmentMetaData
            mediaAttachmentsAdapter.selectAttachment(attachmentMetaData)
        }
        attachmentsPickerTabListener?.onSelectedAttachmentsChanged(selectedAttachments.toList())
    }

    private fun populateAttachments(allowAddMore: Boolean) {
        lifecycleScope.launch(DispatcherProvider.Main) {
            binding.progressBar.isVisible = true

            val attachments = withContext(DispatcherProvider.IO) {
                storageHelper.getMediaAttachments(requireContext())
            }
            val filteredAttachments = attachmentFilter.filterAttachments(attachments)

            if (filteredAttachments.isEmpty()) {
                binding.emptyPlaceholderTextView.setTextStyle(style.mediaAttachmentNoMediaTextStyle)
                binding.emptyPlaceholderTextView.text = style.mediaAttachmentNoMediaText
                binding.emptyPlaceholderTextView.isVisible = true
            } else {
                val attachmentItems = filteredAttachments.map(MediaAttachmentListItem::MediaAttachmentItem)
                val items = if (allowAddMore) {
                    listOf(MediaAttachmentListItem.AddMoreItem) + attachmentItems
                } else {
                    attachmentItems
                }
                mediaAttachmentsAdapter.setItems(items)
            }
            binding.progressBar.isVisible = false
        }
    }

    companion object {
        /**
         * Creates a new instance of [MediaAttachmentFragment].
         *
         * @param style The style for the attachment picker dialog.
         * @return A new instance of the Fragment.
         */
        fun newInstance(style: AttachmentsPickerDialogStyle): MediaAttachmentFragment = MediaAttachmentFragment().apply {
            setStyle(style)
        }

        private const val SPAN_COUNT: Int = 3
        private const val SPACING: Int = 2
    }
}
