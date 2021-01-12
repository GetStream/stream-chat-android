package io.getstream.chat.android.ui.attachments.camera

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
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.attachments.AttachmentDialogFragment
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentCameraBinding
import java.io.File

internal class CameraAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentCameraBinding? = null
    private val binding get() = _binding!!

    private val permissionChecker: PermissionChecker = PermissionChecker()
    private var activityResultLauncher: ActivityResultLauncher<Unit>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiFragmentAttachmentCameraBinding.inflate(inflater, container, false)
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
            grantPermissionsImageView.setImageResource(R.drawable.stream_ui_attachment_permission_camera)
            grantPermissionsTextView.setText(R.string.stream_ui_attachment_dialog_permission_camera)
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
                    AttachmentDialogFragment.REQUEST_KEY_CAMERA,
                    bundleOf(AttachmentDialogFragment.BUNDLE_KEY to result)
                )
            }
    }

    private fun checkPermissions() {
        if (!permissionChecker.isGrantedCameraPermissions(requireContext())) {
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
}
