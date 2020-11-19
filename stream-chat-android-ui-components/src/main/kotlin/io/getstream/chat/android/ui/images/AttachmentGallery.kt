package io.getstream.chat.android.ui.images

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentActivity
import io.getstream.chat.android.ui.databinding.StreamAttachmentGalleryBinding

public class AttachmentGallery : ConstraintLayout {

    private val binding = StreamAttachmentGalleryBinding.inflate(LayoutInflater.from(context), this, true)

    public constructor(context: Context) : super(context) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    public fun init() {
    }

    public fun provideBitmapList(fragmentActivity: FragmentActivity, bitmapList: List<Bitmap>) {
        binding.vpAttachmentGallery.adapter = AttachmentSlidePagerAdapter(fragmentActivity, bitmapList)
    }
}
