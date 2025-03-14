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

    private lateinit var config: AttachmentsPickerSystemConfig
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
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        visualMediaPickerLauncher?.unregister()
        captureMedia?.unregister()
        _binding = null
    }

    private fun setupViews() {
        // Adjust visibility of the tabs based on the enabled flags
        if (!config.fileAttachmentsTabEnabled) {
            binding.buttonFiles.visibility = View.GONE
            binding.textFiles.visibility = View.GONE
        }

        if (!config.visualMediaAttachmentsTabEnabled) {
            binding.buttonMedia.visibility = View.GONE
            binding.textMedia.visibility = View.GONE
        }

        if (!config.cameraAttachmentsTabEnabled) {
            binding.buttonCapture.visibility = View.GONE
            binding.textCapture.visibility = View.GONE
        }
        if (!config.pollAttachmentsTabEnabled) {
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
        visualMediaPickerLauncher = registerVisualMediaPickerLauncher(config.visualMediaAllowMultiple)
        binding.buttonMedia.setOnClickListener {
            visualMediaPickerLauncher?.launch(
                input = PickVisualMediaRequest(config.visualMediaType.toContractVisualMediaType()),
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

        fun newInstance(
            style: AttachmentsPickerDialogStyle,
            attachmentsPickerTabListener: AttachmentsPickerTabListener,
            config: AttachmentsPickerSystemConfig,
        ): Fragment = AttachmentsPickerSystemFragment().apply {
            this.style = style
            this.attachmentsPickerTabListener = attachmentsPickerTabListener
            this.config = config
        }
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
