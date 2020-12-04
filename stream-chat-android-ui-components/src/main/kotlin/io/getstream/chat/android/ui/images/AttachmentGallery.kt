package io.getstream.chat.android.ui.images

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamAttachmentGalleryBinding

public class AttachmentGallery : ConstraintLayout {

    private val binding = StreamAttachmentGalleryBinding.inflate(LayoutInflater.from(context), this, true)

    private var countText: String = "%s - %s"

    public constructor(context: Context) : super(context)

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

    public fun provideImageList(fragmentActivity: FragmentActivity, imageList: List<String>) {
        binding.attachmentGallery.adapter = AttachmentSlidePagerAdapter(fragmentActivity, imageList)
        configPositionCount(imageList.size)
    }

    public fun setShareButtonClickListener(listener: OnClickListener) {
        binding.shareButton.setOnClickListener(listener)
    }

    public fun setMenuButtonClickListener(listener: OnClickListener) {
        binding.menuButton.setOnClickListener(listener)
    }

    private fun configureAttributes(attributeSet: AttributeSet) {
        context.obtainStyledAttributes(attributeSet, R.styleable.StreamUiAttachmentGalleryView).use { tArray ->
            countText = tArray.getString(R.styleable.StreamUiAttachmentGalleryView_streamUiCountText) ?: "%s - %s"

            tArray.getColor(
                R.styleable.StreamUiAttachmentGalleryView_streamUiCountTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_black)
            ).let(binding.photoCount::setTextColor)
        }
    }

    private fun configPositionCount(count: Int) {
        if (count > 1) {
            binding.photoCount.text = String.format(countText, 1, count)
        } else {
            binding.photoCount.isVisible = false
        }

        binding.attachmentGallery.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    binding.photoCount.text = String.format(countText, position + 1, count)
                }
            }
        )
    }
}
