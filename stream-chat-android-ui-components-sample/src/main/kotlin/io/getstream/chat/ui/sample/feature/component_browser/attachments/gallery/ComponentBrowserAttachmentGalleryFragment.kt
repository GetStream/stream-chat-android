package io.getstream.chat.ui.sample.feature.component_browser.attachments.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserAttachmentGalleryBinding

class ComponentBrowserAttachmentGalleryFragment : Fragment() {

    private var _binding: FragmentComponentBrowserAttachmentGalleryBinding? = null
    private val binding get() = _binding!!

    private val args: ComponentBrowserAttachmentGalleryFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComponentBrowserAttachmentGalleryBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.attachmentGallery.run {
            provideImageList(requireActivity(), args.picturesArgs.toList())

            setMenuButtonClickListener {
                Toast.makeText(requireContext(), "Menu click!", Toast.LENGTH_SHORT).show()
            }

            setShareButtonClickListener {
                Toast.makeText(requireContext(), "Share click!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
