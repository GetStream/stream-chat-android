package io.getstream.chat.android.ui.message.list.adapter

internal val FULL_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
    text = true,
    reactions = true,
    attachments = true,
    replies = true,
    syncStatus = true,
    deleted = true,
    positions = true,
    pinned = true,
)
internal val EMPTY_MESSAGE_LIST_ITEM_PAYLOAD_DIFF = MessageListItemPayloadDiff(
    text = false,
    reactions = false,
    attachments = false,
    replies = false,
    syncStatus = false,
    deleted = false,
    positions = false,
    pinned = false,
)
