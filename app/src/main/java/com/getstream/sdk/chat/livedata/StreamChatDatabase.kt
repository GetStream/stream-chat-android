package com.getstream.sdk.chat.livedata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.getstream.sdk.chat.livedata.converter.*
import com.getstream.sdk.chat.livedata.dao.*
import com.getstream.sdk.chat.livedata.entity.*

@Database(
    entities = [
        QueryChannelsEntity::class,
        MessageEntity::class,
        UserEntity::class,
        ReactionEntity::class,
        ChannelStateEntity::class,
        ChannelConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)

@TypeConverters(
    FilterObjectConverter::class,
    QuerySortConverter::class,
    ExtraDataConverter::class,
    Converter::class,
    DateConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun queryChannelsQDao(): ChannelQueryDao
    abstract fun userDao(): UserDao
    abstract fun reactionDao(): ReactionDao
    abstract fun messageDao(): MessageDao
    abstract fun channelStateDao(): ChannelStateDao
    abstract fun channelConfigDao(): ChannelConfigDao


    companion object {
        @Volatile
        private var INSTANCES: MutableMap<String, ChatDatabase?> = mutableMapOf()

        fun getDatabase(context: Context, userId: String): ChatDatabase {
            if (!INSTANCES.containsKey(userId)) {
                synchronized(this) {
                    val db = Room.databaseBuilder(
                        context.applicationContext,
                        ChatDatabase::class.java,
                        "stream_chat_database_$userId"
                    ).build()
                    INSTANCES[userId] = db

                }
            }
            return INSTANCES[userId] ?: error("DB not created")

        }
    }
}