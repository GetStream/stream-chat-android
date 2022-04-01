/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.getstream.chat.android.offline.extensions.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus

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

/**
 * Updates the reaction's sync status based on [result].
 *
 * @param result The API call result.
 *
 * @return [Reaction] object with updated [Reaction.syncStatus].
 */
internal fun Reaction.updateSyncStatus(result: Result<*>): Reaction {
    return if (result.isSuccess) {
        copy(syncStatus = SyncStatus.COMPLETED)
    } else {
        updateFailedReactionSyncStatus(result.error())
    }
}

/**
 * Updates the reaction's sync status based on [chatError].
 * Status can be either [SyncStatus.FAILED_PERMANENTLY] or [SyncStatus.SYNC_NEEDED] depends on type of error.
 *
 * @param chatError The error returned by the API call.
 *
 * @return [Reaction] object with updated [Reaction.syncStatus].
 */
private fun Reaction.updateFailedReactionSyncStatus(chatError: ChatError): Reaction {
    return copy(
        syncStatus = if (chatError.isPermanent()) {
            SyncStatus.FAILED_PERMANENTLY
        } else {
            SyncStatus.SYNC_NEEDED
        },
    )
}

/**
 *
 */
internal fun Reaction.enrichWithDataBeforeSending(
    currentUser: User,
    isOnline: Boolean,
    enforceUnique: Boolean,
): Reaction = copy(
    user = currentUser,
    userId = currentUser.id,
    syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
    enforceUnique = enforceUnique,
)
