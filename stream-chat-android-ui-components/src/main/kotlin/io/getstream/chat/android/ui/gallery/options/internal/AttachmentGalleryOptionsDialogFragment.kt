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

package io.getstream.chat.android.ui.gallery.options.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.FullScreenDialogFragment
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentOptionsBinding

/**
 * Represents the image options menu, used to perform different actions for
 * the currently selected image.
 */
internal class AttachmentGalleryOptionsDialogFragment : FullScreenDialogFragment() {
    private var _binding: StreamUiFragmentAttachmentOptionsBinding? = null
    private val binding get() = _binding!!

    private var showInChatHandler: AttachmentOptionHandler? = null
    private var deleteHandler: AttachmentOptionHandler? = null
    private var replyHandler: AttachmentOptionHandler? = null
    private var saveImageHandler: AttachmentOptionHandler? = null
    private var isMine: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiFragmentAttachmentOptionsBinding.inflate(requireContext().streamThemeInflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.attachmentOptionsMenu.setReplyClickListener {
            replyHandler?.onClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setDeleteClickListener {
            deleteHandler?.onClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setShowInChatClickListener {
            showInChatHandler?.onClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setSaveImageClickListener {
            saveImageHandler?.onClick()
            dismiss()
        }
        binding.attachmentOptionsMenu.setIsMine(isMine)

        binding.root.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AttachmentOptionsDialogFragment"

        /**
         * Creates instances of [AttachmentGalleryOptionsDialogFragment].
         *
         * @param showInChatHandler A callback for the "show in chat" option.
         * @param replyHandler A callback for the "reply" option.
         * @param deleteHandler A callback for the "delete" option.
         * @param saveImageHandler A callback for the "save image" option.
         * @param isMine If the message belongs to the current user.
         */
        fun newInstance(
            showInChatHandler: AttachmentOptionHandler,
            replyHandler: AttachmentOptionHandler,
            deleteHandler: AttachmentOptionHandler,
            saveImageHandler: AttachmentOptionHandler,
            isMine: Boolean,
        ): AttachmentGalleryOptionsDialogFragment {
            return AttachmentGalleryOptionsDialogFragment().apply {
                this.showInChatHandler = showInChatHandler
                this.deleteHandler = deleteHandler
                this.replyHandler = replyHandler
                this.saveImageHandler = saveImageHandler
                this.isMine = isMine
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked when an option is clicked.
     */
    fun interface AttachmentOptionHandler {
        /**
         * Called when an option has been clicked.
         */
        fun onClick()
    }
}
