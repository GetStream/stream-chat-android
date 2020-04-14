package io.getstream.chat.android.livedata.repository

import io.getstream.chat.android.livedata.dao.ReactionDao
import io.getstream.chat.android.livedata.entity.ReactionEntity

class ReactionRepository(var reactionDao: ReactionDao) {

    suspend fun insert(reactionEntity: ReactionEntity) {
        reactionDao.insert(reactionEntity)
    }
    suspend fun select(messageId: String, userId: String, type: String): ReactionEntity? {
        return reactionDao.select(messageId, userId, type)
    }

    suspend fun selectSyncNeeded(): List<ReactionEntity> {
        return reactionDao.selectSyncNeeded()
    }
}