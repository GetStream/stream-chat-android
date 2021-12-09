package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User

/** Updates collection of reactions with more recent data of [users]. */
internal fun Collection<Reaction>.updateByUsers(userMap: Map<String, User>): Collection<Reaction> = if (mapNotNull { it.user?.id }.any(userMap::containsKey)) {
    map { reaction ->
        if (userMap.containsKey(reaction.user?.id ?: reaction.userId)) {
            reaction.copy(user = userMap[reaction.userId] ?: reaction.user)
        } else {
            reaction
        }
    }
} else {
    this
}
