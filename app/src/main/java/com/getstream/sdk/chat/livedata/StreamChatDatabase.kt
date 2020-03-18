package com.getstream.sdk.chat.livedata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.getstream.sdk.chat.livedata.converter.*
import com.getstream.sdk.chat.livedata.dao.ChannelQueryDao
import com.getstream.sdk.chat.livedata.dao.MessageDao
import com.getstream.sdk.chat.livedata.dao.ReactionDao
import com.getstream.sdk.chat.livedata.dao.UserDao
import com.getstream.sdk.chat.livedata.entity.ChannelQuery
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import com.getstream.sdk.chat.livedata.entity.UserEntity

@Database(
    entities = [ChannelQuery::class, UserEntity::class, ReactionEntity::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    FilterObjectConverter::class,
    QuerySortConverter::class,
    ExtraDataConverter::class,
    AttachmentListConverter::class,
    DateConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun queryChannelsQDao(): ChannelQueryDao
    abstract fun userDao(): UserDao
    abstract fun reactionDao(): ReactionDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatDatabase? = null

        fun getDatabase(context: Context): ChatDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatDatabase::class.java,
                    "word_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}