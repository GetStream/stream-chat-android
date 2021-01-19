package io.getstream.chat.android.ui.attachments.file

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import com.getstream.sdk.chat.SelectFilesContract
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.StorageHelper
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.attachments.AttachmentDialogFragment
import io.getstream.chat.android.ui.attachments.AttachmentSelectionListener
import io.getstream.chat.android.ui.attachments.AttachmentSource
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentFileBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class FileAttachmentFragment : Fragment() {

    private var _binding: StreamUiFragmentAttachmentFileBinding? = null
    private val binding get() = _binding!!

    private val storageHelper: StorageHelper = StorageHelper()
    private val permissionChecker: PermissionChecker = PermissionChecker()
    private var activityResultLauncher: ActivityResultLauncher<Unit>? = null

    private val fileAttachmentsAdapter: FileAttachmentAdapter = FileAttachmentAdapter {
        updateFileAttachment(it)
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
        savedInstanceState: Bundle?
    ): View {
        _binding = StreamUiFragmentAttachmentFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupResultListener()
        checkPermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        activityResultLauncher?.unregister()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        attachmentSelectionListener = null
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
        attachmentSelectionListener?.onAttachmentsSelected(selectedAttachments, AttachmentSource.FILE)
    }

    private fun setupViews() {
        binding.apply {
            grantPermissionsInclude.grantPermissionsImageView.setImageResource(R.drawable.stream_ui_attachment_permission_file)
            grantPermissionsInclude.grantPermissionsTextView.setText(R.string.stream_ui_attachment_dialog_permission_files)
            grantPermissionsInclude.grantPermissionsTextView.setOnClickListener {
                checkPermissions()
            }
            recentFilesRecyclerView.adapter = fileAttachmentsAdapter
            fileManagerImageView.setOnClickListener {
                activityResultLauncher?.launch(Unit)
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

    private fun setupResultListener() {
        activityResultLauncher = activity?.activityResultRegistry
            ?.register(LauncherRequestsKeys.SELECT_FILES, SelectFilesContract()) {
                lifecycleScope.launch(DispatcherProvider.Main) {
                    val attachments = withContext(DispatcherProvider.IO) {
                        storageHelper.getAttachmentsFromUriList(requireContext(), it)
                    }
                    setFragmentResult(
                        AttachmentDialogFragment.REQUEST_KEY_FILE_MANAGER,
                        bundleOf(AttachmentDialogFragment.BUNDLE_KEY to attachments.toSet())
                    )
                }
            }
    }

    private fun onPermissionGranted() {
        binding.grantPermissionsInclude.grantPermissionsContainer.isVisible = false
        populateAttachments()
    }

    private fun onPermissionDenied() {
        binding.grantPermissionsInclude.grantPermissionsContainer.isVisible = true
    }

    private fun populateAttachments() {
        lifecycleScope.launch(DispatcherProvider.Main) {
            binding.progressBar.isVisible = true

            val attachments = withContext(DispatcherProvider.IO) {
                storageHelper.getFileAttachments(requireContext())
            }

            if (attachments.isEmpty()) {
                binding.emptyPlaceholderTextView.isVisible = true
            } else {
                fileAttachmentsAdapter.setAttachments(attachments)
            }
            binding.progressBar.isVisible = false
        }
    }

    private object LauncherRequestsKeys {
        const val SELECT_FILES = "select_files_request_key"
    }
}
