package io.getstream.chat.android.ui.message.input.attachment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ToggleButton
import androidx.core.view.forEach
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiDialogAttachmentBinding
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.internal.AttachmentDialogPagerAdapter

@ExperimentalStreamChatApi
public class AttachmentSelectionDialogFragment : BottomSheetDialogFragment(), AttachmentSelectionListener {

    private var _binding: StreamUiDialogAttachmentBinding? = null
    private val binding get() = _binding!!

    private var attachmentSelectionListener: AttachmentSelectionListener? = null
    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()
    private var attachmentSource: AttachmentSource = AttachmentSource.MEDIA

    private val style by lazy { staticStyle!! }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) dismiss()
        setupResultListener()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiDialogAttachmentBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION_ERROR")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val attachmentSelectionDialogStyle = style.attachmentSelectionDialogStyle

            container.setBackgroundColor(attachmentSelectionDialogStyle.backgroundColor)

            attachButton.setImageDrawable(attachmentSelectionDialogStyle.attachButtonIcon)
            attachButton.isEnabled = false
            attachButton.setOnClickListener {
                attachmentSelectionListener?.onAttachmentsSelected(selectedAttachments, attachmentSource)
                dismiss()
            }

            mediaAttachmentButton.run {
                background = attachmentSelectionDialogStyle.pictureAttachmentIcon
                if (attachmentSelectionDialogStyle.pictureAttachmentIconTint != null) {
                    backgroundTintList = attachmentSelectionDialogStyle.pictureAttachmentIconTint
                }

                isChecked = true
                setOnClickListener {
                    setSelectedButton(this, AttachmentDialogPagerAdapter.PAGE_MEDIA_ATTACHMENT)
                }
            }

            fileAttachmentButton.run {
                background = attachmentSelectionDialogStyle.fileAttachmentIcon
                if (attachmentSelectionDialogStyle.fileAttachmentIconTint != null) {
                    backgroundTintList = attachmentSelectionDialogStyle.fileAttachmentIconTint
                }

                setOnClickListener {
                    setSelectedButton(fileAttachmentButton, AttachmentDialogPagerAdapter.PAGE_FILE_ATTACHMENT)
                }
            }

            cameraAttachmentButton.run {
                background = attachmentSelectionDialogStyle.cameraAttachmentIcon
                if (attachmentSelectionDialogStyle.cameraAttachmentIconTint != null) {
                    backgroundTintList = attachmentSelectionDialogStyle.cameraAttachmentIconTint
                }

                setOnClickListener {
                    setSelectedButton(cameraAttachmentButton, AttachmentDialogPagerAdapter.PAGE_CAMERA_ATTACHMENT)
                }
            }

            attachmentPager.adapter = AttachmentDialogPagerAdapter(this@AttachmentSelectionDialogFragment, style)
            attachmentPager.isUserInputEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        staticStyle = null
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        attachmentSelectionListener = null
    }

    override fun getTheme(): Int = R.style.StreamUiAttachmentBottomSheetDialog

    /**
     * Sets the listener that will be notified when picking attachments has been completed.
     */
    public fun setAttachmentSelectionListener(attachmentSelectionListener: AttachmentSelectionListener) {
        this.attachmentSelectionListener = attachmentSelectionListener
    }

    override fun onAttachmentsSelected(attachments: Set<AttachmentMetaData>, attachmentSource: AttachmentSource) {
        this.selectedAttachments = attachments
        this.attachmentSource = attachmentSource
        selectedAttachments.isNotEmpty().let {
            setAttachButtonEnabled(it)
            setUnselectedButtonsEnabled(!it)
        }
    }

    private fun setSelectedButton(selectedButton: ToggleButton, pagePosition: Int) {
        binding.attachmentPager.setCurrentItem(pagePosition, false)
        binding.attachmentButtonsContainer.forEach {
            (it as ToggleButton).isChecked = it == selectedButton
        }
    }

    private fun setAttachButtonEnabled(isEnabled: Boolean) {
        binding.attachButton.isEnabled = isEnabled
    }

    private fun setUnselectedButtonsEnabled(isEnabled: Boolean) {
        binding.attachmentButtonsContainer.forEach {
            it as ToggleButton
            if (!it.isChecked) {
                it.isEnabled = isEnabled
            }
        }
    }

    private fun setupResultListener() {
        childFragmentManager.setFragmentResultListener(REQUEST_KEY_CAMERA, this) { _, bundle ->
            val result = bundle.getSerializable(BUNDLE_KEY) as Set<AttachmentMetaData>
            attachmentSelectionListener?.onAttachmentsSelected(result, AttachmentSource.CAMERA)
            dismiss()
        }
        childFragmentManager.setFragmentResultListener(REQUEST_KEY_FILE_MANAGER, this) { _, bundle ->
            val result = bundle.getSerializable(BUNDLE_KEY) as Set<AttachmentMetaData>
            attachmentSelectionListener?.onAttachmentsSelected(result, AttachmentSource.FILE)
            dismiss()
        }
    }

    public companion object {
        public const val TAG: String = "attachment_dialog_fragment"

        internal const val REQUEST_KEY_CAMERA = "key_camera"
        internal const val REQUEST_KEY_FILE_MANAGER = "key_file_manager"
        internal const val BUNDLE_KEY = "bundle_attachments"

        internal var staticStyle: MessageInputViewStyle? = null

        /**
         * Create a new instance of the Attachment picker dialog.
         *
         * See [AttachmentSelectionDialogStyle.createDefault] to load a default set of icons to be used for the
         * attachment dialog's tabs.
         */
        public fun newInstance(style: MessageInputViewStyle): AttachmentSelectionDialogFragment {
            staticStyle = style
            return AttachmentSelectionDialogFragment()
        }
    }
}
