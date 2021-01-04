package io.getstream.chat.android.ui.images

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import com.getstream.sdk.chat.ImageLoader
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiAttachmentGalleryBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

public class AttachmentGallery : ConstraintLayout {

    private var onSharePictureListener: (pictureUri: Uri) -> Unit = { pictureUri ->
        ContextCompat.startActivity(
            context,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, pictureUri)
                },
                "Share Image"
            ),
            null
        )
    }
    private val binding = StreamUiAttachmentGalleryBinding.inflate(LayoutInflater.from(context), this, true)
    private var countText: String = "%s - %s"
    private lateinit var adapter: AttachmentSlidePagerAdapter

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

    public fun provideImageList(fragmentActivity: FragmentActivity, imageList: List<String>, currentIndex: Int = 0) {
        adapter = AttachmentSlidePagerAdapter(fragmentActivity, imageList)
        binding.attachmentGallery.adapter = adapter
        configPositionCount(imageList.size)
        binding.attachmentGallery.setCurrentItem(currentIndex, false)
        binding.shareButton.setOnClickListener {
            CoroutineScope(DispatcherProvider.IO).launch {
                ImageLoader.getBitmapUri(context, adapter.getItem(binding.attachmentGallery.currentItem))
                    ?.let { pictureUri ->
                        withContext(DispatcherProvider.Main) { onSharePictureListener(pictureUri) }
                    }
            }
        }
    }

    public fun setOnSharePictureListener(listener: (Uri) -> Unit) {
        onSharePictureListener = listener
    }

    public fun setShareButtonClickListener(listener: OnClickListener) {
        binding.shareButton.setOnClickListener(listener)
    }

    public fun setMenuButtonClickListener(listener: OnClickListener) {
        binding.menuButton.setOnClickListener(listener)
    }

    private fun configureAttributes(attributeSet: AttributeSet) {
        context.obtainStyledAttributes(attributeSet, R.styleable.AttachmentGallery).use { tArray ->
            countText = tArray.getString(R.styleable.AttachmentGallery_streamUiCountText) ?: "%s - %s"

            tArray.getColor(
                R.styleable.AttachmentGallery_streamUiCountTextColor,
                ContextCompat.getColor(context, R.color.stream_ui_text_color_strong)
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
