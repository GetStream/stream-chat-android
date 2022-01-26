package io.getstream.chat.ui.sample.util.extensions

import io.getstream.chat.android.client.models.Attachment

private val MEDIA_ATTACHMENT_TYPES: Collection<String> = listOf("image", "giphy")

internal fun Attachment.isMedia(): Boolean = type in MEDIA_ATTACHMENT_TYPES