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
import io.getstream.chat.android.ui.images.menu.ImagesMenuDialogFragment
import io.getstream.chat.android.ui.utils.extensions.getFragmentManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

public class AttachmentGallery : ConstraintLayout {

    private var onSharePictureListener: (pictureUri: Uri) -> Unit = { pictureUri ->
        ContextCompat.startActivity(
            context,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, pictureUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                context.getString(R.string.stream_ui_gallery_share_sheet_title),
            ),
            null
        )
    }
    private val binding = StreamUiAttachmentGalleryBinding.inflate(LayoutInflater.from(context), this, true)
    private var countText: String = "%s - %s"
    private lateinit var adapter: AttachmentSlidePagerAdapter

    private var imagesMenuTitle: String = ""

    private var imageList: List<String> = mutableListOf()

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
        configureImagesMenu()
    }

    public fun provideImageList(fragmentActivity: FragmentActivity, imageList: List<String>, currentIndex: Int = 0) {
        adapter = AttachmentSlidePagerAdapter(fragmentActivity, imageList)
        binding.attachmentGallery.adapter = adapter
        configPositionCount(imageList.size)
        binding.attachmentGallery.setCurrentItem(currentIndex, false)

        this.imageList = imageList

        binding.shareButton.setOnClickListener {
            GlobalScope.launch(DispatcherProvider.Main) {
                ImageLoader.getBitmapUri(context, adapter.getItem(binding.attachmentGallery.currentItem))
                    ?.let(onSharePictureListener)
            }
        }
    }

    public fun setOnSharePictureListener(listener: (Uri) -> Unit) {
        onSharePictureListener = listener
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

            imagesMenuTitle = tArray.getString(R.styleable.AttachmentGallery_streamUiImagesMenuTitle)
                ?: context.getString(R.string.stream_ui_images_menu_default_title)
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

    private fun configureImagesMenu() {
        binding.menuButton.setOnClickListener {
            context.getFragmentManager()?.let { fragmentManager ->
                ImagesMenuDialogFragment.newInstance(imagesMenuTitle, imageList)
                    .apply {
                        setImageClickListener { position ->
                            binding.attachmentGallery.setCurrentItem(position, true)
                            dismiss()
                        }
                    }
                    .show(fragmentManager, null)
            }
        }
    }
}
