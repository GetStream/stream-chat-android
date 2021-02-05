package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.ReactionDao
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel
import java.util.Date

/**
 * We don't do any caching on reactions since usage is infrequent
 */
internal class ReactionRepository(
    private val reactionDao: ReactionDao,
    private val getUser: suspend (userId: String) -> User,
) {

    internal suspend fun insert(reaction: Reaction) {
        require(reaction.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
        require(reaction.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
        require(reaction.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }

        reactionDao.insert(reaction.toEntity())
    }

    internal suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date) {
        reactionDao.setDeleteAt(userId, messageId, deletedAt)
    }

    internal suspend fun selectUserReactionsToMessageByType(
        messageId: String,
        userId: String,
        type: String,
    ): Reaction? {
        return reactionDao.select(messageId, userId, type)?.toModel(getUser)
    }

    internal suspend fun selectSyncNeeded(): List<Reaction> {
        return reactionDao.selectSyncNeeded().map { it.toModel(getUser) }
    }

    internal suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }
}
