package io.getstream.chat.android.ui.images

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.getstream.sdk.chat.ImageLoader
import io.getstream.chat.android.ui.databinding.StreamItemImageGalleryBinding

public class ImageSlidePageFragment : Fragment() {

    public var image: String? = null

    private lateinit var binding: StreamItemImageGalleryBinding

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

        image?.let {
            ImageLoader.run {
                binding.ivImageItem.load(it)
            }
        }
    }
}
