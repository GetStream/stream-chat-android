package io.getstream.chat.android.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import io.getstream.chat.android.ui.databinding.StreamUiFragmentAttachmentOptionsBinding
import io.getstream.chat.android.ui.view.FullScreenDialogFragment

internal class AttachmentOptionsDialogFragment: FullScreenDialogFragment() {
    private var _binding: StreamUiFragmentAttachmentOptionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiFragmentAttachmentOptionsBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    companion object {
        const val TAG = "AttachmentOptionsDialogFragment"

        private const val ARG_OPTIONS_ATTACHMENT_URL = "attachmentUrl"

        fun newInstance(url: String): AttachmentOptionsDialogFragment {
            return AttachmentOptionsDialogFragment().apply {
                arguments = bundleOf(ARG_OPTIONS_ATTACHMENT_URL to url)
            }
        }
    }

}