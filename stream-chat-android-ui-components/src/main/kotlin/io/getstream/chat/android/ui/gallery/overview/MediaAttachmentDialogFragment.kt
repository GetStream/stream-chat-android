package io.getstream.chat.android.ui.gallery.overview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiDialogMediaAttachmentBinding
import io.getstream.chat.android.ui.gallery.AttachmentGalleryViewModel

internal class MediaAttachmentDialogFragment : BottomSheetDialogFragment() {
    private var _binding: StreamUiDialogMediaAttachmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AttachmentGalleryViewModel by viewModels()
    private var imageClickListener: (Int) -> Unit = {}

    override fun getTheme(): Int = R.style.StreamUiBottomSheetDialogTheme

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiDialogMediaAttachmentBinding.inflate(inflater, container, false)
            .apply { _binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.run {
            closeButton.setOnClickListener {
                dismiss()
            }
            mediaAttachmentGridView.setMediaClickListener {
                imageClickListener.invoke(it)
            }
            viewModel.attachmentGalleryItemsLiveData.observe(viewLifecycleOwner, mediaAttachmentGridView::setAttachments)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun setImageClickListener(listener: (Int) -> Unit) {
        imageClickListener = listener
    }

    internal companion object {
        fun newInstance(): MediaAttachmentDialogFragment {
            return MediaAttachmentDialogFragment()
        }
    }
}
