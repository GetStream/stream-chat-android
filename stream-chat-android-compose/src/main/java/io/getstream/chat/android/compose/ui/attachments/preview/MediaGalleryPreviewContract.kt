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

package io.getstream.chat.android.compose.ui.attachments.preview

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.helper.DownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadRequestInterceptor
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing

/**
 * The contract used to start the [MediaGalleryPreviewActivity]
 * given a message ID and the position of the clicked attachment.
 */
public class MediaGalleryPreviewContract :
    ActivityResultContract<MediaGalleryPreviewContract.Input, MediaGalleryPreviewResult?>() {

    /**
     * Creates the intent to start the [MediaGalleryPreviewActivity].
     * It receives a data pair of a [String] and an [Int] that represent the messageId and the attachmentPosition.
     *
     * @return The [Intent] to start the [MediaGalleryPreviewActivity].
     */
    override fun createIntent(context: Context, input: Input): Intent {
        return MediaGalleryPreviewActivity.getIntent(
            context,
            message = input.message,
            attachmentPosition = input.initialPosition,
            downloadAttachmentUriGenerator = input.downloadAttachmentUriGenerator,
            downloadRequestInterceptor = input.downloadRequestInterceptor,
            videoThumbnailsEnabled = input.videoThumbnailsEnabled,
            streamCdnImageResizing = input.streamCdnImageResizing,
            skipEnrichUrl = input.skipEnrichUrl,
        )
    }

    /**
     * We parse the result as [MediaGalleryPreviewResult], which can be null in case there is no result to return.
     *
     * @return The [MediaGalleryPreviewResult] or null if it doesn't exist.
     */
    override fun parseResult(resultCode: Int, intent: Intent?): MediaGalleryPreviewResult? {
        return intent?.getParcelableExtra(MediaGalleryPreviewActivity.KeyMediaGalleryPreviewResult)
    }

    /**
     * Defines the input for the [MediaGalleryPreviewContract].
     *
     * @param message The message containing the attachments.
     * @param initialPosition The initial position of the media gallery, based on the clicked item.
     * @param downloadAttachmentUriGenerator The URI generator for downloading attachments.
     * @param downloadRequestInterceptor The request interceptor for downloading attachments.
     * @param videoThumbnailsEnabled Whether video thumbnails will be displayed in previews or not.
     * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
     * by deleting an attachment contained within it. Set to false by default.
     */
    public class Input(
        public val message: Message,
        public val initialPosition: Int = 0,
        public val videoThumbnailsEnabled: Boolean,
        public val downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator,
        public val downloadRequestInterceptor: DownloadRequestInterceptor,
        public val streamCdnImageResizing: StreamCdnImageResizing,
        public val skipEnrichUrl: Boolean = false,
    )
}
