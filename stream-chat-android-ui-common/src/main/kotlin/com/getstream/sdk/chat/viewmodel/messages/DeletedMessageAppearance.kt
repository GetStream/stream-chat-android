package com.getstream.sdk.chat.viewmodel.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message

public sealed class DeletedMessageAppearance(
    public inline val deletedMessageVisibilityCondition: (deletedMessage: Message) -> Boolean,
) {
    public object VisibleToAuthor : DeletedMessageAppearance(
        deletedMessageVisibilityCondition = { message ->
            val currentUser = ChatClient.instance().getCurrentUser()
            val author = message.user
            currentUser?.id == author.id
        }
    )

    public object VisibleToEveryone : DeletedMessageAppearance(deletedMessageVisibilityCondition = { true })

    public object NotVisibleToAnyone : DeletedMessageAppearance(deletedMessageVisibilityCondition = { false })
}
