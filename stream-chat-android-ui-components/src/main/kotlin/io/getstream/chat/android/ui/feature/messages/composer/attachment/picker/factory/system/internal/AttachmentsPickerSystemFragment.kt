/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.system.internal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import io.getstream.chat.android.models.PollConfig
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.helper.internal.AttachmentFilter
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.permissions.VisualMediaType
import io.getstream.chat.android.ui.common.permissions.toContractVisualMediaType
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentSystemPickerBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.camera.internal.CameraAttachmentFragment.Companion.mode
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.camera.internal.CameraAttachmentFragment.LauncherRequestsKeys
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll.CreatePollDialogFragment
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import java.io.File

internal class AttachmentsPickerSystemFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentSystemPickerBinding? = null
    private val binding get() = _binding!!

    private lateinit var style: AttachmentsPickerDialogStyle

    private val permissionChecker: PermissionChecker = PermissionChecker()

    /**
     * A listener invoked when attachments are selected in the attachment tab.
     */
    private var attachmentsPickerTabListener: AttachmentsPickerTabListener? = null
    private val storageHelper = StorageHelper()
    private val attachmentFilter = AttachmentFilter()
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            if (uri != null) {
                val attachmentMetaData = storageHelper.getAttachmentsFromUriList(requireContext(), listOf(uri))
                attachmentsPickerTabListener?.onSelectedAttachmentsChanged(attachmentMetaData)
            }
            attachmentsPickerTabListener?.onSelectedAttachmentsSubmitted()
        }

    private var visualMediaPickerLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null

    private var captureMedia: ActivityResultLauncher<Unit>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding =
            StreamUiFragmentAttachmentSystemPickerBinding.inflate(
                requireContext().streamThemeInflater,
                container,
                false,
            )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null && ::style.isInitialized) {
            setupViews()
        } else {
            parentFragmentManager.beginTransaction()
                .remove(this)
                .commit()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        visualMediaPickerLauncher?.unregister()
        captureMedia?.unregister()
        _binding = null
    }

    @Suppress("LongMethod")
    private fun setupViews() {
        // Adjust visibility of the tabs based on the enabled flags
        if (!requireArguments().getBoolean(ARG_FILE_ATTACHMENTS_TAB_ENABLED)) {
            binding.buttonFiles.visibility = View.GONE
            binding.textFiles.visibility = View.GONE
        }

        if (!requireArguments().getBoolean(ARG_VISUAL_MEDIA_ATTACHMENTS_TAB_ENABLED)) {
            binding.buttonMedia.visibility = View.GONE
            binding.textMedia.visibility = View.GONE
        }

        if (!requireArguments().getBoolean(ARG_CAMERA_ATTACHMENTS_TAB_ENABLED)) {
            binding.buttonCapture.visibility = View.GONE
            binding.textCapture.visibility = View.GONE
        }
        if (!requireArguments().getBoolean(ARG_POLL_ATTACHMENTS_TAB_ENABLED)) {
            binding.buttonPolls.visibility = View.GONE
            binding.textPoll.visibility = View.GONE
        }

        // Setup listeners and actions
        binding.buttonFiles.setOnClickListener {
            val filePickerIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*" // General type to include multiple types
                putExtra(Intent.EXTRA_MIME_TYPES, attachmentFilter.getSupportedMimeTypes().toTypedArray())
                addCategory(Intent.CATEGORY_OPENABLE)
            }

            filePickerLauncher.launch(filePickerIntent)
        }
        visualMediaPickerLauncher = registerVisualMediaPickerLauncher(
            requireArguments().getBoolean(ARG_VISUAL_MEDIA_ALLOW_MULTIPLE),
        )
        binding.buttonMedia.setOnClickListener {
            visualMediaPickerLauncher?.launch(
                input = PickVisualMediaRequest(
                    (requireArguments().getSerializable(ARG_VISUAL_MEDIA_TYPE) as VisualMediaType)
                        .toContractVisualMediaType(),
                ),
            )
        }

        captureMedia = activity?.activityResultRegistry?.register(
            LauncherRequestsKeys.CAPTURE_MEDIA,
            CaptureMediaContract(style.pickerMediaMode.mode),
        ) { file: File? ->
            val result: List<AttachmentMetaData> = if (file == null) {
                emptyList()
            } else {
                listOf(AttachmentMetaData(requireContext(), file))
            }

            attachmentsPickerTabListener?.onSelectedAttachmentsChanged(result)
            attachmentsPickerTabListener?.onSelectedAttachmentsSubmitted()
        }
        captureMedia?.let {
            binding.buttonCapture.setOnClickListener {
                checkCameraPermissions {
                    if (_binding != null) captureMedia?.launch(Unit)
                }
            }
        }

        binding.buttonPolls.setOnClickListener {
            context.getFragmentManager()?.let {
                CreatePollDialogFragment.newInstance(object : CreatePollDialogFragment.CreatePollDialogListener {
                    override fun onCreatePoll(pollConfig: PollConfig) {
                        attachmentsPickerTabListener?.onPollSubmitted(pollConfig)
                    }

                    override fun onDismiss() {
                        attachmentsPickerTabListener?.onPollSubmitted(null)
                    }
                })
                    .show(it, CreatePollDialogFragment.TAG)
            }
        }
    }

    /**
     * Sets the listener invoked when attachments are selected in the attachment tab.
     *
     * @param attachmentsPickerTabListener The listener invoked when attachments are selected in the tab.
     */
    fun setAttachmentsPickerTabListener(attachmentsPickerTabListener: AttachmentsPickerTabListener) {
        this.attachmentsPickerTabListener = attachmentsPickerTabListener
    }

    /**
     * Set the style.
     *
     * @param style Style for the dialog.
     */
    fun setStyle(style: AttachmentsPickerDialogStyle) {
        this.style = style
    }

    private fun registerVisualMediaPickerLauncher(allowMultiple: Boolean) = if (allowMultiple) {
        registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
            val attachmentMetaData = storageHelper.getAttachmentsFromUriList(requireContext(), uris)
            attachmentsPickerTabListener?.onSelectedAttachmentsChanged(attachmentMetaData)
            attachmentsPickerTabListener?.onSelectedAttachmentsSubmitted()
        }
    } else {
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                val attachmentMetaData = storageHelper.getAttachmentsFromUriList(requireContext(), listOf(uri))
                attachmentsPickerTabListener?.onSelectedAttachmentsChanged(attachmentMetaData)
            }
            attachmentsPickerTabListener?.onSelectedAttachmentsSubmitted()
        }
    }

    private fun checkCameraPermissions(onPermissionGranted: () -> Unit) {
        if (permissionChecker.isNeededToRequestForCameraPermissions(requireContext())) {
            permissionChecker.checkCameraPermissions(
                view = binding.root,
                onPermissionDenied = { /* Do nothing */ },
                onPermissionGranted = onPermissionGranted,
            )
        } else {
            onPermissionGranted()
        }
    }

    companion object {
        private const val ARG_VISUAL_MEDIA_ATTACHMENTS_TAB_ENABLED = "visual_media_attachments_tab_enabled"
        private const val ARG_VISUAL_MEDIA_ALLOW_MULTIPLE = "visual_media_allow_multiple"
        private const val ARG_VISUAL_MEDIA_TYPE = "visual_media_type"
        private const val ARG_FILE_ATTACHMENTS_TAB_ENABLED = "file_attachments_tab_enabled"
        private const val ARG_CAMERA_ATTACHMENTS_TAB_ENABLED = "camera_attachments_tab_enabled"
        private const val ARG_POLL_ATTACHMENTS_TAB_ENABLED = "poll_attachments_tab_enabled"

        fun newInstance(
            style: AttachmentsPickerDialogStyle,
            attachmentsPickerTabListener: AttachmentsPickerTabListener,
            config: AttachmentsPickerSystemConfig,
        ): Fragment = AttachmentsPickerSystemFragment().apply {
            this.style = style
            this.attachmentsPickerTabListener = attachmentsPickerTabListener
            arguments = config.toBundle()
        }

        private fun AttachmentsPickerSystemConfig.toBundle() = bundleOf(
            ARG_VISUAL_MEDIA_ATTACHMENTS_TAB_ENABLED to visualMediaAttachmentsTabEnabled,
            ARG_VISUAL_MEDIA_ALLOW_MULTIPLE to visualMediaAllowMultiple,
            ARG_VISUAL_MEDIA_TYPE to visualMediaType,
            ARG_FILE_ATTACHMENTS_TAB_ENABLED to fileAttachmentsTabEnabled,
            ARG_CAMERA_ATTACHMENTS_TAB_ENABLED to cameraAttachmentsTabEnabled,
            ARG_POLL_ATTACHMENTS_TAB_ENABLED to pollAttachmentsTabEnabled,
        )
    }
}

internal data class AttachmentsPickerSystemConfig(
    val visualMediaAttachmentsTabEnabled: Boolean,
    val visualMediaAllowMultiple: Boolean,
    val visualMediaType: VisualMediaType,
    val fileAttachmentsTabEnabled: Boolean,
    val cameraAttachmentsTabEnabled: Boolean,
    val pollAttachmentsTabEnabled: Boolean,
)
