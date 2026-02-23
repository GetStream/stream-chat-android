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

package io.getstream.chat.android.compose.ui.attachments.preview

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.internal.file.StreamFileManager
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.Delete
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewAction
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewActivityState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.state.mediagallerypreview.Reply
import io.getstream.chat.android.compose.state.mediagallerypreview.SaveMedia
import io.getstream.chat.android.compose.state.mediagallerypreview.ShowInChat
import io.getstream.chat.android.compose.state.mediagallerypreview.toMediaGalleryPreviewActivityState
import io.getstream.chat.android.compose.state.mediagallerypreview.toMessage
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.LocalStreamImageLoader
import io.getstream.chat.android.compose.util.AttachmentFileController
import io.getstream.chat.android.compose.util.attachmentDownloadState
import io.getstream.chat.android.compose.util.onDownloadHandleRequest
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModel
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModelFactory
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode
import io.getstream.chat.android.ui.common.helper.DefaultDownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadRequestInterceptor
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.chat.android.ui.common.utils.AttachmentConstants
import io.getstream.chat.android.ui.common.utils.extensions.getDisplayableName
import io.getstream.chat.android.ui.common.utils.shareLocalFile
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Shows an image and video previews along with enabling
 * the user to perform various actions such as image or file deletion.
 */
@OptIn(ExperimentalPermissionsApi::class)
public class MediaGalleryPreviewActivity : AppCompatActivity() {

    /**
     * Factory used to build the screen ViewModel given the received message ID.
     */
    private val factory by lazy {
        val messageId = intent
            ?.getParcelable<MediaGalleryPreviewActivityState>(KeyMediaGalleryPreviewActivityState)
            ?.messageId ?: ""

        MediaGalleryPreviewViewModelFactory(
            chatClient = ChatClient.instance(),
            messageId = messageId,
            skipEnrichUrl = intent?.getBooleanExtra(KeySkipEnrichUrl, false) ?: false,
        )
    }

    /**
     * Holds a job used to share an image or a file.
     */
    private var fileSharingJob: Job? = null

    /**
     * The current state of the screen.
     */
    private var uiState: MediaGalleryPreviewActivityState? = null

    /**
     * The ViewModel that exposes screen data.
     */
    private val mediaGalleryPreviewViewModel by viewModels<MediaGalleryPreviewViewModel>(factoryProducer = { factory })

    private val attachmentFileController by lazy { AttachmentFileController(context = applicationContext) }

    /**
     * Sets up the data required to show the previews of images or videos within the given message.
     *
     * Immediately finishes in case the data is invalid.
     */
    @Suppress("LongMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ChatClient.isInitialized.not()) {
            finish()
            return
        }

        uiState = savedInstanceState?.getParcelable(KeyMediaGalleryPreviewActivityState)
            ?: intent?.getParcelableExtra(KeyMediaGalleryPreviewActivityState)

        val videoThumbnailsEnabled = intent?.getBooleanExtra(KeyVideoThumbnailsEnabled, true) ?: true
        val streamCdnImageResizing = intent?.createStreamCdnImageResizing()
            ?: StreamCdnImageResizing.defaultStreamCdnImageResizing()
        val messageId = uiState?.messageId ?: ""
        require(messageId.isNotBlank()) { "Missing messageId necessary to load images." }

        if (!mediaGalleryPreviewViewModel.hasCompleteMessage) {
            val message = uiState?.toMessage()

            if (message != null) {
                mediaGalleryPreviewViewModel.setInitialMessage(message)
            }
        }

        val attachmentPosition = intent?.getIntExtra(KeyAttachmentPosition, 0) ?: 0

        setContent {
            ChatTheme(
                videoThumbnailsEnabled = videoThumbnailsEnabled,
                streamCdnImageResizing = streamCdnImageResizing,
                downloadAttachmentUriGenerator = downloadAttachmentUriGenerator,
                downloadRequestInterceptor = downloadRequestInterceptor,
                mediaGalleryConfig = intent.getParcelable(KeyConfig) ?: MediaGalleryConfig(),
            ) {
                SetupEdgeToEdge()

                val (writePermissionState, downloadPayload) = attachmentDownloadState()
                val downloadAttachmentUriGenerator = ChatTheme.streamDownloadAttachmentUriGenerator
                val downloadRequestInterceptor = ChatTheme.streamDownloadRequestInterceptor

                // Take the imageLoader from the injector, which is populated by the MediaAttachmentContent. This is a
                // workaround for the fact that the MediaGalleryPreviewActivity is not a part of the composition tree of
                // the MessageList, so the provided imageLoaderFactory from the MessageList ChatTheme cannot be used.
                val imageLoader = MediaGalleryInjector.imageLoader ?: LocalStreamImageLoader.current
                CompositionLocalProvider(LocalStreamImageLoader provides imageLoader) {
                    MediaGalleryPreviewScreen(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.systemBars),
                        viewModel = mediaGalleryPreviewViewModel,
                        initialPage = attachmentPosition,
                        onHeaderLeadingContentClick = ::finish,
                        onOptionClick = { attachment, option ->
                            handleMediaAction(
                                attachment,
                                option.action,
                                writePermissionState,
                                downloadPayload,
                                downloadAttachmentUriGenerator::generateDownloadUri,
                                downloadRequestInterceptor::intercept,
                            )
                        },
                        onRequestShareAttachment = ::onRequestShareAttachment,
                        onConfirmShareAttachment = ::shareAttachment,
                    )
                }
            }
            // Close the activity when the closeScreen state is true (message deleted or no more attachments).
            val closeScreen by mediaGalleryPreviewViewModel.closeScreen.collectAsStateWithLifecycle(false)
            LaunchedEffect(closeScreen) {
                if (closeScreen) {
                    finish()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        uiState?.also {
            outState.putParcelable(KeyMediaGalleryPreviewActivityState, it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        StreamFileManager().clearCache(applicationContext)
    }

    @Composable
    private fun SetupEdgeToEdge() {
        val nightMode = isSystemInDarkTheme()
        val systemBarsColor = ChatTheme.colors.backgroundElevationElevation1.toArgb()
        LaunchedEffect(nightMode) {
            val style = if (nightMode) {
                SystemBarStyle.dark(systemBarsColor)
            } else {
                SystemBarStyle.light(systemBarsColor, systemBarsColor)
            }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }
    }

    /**
     * Consumes the action user selected to perform for the current media attachment.
     *
     * @param attachment The [Attachment] for which the action is handled.
     * @param action The action the user selected.
     * @param downloadPayload The attachment to be downloaded.
     * @param writePermissionState The current state of permissions.
     * @param generateDownloadUri The function to generate the download URI.
     * @param interceptRequest The function to intercept the download request.
     */
    @Suppress("LongParameterList")
    private fun handleMediaAction(
        attachment: Attachment,
        action: MediaGalleryPreviewAction,
        writePermissionState: PermissionState,
        downloadPayload: MutableState<Attachment?>,
        generateDownloadUri: (Attachment) -> Uri,
        interceptRequest: DownloadManager.Request.() -> Unit,
    ) {
        when (action) {
            is ShowInChat -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = action.message.id,
                        parentMessageId = action.message.parentId,
                        resultType = MediaGalleryPreviewResultType.SHOW_IN_CHAT,
                    ),
                )
            }

            is Reply -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = action.message.id,
                        parentMessageId = action.message.parentId,
                        resultType = MediaGalleryPreviewResultType.QUOTE,
                    ),
                )
            }

            is Delete -> {
                mediaGalleryPreviewViewModel.deleteCurrentMediaAttachment(attachment)
            }

            is SaveMedia -> {
                onDownloadHandleRequest(
                    context = this,
                    payload = attachment,
                    permissionState = writePermissionState,
                    downloadPayload = downloadPayload,
                    generateDownloadUri = generateDownloadUri,
                    interceptRequest = interceptRequest,
                )
            }
        }
    }

    /**
     * Prepares and sets the result of this Activity and propagates it back to the user.
     *
     * @param result The chosen action result.
     */
    private fun handleResult(result: MediaGalleryPreviewResult) {
        val data = Intent().apply {
            putExtra(KeyMediaGalleryPreviewResult, result)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    private fun onRequestShareAttachment(attachment: Attachment) {
        when {
            mediaGalleryPreviewViewModel.isSharingInProgress -> {
                fileSharingJob?.cancel()
                mediaGalleryPreviewViewModel.isSharingInProgress = false
            }

            attachment.fileSize >= AttachmentConstants.MAX_SIZE_BEFORE_DOWNLOAD_WARNING_IN_BYTES -> {
                lifecycleScope.launch {
                    val result = attachmentFileController.getFileFromCache(attachment)
                    when (result) {
                        is Result.Success -> {
                            shareAttachment(
                                mediaUri = result.value,
                                mimeType = attachment.mimeType,
                                text = attachment.getDisplayableName(),
                            )
                        }

                        is Result.Failure -> {
                            mediaGalleryPreviewViewModel.promptedAttachment = attachment
                        }
                    }
                }
            }

            else -> shareAttachment(attachment)
        }
    }

    /**
     * Handles the logic of sharing a file.
     *
     * @param attachment The attachment to be shared.
     */
    private fun shareAttachment(attachment: Attachment) {
        fileSharingJob = lifecycleScope.launch {
            mediaGalleryPreviewViewModel.isSharingInProgress = true

            val result = if (attachment.isImage() && attachment.imageUrl != null) {
                attachmentFileController.downloadImage(attachment)
            } else {
                attachmentFileController.downloadFile(attachment)
            }

            mediaGalleryPreviewViewModel.isSharingInProgress = false

            when (result) {
                is Result.Success -> shareLocalFile(
                    uri = result.value,
                    mimeType = attachment.mimeType,
                    text = attachment.getDisplayableName(),
                )

                is Result.Failure -> toastFailedShare()
            }
        }
    }

    /**
     * Displays a toast saying that sharing the attachment has failed.
     */
    private fun toastFailedShare() {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.stream_compose_media_gallery_preview_could_not_share_attachment),
            Toast.LENGTH_SHORT,
        ).show()
    }

    private fun shareAttachment(
        mediaUri: Uri?,
        mimeType: String?,
        text: String?,
    ) {
        mediaGalleryPreviewViewModel.isSharingInProgress = false

        if (mediaUri == null) {
            toastFailedShare()
            return
        }

        shareLocalFile(
            uri = mediaUri,
            mimeType = mimeType,
            text = text,
        )
    }

    /**
     * Fetches individual image resizing options from the bundle and
     * packs them into [StreamCdnImageResizing].
     *
     * @return An instance of [StreamCdnImageResizing] created from individual options fetched from
     * the bundle packed inside the given intent.
     */
    private fun Intent.createStreamCdnImageResizing(): StreamCdnImageResizing {
        val imageResizingEnabled = getBooleanExtra(KeyImageResizingEnabled, false)

        val resizedWidthPercentage = getFloatExtra(KeyStreamCdnResizeImagedWidthPercentage, 1f)
        val resizedHeightPercentage = getFloatExtra(KeyStreamCdnResizeImagedHeightPercentage, 1f)

        val resizeModeEnumValue = getStringExtra(KeyStreamCdnResizeImageMode)
        val resizeMode =
            if (resizeModeEnumValue != null) StreamCdnResizeImageMode.valueOf(value = resizeModeEnumValue) else null

        val cropModeEnumValue = getStringExtra(KeyStreamCdnResizeImageCropMode)
        val cropMode =
            if (cropModeEnumValue != null) StreamCdnCropImageMode.valueOf(value = cropModeEnumValue) else null

        return StreamCdnImageResizing(
            imageResizingEnabled = imageResizingEnabled,
            resizedWidthPercentage = resizedWidthPercentage,
            resizedHeightPercentage = resizedHeightPercentage,
            cropMode = cropMode,
            resizeMode = resizeMode,
        )
    }

    private inline fun <reified T> Intent.getParcelable(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(key)
        }
    }

    public companion object {

        /**
         * Represents the key for the ID of the message with the attachments we're browsing.
         */
        private const val KeyMediaGalleryPreviewActivityState: String = "mediaGalleryPreviewActivityState"

        /**
         * Represents the key for the [Boolean] value dictating whether video thumbnails
         * will be displayed in previews or not.
         */
        private const val KeyVideoThumbnailsEnabled: String = "videoThumbnailsEnabled"

        /**
         * Represents the key for the [Boolean] value found inside [StreamCdnImageResizing]
         * turning image resizing on or off.
         */
        private const val KeyImageResizingEnabled: String = "imageResizingEnabled"

        /**
         * Represents the key for the [Float] value found inside [StreamCdnImageResizing]
         * dictating the resized image width percentage.
         */
        private const val KeyStreamCdnResizeImagedWidthPercentage: String = "streamCdnResizeImagedWidthPercentage"

        /**
         * Represents the key for the [Float] value found inside [StreamCdnImageResizing]
         * dictating the resized image height percentage.
         */
        private const val KeyStreamCdnResizeImagedHeightPercentage: String = "streamCdnResizeImagedHeightPercentage"

        /**
         * Represents the key for the [StreamCdnResizeImageMode] value found inside [StreamCdnImageResizing]
         * dictating the resize image mode.
         */
        private const val KeyStreamCdnResizeImageMode: String = "streamCdnResizeImageMode"

        /**
         * Represents the key for the [StreamCdnCropImageMode] value found inside [StreamCdnImageResizing]
         * dictating the crop image mode.
         */
        private const val KeyStreamCdnResizeImageCropMode: String = "streamCdnResizeImageCropMode"

        /**
         * Represents the key for the starting attachment position based on the clicked attachment.
         */
        private const val KeyAttachmentPosition: String = "attachmentPosition"

        /**
         * Represents the key for the result of the preview, like scrolling to the message.
         */
        public const val KeyMediaGalleryPreviewResult: String = "mediaGalleryPreviewResult"

        /**
         * Represents the key for the boolean which dictates if we should skip enriching URLs when updating a
         * message.
         */
        private const val KeySkipEnrichUrl: String = "skipEnrichUrl"

        /**
         * Represents the key for the [MediaGalleryConfig] passed to the activity.
         */
        private const val KeyConfig: String = "config"

        /**
         * Used to generate download URIs for attachments.
         */
        private var downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator =
            DefaultDownloadAttachmentUriGenerator

        /**
         * Used to intercept download requests.
         */
        private var downloadRequestInterceptor: DownloadRequestInterceptor = DownloadRequestInterceptor { }

        /**
         * Used to build an [Intent] to start the [MediaGalleryPreviewActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param message The [Message] containing the attachments.
         * @param attachmentPosition The initial position of the clicked media attachment.
         * @param videoThumbnailsEnabled Whether video thumbnails will be displayed in previews or not.
         * @param downloadAttachmentUriGenerator Used to generate download URIs for attachments.
         * @param downloadRequestInterceptor Used to intercept download requests.
         * @param streamCdnImageResizing Sets the Stream CDN hosted image resizing strategy. Turned off by default.
         * Please note that only Stream CDN hosted images containing original width (ow) and original height (oh)
         * parameters are able to be resized.
         * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
         * by deleting an attachment contained within it. Set to false by default.
         * @param config The [MediaGalleryConfig] for configuring the media gallery.
         */
        @Suppress("LongParameterList")
        public fun getIntent(
            context: Context,
            message: Message,
            attachmentPosition: Int,
            videoThumbnailsEnabled: Boolean,
            downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator,
            downloadRequestInterceptor: DownloadRequestInterceptor,
            streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
            skipEnrichUrl: Boolean = false,
            config: MediaGalleryConfig = MediaGalleryConfig(),
        ): Intent {
            this.downloadAttachmentUriGenerator = downloadAttachmentUriGenerator
            this.downloadRequestInterceptor = downloadRequestInterceptor
            return Intent(context, MediaGalleryPreviewActivity::class.java).apply {
                val mediaGalleryPreviewActivityState = message.toMediaGalleryPreviewActivityState()

                putExtra(KeyMediaGalleryPreviewActivityState, mediaGalleryPreviewActivityState)
                putExtra(KeyAttachmentPosition, attachmentPosition)
                putExtra(KeyVideoThumbnailsEnabled, videoThumbnailsEnabled)

                // Image resizing options
                putExtra(KeyImageResizingEnabled, streamCdnImageResizing.imageResizingEnabled)
                putExtra(KeyStreamCdnResizeImagedWidthPercentage, streamCdnImageResizing.resizedWidthPercentage)
                putExtra(KeyStreamCdnResizeImagedHeightPercentage, streamCdnImageResizing.resizedHeightPercentage)
                putExtra(KeyStreamCdnResizeImageMode, streamCdnImageResizing.resizeMode?.name)
                putExtra(KeyStreamCdnResizeImageCropMode, streamCdnImageResizing.cropMode?.name)
                putExtra(KeySkipEnrichUrl, skipEnrichUrl)

                // Customization config
                putExtra(KeyConfig, config)
            }
        }
    }
}
