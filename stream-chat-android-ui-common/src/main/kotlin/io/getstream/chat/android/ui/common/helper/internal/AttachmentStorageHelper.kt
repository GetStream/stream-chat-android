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

package io.getstream.chat.android.ui.common.helper.internal

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.helper.internal.AttachmentStorageHelper.Companion.EXTRA_SOURCE_URI
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.log.taggedLogger
import java.io.File

/**
 * Handles querying device storage for attachment metadata and converting between
 * [AttachmentMetaData] and the SDK [Attachment] model.
 *
 * @param context The context of the app, used to fetch files and media.
 * @param storageHelper Queries the device's content providers for files and media.
 * @param attachmentFilter A filter that is used to filter out attachments that will not be accepted
 * by the backend.
 */
@InternalStreamChatApi
public class AttachmentStorageHelper(
    private val context: Context,
    private val storageHelper: StorageHelper = StorageHelper(),
    private val attachmentFilter: AttachmentFilter = AttachmentFilter(),
) {
    private val logger by taggedLogger("AttachmentStorageHelper")

    /**
     * Loads file metadata from the system, filtered against file types accepted by the backend.
     *
     * @return Filtered list of [AttachmentMetaData] describing available files.
     */
    @WorkerThread
    public fun getFileMetadata(): List<AttachmentMetaData> =
        attachmentFilter.filterAttachments(storageHelper.getFileAttachments(context))

    /**
     * Loads media metadata from the system, filtered against file types accepted by the backend.
     *
     * @return Filtered list of [AttachmentMetaData] describing available media.
     */
    @WorkerThread
    public fun getMediaMetadata(): List<AttachmentMetaData> =
        attachmentFilter.filterAttachments(storageHelper.getMediaAttachments(context))

    /**
     * Converts a list of [AttachmentMetaData] into lightweight [Attachment]s for preview.
     *
     * The original content URI from each metadata entry is stored under [EXTRA_SOURCE_URI]
     * in [Attachment.extraData]. [Attachment.upload] is always `null`; files are resolved
     * later via [resolveAttachmentFiles] at send time.
     *
     * @param metaData The metadata to convert.
     * @return List of lightweight [Attachment]s.
     */
    public fun toAttachments(metaData: List<AttachmentMetaData>): List<Attachment> = metaData.map { meta ->
        val extra = meta.uri?.let { uri -> meta.extraData + (EXTRA_SOURCE_URI to uri.toString()) }
            ?: meta.extraData
        Attachment(
            type = meta.type,
            name = meta.title ?: "",
            fileSize = meta.size.toInt(),
            mimeType = meta.mimeType,
            extraData = extra,
        )
    }

    /**
     * Resolves deferred attachments by copying their source content into local cache files.
     *
     * Attachments that already have a non-null [Attachment.upload] are returned unchanged.
     * For others, the original content URI is read from [EXTRA_SOURCE_URI] and copied to a
     * local cache file. Attachments whose source URI cannot be resolved (e.g. the content
     * URI is no longer accessible or the cache write fails) are **dropped** from the result.
     *
     * @param attachments The attachments to resolve.
     * @return Attachments with [Attachment.upload] populated for every entry that had a source URI.
     */
    @WorkerThread
    public fun resolveAttachmentFiles(
        attachments: List<Attachment>,
    ): List<Attachment> = attachments.mapNotNull { attachment ->
        if (attachment.upload != null) return@mapNotNull attachment
        val sourceUri = (attachment.extraData[EXTRA_SOURCE_URI] as? String)
            ?.let(Uri::parse) ?: return@mapNotNull attachment
        val metaData = AttachmentMetaData(
            uri = sourceUri,
            type = attachment.type,
            mimeType = attachment.mimeType,
            title = attachment.name,
        ).apply { size = attachment.fileSize.toLong() }
        val file = storageHelper.getCachedFileFromUri(context, metaData)
        if (file == null) {
            logger.w { "[resolveAttachmentFiles] Failed to resolve file for URI: $sourceUri" }
            return@mapNotNull null
        }

        val (width, height) = if (attachment.originalWidth == null && attachment.originalHeight == null) {
            resolveLocalDimensions(file, attachment)
        } else {
            attachment.originalWidth to attachment.originalHeight
        }
        attachment.copy(
            upload = file,
            extraData = attachment.extraData - EXTRA_SOURCE_URI,
            originalWidth = width,
            originalHeight = height,
        )
    }

    /**
     * Resolves a list of file [Uri]s into [AttachmentMetaData].
     *
     * @param uris Selected file URIs to resolve.
     * @return Filtered list of [AttachmentMetaData] describing the files.
     */
    @WorkerThread
    public fun resolveMetadata(uris: List<Uri>): List<AttachmentMetaData> =
        storageHelper.getAttachmentsFromUriList(context, uris).let(attachmentFilter::filterAttachments)

    /**
     * Partitions [metadata] into resolvable and unresolvable items.
     *
     * An item is considered resolvable if its content URI can be opened for reading.
     * Metadata entries without a URI are treated as resolvable.
     *
     * @param metadata The metadata to partition.
     * @return A [Pair] where the first element contains resolvable items and the
     * second contains unresolvable items.
     */
    @WorkerThread
    public fun partitionResolvable(
        metadata: List<AttachmentMetaData>,
    ): Pair<List<AttachmentMetaData>, List<AttachmentMetaData>> =
        metadata.partition { meta ->
            meta.uri?.let { storageHelper.isUriResolvable(context, it) } ?: true
        }

    @Suppress("MagicNumber")
    private fun resolveLocalDimensions(file: File, attachment: Attachment): Pair<Int?, Int?> = when {
        attachment.isImage() -> {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            val w = options.outWidth.takeIf { it > 0 }
            val h = options.outHeight.takeIf { it > 0 }
            w to h
        }

        attachment.isVideo() -> {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(file.absolutePath)
                val w = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                    ?.toIntOrNull()?.takeIf { it > 0 }
                val h = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                    ?.toIntOrNull()?.takeIf { it > 0 }
                val rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0
                if (rotation == 90 || rotation == 270) h to w else w to h
            } catch (_: Exception) {
                null to null
            } finally {
                retriever.release()
            }
        }

        else -> null to null
    }

    public companion object {
        /**
         * Key in [Attachment.extraData] holding the original content URI string
         * from the device's [MediaStore][android.provider.MediaStore].
         */
        public const val EXTRA_SOURCE_URI: String = "io.getstream.sourceUri"
    }
}
