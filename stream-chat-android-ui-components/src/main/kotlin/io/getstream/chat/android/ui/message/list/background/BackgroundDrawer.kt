package io.getstream.chat.android.ui.message.list.background

import android.content.Context
import android.graphics.drawable.Drawable
import com.getstream.sdk.chat.adapter.MessageListItem

public interface BackgroundDrawer {

    public fun plainTextMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    public fun deletedMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable

    public fun textAndAttachmentMessageBackground(context: Context, data: MessageListItem.MessageItem): Drawable
}
