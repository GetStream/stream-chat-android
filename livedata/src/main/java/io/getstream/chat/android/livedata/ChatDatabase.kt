package io.getstream.chat.android.livedata

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.getstream.chat.android.livedata.converter.*
import io.getstream.chat.android.livedata.dao.*
import io.getstream.chat.android.livedata.entity.*

@Database(
    entities = [
        QueryChannelsEntity::class,
        MessageEntity::class,
        UserEntity::class,
        ReactionEntity::class,
        ChannelEntity::class,
        ChannelConfigEntity::class,
        SyncStateEntity::class
    ],
    version = 17,
    exportSchema = false
)

@TypeConverters(
    FilterObjectConverter::class,
    QuerySortConverter::class,
    ExtraDataConverter::class,
    ListConverter::class,
    MapConverter::class,
    SetConverter::class,
    ConfigConverter::class,
    SyncStatusConverter::class,
    DateConverter::class
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun queryChannelsQDao(): QueryChannelsDao
    abstract fun userDao(): UserDao
    abstract fun reactionDao(): ReactionDao
    abstract fun messageDao(): MessageDao
    abstract fun channelStateDao(): ChannelDao
    abstract fun channelConfigDao(): ChannelConfigDao
    abstract fun syncStateDao(): SyncStateDao

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
                    ).fallbackToDestructiveMigration().build()
                    INSTANCES[userId] = db
                }
            }
            return INSTANCES[userId] ?: error("DB not created")
        }
    }
}
