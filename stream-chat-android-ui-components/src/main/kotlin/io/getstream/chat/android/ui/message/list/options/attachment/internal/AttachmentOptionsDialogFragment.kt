package io.getstream.chat.android.ui.message.list.options.attachment.internal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        return StreamUiFragmentAttachmentOptionsBinding.inflate(inflater, container, false)
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
        binding.attachmentOptionsMenu.setDeleteItemVisiblity(isMine)
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
        fun onClick(): Unit
    }
}
