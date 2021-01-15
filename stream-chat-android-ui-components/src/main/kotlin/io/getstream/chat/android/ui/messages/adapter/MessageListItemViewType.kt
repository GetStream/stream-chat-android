package io.getstream.chat.android.ui.messages.adapter

/**
 * View type constants used by [MessageListItemViewHolderFactory].
 */
public object MessageListItemViewType {
    // A base offset to avoid clashes between built-in and custom view types
    private const val OFFSET = 1000

    public const val DATE_DIVIDER: Int = OFFSET + 1
    public const val MESSAGE_DELETED: Int = OFFSET + 2
    public const val PLAIN_TEXT: Int = OFFSET + 3
    public const val PLAIN_TEXT_WITH_FILE_ATTACHMENTS: Int = OFFSET + 4
    public const val PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS: Int = OFFSET + 5
    public const val MEDIA_ATTACHMENTS: Int = OFFSET + 6
    public const val ATTACHMENTS: Int = OFFSET + 7
    public const val LOADING_INDICATOR: Int = OFFSET + 8
    public const val THREAD_SEPARATOR: Int = OFFSET + 9
    public const val TYPING_INDICATOR: Int = OFFSET + 10
    public const val GIPHY: Int = OFFSET + 11
}
