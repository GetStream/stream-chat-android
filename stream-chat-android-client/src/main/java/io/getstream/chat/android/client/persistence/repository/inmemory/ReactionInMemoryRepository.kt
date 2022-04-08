package io.getstream.chat.android.client.persistence.repository.inmemory

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.persistence.repository.ReactionRepository
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

internal class ReactionInMemoryRepository: ReactionRepository {

    override suspend fun insertReaction(reaction: Reaction) {
        TODO("Not yet implemented")
    }

    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        TODO("Not yet implemented")
    }

    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> {
        TODO("Not yet implemented")
    }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction? {
        TODO("Not yet implemented")
    }

    override suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<Reaction> {
        TODO("Not yet implemented")
    }
}
