package io.getstream.chat.android.ui.common.extensions.internal

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

internal fun Attachment.isImage(): Boolean = type == ModelType.attach_image

internal fun Attachment.isGiphy(): Boolean = type == ModelType.attach_giphy

internal fun Attachment.hasLink(): Boolean = titleLink != null || ogUrl != null
