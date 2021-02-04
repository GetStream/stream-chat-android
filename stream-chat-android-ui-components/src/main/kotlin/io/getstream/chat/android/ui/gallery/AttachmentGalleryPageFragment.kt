package io.getstream.chat.android.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.images.load
import io.getstream.chat.android.ui.databinding.StreamUiItemImageGalleryBinding

internal class AttachmentGalleryPageFragment : Fragment() {
    private lateinit var binding: StreamUiItemImageGalleryBinding
    private val imageUrl: String by lazy {
        requireNotNull(requireArguments().getString(ARG_IMAGE_URL)) { "Image URL must not be null" }
    }

    private var imageClickListener: () -> Unit = {}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return StreamUiItemImageGalleryBinding.inflate(inflater)
            .apply { binding = this }
            .root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding.photoView) {
            load(data = imageUrl)
            setOnClickListener {
                imageClickListener()
            }
        }
    }

    companion object {
        private const val ARG_IMAGE_URL = "image_url"

        fun create(imageUrl: String, imageClickListener: () -> Unit = {}): Fragment {
            return AttachmentGalleryPageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_IMAGE_URL, imageUrl)
                }
                this.imageClickListener = imageClickListener
            }
        }
    }
}
