package io.getstream.chat.android.ui.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.ImageLoader.load
import io.getstream.chat.android.ui.databinding.StreamItemImageGalleryBinding

private const val IMAGE_ID = "IMAGE_ID"

public class ImageSlidePageFragment : Fragment() {

    private lateinit var binding: StreamItemImageGalleryBinding

    public companion object {
        public fun create(imageUrl: String): Fragment {
            return ImageSlidePageFragment().apply {
                arguments = Bundle().apply {
                    putString(IMAGE_ID, imageUrl)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return StreamItemImageGalleryBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.attachmentImage.load(arguments?.getString(IMAGE_ID))
    }
}
