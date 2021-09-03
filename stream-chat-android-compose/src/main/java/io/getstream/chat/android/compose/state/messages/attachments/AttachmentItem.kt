package io.getstream.chat.android.compose.state.messages.attachments

import com.getstream.sdk.chat.model.AttachmentMetaData

/**
 * Represents each attachment item in our attachment picker. Each item can be selected and has an
 * appropriate set of metadata to describe it.
 *
 * @param attachmentMetaData The metadata for the item, holding the links, size, types, name etc.
 * @param isSelected If the item is selected or not.
 */
public data class AttachmentItem(
    val attachmentMetaData: AttachmentMetaData,
    val isSelected: Boolean,
)
