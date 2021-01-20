package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.dao.ReactionDao
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.extensions.isPermanent
import io.getstream.chat.android.livedata.repository.mapper.toEntity
import io.getstream.chat.android.livedata.repository.mapper.toModel

/**
 * We don't do any caching on reactions since usage is infrequent
 */
internal class ReactionRepository(var reactionDao: ReactionDao, var currentUser: User, var client: ChatClient) {

    suspend fun insertReaction(reaction: Reaction, enforceUnique: Boolean = false) {
        insert(listOf(reaction.toEntity(enforceUnique)))
    }

    suspend fun insertManyReactions(reactions: List<Reaction>) {
        val entities = reactions.map(Reaction::toEntity)
        insert(entities)
    }

    suspend fun insert(reactionEntity: ReactionEntity) {
        insert(listOf(reactionEntity))
    }

    suspend fun insert(reactionEntities: List<ReactionEntity>) {
        for (reactionEntity in reactionEntities) {
            require(reactionEntity.messageId.isNotEmpty()) { "message id can't be empty when creating a reaction" }
            require(reactionEntity.type.isNotEmpty()) { "type can't be empty when creating a reaction" }
            require(reactionEntity.userId.isNotEmpty()) { "user id can't be empty when creating a reaction" }
        }

        reactionDao.insert(reactionEntities)
    }
    suspend fun select(messageId: String, userId: String, type: String): ReactionEntity? {
        return reactionDao.select(messageId, userId, type)
    }

    suspend fun selectSyncNeeded(): List<ReactionEntity> {
        return reactionDao.selectSyncNeeded()
    }

    suspend fun selectUserReactionsToMessage(messageId: String, userId: String): List<ReactionEntity> {
        return reactionDao.selectUserReactionsToMessage(messageId = messageId, userId = userId)
    }

    suspend fun retryReactions(): List<ReactionEntity> {
        val reactionEntities = selectSyncNeeded()
        for (reactionEntity in reactionEntities) {
            val reaction = reactionEntity.toModel { currentUser }
            reaction.user = null
            val result = if (reactionEntity.deletedAt != null) {
                client.deleteReaction(reaction.messageId, reaction.type).execute()
            } else {
                client.sendReaction(reaction, reactionEntity.enforceUnique).execute()
            }

            if (result.isSuccess) {
                reactionEntity.syncStatus = SyncStatus.COMPLETED
                insert(reactionEntity)
            } else if (result.error().isPermanent()) {
                reactionEntity.syncStatus = SyncStatus.FAILED_PERMANENTLY
                insert(reactionEntity)
            }
        }
        return reactionEntities
    }
}
