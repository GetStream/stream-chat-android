package io.getstream.chat.android.ui.message.list

import com.getstream.sdk.chat.adapter.MessageListItem

/**
 * Predicate class used to filter [MessageListItem.MessageItem] items which are deleted. Used by [MessageListView.setDeletedMessageListItemPredicate].
 */
public sealed class DeletedMessageListItemPredicate : MessageListView.MessageListItemPredicate {
    /**
     * Predicate object used to hide deleted [MessageListItem.MessageItem] items from everyone.
     */
    public object NotVisibleToAnyone : DeletedMessageListItemPredicate() {
        override fun predicate(item: MessageListItem): Boolean {
            return false
        }
    }

    /**
     * Predicate object used to show deleted [MessageListItem.MessageItem] items to everyone.
     */
    public object VisibleToEveryone : DeletedMessageListItemPredicate() {
        override fun predicate(item: MessageListItem): Boolean {
            return true
        }
    }

    /**
     * Predicate object used to hide deleted [MessageListItem.MessageItem] items from everyone except for the author of the message.
     */
    public object VisibleToAuthorOnly : DeletedMessageListItemPredicate() {
        override fun predicate(item: MessageListItem): Boolean {
            return true
        }
    }
}
