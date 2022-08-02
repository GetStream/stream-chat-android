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

package io.getstream.chat.android.ui.message.input.attachment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.FrameLayout
import androidx.core.view.descendants
import com.getstream.sdk.chat.model.AttachmentMetaData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.databinding.StreamUiDialogAttachmentBinding
import io.getstream.chat.android.ui.message.input.MessageInputViewStyle
import io.getstream.chat.android.ui.message.input.attachment.factory.AttachmentsPickerTabFactories
import io.getstream.chat.android.ui.message.input.attachment.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.ui.message.input.attachment.factory.AttachmentsPickerTabListener
import io.getstream.chat.android.ui.message.input.attachment.internal.AttachmentDialogPagerAdapter

/**
 * Represent the bottom sheet dialog that allows users to pick attachments.
 */
@ExperimentalStreamChatApi
public class AttachmentSelectionDialogFragment : BottomSheetDialogFragment(), AttachmentSelectionListener {

    private var _binding: StreamUiDialogAttachmentBinding? = null
    private val binding get() = _binding!!

    /**
     * Style for the dialog.
     */
    private lateinit var style: MessageInputViewStyle

    /**
     * The list of factories for the tabs that will be displayed in the attachment picker.
     */
    private lateinit var attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory>

    /**
     * A listener that is invoked when attachment picking has been completed
     */
    private var attachmentSelectionListener: AttachmentSelectionListener? = null

    /**
     * The list of currently selected attachments.
     */
    private var selectedAttachments: Set<AttachmentMetaData> = emptySet()

    /**
     * The source of the selected attachments.
     */
    private var attachmentSource: AttachmentSource = AttachmentSource.MEDIA

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = StreamUiDialogAttachmentBinding.inflate(requireContext().streamThemeInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState == null && ::style.isInitialized && ::attachmentsPickerTabFactories.isInitialized) {
            setupDialog()
        } else {
            dismiss()
        }
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        binding.container.setBackgroundColor(style.attachmentSelectionDialogStyle.backgroundColor)

        setupAttachButton()
        setupTabs()
        setupPages()
    }

    /**
     * Initializes the submit attachments button.
     */
    private fun setupAttachButton() {
        binding.attachButton.setImageDrawable(style.attachmentSelectionDialogStyle.attachButtonIcon)
        binding.attachButton.isEnabled = false
        binding.attachButton.setOnClickListener {
            attachmentSelectionListener?.onAttachmentsSelected(selectedAttachments, attachmentSource)
            dismiss()
        }
    }

    /**
     * Initializes the tabs of the picker.
     */
    private fun setupTabs() {
        attachmentsPickerTabFactories.forEachIndexed { index, factory ->
            val frameLayout = layoutInflater.inflate(
                R.layout.stream_ui_dialog_attachment_tab,
                binding.attachmentButtonsContainer,
                false
            ) as FrameLayout

            val checkedTextView = frameLayout.findViewById<CheckedTextView>(R.id.checkedTextView)
            checkedTextView.background = factory.createTabIcon(style)
            checkedTextView.backgroundTintList = style.attachmentSelectionDialogStyle.toggleButtonColorStateList
            checkedTextView.isChecked = index == 0

            frameLayout.setOnClickListener {
                setSelectedTab(checkedTextView, index)
            }

            binding.attachmentButtonsContainer.addView(frameLayout)
        }
    }

    /**
     * Initializes the content of the picker.
     */
    private fun setupPages() {
        val attachmentsPickerTabListener: AttachmentsPickerTabListener = object : AttachmentsPickerTabListener {
            override fun onSelectedAttachmentsChanged(
                attachments: List<AttachmentMetaData>,
                attachmentSource: AttachmentSource,
            ) {
                onAttachmentsSelected(attachments.toSet(), attachmentSource)
            }

            override fun onSelectedAttachmentsSubmitted() {
                attachmentSelectionListener?.onAttachmentsSelected(selectedAttachments, attachmentSource)
                dismiss()
            }
        }

        binding.attachmentPager.adapter = AttachmentDialogPagerAdapter(
            fragment = this,
            style = style,
            attachmentsPickerTabFactories = attachmentsPickerTabFactories,
            attachmentsPickerTabListener = attachmentsPickerTabListener
        )
        binding.attachmentPager.isUserInputEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        attachmentSelectionListener = null
    }

    override fun getTheme(): Int = R.style.StreamUiAttachmentBottomSheetDialog

    /**
     * Initializes the dialog with the style.
     *
     * @param style Style for the dialog.
     */
    public fun setStyle(style: MessageInputViewStyle) {
        this.style = style
    }

    /**
     * Sets the list of factories for the tabs that will be displayed in the attachment picker.
     */
    public fun setAttachmentsPickerTabFactories(attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory>) {
        this.attachmentsPickerTabFactories = attachmentsPickerTabFactories
    }

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

    private fun setSelectedTab(checkedTextView: CheckedTextView, pagePosition: Int) {
        binding.attachmentPager.setCurrentItem(pagePosition, false)
        binding.attachmentButtonsContainer.descendants.forEach {
            (it as? CheckedTextView)?.isChecked = it == checkedTextView
        }
    }

    private fun setAttachButtonEnabled(isEnabled: Boolean) {
        binding.attachButton.isEnabled = isEnabled
    }

    private fun setUnselectedButtonsEnabled(isEnabled: Boolean) {
        binding.attachmentButtonsContainer.descendants.forEach {
            if (it is CheckedTextView && !it.isChecked) {
                it.isEnabled = isEnabled
            }
        }
    }

    public companion object {
        public const val TAG: String = "attachment_dialog_fragment"

        /**
         * Creates a new instance of [AttachmentSelectionDialogFragment].
         *
         * See [AttachmentSelectionDialogStyle.createDefault] to load a default set of icons
         * to be used for the attachment dialog's tabs.
         *
         * @param style Style for the dialog.
         * @param attachmentsPickerTabFactories The list of factories for the tabs in the attachment picker.
         * @return A new instance of [AttachmentSelectionDialogFragment].
         */
        public fun newInstance(
            style: MessageInputViewStyle,
            attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory> = AttachmentsPickerTabFactories
                .defaultFactories(style),
        ): AttachmentSelectionDialogFragment {
            return AttachmentSelectionDialogFragment().apply {
                setStyle(style)
                setAttachmentsPickerTabFactories(attachmentsPickerTabFactories)
            }
        }
    }
}
