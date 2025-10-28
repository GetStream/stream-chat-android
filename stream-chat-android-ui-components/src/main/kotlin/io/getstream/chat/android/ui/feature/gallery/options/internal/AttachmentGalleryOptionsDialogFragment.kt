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

package io.getstream.chat.android.ui.feature.gallery.options.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentOptionsBinding
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.widgets.FullScreenDialogFragment

/**
 * Represents the image options menu, used to perform different actions for
 * the currently selected image.
 */
internal class AttachmentGalleryOptionsDialogFragment : FullScreenDialogFragment() {

    private var _binding: StreamUiFragmentAttachmentOptionsBinding? = null
    private val binding get() = _binding!!

    /**
     * A callback for the "show in chat" option.
     */
    private var showInChatOptionHandler: AttachmentOptionHandler? = null

    /**
     * A callback for the "reply" option.
     */
    private var replyOptionHandler: AttachmentOptionHandler? = null

    /**
     * A callback for the "delete" option.
     */
    private var deleteOptionHandler: AttachmentOptionHandler? = null

    /**
     * A callback for the "save image" option.
     */
    private var saveImageOptionHandler: AttachmentOptionHandler? = null

    /**
     * If the message belongs to the current user.
     */
    private var isMessageMine: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = StreamUiFragmentAttachmentOptionsBinding.inflate(requireContext().streamThemeInflater, container, false)
        .apply { _binding = this }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isInitialized = showInChatOptionHandler != null && replyOptionHandler != null &&
            deleteOptionHandler != null && saveImageOptionHandler != null
        if (savedInstanceState == null && isInitialized) {
            setupDialog()
        } else {
            // The process has been killed
            dismiss()
        }
    }

    /**
     * Initializes the dialog.
     */
    private fun setupDialog() {
        binding.attachmentOptionsMenu.setReplyClickListener {
            replyOptionHandler?.onAttachmentOptionClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setDeleteClickListener {
            deleteOptionHandler?.onAttachmentOptionClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setShowInChatClickListener {
            showInChatOptionHandler?.onAttachmentOptionClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setSaveMediaClickListener {
            saveImageOptionHandler?.onAttachmentOptionClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setIsMine(isMessageMine)

        binding.root.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**
     * Sets a callback for the "show in chat" option.
     *
     * @param showInChatOptionHandler The handler to set.
     */
    fun setShowInChatOptionHandler(showInChatOptionHandler: AttachmentOptionHandler) {
        this.showInChatOptionHandler = showInChatOptionHandler
    }

    /**
     * Sets a callback for the "reply" option.
     *
     * @param replyOptionHandler The handler to set.
     */
    fun setReplyOptionHandler(replyOptionHandler: AttachmentOptionHandler) {
        this.replyOptionHandler = replyOptionHandler
    }

    /**
     * Sets a callback for the "delete" option.
     *
     * @param deleteOptionHandler The handler to set.
     */
    fun setDeleteOptionHandler(deleteOptionHandler: AttachmentOptionHandler) {
        this.deleteOptionHandler = deleteOptionHandler
    }

    /**
     * Sets a callback for the "save image" option.
     *
     * @param saveImageOptionHandler The handler to set.
     */
    fun setSaveImageOptionHandler(saveImageOptionHandler: AttachmentOptionHandler) {
        this.saveImageOptionHandler = saveImageOptionHandler
    }

    /**
     * Set the message ownership.
     *
     * @param isMessageMine If the message belongs to the current user.
     */
    fun setIsMessageMine(isMessageMine: Boolean) {
        this.isMessageMine = isMessageMine
    }

    /**
     * Interface definition for a callback to be invoked when an option is clicked.
     */
    fun interface AttachmentOptionHandler {
        /**
         * Called when an option has been clicked.
         */
        fun onAttachmentOptionClick()
    }

    companion object {
        const val TAG = "AttachmentOptionsDialogFragment"

        /**
         * Creates a new instance of [AttachmentGalleryOptionsDialogFragment].
         *
         * @param showInChatOptionHandler A callback for the "show in chat" option.
         * @param replyOptionHandler A callback for the "reply" option.
         * @param deleteOptionHandler A callback for the "delete" option.
         * @param saveMediaOptionHandler A callback for the "save image" option.
         * @param isMessageMine If the message belongs to the current user.
         */
        fun newInstance(
            showInChatOptionHandler: AttachmentOptionHandler,
            replyOptionHandler: AttachmentOptionHandler,
            deleteOptionHandler: AttachmentOptionHandler,
            saveMediaOptionHandler: AttachmentOptionHandler,
            isMessageMine: Boolean,
        ): AttachmentGalleryOptionsDialogFragment = AttachmentGalleryOptionsDialogFragment().apply {
            setShowInChatOptionHandler(showInChatOptionHandler)
            setReplyOptionHandler(replyOptionHandler)
            setDeleteOptionHandler(deleteOptionHandler)
            setSaveImageOptionHandler(saveMediaOptionHandler)
            setIsMessageMine(isMessageMine)
        }
    }
}
