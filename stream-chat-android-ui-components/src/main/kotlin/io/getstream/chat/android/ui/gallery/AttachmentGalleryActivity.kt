package io.getstream.chat.android.ui.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.getstream.sdk.chat.StreamFileProvider
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentGalleryBinding
import io.getstream.chat.android.ui.gallery.overview.MediaAttachmentDialogFragment
import io.getstream.chat.android.ui.options.attachment.AttachmentOptionsDialogFragment
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.Date

public class AttachmentGalleryActivity : AppCompatActivity() {
    private lateinit var binding: StreamUiActivityAttachmentGalleryBinding

    private val initialIndex: Int by lazy { intent.getIntExtra(EXTRA_KEY_INITIAL_INDEX, 0) }
    private val viewModel: AttachmentGalleryViewModel by viewModels()
    private val dateFormatter: DateFormatter by lazy { DateFormatter.from(this) }
    private lateinit var adapter: AttachmentGalleryPagerAdapter

    private var attachmentGalleryItems: List<AttachmentGalleryItem> = emptyList()
    private val attachmentGalleryResultItem: AttachmentGalleryResultItem
        get() {
            val currentItem = attachmentGalleryItems[binding.galleryViewPager.currentItem]
            return currentItem.attachment.toAttachmentGalleryResultItem(
                messageId = currentItem.messageId,
                cid = currentItem.cid,
                userName = currentItem.user.name,
                isMine = currentItem.isMine
            )
        }
    private var isFullScreen = false

    private var onSharePictureListener: (pictureUri: Uri) -> Unit = { pictureUri ->
        ContextCompat.startActivity(
            this,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, pictureUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                getString(R.string.stream_ui_gallery_share_sheet_title),
            ),
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = StreamUiActivityAttachmentGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupGalleryOverviewButton()
        binding.closeButton.setOnClickListener { onBackPressed() }
        viewModel.attachmentGalleryItemsLiveData.observe(this, ::setupGallery)
    }

    private fun setupGallery(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        this.attachmentGalleryItems = attachmentGalleryItems
        setupGalleryAdapter()
        setupShareImageButton()
        setupAttachmentActionsButton()
        observePageChanges()
    }

    private fun setupGalleryAdapter() {
        adapter = AttachmentGalleryPagerAdapter(
            fragmentActivity = this,
            imageList = attachmentGalleryItems.map { it.attachment.imageUrl!! },
            imageClickListener = {
                isFullScreen = !isFullScreen
                if (isFullScreen) enterFullScreenMode() else exitFullScreenMode()
            }
        )
        binding.galleryViewPager.adapter = adapter
        binding.galleryViewPager.setCurrentItem(initialIndex, false)
    }

    private fun setupShareImageButton() {
        binding.shareImageButton.setOnClickListener {
            it.isEnabled = false
            GlobalScope.launch(DispatcherProvider.Main) {
                StreamImageLoader.instance().loadAsBitmap(
                    context = applicationContext,
                    url = adapter.getItem(binding.galleryViewPager.currentItem)
                )?.let { bitmap ->
                    StreamFileProvider.writeImageToSharableFile(applicationContext, bitmap)
                }?.let(onSharePictureListener)

                delay(500)
                it.isEnabled = true
            }
        }
    }

    private fun setupAttachmentActionsButton() {
        binding.attachmentActionsButton.setOnClickListener {
            AttachmentOptionsDialogFragment.newInstance(
                showInChatHandler = { setResultAndFinish(AttachmentOptionResult.ShowInChat(attachmentGalleryResultItem)) },
                deleteHandler = { setResultAndFinish(AttachmentOptionResult.Delete(attachmentGalleryResultItem)) },
                replyHandler = { setResultAndFinish(AttachmentOptionResult.Reply(attachmentGalleryResultItem)) },
                saveImageHandler = { setResultAndFinish(AttachmentOptionResult.Download(attachmentGalleryResultItem)) },
                isMine = attachmentGalleryItems[binding.galleryViewPager.currentItem].isMine,
            ).show(supportFragmentManager, AttachmentOptionsDialogFragment.TAG)
        }
    }

    private fun observePageChanges() {
        binding.galleryViewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    onGalleryPageSelected(position)
                }
            }
        )
        onGalleryPageSelected(initialIndex)
    }

    private fun setResultAndFinish(result: AttachmentOptionResult) {
        Intent().apply {
            putExtra(EXTRA_ATTACHMENT_OPTION_RESULT, result)
            setResult(RESULT_OK, this)
        }
        finish()
    }

    private fun onGalleryPageSelected(position: Int) {
        binding.imageCountTextView.text = getString(
            R.string.stream_ui_gallery_count_template,
            position + 1,
            attachmentGalleryItems.size
        )

        val currentItem = attachmentGalleryItems[position]
        binding.attachmentDateTextView.text = getRelativeAttachmentDate(currentItem.createdAt)
        binding.userTextView.text = currentItem.user.name
    }

    private fun getRelativeAttachmentDate(createdAt: Date): String {
        val relativeDay = DateUtils
            .getRelativeTimeSpanString(
                createdAt.time,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            )
            .toString()
            .decapitalize()

        return getString(
            R.string.stream_ui_date_and_time_pattern,
            relativeDay,
            dateFormatter.formatTime(createdAt)
        )
    }

    private fun setupGalleryOverviewButton() {
        binding.galleryOverviewButton.setOnClickListener {
            MediaAttachmentDialogFragment.newInstance()
                .apply {
                    setImageClickListener {
                        binding.galleryViewPager.setCurrentItem(it, true)
                        dismiss()
                    }
                }
                .show(supportFragmentManager, null)
        }
    }

    private fun enterFullScreenMode() {
        with(binding) {
            toolbar.isVisible = false
            bottomBar.isVisible = false
            galleryViewPager.setBackgroundColor(getColorCompat(R.color.stream_ui_black))
            ConstraintSet().apply {
                constrainViewToParentBySide(galleryViewPager, ConstraintSet.TOP)
                constrainViewToParentBySide(galleryViewPager, ConstraintSet.BOTTOM)
                constrainViewToParentBySide(galleryViewPager, ConstraintSet.START)
                constrainViewToParentBySide(galleryViewPager, ConstraintSet.END)
            }.applyTo(root)
        }
    }

    private fun exitFullScreenMode() {
        with(binding) {
            toolbar.isVisible = true
            bottomBar.isVisible = true
            galleryViewPager.setBackgroundColor(getColorCompat(R.color.stream_ui_white_snow))
            ConstraintSet().apply {
                connect(galleryViewPager.id, ConstraintSet.TOP, binding.toolbar.id, ConstraintSet.BOTTOM, 0)
                connect(galleryViewPager.id, ConstraintSet.BOTTOM, binding.bottomBar.id, ConstraintSet.TOP, 0)
                constrainViewToParentBySide(galleryViewPager, ConstraintSet.START)
                constrainViewToParentBySide(galleryViewPager, ConstraintSet.END)
            }.applyTo(binding.root)
        }
    }

    public fun interface AttachmentShowInChatOptionHandler {
        public fun onClick(result: AttachmentGalleryResultItem)
    }

    public fun interface AttachmentReplyOptionHandler {
        public fun onClick(result: AttachmentGalleryResultItem)
    }

    public fun interface AttachmentDownloadOptionHandler {
        public fun onClick(result: AttachmentGalleryResultItem)
    }

    public fun interface AttachmentDeleteOptionHandler {
        public fun onClick(result: AttachmentGalleryResultItem)
    }

    internal sealed class AttachmentOptionResult(open val result: AttachmentGalleryResultItem) : Parcelable {
        @Parcelize
        internal class Reply(override val result: AttachmentGalleryResultItem) : AttachmentOptionResult(result)

        @Parcelize
        internal class ShowInChat(override val result: AttachmentGalleryResultItem) : AttachmentOptionResult(result)

        @Parcelize
        internal class Delete(override val result: AttachmentGalleryResultItem) : AttachmentOptionResult(result)

        @Parcelize
        internal class Download(override val result: AttachmentGalleryResultItem) : AttachmentOptionResult(result)
    }

    public companion object {
        private const val EXTRA_KEY_INITIAL_INDEX: String = "extra_key_initial_index"

        internal const val EXTRA_ATTACHMENT_OPTION_RESULT = "extra_attachment_option_result"

        @JvmStatic
        public fun createIntent(context: Context, initialIndex: Int): Intent {
            return Intent(context, AttachmentGalleryActivity::class.java).apply {
                putExtra(EXTRA_KEY_INITIAL_INDEX, initialIndex)
            }
        }
    }
}
