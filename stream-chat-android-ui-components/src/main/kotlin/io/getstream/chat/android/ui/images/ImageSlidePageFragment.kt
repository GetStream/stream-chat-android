package io.getstream.chat.android.ui.images

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamItemImageGalleryBinding

public class ImageSlidePageFragment : Fragment() {

    public var imageBitmap: Bitmap? = null

    private val binding = StreamItemImageGalleryBinding.inflate(LayoutInflater.from(context), null, true)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.stream_item_image_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageBitmap?.let(binding.ivImageItem::setImageBitmap)
    }
}
