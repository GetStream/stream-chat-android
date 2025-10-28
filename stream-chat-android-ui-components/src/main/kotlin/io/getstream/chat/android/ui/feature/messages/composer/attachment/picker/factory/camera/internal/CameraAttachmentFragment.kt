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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.camera.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentCameraBinding
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.AttachmentsPickerDialogStyle
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.PickerMediaMode
import io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.font.setTextStyle
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import java.io.File

/**
 * Represents the tab of the attachment picker with media capture.
 */
internal class CameraAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentCameraBinding? = null
    private val binding get() = _binding!!

    private val permissionChecker: PermissionChecker = PermissionChecker()
    private var activityResultLauncher: ActivityResultLauncher<Unit>? = null

    /**
     * Style for the attachment picker dialog.
     */
    private lateinit var style: AttachmentsPickerDialogStyle

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
            StreamUiFragmentAttachmentCameraBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (::style.isInitialized) {
            setupViews()
            setupResultListener()
            checkPermissions()
        }
    }

    /**
     * Initializes the dialog with the style.
     *
     * @param style Style for the dialog.
     */
    fun setStyle(style: AttachmentsPickerDialogStyle) {
        this.style = style
    }

    /**
     * Sets the listener invoked when attachments are selected in the attachment tab.
     */
    fun setAttachmentsPickerTabListener(attachmentsPickerTabListener: AttachmentsPickerTabListener) {
        this.attachmentsPickerTabListener = attachmentsPickerTabListener
    }

    private fun setupViews() {
        binding.grantPermissionsInclude.apply {
            grantPermissionsImageView.setImageDrawable(style.allowAccessToCameraIconDrawable)
            grantPermissionsTextView.text = style.allowAccessToCameraButtonText
            grantPermissionsTextView.setTextStyle(style.allowAccessButtonTextStyle)
            grantPermissionsTextView.setOnClickListener {
                checkPermissions()
            }
        }
    }

    private fun setupResultListener() {
        activityResultLauncher = activity?.activityResultRegistry
            ?.register(
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
    }

    private fun checkPermissions() {
        if (permissionChecker.isNeededToRequestForCameraPermissions(requireContext())) {
            permissionChecker.checkCameraPermissions(
                binding.root,
                onPermissionDenied = ::onPermissionDenied,
                onPermissionGranted = ::onPermissionGranted,
            )
            return
        }
        onPermissionGranted()
    }

    private fun onPermissionGranted() {
        _binding?.run {
            grantPermissionsInclude.grantPermissionsContainer.isVisible = false
            activityResultLauncher?.launch(Unit)
        }
    }

    private fun onPermissionDenied() {
        _binding?.run {
            grantPermissionsInclude.grantPermissionsContainer.isVisible = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activityResultLauncher?.unregister()
        _binding = null
    }

    internal object LauncherRequestsKeys {
        const val CAPTURE_MEDIA = "capture_media_request_key"
    }

    companion object {
        /**
         * Creates a new instance of [CameraAttachmentFragment].
         *
         * @param style The style for the attachment picker dialog.
         * @return A new instance of the Fragment.
         */
        fun newInstance(style: AttachmentsPickerDialogStyle): CameraAttachmentFragment = CameraAttachmentFragment().apply {
            setStyle(style)
        }

        /**
         * Map [PickerMediaMode] into [CaptureMediaContract.Mode]
         */
        internal val PickerMediaMode.mode: CaptureMediaContract.Mode
            get() = when (this) {
                PickerMediaMode.PHOTO -> CaptureMediaContract.Mode.PHOTO
                PickerMediaMode.VIDEO -> CaptureMediaContract.Mode.VIDEO
                PickerMediaMode.PHOTO_AND_VIDEO -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
            }
    }
}
