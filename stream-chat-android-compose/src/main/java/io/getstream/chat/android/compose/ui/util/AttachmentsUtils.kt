package io.getstream.chat.android.compose.ui.util

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment

/**
 * Attachment types that represent media content.
 */
private val MEDIA_ATTACHMENT_TYPES: Collection<String> = listOf(ModelType.attach_image, ModelType.attach_giphy)

/**
 * @return If the [Attachment] is media content or not.
 */
internal fun Attachment.isMedia(): Boolean = type in MEDIA_ATTACHMENT_TYPES

/**
 * @return If the [Attachment] is a link attachment or not.
 */
internal fun Attachment.hasLink(): Boolean = titleLink != null || ogUrl != null

/**
 * @return If the attachment is currently being uploaded to the server.
 */
internal fun Attachment.isUploading(): Boolean =
    uploadState == Attachment.UploadState.InProgress && upload != null && uploadId != null
