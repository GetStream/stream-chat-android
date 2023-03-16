/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.getstream.sdk.chat.StreamFileUtil
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.PermissionChecker
import com.getstream.sdk.chat.utils.extensions.constrainViewToParentBySide
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.createStreamThemeWrapper
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentGalleryBinding
import io.getstream.chat.android.ui.gallery.internal.AttachmentGalleryPagerAdapter
import io.getstream.chat.android.ui.gallery.internal.AttachmentGalleryViewModel
import io.getstream.chat.android.ui.gallery.options.AttachmentGalleryOptionsViewStyle
import io.getstream.chat.android.ui.gallery.options.internal.AttachmentGalleryOptionsDialogFragment
import io.getstream.chat.android.ui.gallery.overview.internal.MediaAttachmentDialogFragment
import io.getstream.logging.StreamLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.Date

public class AttachmentGalleryActivity : AppCompatActivity() {

    private lateinit var binding: StreamUiActivityAttachmentGalleryBinding

    private val logger = StreamLog.getLogger("Chat:AttachmentGalleryActivity")

    /**
     * If the "reply" option is present in the list.
     */
    private var replyOptionEnabled: Boolean = true

    /**
     * If the "show in chat" option present in the list.
     */
    private var showInChatOptionEnabled: Boolean = true

    /**
     * If the "save image" option is present in the list
     */
    private var saveImageOptionEnabled: Boolean = true

    /**
     * If the "delete" option is present in the list.
     */
    private var deleteOptionEnabled: Boolean = true

    private val initialIndex: Int by lazy { intent.getIntExtra(EXTRA_KEY_INITIAL_INDEX, 0) }
    private val viewModel: AttachmentGalleryViewModel by viewModels()
    private lateinit var adapter: AttachmentGalleryPagerAdapter
    private val permissionChecker = PermissionChecker()

    private var attachmentGalleryItems: List<AttachmentGalleryItem> = emptyList()
    private val attachmentGalleryResultItem: AttachmentGalleryResultItem
        get() {
            val currentItem = attachmentGalleryItems[binding.galleryViewPager.currentItem]
            return currentItem.attachment.toAttachmentGalleryResultItem(
                messageId = currentItem.messageId,
                cid = currentItem.cid,
                userName = currentItem.user.name,
                isMine = currentItem.isMine,
                parentMessageId = currentItem.parentMessageId
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
                getString(R.string.stream_ui_attachment_gallery_share),
            ),
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = StreamUiActivityAttachmentGalleryBinding.inflate(streamThemeInflater)
        setContentView(binding.root)
        setupGalleryOverviewButton()
        binding.closeButton.setOnClickListener { onBackPressed() }
        viewModel.attachmentGalleryItemsLiveData.observe(this, ::setupGallery)
    }

    private fun setupGallery(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        if (attachmentGalleryItems.isEmpty()) {
            onBackPressed()
        } else {
            this.attachmentGalleryItems = attachmentGalleryItems
            setupGalleryAdapter()
            setupShareImageButton()
            setupAttachmentActionsButton()
            obtainOptionsViewStyle()
            observePageChanges()
        }
    }

    private fun setupGalleryAdapter() {
        adapter = AttachmentGalleryPagerAdapter(
            fragmentActivity = this,
            imageList = attachmentGalleryItems.map {
                val attachment = it.attachment
                attachment.imageUrl ?: attachment.thumbUrl ?: ""
            },
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
                    StreamFileUtil.writeImageToSharableFile(applicationContext, bitmap)
                }?.let(onSharePictureListener)

                delay(500)
                it.isEnabled = true
            }
        }
    }

    private fun setupAttachmentActionsButton() {
        binding.attachmentActionsButton.setOnClickListener {
            AttachmentGalleryOptionsDialogFragment.newInstance(
                showInChatOptionHandler = {
                    setResultAndFinish(AttachmentOptionResult.ShowInChat(attachmentGalleryResultItem))
                },
                deleteOptionHandler = { setResultAndFinish(AttachmentOptionResult.Delete(attachmentGalleryResultItem)) },
                replyOptionHandler = { setResultAndFinish(AttachmentOptionResult.Reply(attachmentGalleryResultItem)) },
                saveImageOptionHandler = handleSaveImage,
                isMessageMine = attachmentGalleryItems[binding.galleryViewPager.currentItem].isMine,
            ).show(supportFragmentManager, AttachmentGalleryOptionsDialogFragment.TAG)
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

    private val handleSaveImage = AttachmentGalleryOptionsDialogFragment.AttachmentOptionHandler {
        permissionChecker.checkWriteStoragePermissions(
            binding.root,
            onPermissionGranted = {
                setResultAndFinish(AttachmentOptionResult.Download(attachmentGalleryResultItem))
            }
        )
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
            R.string.stream_ui_attachment_gallery_count,
            position + 1,
            attachmentGalleryItems.size
        )

        val currentItem = attachmentGalleryItems[position]
        binding.attachmentDateTextView.text = getRelativeAttachmentDate(currentItem.createdAt)
        binding.userTextView.text = currentItem.user.name
        binding.attachmentActionsButton.isVisible = shouldShowOptionsButton(currentItem.isMine)
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
            .replaceFirstChar(Char::lowercase)

        return getString(
            R.string.stream_ui_attachment_gallery_date,
            relativeDay,
            ChatUI.dateFormatter.formatTime(createdAt)
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
            galleryViewPager.setBackgroundColor(getColorCompat(R.color.stream_ui_literal_black))
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

    /**
     * Obtains style attributes for the options list.
     */
    private fun obtainOptionsViewStyle() {
        try {
            createStreamThemeWrapper().obtainStyledAttributes(
                null,
                R.styleable.AttachmentOptionsView,
                R.attr.streamUiAttachmentGalleryOptionsStyle,
                R.style.StreamUi_AttachmentGallery_Options
            ).use {
                val style = AttachmentGalleryOptionsViewStyle(this, it)
                replyOptionEnabled = style.replyOptionEnabled
                showInChatOptionEnabled = style.showInChatOptionEnabled
                saveImageOptionEnabled = style.saveImageOptionEnabled
                deleteOptionEnabled = style.deleteOptionEnabled
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to obtain style attribute for the options menu" }
        }
    }

    /**
     * Checks if we need to show the options menu button. We show the options button
     * if there is at least one option available.
     *
     * @param isMine If the message belongs to the current user.
     */
    private fun shouldShowOptionsButton(isMine: Boolean): Boolean {
        return replyOptionEnabled ||
            showInChatOptionEnabled ||
            saveImageOptionEnabled ||
            (deleteOptionEnabled && isMine)
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
