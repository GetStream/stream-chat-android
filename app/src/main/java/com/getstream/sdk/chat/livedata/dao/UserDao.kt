package com.getstream.sdk.chat.livedata.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.getstream.sdk.chat.livedata.entity.ChannelStateEntity
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.UserEntity

@Dao
interface UserDao {

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
    suspend fun select(id: String?): UserEntity?

}