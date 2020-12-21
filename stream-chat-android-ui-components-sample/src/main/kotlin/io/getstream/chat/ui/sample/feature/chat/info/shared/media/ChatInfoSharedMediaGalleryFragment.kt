package io.getstream.chat.ui.sample.feature.chat.info.shared.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import io.getstream.chat.ui.sample.databinding.FragmentChatInfoSharedMediaGalleryBinding

class ChatInfoSharedMediaGalleryFragment : Fragment() {

    private var _binding: FragmentChatInfoSharedMediaGalleryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ChatInfoSharedMediaViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatInfoSharedMediaGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.attachmentWithDate.observe(viewLifecycleOwner) { attachments ->
            binding.attachmentGallery.provideImageList(
                requireActivity(),
                attachments.mapNotNull { attachmentWithDate ->
                    attachmentWithDate.attachment.url ?: attachmentWithDate.attachment.imageUrl
                }
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
