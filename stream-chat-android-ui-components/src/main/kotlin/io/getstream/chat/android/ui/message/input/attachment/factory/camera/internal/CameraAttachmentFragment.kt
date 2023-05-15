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

package io.getstream.chat.android.ui.message.input.attachment.factory.camera.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.CaptureMediaContract
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.PermissionChecker
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.style.setTextStyle
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentCameraBinding
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSource
import io.getstream.chat.android.ui.message.input.attachment.factory.AttachmentsPickerTabListener
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
    private lateinit var style: MessageInputViewStyle

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
            setupResultListener(style.mode)
            checkPermissions()
        }
    }

    /**
     * Initializes the dialog with the style.
     *
     * @param style Style for the dialog.
     */
    fun setStyle(style: MessageInputViewStyle) {
        this.style = style
    }

    /**
     * Sets the listener invoked when attachments are selected in the attachment tab.
     */
    fun setAttachmentsPickerTabListener(attachmentsPickerTabListener: AttachmentsPickerTabListener) {
        this.attachmentsPickerTabListener = attachmentsPickerTabListener
    }

    private fun setupViews() {
        val dialogStyle = style.attachmentSelectionDialogStyle

        binding.grantPermissionsInclude.apply {
            grantPermissionsImageView.setImageDrawable(dialogStyle.allowAccessToCameraIcon)
            grantPermissionsTextView.text = dialogStyle.allowAccessToCameraText
            grantPermissionsTextView.setTextStyle(dialogStyle.grantPermissionsTextStyle)
            grantPermissionsTextView.setOnClickListener {
                checkPermissions()
            }
        }
    }

    private fun setupResultListener(mode: CaptureMediaContract.Mode) {
        activityResultLauncher = activity?.activityResultRegistry
            ?.register(LauncherRequestsKeys.CAPTURE_MEDIA, CaptureMediaContract(mode)) { file: File? ->
                val result: List<AttachmentMetaData> = if (file == null) {
                    emptyList()
                } else {
                    listOf(AttachmentMetaData(requireContext(), file))
                }

                attachmentsPickerTabListener?.onSelectedAttachmentsChanged(result, AttachmentSource.CAMERA)
                attachmentsPickerTabListener?.onSelectedAttachmentsSubmitted()
            }
    }

    private fun checkPermissions() {
        if (permissionChecker.isNeededToRequestForCameraPermissions(requireContext())) {
            permissionChecker.checkCameraPermissions(
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
        activityResultLauncher?.launch(Unit)
    }

    private fun onPermissionDenied() {
        binding.grantPermissionsInclude.grantPermissionsContainer.isVisible = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activityResultLauncher?.unregister()
        _binding = null
    }

    private object LauncherRequestsKeys {
        const val CAPTURE_MEDIA = "capture_media_request_key"
    }

    companion object {
        /**
         * Creates a new instance of [CameraAttachmentFragment].
         *
         * @param style The style for the attachment picker dialog.
         * @return A new instance of the Fragment.
         */
        fun newInstance(style: MessageInputViewStyle): CameraAttachmentFragment {
            return CameraAttachmentFragment().apply {
                setStyle(style)
            }
        }

        private val MessageInputViewStyle.mode: CaptureMediaContract.Mode
            get() = this.attachmentSelectionDialogStyle.mode

        private val AttachmentSelectionDialogStyle.mode: CaptureMediaContract.Mode
            get() = when {
                takeImageEnabled && recordVideoEnabled.not() -> CaptureMediaContract.Mode.PHOTO
                takeImageEnabled.not() && recordVideoEnabled -> CaptureMediaContract.Mode.VIDEO
                else -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
            }
    }
}
