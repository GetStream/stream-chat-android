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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.file.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.contract.internal.SelectFilesContract
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.permissions.FilesAccess
import io.getstream.chat.android.ui.common.permissions.resolveFilesAccessState
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentFileBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Represents the tab of the attachment picker with a list of files.
 */
internal class FileAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentFileBinding? = null
    private val binding get() = _binding!!

    private val storageHelper: StorageHelper = StorageHelper()
    private val permissionChecker: PermissionChecker = PermissionChecker()
    private val attachmentFilter: AttachmentFilter = AttachmentFilter()
    private var activityResultLauncher: ActivityResultLauncher<Unit>? = null

    /**
     * Style for the attachment picker dialog.
     */
    private lateinit var style: AttachmentsPickerDialogStyle

    /**
     * A listener invoked when attachments are selected in the attachment tab.
     */
    private var attachmentsPickerTabListener: AttachmentsPickerTabListener? = null

    /**
     * Initializes the dialog with the style.
     *
     * @param style Style for the dialog.
     */
    fun setStyle(style: AttachmentsPickerDialogStyle) {
        this.style = style
    }

    private val fileAttachmentsAdapter: FileAttachmentAdapter by lazy {
        FileAttachmentAdapter(style) {
            updateFileAttachment(it)
        }
    }

    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentAttachmentFileBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (::style.isInitialized) {
            setupViews()
            setupResultListener()
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
        activityResultLauncher?.unregister()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        attachmentsPickerTabListener = null
    }

    private fun updateFileAttachment(attachmentMetaData: AttachmentMetaData) {
        if (attachmentMetaData.isSelected) {
            attachmentMetaData.isSelected = false
            selectedAttachments = selectedAttachments - attachmentMetaData
            fileAttachmentsAdapter.deselectAttachment(attachmentMetaData)
        } else {
            attachmentMetaData.isSelected = true
            selectedAttachments = selectedAttachments + attachmentMetaData
            fileAttachmentsAdapter.selectAttachment(attachmentMetaData)
        }
        attachmentsPickerTabListener?.onSelectedAttachmentsChanged(selectedAttachments.toList())
    }

    private fun setupViews() {
        binding.apply {
            grantPermissionsInclude.grantPermissionsImageView.setImageDrawable(style.allowAccessToFilesIconDrawable)
            grantPermissionsInclude.grantPermissionsTextView.text = style.allowAccessToFilesButtonText
            grantPermissionsInclude.grantPermissionsTextView.setTextStyle(style.allowAccessButtonTextStyle)
            grantPermissionsInclude.grantPermissionsTextView.setOnClickListener {
                permissionChecker.checkFilesPermissions(binding.root) {
                    checkPermissions()
                }
            }
            recentFilesRecyclerView.adapter = fileAttachmentsAdapter
            fileManagerImageView.setImageDrawable(style.fileManagerIconDrawable)
            recentFilesTextView.text = style.recentFilesText
            recentFilesTextView.setTextStyle(style.recentFilesTextStyle)
            fileManagerImageView.setOnClickListener {
                activityResultLauncher?.launch(Unit)
            }
            // Audio access label
            audioAccessTextView.text = style.allowAccessToAudioText
            audioAccessTextView.setTextStyle(style.allowAccessToAudioTextStyle)
            audioAccessImageView.setImageDrawable(style.allowAccessToAudioIconDrawable)
            audioAccessImageView.setOnClickListener {
                permissionChecker.checkAudioPermissions(binding.root) {
                    checkPermissions()
                }
            }
            // Visual media access label
            visualMediaAccessTextView.setTextStyle(style.allowAccessToVisualMediaTextStyle)
            visualMediaAccessImageView.setImageDrawable(style.allowAccessToVisualMediaIconDrawable)
            visualMediaAccessImageView.setOnClickListener {
                permissionChecker.checkVisualMediaPermissions(binding.root) {
                    checkPermissions()
                }
            }
        }
    }

    private fun checkPermissions() {
        val filesAccess = resolveFilesAccessState(requireContext())
        handleFilesAccessState(filesAccess)
    }

    private fun handleFilesAccessState(access: FilesAccess) {
        when (access) {
            FilesAccess.DENIED -> {
                showGrantPermissionsContainer(show = true)
                showRequestAudioAccessLabel(show = false)
                showRequestVisualMediaAccessLabel(show = false)
            }

            else -> {
                val hasAudio = access == FilesAccess.AUDIO_AND_FULL_VISUAL ||
                    access == FilesAccess.AUDIO_AND_PARTIAL_VISUAL ||
                    access == FilesAccess.AUDIO
                val hasFullVisual = access == FilesAccess.AUDIO_AND_FULL_VISUAL ||
                    access == FilesAccess.FULL_VISUAL
                val hasPartialVisual = access == FilesAccess.AUDIO_AND_PARTIAL_VISUAL ||
                    access == FilesAccess.PARTIAL_VISUAL

                showGrantPermissionsContainer(show = false)
                showRequestAudioAccessLabel(show = !hasAudio)
                showRequestVisualMediaAccessLabel(show = !hasFullVisual, hasPartialAccess = hasPartialVisual)
                populateAttachments()
            }
        }
    }

    private fun showRequestAudioAccessLabel(show: Boolean) {
        _binding?.run {
            audioAccessTextView.isVisible = show
            audioAccessImageView.isVisible = show
        }
    }

    private fun showRequestVisualMediaAccessLabel(show: Boolean, hasPartialAccess: Boolean = false) {
        _binding?.run {
            val text = if (hasPartialAccess) {
                style.allowAccessToMoreVisualMediaText
            } else {
                style.allowAccessToVisualMediaText
            }
            visualMediaAccessTextView.text = text
            visualMediaAccessTextView.isVisible = show
            visualMediaAccessImageView.isVisible = show
        }
    }

    private fun showGrantPermissionsContainer(show: Boolean) {
        _binding?.run {
            grantPermissionsInclude.grantPermissionsContainer.isVisible = show
        }
    }

    private fun setupResultListener() {
        activityResultLauncher = activity?.activityResultRegistry
            ?.register(LauncherRequestsKeys.SELECT_FILES, SelectFilesContract()) {
                lifecycleScope.launch(DispatcherProvider.Main) {
                    val attachments = withContext(DispatcherProvider.IO) {
                        storageHelper.getAttachmentsFromUriList(requireContext(), it)
                    }
                    val filteredAttachments = attachmentFilter.filterAttachments(attachments)

                    if (filteredAttachments.size < attachments.size) {
                        Toast.makeText(
                            context,
                            getString(R.string.stream_ui_message_composer_file_not_supported),
                            Toast.LENGTH_SHORT,
                        ).show()
                    }

                    attachmentsPickerTabListener?.onSelectedAttachmentsChanged(filteredAttachments)
                    attachmentsPickerTabListener?.onSelectedAttachmentsSubmitted()
                }
            }
    }

    /**
     * Sets the listener invoked when attachments are selected in the attachment tab.
     */
    fun setAttachmentsPickerTabListener(attachmentsPickerTabListener: AttachmentsPickerTabListener) {
        this.attachmentsPickerTabListener = attachmentsPickerTabListener
    }

    private fun populateAttachments() {
        lifecycleScope.launch(DispatcherProvider.Main) {
            binding.progressBar.isVisible = true

            val attachments = withContext(DispatcherProvider.IO) {
                storageHelper.getFileAttachments(requireContext())
            }
            val filteredAttachments = attachmentFilter.filterAttachments(attachments)

            if (filteredAttachments.isEmpty()) {
                binding.emptyPlaceholderTextView.setTextStyle(style.fileAttachmentsNoFilesTextStyle)
                binding.emptyPlaceholderTextView.text = style.fileAttachmentsNoFilesText
                binding.emptyPlaceholderTextView.isVisible = true
            } else {
                fileAttachmentsAdapter.setAttachments(filteredAttachments)
            }
            binding.progressBar.isVisible = false
        }
    }

    private object LauncherRequestsKeys {
        const val SELECT_FILES = "select_files_request_key"
    }

    companion object {
        /**
         * Creates a new instance of [FileAttachmentFragment].
         *
         * @param style The style for the attachment picker dialog.
         * @return A new instance of the Fragment.
         */
        fun newInstance(style: AttachmentsPickerDialogStyle): FileAttachmentFragment {
            return FileAttachmentFragment().apply {
                setStyle(style)
            }
        }
    }
}
