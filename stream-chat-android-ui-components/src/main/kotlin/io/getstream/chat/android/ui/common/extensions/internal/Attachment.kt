package io.getstream.chat.android.ui.common.extensions.internal

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

private val MEDIA_ATTACHMENT_TYPES: Collection<String> = listOf(ModelType.attach_image, ModelType.attach_giphy)

internal fun Attachment.isMedia(): Boolean = type in MEDIA_ATTACHMENT_TYPES

internal fun Attachment.hasLink(): Boolean = ogUrl != null
