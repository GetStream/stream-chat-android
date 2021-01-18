package io.getstream.chat.android.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentOptionsBinding
import io.getstream.chat.android.ui.view.FullScreenDialogFragment

internal class AttachmentOptionsDialogFragment: FullScreenDialogFragment() {
    private var _binding: StreamUiFragmentAttachmentOptionsBinding? = null
    private val binding get() = _binding!!

    private var showInChatHandler: ShowInChatHandler? = null
    private var deleteHandler: DeleteHandler? = null
    private var replyHandler: ReplyHandler? = null
    private var saveImageHandler: SaveImageHandler? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiFragmentAttachmentOptionsBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.attachmentOptionsMenu.setReplyClickListener(object : AttachmentOptionsView.ReplyClickListener {
            override fun onClick() {
                replyHandler?.onClick()
                dismiss()
            }
        })
        binding.attachmentOptionsMenu.setDeleteClickListener(object : AttachmentOptionsView.DeleteClickListener {
            override fun onClick() {
                deleteHandler?.onClick()
                dismiss()
            }
        })
        binding.attachmentOptionsMenu.setShowInChatClickListener(object : AttachmentOptionsView.ShowInChatClickListener {
            override fun onClick() {
                showInChatHandler?.onClick()
                dismiss()
            }
        })
        binding.attachmentOptionsMenu.setSaveImageClickListener(object : AttachmentOptionsView.SaveImageClickListener {
            override fun onClick() {
                saveImageHandler?.onClick()
                dismiss()
            }
        })
        binding.root.setOnClickListener { dismiss() }
    }

    internal interface ShowInChatHandler { fun onClick(): Unit }
    internal interface DeleteHandler { fun onClick(): Unit }
    internal interface ReplyHandler { fun onClick(): Unit }
    internal interface SaveImageHandler { fun onClick(): Unit }

    companion object {
        const val TAG = "AttachmentOptionsDialogFragment"

        fun newInstance(
            showInChatHandler: ShowInChatHandler? = null,
            deleteHandler: DeleteHandler? = null,
            replyHandler: ReplyHandler? = null,
            saveImageHandler: SaveImageHandler? = null,
        ): AttachmentOptionsDialogFragment {
            return AttachmentOptionsDialogFragment().apply {
                this.showInChatHandler = showInChatHandler
                this.deleteHandler = deleteHandler
                this.replyHandler = replyHandler
                this.saveImageHandler = saveImageHandler
            }
        }
    }
}
