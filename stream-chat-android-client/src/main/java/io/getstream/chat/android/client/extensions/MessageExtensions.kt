package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User

public fun Message.enrichWithCid(cid: String): Message = apply {
    replyTo?.enrichWithCid(cid)
    this.cid = cid
}

public fun Message.users(): List<User> {
    return latestReactions.mapNotNull(Reaction::user) +
        user +
        (replyTo?.users().orEmpty()) +
        mentionedUsers +
        ownReactions.mapNotNull(Reaction::user) +
        threadParticipants
}
