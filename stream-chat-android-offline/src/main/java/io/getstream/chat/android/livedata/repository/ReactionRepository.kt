package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.dao.ReactionDao
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel

/**
 * We don't do any caching on reactions since usage is infrequent
 */
internal class ReactionRepository(private val reactionDao: ReactionDao) {

    internal suspend fun insert(reaction: Reaction) {
        insert(listOf(reaction.toEntity()))
    }

    internal suspend fun insert(reactions: List<Reaction>) {
        val entities = reactions.map(Reaction::toEntity)
        insert(entities)
    }

    private suspend fun insert(reactionEntities: List<ReactionEntity>) {
        for (reactionEntity in reactionEntities) {
            require(reactionEntity.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
            require(reactionEntity.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
            require(reactionEntity.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }
        }

        reactionDao.insert(reactionEntities)
    }

    internal suspend fun select(
        messageId: String,
        userId: String,
        type: String,
        getUser: suspend (userId: String) -> User,
    ): Reaction? {
        return reactionDao.select(messageId, userId, type)?.toModel(getUser)
    }

    internal suspend fun selectSyncNeeded(getUser: suspend (userId: String) -> User): List<Reaction> {
        return reactionDao.selectSyncNeeded().map { it.toModel(getUser) }
    }

    internal suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
        getUser: suspend (userId: String) -> User,
    ): List<Reaction> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
            .map { it.toModel(getUser) }
    }
}
