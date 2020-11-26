package io.getstream.chat.android.ui.attachments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.core.view.forEach
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamDialogAttachmentBinding

internal class AttachmentDialogFragment : BottomSheetDialogFragment(), AttachmentSelectionListener {

    private var _binding: StreamDialogAttachmentBinding? = null
    private val binding get() = _binding!!

    private var attachmentSelectionListener: AttachmentSelectionListener? = null
    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) dismiss()
        setupResultListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = StreamDialogAttachmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            attachButton.isEnabled = false
            attachButton.setOnClickListener {
                attachmentSelectionListener?.onAttachmentsSelected(selectedAttachments)
                dismiss()
            }

            mediaAttachmentButton.isChecked = true
            mediaAttachmentButton.setOnClickListener {
                setSelectedButton(mediaAttachmentButton, AttachmentDialogPagerAdapter.PAGE_MEDIA_ATTACHMENT)
            }
            fileAttachmentButton.setOnClickListener {
                setSelectedButton(fileAttachmentButton, AttachmentDialogPagerAdapter.PAGE_FILE_ATTACHMENT)
            }
            cameraAttachmentButton.setOnClickListener {
                setSelectedButton(cameraAttachmentButton, AttachmentDialogPagerAdapter.PAGE_CAMERA_ATTACHMENT)
            }
            attachmentPager.adapter = AttachmentDialogPagerAdapter(this@AttachmentDialogFragment)
            attachmentPager.isUserInputEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        attachmentSelectionListener = null
    }

    override fun getTheme(): Int = R.style.StreamAttachmentBottomSheetDialog

    fun setAttachmentsSelectedListener(attachmentSelectionListener: AttachmentSelectionListener) {
        this.attachmentSelectionListener = attachmentSelectionListener
    }

    override fun onAttachmentsSelected(attachments: Set<AttachmentMetaData>) {
        this.selectedAttachments = attachments
        binding.attachButton.isEnabled = selectedAttachments.isNotEmpty()
    }

    private fun setSelectedButton(selectedButton: ToggleButton, pagePosition: Int) {
        binding.attachmentPager.setCurrentItem(pagePosition, false)
        binding.attachmentButtonsContainer.forEach {
            (it as ToggleButton).isChecked = it == selectedButton
        }
    }

    private fun setupResultListener() {
        childFragmentManager.setFragmentResultListener(REQUEST_KEY, this) { _, bundle ->
            val result = bundle.getSerializable(BUNDLE_KEY) as Set<AttachmentMetaData>
            attachmentSelectionListener?.onAttachmentsSelected(result)
            dismiss()
        }
    }

    companion object {
        const val TAG = "attachments"

        const val REQUEST_KEY = "request_attachments"
        const val BUNDLE_KEY = "bundle_attachments"

        fun newInstance() = AttachmentDialogFragment()
    }
}
