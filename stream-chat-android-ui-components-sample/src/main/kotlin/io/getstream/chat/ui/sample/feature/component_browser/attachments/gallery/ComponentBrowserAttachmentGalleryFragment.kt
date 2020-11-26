package io.getstream.chat.ui.sample.feature.component_browser.attachments.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.ui.sample.databinding.FragmentComponentBrowserAttachmentGalleryBinding

class ComponentBrowserAttachmentGalleryFragment : Fragment() {

    private var _binding: FragmentComponentBrowserAttachmentGalleryBinding? = null
    private val binding get() = _binding!!

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
        val imageUrlList = listOf(
            "http://images.ctfassets.net/yadj1kx9rmg0/wtrHxeu3zEoEce2MokCSi/cf6f68efdcf625fdc060607df0f3baef/quwowooybuqbl6ntboz3.jpg",
            "https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=paul",
            "https://getstream.io/random_png/?id=80c26629-bc25-4ee5-a8ae-4824f8097b53&name=letto",
        )

        binding.attachmentGallery.provideImageList(requireActivity(), imageUrlList)
    }
}
