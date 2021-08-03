package com.getstream.sdk.chat.viewmodel.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message

/**
 * Class used by [ChatUI.deletedMessageVisibilityPredicate]. It's used to specify visibility of the deleted messages.
 * @property deletedMessageVisibilityCondition a function returning true if the `deleted message` item should be displayed to the [MessageListView], false otherwise.
 */
public sealed class DeletedMessageVisibility(
    public inline val deletedMessageVisibilityCondition: (deletedMessage: Message) -> Boolean,
) {
    /**
     * Deleted message label is visible only to the author.
     */
    public object VisibleToAuthor : DeletedMessageVisibility(
        deletedMessageVisibilityCondition = { message ->
            val currentUser = ChatClient.instance().getCurrentUser()
            val author = message.user
            currentUser?.id == author.id
        }
    )

    /**
     * Deleted message label is visible to everyone.
     */
    public object VisibleToEveryone : DeletedMessageVisibility(deletedMessageVisibilityCondition = { true })

    /**
     * Deleted message label is not visible to anyone.
     */
    public object NotVisibleToAnyone : DeletedMessageVisibility(deletedMessageVisibilityCondition = { false })
}
