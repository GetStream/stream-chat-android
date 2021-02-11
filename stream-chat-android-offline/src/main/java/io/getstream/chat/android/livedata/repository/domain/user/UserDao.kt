package io.getstream.chat.android.livedata.repository.domain.user

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
internal interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMany(users: List<UserEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userEntity: UserEntity)

    @Query(
        "SELECT * FROM stream_chat_user " +
            "WHERE stream_chat_user.id IN (:ids)"
    )
    suspend fun select(ids: List<String>): List<UserEntity>

    @Query(
        "SELECT * FROM stream_chat_user " +
            "WHERE stream_chat_user.id IN (:id)"
    )
    suspend fun select(id: String): UserEntity?
}
