package com.getstream.sdk.chat.viewmodel.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message

/**
 * Class used by [ChatUI.deletedMessageVisibility]. It's used to specify visibility of the deleted messages.
 * @property deletedMessageVisibilityCondition a function returning true if the `deleted message` item should be displayed to the [MessageListView], false otherwise.
 */
public interface DeletedMessageVisibility {
    public fun shouldDisplayMessage(deletedMessage: Message): Boolean
    /**
     * Deleted message label is visible only to the author.
     */
    public object VisibleToAuthor : DeletedMessageVisibility {
        override fun shouldDisplayMessage(deletedMessage: Message): Boolean {
            val currentUser = ChatClient.instance().getCurrentUser()
            val author = deletedMessage.user
            return currentUser?.id == author.id
        }
    }

    /**
     * Deleted message label is visible to everyone.
     */
    public object VisibleToEveryone : DeletedMessageVisibility {
        override fun shouldDisplayMessage(deletedMessage: Message): Boolean {
            return true
        }
    }

    /**
     * Deleted message label is not visible to anyone.
     */
    public object NotVisibleToAnyone : DeletedMessageVisibility {
        override fun shouldDisplayMessage(deletedMessage: Message): Boolean {
            return true
        }
    }
}
