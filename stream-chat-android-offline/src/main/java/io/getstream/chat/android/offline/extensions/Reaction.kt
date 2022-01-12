package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User

/** Updates collection of reactions with more recent data of [users]. */
internal fun Collection<Reaction>.updateByUsers(userMap: Map<String, User>): Collection<Reaction> =
    if (mapNotNull { it.user?.id }.any(userMap::containsKey)) {
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

/**
 * Merges two collections of reactions by their [Reaction.type].
 *
 * @param recentReactions More recent collection of reactions.
 * @param cachedReactions More outdated collection of reactions.
 *
 * @return Collection of reactions where cached data is substituted by more recent one if they have same [Reaction.type].
 */
internal fun mergeReactions(
    recentReactions: Collection<Reaction>,
    cachedReactions: Collection<Reaction>,
): Collection<Reaction> {
    return (
        cachedReactions.associateBy(Reaction::type) +
            recentReactions.associateBy(Reaction::type)
        ).values
}
