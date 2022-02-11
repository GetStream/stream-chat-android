package io.getstream.chat.android.offline.repository.domain.reaction

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

internal interface ReactionRepository {
    suspend fun insertReaction(reaction: Reaction)
    suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date)
    suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction>
    /**
     * Selects the reaction of given type to the message if exists.
     *
     * @param reactionType The type of reaction.
     * @param messageId The id of the message to which reaction belongs.
     * @param userId The id of the user who is the owner of reaction.
     *
     * @return [Reaction] if exists, null otherwise.
     */
    suspend fun selectUserReactionToMessage(reactionType: String, messageId: String, userId: String): Reaction?

    suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction>
}

/**
 * We don't do any caching on reactions since usage is infrequent.
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

    override suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction> {
        return reactionDao.selectSyncStatus(syncStatus).map { it.toModel(getUser) }
    }

    override suspend fun selectUserReactionToMessage(
        reactionType: String,
        messageId: String,
        userId: String,
    ): Reaction {
        return reactionDao.selectUserReactionToMessage(
            reactionType = reactionType,
            messageId = messageId,
            userId = userId,
        )?.toModel(getUser)
    }

    override suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }
}
