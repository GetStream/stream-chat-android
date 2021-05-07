package io.getstream.chat.android.offline.repository.domain.reaction

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import java.util.Date

internal interface ReactionRepository {
    suspend fun insertReaction(reaction: Reaction)
    suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date)
    suspend fun selectReactionsSyncNeeded(): List<Reaction>
    suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction>
}

/**
 * We don't do any caching on reactions since usage is infrequent
 */
internal class ReactionRepositoryImpl(
    private val reactionDao: ReactionDao,
    private val getUser: suspend (userId: String) -> User,
) : ReactionRepository {

    override suspend fun insertReaction(reaction: Reaction) {
        require(reaction.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
        require(reaction.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
        require(reaction.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }

        reactionDao.insert(reaction.toEntity())
    }

    override suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        reactionDao.setDeleteAt(userId, messageId, deletedAt)
    }

    override suspend fun selectReactionsSyncNeeded(): List<Reaction> {
        return reactionDao.selectSyncNeeded().map { it.toModel(getUser) }
    }

    override suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }
}
