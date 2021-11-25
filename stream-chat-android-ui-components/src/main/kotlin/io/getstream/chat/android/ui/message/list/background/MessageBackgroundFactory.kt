package io.getstream.chat.android.ui.message.list.background

import android.content.Context
import android.graphics.drawable.Drawable
import com.getstream.sdk.chat.adapter.MessageListItem

/**
 * Drawer of background of message items
 */
public interface MessageBackgroundFactory {

    /**
     * Background for message of plain text
     */
    public fun plainTextMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for deleted messages
     */
    public fun deletedMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * Background for messages with attachments and text
     */
    public fun textAndAttachmentMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    /**
     * ShapeAppearanceModel for giphy card
     */
    public fun giphyAppearanceModel(context: Context): Drawable
}
