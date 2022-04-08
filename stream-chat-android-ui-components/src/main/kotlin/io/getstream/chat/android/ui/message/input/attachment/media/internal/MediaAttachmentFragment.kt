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

package io.getstream.chat.android.ui.message.input.attachment.media.internal

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.AttachmentFilter
import com.getstream.sdk.chat.utils.GridSpacingItemDecoration
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentMediaBinding
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionListener
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MediaAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentMediaBinding? = null
    private val binding get() = _binding!!

    private val storageHelper: StorageHelper = StorageHelper()
    private val permissionChecker: PermissionChecker = PermissionChecker()
    private val attachmentFilter: AttachmentFilter = AttachmentFilter()

    private val gridLayoutManager = GridLayoutManager(context, SPAN_COUNT, RecyclerView.VERTICAL, false)
    private val gridSpacingItemDecoration = GridSpacingItemDecoration(SPAN_COUNT, SPACING, false)
    private val style by lazy { staticStyle }
    private val dialogStyle: AttachmentSelectionDialogStyle by lazy {
        style?.attachmentSelectionDialogStyle ?: AttachmentSelectionDialogStyle.createDefault(requireContext())
    }

    private val mediaAttachmentsAdapter: MediaAttachmentAdapter by lazy {
        MediaAttachmentAdapter(style = dialogStyle, ::updateMediaAttachment)
    }

    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()
    private var attachmentSelectionListener: AttachmentSelectionListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        attachmentSelectionListener = parentFragment as? AttachmentSelectionListener
    }

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
        setupViews()
        checkPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        attachmentSelectionListener = null
    }

    private fun setupViews() {
        binding.mediaFileRecyclerView.apply {
            layoutManager = gridLayoutManager
            addItemDecoration(gridSpacingItemDecoration)
            adapter = mediaAttachmentsAdapter
        }
        binding.grantPermissionsInclude.apply {
            grantPermissionsImageView.setImageDrawable(dialogStyle.allowAccessToGalleryIcon)
            grantPermissionsTextView.text = dialogStyle.allowAccessToGalleryText
            dialogStyle.grantPermissionsTextStyle.apply(grantPermissionsTextView)
            grantPermissionsTextView.setOnClickListener {
                checkPermissions()
            }
        }
    }

    private fun checkPermissions() {
        if (!permissionChecker.isGrantedStoragePermissions(requireContext())) {
            permissionChecker.checkStoragePermissions(
                binding.root,
                onPermissionDenied = ::onPermissionDenied,
                onPermissionGranted = ::onPermissionGranted
            )
            return
        }
        onPermissionGranted()
    }

    private fun onPermissionGranted() {
        binding.grantPermissionsInclude.grantPermissionsContainer.isVisible = false
        populateAttachments()
    }

    private fun onPermissionDenied() {
        binding.grantPermissionsInclude.grantPermissionsContainer.isVisible = true
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
        attachmentSelectionListener?.onAttachmentsSelected(selectedAttachments, AttachmentSource.MEDIA)
    }

    private fun populateAttachments() {
        lifecycleScope.launch(DispatcherProvider.Main) {
            binding.progressBar.isVisible = true

            val attachments = withContext(DispatcherProvider.IO) {
                storageHelper.getMediaAttachments(requireContext())
            }
            val filteredAttachments = attachmentFilter.filterAttachments(attachments)

            if (filteredAttachments.isEmpty()) {
                style?.mediaAttachmentEmptyStateTextStyle?.apply(binding.emptyPlaceholderTextView)
                binding.emptyPlaceholderTextView.text = style?.mediaAttachmentEmptyStateText
                    ?: requireContext().getString(R.string.stream_ui_message_input_no_files)
                binding.emptyPlaceholderTextView.isVisible = true
            } else {
                mediaAttachmentsAdapter.setAttachments(filteredAttachments)
            }
            binding.progressBar.isVisible = false
        }
    }

    companion object {
        internal var staticStyle: MessageInputViewStyle? = null

        fun newInstance(style: MessageInputViewStyle): Fragment {
            staticStyle = style
            return MediaAttachmentFragment()
        }

        private const val SPAN_COUNT: Int = 3
        private const val SPACING: Int = 2
    }
}
