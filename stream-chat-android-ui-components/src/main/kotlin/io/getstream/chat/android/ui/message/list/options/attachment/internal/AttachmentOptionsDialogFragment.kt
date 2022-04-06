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
 
package io.getstream.chat.android.ui.message.list.options.attachment.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.FullScreenDialogFragment
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentOptionsBinding

internal class AttachmentOptionsDialogFragment : FullScreenDialogFragment() {
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
        binding.attachmentOptionsMenu.setReplyClickListener(
            object : AttachmentOptionsView.ReplyClickListener {
                override fun onClick() {
                    replyHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.attachmentOptionsMenu.setDeleteClickListener(
            object : AttachmentOptionsView.DeleteClickListener {
                override fun onClick() {
                    deleteHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.attachmentOptionsMenu.setShowInChatClickListener(
            object : AttachmentOptionsView.ShowInChatClickListener {
                override fun onClick() {
                    showInChatHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.attachmentOptionsMenu.setSaveImageClickListener(
            object : AttachmentOptionsView.SaveImageClickListener {
                override fun onClick() {
                    saveImageHandler?.onClick()
                    dismiss()
                }
            }
        )
        binding.root.setOnClickListener { dismiss() }
        binding.attachmentOptionsMenu.setDeleteItemVisiblity(isMine && binding.attachmentOptionsMenu.isDeleteEnabled)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AttachmentOptionsDialogFragment"

        fun newInstance(
            showInChatHandler: AttachmentOptionHandler,
            replyHandler: AttachmentOptionHandler,
            deleteHandler: AttachmentOptionHandler,
            saveImageHandler: AttachmentOptionHandler,
            isMine: Boolean,
        ): AttachmentOptionsDialogFragment {
            return AttachmentOptionsDialogFragment().apply {
                this.showInChatHandler = showInChatHandler
                this.deleteHandler = deleteHandler
                this.replyHandler = replyHandler
                this.saveImageHandler = saveImageHandler
                this.isMine = isMine
            }
        }
    }

    fun interface AttachmentOptionHandler {
        fun onClick()
    }
}
