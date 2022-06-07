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

package io.getstream.chat.android.ui.message.input.attachment.camera.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.getstream.sdk.chat.CaptureMediaContract
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.PermissionChecker
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentCameraBinding
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogFragment
import io.getstream.chat.android.ui.message.input.attachment.AttachmentSelectionDialogStyle
import java.io.File

internal class CameraAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentCameraBinding? = null
    private val binding get() = _binding!!

    private val permissionChecker: PermissionChecker = PermissionChecker()
    private var activityResultLauncher: ActivityResultLauncher<Unit>? = null

    private val style by lazy { staticStyle!! }

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
        setupViews()
        setupResultListener()
        checkPermissions()
    }

    private fun setupViews() {
        binding.grantPermissionsInclude.apply {
            grantPermissionsImageView.setImageDrawable(style.allowAccessToCameraIcon)
            grantPermissionsTextView.text = style.allowAccessToCameraText
            style.grantPermissionsTextStyle.apply(grantPermissionsTextView)
            grantPermissionsTextView.setOnClickListener {
                checkPermissions()
            }
        }
    }

    private fun setupResultListener() {
        activityResultLauncher = activity?.activityResultRegistry
            ?.register(LauncherRequestsKeys.CAPTURE_MEDIA, CaptureMediaContract()) { file: File? ->
                val result: Set<AttachmentMetaData> = if (file == null) {
                    emptySet()
                } else {
                    setOf(AttachmentMetaData(requireContext(), file))
                }
                setFragmentResult(
                    AttachmentSelectionDialogFragment.REQUEST_KEY_CAMERA,
                    bundleOf(AttachmentSelectionDialogFragment.BUNDLE_KEY to result)
                )
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
        internal var staticStyle: AttachmentSelectionDialogStyle? = null

        fun newInstance(style: AttachmentSelectionDialogStyle): Fragment {
            staticStyle = style
            return CameraAttachmentFragment()
        }
    }
}
