package io.getstream.chat.android.ui.gallery.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiDialogMediaAttachmentBinding

internal class MediaAttachmentDialogFragment : BottomSheetDialogFragment() {

    private var _binding: StreamUiDialogMediaAttachmentBinding? = null
    private val binding get() = _binding!!

    private val title: String by lazy { requireArguments().getString(ARG_TITLE)!! }
    private lateinit var userMediaAttachments: List<UserMediaAttachment>

    private var imageClickListener: (Int) -> Unit = {}

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiDialogMediaAttachmentBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        consumeUserMediaAttachmentsArg()

        binding.run {
            closeButton.setOnClickListener {
                dismiss()
            }
            title.text = this@MediaAttachmentDialogFragment.title
            mediaAttachmentGridView.setMediaClickListener {
                imageClickListener.invoke(it)
            }

            mediaAttachmentGridView.setAttachments(userMediaAttachments)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun setImageClickListener(listener: (Int) -> Unit) {
        imageClickListener = listener
    }

    private fun consumeUserMediaAttachmentsArg() {
        userMediaAttachmentsArg?.let {
            userMediaAttachments = it
            userMediaAttachmentsArg = null
        } ?: dismiss()
    }

    internal companion object {
        private const val ARG_TITLE = "title"

        private var userMediaAttachmentsArg: List<UserMediaAttachment>? = null

        fun newInstance(title: String, userMediaAttachments: List<UserMediaAttachment>): MediaAttachmentDialogFragment {
            return MediaAttachmentDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                }
                // pass attachments via static field
                userMediaAttachmentsArg = userMediaAttachments
            }
        }
    }
}
