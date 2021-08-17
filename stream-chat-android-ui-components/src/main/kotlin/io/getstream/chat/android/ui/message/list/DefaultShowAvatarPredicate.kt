package io.getstream.chat.android.ui.message.list

import com.getstream.sdk.chat.adapter.MessageListItem

public class DefaultShowAvatarPredicate: MessageListView.ShowAvatarPredicate {
    override fun shouldShow(messageItem: MessageListItem.MessageItem): Boolean {
        return messageItem.positions.contains(MessageListItem.Position.BOTTOM) && messageItem.isTheirs
    }
}
