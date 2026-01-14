/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.feature.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.format.DateUtils
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import io.getstream.chat.android.ui.common.utils.shareLocalFile
import io.getstream.chat.android.ui.databinding.StreamUiActivityAttachmentGalleryBinding
import io.getstream.chat.android.ui.feature.gallery.internal.AttachmentGalleryPagerAdapter
import io.getstream.chat.android.ui.feature.gallery.internal.AttachmentGalleryViewModel
import io.getstream.chat.android.ui.feature.gallery.options.AttachmentGalleryOptionsViewStyle
import io.getstream.chat.android.ui.feature.gallery.options.internal.AttachmentGalleryOptionsDialogFragment
import io.getstream.chat.android.ui.feature.gallery.overview.internal.MediaAttachmentDialogFragment
import io.getstream.chat.android.ui.utils.PermissionChecker
import io.getstream.chat.android.ui.utils.extensions.applyEdgeToEdgePadding
import io.getstream.chat.android.ui.utils.extensions.constrainViewToParentBySide
import io.getstream.chat.android.ui.utils.extensions.createStreamThemeWrapper
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.streamThemeInflater
import io.getstream.chat.android.ui.utils.extensions.use
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.util.Date

public class AttachmentGalleryActivity : AppCompatActivity() {

    private lateinit var binding: StreamUiActivityAttachmentGalleryBinding

    private val logger by taggedLogger("Chat:AttachmentGalleryActivity")

    /**
     * If the "reply" option is present in the list.
     */
    private var replyOptionEnabled: Boolean = true

    /**
     * If the "show in chat" option present in the list.
     */
    private var showInChatOptionEnabled: Boolean = true

    /**
     * If the "save media" option is present in the list
     */
    private var saveMediaOptionEnabled: Boolean = true

    /**
     * If the "delete" option is present in the list.
     */
    private var deleteOptionEnabled: Boolean = true

    /**
     * Represents the job used to share an attachment.
     *
     * If the attachment is larger, this could end up being a longer run job
     * so cancel it accordingly.
     */
    private var shareMediaJob: Job? = null

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
                parentId = currentItem.messageId,
                cid = currentItem.cid,
                userName = currentItem.user.name,
                isMine = currentItem.isMine,
            )
        }
    private var isFullScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ChatClient.isInitialized.not()) {
            finish()
            return
        }

        binding = StreamUiActivityAttachmentGalleryBinding.inflate(streamThemeInflater)
        setContentView(binding.root)
        setupEdgeToEdge()
        setupGalleryOverviewButton()
        binding.closeButton.setOnClickListener { finish() }
        viewModel.attachmentGalleryItemsLiveData.observe(this, ::setupGallery)
    }

    private fun setupEdgeToEdge() {
        binding.root.applyEdgeToEdgePadding(typeMask = WindowInsetsCompat.Type.systemBars())
    }

    private fun setupGallery(attachmentGalleryItems: List<AttachmentGalleryItem>) {
        if (attachmentGalleryItems.isEmpty()) {
            finish()
        } else {
            this.attachmentGalleryItems = attachmentGalleryItems
            setupGalleryAdapter()
            setupShareMediaButton()
            setupAttachmentActionsButton()
            obtainOptionsViewStyle()
            observePageChanges()
        }
    }

    private fun setupGalleryAdapter() {
        adapter = AttachmentGalleryPagerAdapter(
            fragmentActivity = this,
            mediaList = attachmentGalleryItems.map { it.attachment },
            mediaClickListener = {
                isFullScreen = !isFullScreen
                if (isFullScreen) enterFullScreenMode() else exitFullScreenMode()
            },
        )
        binding.galleryViewPager.adapter = adapter
        binding.galleryViewPager.setCurrentItem(initialIndex, false)
    }

    /**
     * Sets an on click listener with media sharing capability
     * on the share button.
     */
    private fun setupShareMediaButton() {
        binding.shareMediaButton.setOnClickListener {
            if (shareMediaJob == null || shareMediaJob?.isCompleted == true) {
                setSharingInProgressUi()
                shareMedia()
            } else {
                shareMediaJob?.cancel()
                setNoSharingInProgressUi()
            }
        }
    }

    /**
     * Begins the process of sharing media depending on
     * the attachment type.
     */
    private fun shareMedia() {
        val attachment = adapter.getItem(binding.galleryViewPager.currentItem)

        when (attachment.type) {
            AttachmentType.IMAGE -> shareImage(attachment = attachment)
            AttachmentType.VIDEO -> shareVideo(attachment = attachment)
            else -> toastFailedShare()
        }
    }

    /**
     * Prepares an image attachment for sharing.
     *
     * @param attachment The attachment to share.
     **/
    private fun shareImage(attachment: Attachment) {
        val imageUrl = attachment.imageUrl

        if (imageUrl != null) {
            shareMediaJob?.cancel()

            shareMediaJob = lifecycleScope.launch(DispatcherProvider.Main) {
                StreamImageLoader.instance().loadAsBitmap(
                    context = applicationContext,
                    url = imageUrl,
                )?.let { bitmap ->
                    StreamFileUtil.writeImageToSharableFile(applicationContext, bitmap)
                }?.onSuccess { uri ->
                    shareLocalFile(
                        uri = uri,
                        mimeType = attachment.mimeType,
                    )
                }
                setNoSharingInProgressUi()
            }
        } else {
            toastFailedShare()
            setNoSharingInProgressUi()
        }
    }

    /**
     * Prepares a video attachment for sharing.
     *
     * @param attachment The attachment to share.
     */
    private fun shareVideo(attachment: Attachment) {
        shareMediaJob?.cancel()

        shareMediaJob = lifecycleScope.launch {
            val result = StreamFileUtil.writeFileToShareableFile(
                context = applicationContext,
                attachment = attachment,
            )

            when (result) {
                is Result.Success -> {
                    shareLocalFile(
                        uri = result.value,
                        mimeType = attachment.mimeType,
                    )
                }
                is Result.Failure -> {
                    toastFailedShare()
                }
            }
            setNoSharingInProgressUi()
        }
    }

    /**
     * Displays a toast saying that sharing the attachment has failed.
     */
    private fun toastFailedShare() {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.stream_ui_attachment_gallery_share_error),
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun setupAttachmentActionsButton() {
        binding.attachmentActionsButton.setOnClickListener {
            AttachmentGalleryOptionsDialogFragment.newInstance(
                showInChatOptionHandler = {
                    setResultAndFinish(AttachmentOptionResult.ShowInChat(attachmentGalleryResultItem))
                },
                deleteOptionHandler = { setResultAndFinish(AttachmentOptionResult.Delete(attachmentGalleryResultItem)) },
                replyOptionHandler = { setResultAndFinish(AttachmentOptionResult.Reply(attachmentGalleryResultItem)) },
                saveMediaOptionHandler = handleSaveMedia,
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
            },
        )
        onGalleryPageSelected(initialIndex)
    }

    private val handleSaveMedia = AttachmentGalleryOptionsDialogFragment.AttachmentOptionHandler {
        permissionChecker.checkWriteStoragePermissions(
            binding.root,
            onPermissionGranted = {
                setResultAndFinish(AttachmentOptionResult.Download(attachmentGalleryResultItem))
            },
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
        binding.mediaInformationTextView.text = getString(
            R.string.stream_ui_attachment_gallery_count,
            position + 1,
            attachmentGalleryItems.size,
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
                DateUtils.FORMAT_ABBREV_RELATIVE,
            )
            .toString()
            .replaceFirstChar(Char::lowercase)

        return getString(
            R.string.stream_ui_attachment_gallery_date,
            relativeDay,
            ChatUI.dateFormatter.formatTime(createdAt),
        )
    }

    private fun setupGalleryOverviewButton() {
        binding.galleryOverviewButton.setOnClickListener {
            MediaAttachmentDialogFragment.newInstance()
                .apply {
                    setMediaClickListener {
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
     * Sets up the UI in a way that signals to the user
     * that sharing is in progress.
     */
    private fun setSharingInProgressUi() {
        with(binding) {
            mediaInformationTextView.text =
                applicationContext.getString(R.string.stream_ui_attachment_gallery_preview_preparing)
            progressBar.visibility = View.VISIBLE

            val drawable =
                ContextCompat.getDrawable(applicationContext, R.drawable.stream_ui_ic_clear)?.mutate()
                    ?.apply { setTint(ContextCompat.getColor(applicationContext, R.color.stream_ui_black)) }

            binding.shareMediaButton.setImageDrawable(drawable)
        }
    }

    /**
     * Sets up the UI in a way that signals to the user
     * that no attachment is being shared currently.
     */
    private fun setNoSharingInProgressUi() {
        with(binding) {
            progressBar.visibility = View.GONE
            shareMediaButton.setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    R.drawable.stream_ui_ic_share,
                ),
            )

            mediaInformationTextView.text = getString(
                R.string.stream_ui_attachment_gallery_count,
                galleryViewPager.currentItem + 1,
                attachmentGalleryItems.size,
            )
        }
    }

    /**
     * Obtains style attributes for the gallery and its children.
     */
    private fun obtainOptionsViewStyle() {
        try {
            createStreamThemeWrapper().obtainStyledAttributes(
                null,
                R.styleable.AttachmentOptionsView,
                R.attr.streamUiAttachmentGalleryOptionsStyle,
                R.style.StreamUi_AttachmentGallery_Options,
            ).use {
                val style = AttachmentGalleryOptionsViewStyle(this, it)
                replyOptionEnabled = style.replyOptionEnabled
                showInChatOptionEnabled = style.showInChatOptionEnabled
                saveMediaOptionEnabled = style.saveMediaOptionEnabled
                deleteOptionEnabled = style.deleteOptionEnabled
            }
        } catch (e: Exception) {
            logger.e(e) { "Failed to obtain styles" }
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
            saveMediaOptionEnabled ||
            (deleteOptionEnabled && isMine)
    }

    /**
     * Clear the cache when the activity is
     * destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        StreamFileUtil.clearStreamCache(context = applicationContext)
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
