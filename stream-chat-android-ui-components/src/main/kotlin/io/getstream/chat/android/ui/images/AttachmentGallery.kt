package io.getstream.chat.android.ui.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamAttachmentGalleryBinding

public class AttachmentGallery : ConstraintLayout {

    private val binding = StreamAttachmentGalleryBinding.inflate(LayoutInflater.from(context), this, true)

    private var countText: String = "%s - %s"

    public constructor(context: Context) : super(context) {
        init(null)
    }

    public constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    public constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    public fun init(attr: AttributeSet?) {
        attr?.let(::configureAttributes)
    }

    private fun configureAttributes(attributeSet: AttributeSet) {
        context.obtainStyledAttributes(attributeSet, R.styleable.StreamAttachmentGalleryView).use { tArray ->
            countText = tArray.getString(R.styleable.StreamAttachmentGalleryView_streamCountText) ?: "%s - %s"
        }
    }

    public fun provideImageList(fragmentActivity: FragmentActivity, imageList: List<String>) {
        binding.vpAttachmentGallery.adapter = AttachmentSlidePagerAdapter(fragmentActivity, imageList)
        configPositionCount(imageList.size)
    }

    private fun configPositionCount(count: Int) {
        if (count > 1) {
            binding.tvPhotoCount.text = String.format(countText, 1, count)
        } else {
            binding.tvPhotoCount.isVisible = false
        }

        binding.vpAttachmentGallery.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    binding.tvPhotoCount.text = String.format(countText, position + 1, count)
                }
            }
        )
    }
}
