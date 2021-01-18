package io.getstream.chat.android.ui.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.images.load
import io.getstream.chat.android.ui.databinding.StreamUiItemImageGalleryBinding

private const val IMAGE_ID = "IMAGE_ID"

public class ImageSlidePageFragment : Fragment() {

    private lateinit var binding: StreamUiItemImageGalleryBinding

    private var imageClickListener: () -> Unit = {}

    public companion object {
        public fun create(imageUrl: String, imageClickListener: () -> Unit = {}): Fragment {
            return ImageSlidePageFragment().apply {
                arguments = Bundle().apply {
                    putString(IMAGE_ID, imageUrl)
                }

                this.imageClickListener = imageClickListener
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return StreamUiItemImageGalleryBinding.inflate(LayoutInflater.from(context)).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.attachmentImage.run {
            load(data = arguments?.getString(IMAGE_ID))

            setOnClickListener {
                imageClickListener()
            }
        }
    }
}
